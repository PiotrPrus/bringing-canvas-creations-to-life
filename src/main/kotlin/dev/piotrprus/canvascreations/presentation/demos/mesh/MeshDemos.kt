package dev.piotrprus.canvascreations.presentation.demos.mesh

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.demos.TALK_TITLE

// Warm palette (from anim_flair_mesh_orange.png), standing in for the docs'
// red / blue / green / yellow / purple / pink / indigo.
val Sunshine = Color(255, 212, 90)
val Amber = Color(255, 169, 90)
val Peach = Color(255, 139, 90)
val Coral = Color(255, 90, 90)
val Red = Color(0xFFEE2A45)
val Crimson = Color(0xFFB01238)

// The code below mirrors the snippets in MeshSlides.kt line for line
// (developer.android.com/develop/ui/compose/graphics/draw/mesh-gradient).

/** Docs sample 1: simple 1×1 mesh, four corners. */
@Composable
fun rememberSimpleMesh(): MeshGradientPainter {
    val gradientPainter = remember {
        MeshGradientPainter(rows = 1, columns = 1) {
            setVertex(0, 0, Offset(0f, 0f), Red)      // top-left
            setVertex(0, 1, Offset(1f, 0f), Sunshine) // top-right
            setVertex(1, 0, Offset(0f, 1f), Peach)    // bottom-left
            setVertex(1, 1, Offset(1f, 1f), Crimson)  // bottom-right
        }
    }
    return gradientPainter
}

/** Docs sample 3: advanced 3×3 grid (4×4 vertices). */
@Composable
fun rememberGridMesh(): MeshGradientPainter {
    val points = listOf(
        Offset(0.0f, 0.0f), Offset(0.3f, 0.0f), Offset(0.7f, 0.0f), Offset(1.0f, 0.0f),
        Offset(0.0f, 0.3f), Offset(0.2f, 0.4f), Offset(0.7f, 0.2f), Offset(1.0f, 0.3f),
        Offset(0.0f, 0.7f), Offset(0.3f, 0.8f), Offset(0.7f, 0.6f), Offset(1.0f, 0.7f),
        Offset(0.0f, 1.0f), Offset(0.3f, 1.0f), Offset(0.7f, 1.0f), Offset(1.0f, 1.0f),
    )
    val gradientPainter = remember {
        MeshGradientPainter(rows = 3, columns = 3) {
            setVertex(0, 0, points[0], Sunshine)
            setVertex(0, 1, points[1], Amber)
            setVertex(0, 2, points[2], Sunshine)
            setVertex(0, 3, points[3], Red)

            setVertex(1, 0, points[4], Coral)
            setVertex(1, 1, points[5], Sunshine)
            setVertex(1, 2, points[6], Coral)
            setVertex(1, 3, points[7], Red)

            setVertex(2, 0, points[8], Crimson)
            setVertex(2, 1, points[9], Coral)
            setVertex(2, 2, points[10], Red)
            setVertex(2, 3, points[11], Crimson)

            setVertex(3, 0, points[12], Red)
            setVertex(3, 1, points[13], Crimson)
            setVertex(3, 2, points[14], Coral)
            setVertex(3, 3, points[15], Sunshine)
        }
    }
    return gradientPainter
}

/**
 * Docs sample 4: animated mesh. Wider range and faster than the docs
 * (±0.15, 2 s), and keyed on offset so the painter follows the animation.
 * Also paints the title text.
 */
@Composable
fun rememberAnimatedMesh(): MeshGradientPainter {
    val infiniteTransition = rememberInfiniteTransition(label = "meshMovement")
    val offset by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "offset",
    )
    // keyed on offset, so the painter follows the animation
    val gradientPainter = remember(offset) {
        MeshGradientPainter(rows = 3, columns = 3) {
            setVertex(0, 0, Offset(0.0f, 0.0f), Crimson)
            setVertex(0, 1, Offset(0.3f, 0.0f), Peach)
            setVertex(0, 2, Offset(0.7f, 0.0f), Amber)
            setVertex(0, 3, Offset(1.0f, 0.0f), Sunshine)
            setVertex(1, 0, Offset(0.0f, 0.3f), Red)
            setVertex(1, 1, Offset(0.2f, 0.4f) + Offset(offset, offset), Coral)
            setVertex(1, 2, Offset(0.7f, 0.2f) + Offset(offset, offset), Peach)
            setVertex(1, 3, Offset(1.0f, 0.3f), Crimson)
            setVertex(2, 0, Offset(0.0f, 0.7f), Coral)
            setVertex(2, 1, Offset(0.3f, 0.8f) + Offset(offset, 0f), Red)
            setVertex(2, 2, Offset(0.7f, 0.6f) + Offset(offset, 0f), Sunshine)
            setVertex(2, 3, Offset(1.0f, 0.7f), Amber)
            setVertex(3, 0, Offset(0.0f, 1.0f), Sunshine)
            setVertex(3, 1, Offset(0.3f, 1.0f), Amber)
            setVertex(3, 2, Offset(0.7f, 1.0f), Red)
            setVertex(3, 3, Offset(1.0f, 1.0f), Crimson)
        }
    }
    return gradientPainter
}

/** A box filled with [painter] via `Modifier.paint`, optionally marking each vertex with its color. */
@Composable
fun MeshPreview(
    painter: MeshGradientPainter,
    modifier: Modifier = Modifier,
    showVertices: Boolean = false,
) {
    Box(modifier = modifier.paint(painter)) {
        if (showVertices) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Keep markers on the edges fully inside the frame.
                val inset = 20.dp.toPx()
                if (size.minDimension <= inset * 2) return@Canvas
                painter.vertices().forEach { (position, color) ->
                    val center = Offset(
                        x = (position.x * size.width).coerceIn(inset, size.width - inset),
                        y = (position.y * size.height).coerceIn(inset, size.height - inset),
                    )
                    drawCircle(Color.Black.copy(alpha = 0.35f), radius = 17.dp.toPx(), center = center)
                    drawCircle(color, radius = 13.dp.toPx(), center = center)
                    drawCircle(Color.White, radius = 13.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
                }
            }
        }
    }
}

/** The talk title painted with the animated mesh: offscreen layer + SrcAtop. */
@Composable
fun MeshGradientTitle(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 84.sp,
) {
    val meshPainter = rememberAnimatedMesh()
    Text(
        text = TALK_TITLE,
        color = Color.White,
        fontSize = fontSize,
        lineHeight = fontSize * 1.2f,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawIntoCanvas { canvas ->
                    canvas.saveLayer(size.toRect(), Paint().apply { blendMode = BlendMode.SrcAtop })
                    with(meshPainter) { draw(size) }
                    canvas.restore()
                }
            },
    )
}
