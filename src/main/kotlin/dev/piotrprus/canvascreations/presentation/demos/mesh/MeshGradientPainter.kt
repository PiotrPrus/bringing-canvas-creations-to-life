package dev.piotrprus.canvascreations.presentation.demos.mesh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asSkiaColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Paint
import org.jetbrains.skia.VertexMode

/**
 * Desktop stand-in for `androidx.compose.ui.graphics.MeshGradientPainter`, which
 * ships in Jetpack Compose for Android but not (yet) in Compose Multiplatform.
 * Same shape of API so slide code reads like the Android version.
 *
 * A grid of [rows] × [columns] patches has (rows + 1) × (columns + 1) vertices,
 * positioned in normalized 0..1 coordinates. Each patch is a Coons patch whose
 * edges are cubic Béziers; it is tessellated into triangles and drawn with
 * Skia's `drawVertices`, colors interpolated per vertex.
 *
 * [block] runs on every draw, so state read inside it (e.g. an animated offset)
 * re-draws the mesh without re-creating the painter.
 */
class MeshGradientPainter(
    val rows: Int,
    val columns: Int,
    private val block: MeshGradientPainter.() -> Unit,
) : Painter() {

    /** `true` → Catmull-Rom (bicubic) color interpolation across the grid, `false` → bilinear per patch. */
    var hasBicubicColor: Boolean = true

    private val vertexCount = (rows + 1) * (columns + 1)
    private val positions = Array(vertexCount) { Offset.Zero }
    private val colors = Array(vertexCount) { Color.Transparent }
    private val rightControls = Array(vertexCount) { Offset.Unspecified }
    private val bottomControls = Array(vertexCount) { Offset.Unspecified }

    private var alpha = 1f
    private var colorFilter: ColorFilter? = null

    init {
        require(rows >= 1 && columns >= 1) { "A mesh needs at least one row and one column" }
    }

    /**
     * Places vertex ([row], [column]) at [position] (normalized) with [color].
     * [rightControlPoint] / [bottomControlPoint] are the Bézier control points of
     * the edges leaving this vertex to the right / downwards, in the same
     * normalized space. `Offset.Unspecified` infers smooth tangents from the
     * neighbours, `Offset.Zero` makes the edge a straight line.
     */
    fun setVertex(
        row: Int,
        column: Int,
        position: Offset,
        color: Color,
        rightControlPoint: Offset = Offset.Unspecified,
        bottomControlPoint: Offset = Offset.Unspecified,
    ) {
        val index = index(row, column)
        positions[index] = position
        colors[index] = color
        rightControls[index] = rightControlPoint
        bottomControls[index] = bottomControlPoint
    }

    /**
     * Current vertices (normalized position + color), row by row. Runs [block]
     * first, so animated state read there is current. Used for slide overlays.
     */
    fun vertices(): List<Pair<Offset, Color>> {
        block()
        return positions.indices.map { positions[it] to colors[it] }
    }

    override val intrinsicSize: Size get() = Size.Unspecified

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        block()
        val width = size.width
        val height = size.height
        val (points, vertexColors, indices) = tessellate(width, height)
        // Opaque white paint + BlendMode.DST: the triangles keep their vertex colors,
        // only the painter alpha is applied on top.
        val paint = Paint().apply {
            isAntiAlias = true
            color = org.jetbrains.skia.Color.WHITE
            this.alpha = (255 * this@MeshGradientPainter.alpha).toInt().coerceIn(0, 255)
        }
        colorFilter?.let { paint.colorFilter = it.asSkiaColorFilter() }
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawVertices(
                VertexMode.TRIANGLES,
                points,
                vertexColors,
                null,
                indices,
                BlendMode.DST,
                paint,
            )
        }
    }

    private fun index(row: Int, column: Int) = row * (columns + 1) + column

    private fun pos(row: Int, column: Int) = positions[index(row.coerceIn(0, rows), column.coerceIn(0, columns))]

    /** Outgoing tangent control point of the horizontal edge (row, column) → (row, column + 1). */
    private fun rightOut(row: Int, column: Int): Offset {
        val explicit = rightControls[index(row, column)]
        val p = pos(row, column)
        val next = pos(row, column + 1)
        return when {
            explicit == Offset.Zero -> p + (next - p) / 3f
            explicit.isSpecified() -> explicit
            else -> p + (next - pos(row, column - 1)) / 6f
        }
    }

    /** Incoming control point of the same edge, mirrored from the end vertex's right tangent. */
    private fun rightIn(row: Int, column: Int): Offset {
        val end = pos(row, column + 1)
        val start = pos(row, column)
        val explicit = rightControls[index(row, column + 1)]
        return when {
            explicit == Offset.Zero -> end + (start - end) / 3f
            explicit.isSpecified() -> end * 2f - explicit
            else -> end - (pos(row, column + 2) - start) / 6f
        }
    }

    private fun bottomOut(row: Int, column: Int): Offset {
        val explicit = bottomControls[index(row, column)]
        val p = pos(row, column)
        val next = pos(row + 1, column)
        return when {
            explicit == Offset.Zero -> p + (next - p) / 3f
            explicit.isSpecified() -> explicit
            else -> p + (next - pos(row - 1, column)) / 6f
        }
    }

    private fun bottomIn(row: Int, column: Int): Offset {
        val end = pos(row + 1, column)
        val start = pos(row, column)
        val explicit = bottomControls[index(row + 1, column)]
        return when {
            explicit == Offset.Zero -> end + (start - end) / 3f
            explicit.isSpecified() -> end * 2f - explicit
            else -> end - (pos(row + 2, column) - start) / 6f
        }
    }

    private fun tessellate(width: Float, height: Float): Triple<FloatArray, IntArray, ShortArray> {
        val steps = SUBDIVISIONS
        val gridW = columns * steps + 1
        val gridH = rows * steps + 1
        val points = FloatArray(gridW * gridH * 2)
        val vertexColors = IntArray(gridW * gridH)
        for (gy in 0 until gridH) {
            val row = (gy / steps).coerceAtMost(rows - 1)
            val v = (gy - row * steps) / steps.toFloat()
            for (gx in 0 until gridW) {
                val column = (gx / steps).coerceAtMost(columns - 1)
                val u = (gx - column * steps) / steps.toFloat()
                val point = coons(row, column, u, v)
                val i = gy * gridW + gx
                points[i * 2] = point.x * width
                points[i * 2 + 1] = point.y * height
                vertexColors[i] = colorAt(row, column, u, v).toArgb()
            }
        }
        val indices = ShortArray((gridW - 1) * (gridH - 1) * 6)
        var k = 0
        for (gy in 0 until gridH - 1) {
            for (gx in 0 until gridW - 1) {
                val a = gy * gridW + gx
                val b = a + 1
                val c = a + gridW
                val d = c + 1
                indices[k++] = a.toShort(); indices[k++] = b.toShort(); indices[k++] = c.toShort()
                indices[k++] = b.toShort(); indices[k++] = d.toShort(); indices[k++] = c.toShort()
            }
        }
        return Triple(points, vertexColors, indices)
    }

    private fun coons(row: Int, column: Int, u: Float, v: Float): Offset {
        val p00 = pos(row, column)
        val p01 = pos(row, column + 1)
        val p10 = pos(row + 1, column)
        val p11 = pos(row + 1, column + 1)
        val top = cubic(p00, rightOut(row, column), rightIn(row, column), p01, u)
        val bottom = cubic(p10, rightOut(row + 1, column), rightIn(row + 1, column), p11, u)
        val left = cubic(p00, bottomOut(row, column), bottomIn(row, column), p10, v)
        val right = cubic(p01, bottomOut(row, column + 1), bottomIn(row, column + 1), p11, v)
        val ruled = top * (1 - v) + bottom * v + left * (1 - u) + right * u
        val bilinear = p00 * ((1 - u) * (1 - v)) + p01 * (u * (1 - v)) + p10 * ((1 - u) * v) + p11 * (u * v)
        return ruled - bilinear
    }

    private fun colorAt(row: Int, column: Int, u: Float, v: Float): Color {
        if (!hasBicubicColor) {
            val top = lerpColor(colorOf(row, column), colorOf(row, column + 1), u)
            val bottom = lerpColor(colorOf(row + 1, column), colorOf(row + 1, column + 1), u)
            return lerpColor(top, bottom, v)
        }
        // Catmull-Rom across the whole vertex grid: rows first, then the column result.
        val rowsInterpolated = (-1..2).map { dr ->
            catmullRom(
                colorOf(row + dr, column - 1),
                colorOf(row + dr, column),
                colorOf(row + dr, column + 1),
                colorOf(row + dr, column + 2),
                u,
            )
        }
        return catmullRom(rowsInterpolated[0], rowsInterpolated[1], rowsInterpolated[2], rowsInterpolated[3], v)
    }

    private fun colorOf(row: Int, column: Int) = colors[index(row.coerceIn(0, rows), column.coerceIn(0, columns))]

    private companion object {
        const val SUBDIVISIONS = 16
    }
}

private fun Offset.isSpecified() = this != Offset.Unspecified && !x.isNaN()

private fun cubic(p0: Offset, c0: Offset, c1: Offset, p1: Offset, t: Float): Offset {
    val mt = 1 - t
    return p0 * (mt * mt * mt) + c0 * (3 * mt * mt * t) + c1 * (3 * mt * t * t) + p1 * (t * t * t)
}

private fun lerpColor(a: Color, b: Color, t: Float) = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)

private fun catmullRom(p0: Color, p1: Color, p2: Color, p3: Color, t: Float): Color {
    fun channel(a: Float, b: Float, c: Float, d: Float): Float {
        val t2 = t * t
        val t3 = t2 * t
        return (0.5f * (2 * b + (-a + c) * t + (2 * a - 5 * b + 4 * c - d) * t2 + (-a + 3 * b - 3 * c + d) * t3))
            .coerceIn(0f, 1f)
    }
    return Color(
        red = channel(p0.red, p1.red, p2.red, p3.red),
        green = channel(p0.green, p1.green, p2.green, p3.green),
        blue = channel(p0.blue, p1.blue, p2.blue, p3.blue),
        alpha = channel(p0.alpha, p1.alpha, p2.alpha, p3.alpha),
    )
}
