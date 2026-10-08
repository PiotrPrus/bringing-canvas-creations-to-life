package dev.piotrprus.canvascreations.presentation.demos.blobs

// Port of KBlobs 0.1.0 (github.com/PiotrPrus/KBlobs, io.github.piotrprus:kblobs).
// The published library targets Android and iOS only — there is no JVM/desktop
// artifact — so the core is copied here unchanged apart from the package and
// visibility. Blob, BlobLayer, BlobPresets, LayerMotion, wobbleAt and
// displacement are the library's own code.

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Draws [layers] as morphing outlines of [shape], the first layer at the bottom. */
@Composable
fun Blob(
    layers: List<BlobLayer>,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    intensity: () -> Float = { 1f },
    speed: () -> Float = { 1f },
    animate: Boolean = true,
) {
    val clock = remember { BlobClock() }
    val latestLayers by rememberUpdatedState(layers)
    val latestSpeed by rememberUpdatedState(speed)

    LaunchedEffect(clock, animate) {
        if (!animate) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                // Cap the step so a dropped frame or a return from background does not jump.
                val dt = if (last == 0L) 0f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                clock.advance(dt, latestLayers, latestSpeed())
            }
        }
    }

    Spacer(
        modifier.drawWithCache {
            val maxSegments = layers.maxOfOrNull { it.segments } ?: 1
            val samples = sampleOutline(
                path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) },
                count = (maxSegments * 16).coerceIn(MIN_SAMPLES, MAX_SAMPLES),
            )
            val prepared = layers.mapIndexed { index, layer ->
                PreparedLayer(
                    layer = layer,
                    motion = LayerMotion(layer.seed ?: defaultSeed(index), layer.segments),
                    values = FloatArray(layer.segments),
                    path = Path(),
                    minPx = layer.amplitude.start.toPx(),
                    maxPx = layer.amplitude.endInclusive.toPx(),
                    blurPx = layer.blur.toPx(),
                    style = if (layer.strokeWidth.value > 0f) {
                        Stroke(width = layer.strokeWidth.toPx(), join = StrokeJoin.Round)
                    } else {
                        Fill
                    },
                    graphicsLayer = if (layer.blur.value > 0f) obtainGraphicsLayer() else null,
                )
            }

            onDrawBehind {
                // Reading the frame counter ties this draw to the clock: each tick redraws
                // without recomposing.
                clock.frame
                if (samples == null) return@onDrawBehind
                val swing = intensity()
                prepared.forEachIndexed { index, p ->
                    if (!p.layer.visible) return@forEachIndexed
                    p.buildPath(samples, clock.time(index), clock.spin(index), swing)
                    drawPrepared(p)
                }
            }
        },
    )
}

/** One morphing outline drawn by [Blob]. */
@Immutable
data class BlobLayer(
    val color: Color = Color.White,
    val brush: Brush? = null,
    val alpha: Float = 1f,
    val segments: Int = 6,
    val amplitude: ClosedRange<Dp> = (-4).dp..4.dp,
    val tempo: Float = 1f,
    val rotation: Float = 0f,
    val spin: Float = 0f,
    val blur: Dp = 0.dp,
    val blendMode: BlendMode = BlendMode.SrcOver,
    val strokeWidth: Dp = 0.dp,
    val visible: Boolean = true,
    val seed: Int? = null,
) {
    init {
        require(segments >= 1) { "segments must be at least 1, was $segments" }
        require(alpha in 0f..1f) { "alpha must be in 0..1, was $alpha" }
        require(tempo >= 0f) { "tempo must not be negative, was $tempo" }
        require(blur >= 0.dp) { "blur must not be negative, was $blur" }
        require(strokeWidth >= 0.dp) { "strokeWidth must not be negative, was $strokeWidth" }
        require(amplitude.start <= amplitude.endInclusive) {
            "amplitude must not be empty, was $amplitude"
        }
    }
}

/** Ready-made layer stacks to start from. */
object BlobPresets {
    /** A still, solid body wrapped in translucent layers that morph out of phase: an aura. */
    fun aura(
        body: Color,
        aura: Color = body,
        layers: Int = 3,
        reach: Dp = 24.dp,
    ): List<BlobLayer> {
        require(layers >= 0) { "layers must not be negative, was $layers" }
        val moving = (0 until layers).map { i ->
            // Outermost first, so the denser inner layers draw over it.
            val depth = if (layers == 1) 1f else 1f - i.toFloat() / layers
            BlobLayer(
                color = aura,
                alpha = 0.12f + 0.14f * (1f - depth),
                segments = 5 + i * 2,
                amplitude = (-4).dp..reach * depth,
                tempo = 0.7f + 0.35f * i,
                rotation = i * 47f,
            )
        }
        return moving + BlobLayer(color = body, amplitude = 0.dp..0.dp)
    }
}

private const val MIN_SAMPLES = 120
private const val MAX_SAMPLES = 1024

private fun defaultSeed(index: Int): Int = 0x5EED + index * 7919

private class PreparedLayer(
    val layer: BlobLayer,
    val motion: LayerMotion,
    val values: FloatArray,
    val path: Path,
    val minPx: Float,
    val maxPx: Float,
    val blurPx: Float,
    val style: DrawStyle,
    val graphicsLayer: GraphicsLayer?,
) {
    fun buildPath(samples: OutlineSamples, time: Float, spinDegrees: Float, intensity: Float) {
        motion.values(time, values)
        val offset = (layer.rotation + spinDegrees) / 360f
        path.rewind()
        val n = samples.count
        for (i in 0 until n) {
            val wobble = wobbleAt(values, layer.segments, i.toFloat() / n - offset)
            val d = displacement(wobble, minPx, maxPx, intensity)
            val x = samples.x[i] + samples.normalX[i] * d
            val y = samples.y[i] + samples.normalY[i] * d
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
    }
}

private fun DrawScope.drawPrepared(p: PreparedLayer) {
    val layer = p.layer
    val graphicsLayer = p.graphicsLayer
    if (graphicsLayer == null) {
        drawLayerPath(p, blend = true)
        return
    }
    // A blurred layer is drawn into its own graphics layer, grown on every side so the outward
    // swing and the blur's falloff are not clipped at the composable's bounds.
    val strokeHalf = (p.style as? Stroke)?.width?.div(2f) ?: 0f
    val pad = ceil(max(0f, max(p.maxPx, -p.minPx)) + strokeHalf + p.blurPx * 3f).toInt()
    graphicsLayer.renderEffect = BlurEffect(p.blurPx, p.blurPx, TileMode.Decal)
    graphicsLayer.blendMode = layer.blendMode
    graphicsLayer.topLeft = IntOffset(-pad, -pad)
    graphicsLayer.record(
        density = this,
        layoutDirection = layoutDirection,
        size = IntSize(ceil(size.width).toInt() + 2 * pad, ceil(size.height).toInt() + 2 * pad),
    ) {
        translate(pad.toFloat(), pad.toFloat()) { drawLayerPath(p, blend = false) }
    }
    drawLayer(graphicsLayer)
}

private fun DrawScope.drawLayerPath(p: PreparedLayer, blend: Boolean) {
    val layer = p.layer
    val blendMode = if (blend) layer.blendMode else DrawScope.DefaultBlendMode
    val brush = layer.brush
    if (brush != null) {
        drawPath(p.path, brush, alpha = layer.alpha, style = p.style, blendMode = blendMode)
    } else {
        drawPath(p.path, layer.color, alpha = layer.alpha, style = p.style, blendMode = blendMode)
    }
}

/** Per-layer time, advanced frame by frame, so tempo changes stay continuous. */
private class BlobClock {
    var frame by mutableLongStateOf(0L)
        private set
    private var times = FloatArray(0)
    private var spins = FloatArray(0)

    fun time(index: Int): Float = times.getOrElse(index) { 0f }
    fun spin(index: Int): Float = spins.getOrElse(index) { 0f }

    fun advance(dt: Float, layers: List<BlobLayer>, speed: Float) {
        if (times.size < layers.size) {
            times = times.copyOf(layers.size)
            spins = spins.copyOf(layers.size)
        }
        layers.forEachIndexed { i, layer ->
            times[i] += dt * layer.tempo * speed
            // Kept in one lap so the float keeps its precision over a long session.
            spins[i] = (spins[i] + dt * layer.spin * speed) % 360f
        }
        frame++
    }
}

// ---- internal/OutlineSamples.kt ----

/** A closed outline sampled at equal distances, with an outward unit normal at every point. */
private class OutlineSamples(
    val x: FloatArray,
    val y: FloatArray,
    val normalX: FloatArray,
    val normalY: FloatArray,
) {
    val count: Int get() = x.size
}

private fun sampleOutline(path: Path, count: Int): OutlineSamples? {
    val measure = PathMeasure()
    measure.setPath(path, forceClosed = true)
    val length = measure.length
    if (length <= 0f || count < 3) return null

    val x = FloatArray(count)
    val y = FloatArray(count)
    for (i in 0 until count) {
        val p = measure.getPosition(length * i / count)
        x[i] = p.x
        y[i] = p.y
    }
    return fromPoints(x, y)
}

private fun fromPoints(x: FloatArray, y: FloatArray): OutlineSamples {
    val count = x.size
    var nx = FloatArray(count)
    var ny = FloatArray(count)

    var area = 0f
    for (i in 0 until count) {
        val j = (i + 1) % count
        area += x[i] * y[j] - x[j] * y[i]
    }
    val sign = if (area >= 0f) 1f else -1f

    for (i in 0 until count) {
        val prev = (i - 1 + count) % count
        val next = (i + 1) % count
        nx[i] = sign * (y[next] - y[prev])
        ny[i] = sign * -(x[next] - x[prev])
    }
    repeat(2) {
        val sx = FloatArray(count)
        val sy = FloatArray(count)
        for (i in 0 until count) {
            val prev = (i - 1 + count) % count
            val next = (i + 1) % count
            sx[i] = nx[prev] + 2f * nx[i] + nx[next]
            sy[i] = ny[prev] + 2f * ny[i] + ny[next]
        }
        nx = sx
        ny = sy
    }
    for (i in 0 until count) {
        val len = sqrt(nx[i] * nx[i] + ny[i] * ny[i])
        if (len > 0f) {
            nx[i] /= len
            ny[i] /= len
        }
    }
    return OutlineSamples(x, y, nx, ny)
}

// ---- internal/Wobble.kt ----

private const val TWO_PI = (2 * PI).toFloat()

/** Two sines per control point with seeded frequencies and phases. */
private class LayerMotion(seed: Int, val segments: Int) {
    private val slowFrequency = FloatArray(segments)
    private val fastFrequency = FloatArray(segments)
    private val slowPhase = FloatArray(segments)
    private val fastPhase = FloatArray(segments)

    init {
        val random = Random(seed)
        for (i in 0 until segments) {
            slowFrequency[i] = TWO_PI * 0.25f * (0.7f + 0.6f * random.nextFloat())
            fastFrequency[i] = TWO_PI * 0.55f * (0.7f + 0.6f * random.nextFloat())
            slowPhase[i] = TWO_PI * random.nextFloat()
            fastPhase[i] = TWO_PI * random.nextFloat()
        }
    }

    fun values(time: Float, out: FloatArray) {
        for (i in 0 until segments) {
            out[i] = 0.65f * sin(slowFrequency[i] * time + slowPhase[i]) +
                0.35f * sin(fastFrequency[i] * time + fastPhase[i])
        }
    }
}

/**
 * The wobble at [position] along the outline (`0..1` is one lap), given control point
 * [values] spread evenly around it. Closed Catmull-Rom through the points, clamped to `-1..1`.
 */
fun wobbleAt(values: FloatArray, position: Float): Float = wobbleAt(values, values.size, position)

private fun wobbleAt(values: FloatArray, count: Int, position: Float): Float {
    if (count == 1) return values[0]
    val lap = position - floor(position)
    val s = lap * count
    val i = floor(s).toInt().coerceAtMost(count - 1)
    val t = s - i
    val p0 = values[(i - 1 + count) % count]
    val p1 = values[i]
    val p2 = values[(i + 1) % count]
    val p3 = values[(i + 2) % count]
    val t2 = t * t
    val t3 = t2 * t
    val v = 0.5f * (
        2f * p1 +
            (p2 - p0) * t +
            (2f * p0 - 5f * p1 + 4f * p2 - p3) * t2 +
            (3f * p1 - p0 - 3f * p2 + p3) * t3
        )
    return v.coerceIn(-1f, 1f)
}

private fun displacement(wobble: Float, min: Float, max: Float, intensity: Float): Float {
    val mid = (min + max) / 2f
    val half = (max - min) / 2f
    return mid + half * wobble * intensity
}
