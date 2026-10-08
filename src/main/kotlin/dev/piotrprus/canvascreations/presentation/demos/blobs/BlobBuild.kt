package dev.piotrprus.canvascreations.presentation.demos.blobs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// The blob built up from a circle, one step at a time. BlobBuildDemo runs
// exactly what blobBuildCode(step) shows — keep the two in sync.

/** Warm gold of Lumeo's orb, used across the blob slides. */
val Gold = Color(0xFFD3B26E)

val TAU = (2 * PI).toFloat()

/** Unit vector pointing at [position] around the circle, where `0..1` is one lap. */
fun direction(position: Float): Offset {
    val angle = position * TAU
    return Offset(cos(angle), sin(angle))
}

/** Seconds since the first frame, updated every frame. */
@Composable
fun rememberTime(): State<Float> {
    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { time.floatValue = (it - start) / 1_000_000_000f }
    }
    return time
}

/**
 * 0 circle · 1 points on it · 2 path through the points ·
 * 3 points move along their radius over time · 4 smooth curve (Catmull-Rom).
 */
@Composable
fun BlobBuildDemo(step: Int, modifier: Modifier = Modifier) {
    val segments = 6
    val amplitude = 28.dp
    val random = Random(seed = 7)
    val phases = remember { List(segments) { random.nextFloat() * TAU } }
    val speeds = remember { List(segments) { 1f + random.nextFloat() } }
    val time by rememberTime()

    Canvas(modifier = modifier.size(480.dp)) {
        val radius = size.minDimension / 3f
        if (step == 0) {
            drawCircle(Gold, radius, style = Stroke(3.dp.toPx()))
            return@Canvas
        }
        drawCircle(Gold, radius, alpha = 0.3f, style = Stroke(3.dp.toPx()))
        val values = FloatArray(segments) { sin(speeds[it] * time + phases[it]) }
        val points = List(segments) { i ->
            val offset = if (step >= 3) amplitude.toPx() * values[i] else 0f
            center + direction(i / segments.toFloat()) * (radius + offset)
        }
        if (step >= 2) {
            val path = Path()
            if (step >= 4) {
                for (s in 0 until 120) {
                    val position = s / 120f
                    val offset = amplitude.toPx() * wobbleAt(values, position)
                    val p = center + direction(position) * (radius + offset)
                    if (s == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                }
            } else {
                points.forEachIndexed { i, p ->
                    if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                }
            }
            path.close()
            drawPath(path, Gold, alpha = 0.35f)
        }
        points.forEach { drawCircle(Gold, radius = 8.dp.toPx(), center = it) }
    }
}

/** The code on screen for [step]; what [BlobBuildDemo] runs at that step. */
fun blobBuildCode(step: Int): String = buildList {
    if (step >= 1) add("val segments = 6")
    if (step >= 3) {
        add("val amplitude = 28.dp")
        add("val random = Random(seed = 7)")
        add("val phases = remember { List(segments) { random.nextFloat() * TAU } }")
        add("val speeds = remember { List(segments) { 1f + random.nextFloat() } }")
        add("val time by rememberTime() // withFrameNanos loop")
    }
    if (step >= 1) add("")
    add("Canvas(modifier = Modifier.size(480.dp)) {")
    add("    val radius = size.minDimension / 3f")
    if (step == 0) {
        add("    drawCircle(Gold, radius, style = Stroke(3.dp.toPx()))")
    } else {
        add("    drawCircle(Gold, radius, alpha = 0.3f, style = Stroke(3.dp.toPx()))")
        if (step >= 3) {
            add("    val values = FloatArray(segments) { sin(speeds[it] * time + phases[it]) }")
        }
        add("    val points = List(segments) { i ->")
        if (step >= 3) {
            add("        val offset = amplitude.toPx() * values[i]")
            add("        center + direction(i / segments.toFloat()) * (radius + offset)")
        } else {
            add("        center + direction(i / segments.toFloat()) * radius")
        }
        add("    }")
        if (step >= 2) {
            add("    val path = Path()")
            if (step >= 4) {
                add("    for (s in 0 until 120) {")
                add("        val position = s / 120f")
                add("        // Catmull-Rom between the points, from KBlobs")
                add("        val offset = amplitude.toPx() * wobbleAt(values, position)")
                add("        val p = center + direction(position) * (radius + offset)")
                add("        if (s == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)")
                add("    }")
            } else {
                add("    points.forEachIndexed { i, p ->")
                add("        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)")
                add("    }")
            }
            add("    path.close()")
            add("    drawPath(path, Gold, alpha = 0.35f)")
        }
        add("    points.forEach { drawCircle(Gold, radius = 8.dp.toPx(), center = it) }")
    }
    add("}")
    if (step >= 1) {
        add("")
        add("fun direction(position: Float): Offset {")
        add("    val angle = position * TAU // 0..1 is one lap")
        add("    return Offset(cos(angle), sin(angle))")
        add("}")
    }
}.joinToString("\n")
