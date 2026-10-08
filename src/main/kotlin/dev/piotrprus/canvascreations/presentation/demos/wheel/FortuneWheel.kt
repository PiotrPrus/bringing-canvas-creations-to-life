package dev.piotrprus.canvascreations.presentation.demos.wheel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Shader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// Desktop port of the SpinWheel sample (FortuneWheel.kt). Android-only bits are
// swapped for their Skia equivalents: BlurMaskFilter → MaskFilter.makeBlur,
// android SweepGradient → Shader.makeSweepGradient. The API 33 AGSL rotational
// blur is left out — the ghost layers below are the motion blur shown on stage.

private object WheelColors {
    val neutral6_a76 = Color(0xC2E0E0E0)
    val neutral6_a100 = Color(0xFFE0E0E0)
    val neutral4_a100 = Color(0xFF9E9E9E)
    val neutral1_a76 = Color(0xC2212121)
    val bgPagePrimary_a100 = Color(0xFF1A1A2E)
    val fgIntLightPrimary = Color(0x40000000)
    val fgNeutralSecondary = Color(0xFFBBBBBB)
    val bgIntNeutralSecondary = Color(0xFF333333)
}

enum class FortuneWheelIndicatorPosition {
    TOP, BOTTOM, LEFT, RIGHT
}

data class FortuneWheelSegment(
    val id: String,
    val amount: Int,
    val displayOrder: Int,
    val label: String = "$${amount / 100}",
)

val DemoWheelSegments = listOf(
    FortuneWheelSegment(id = "1", amount = 500, displayOrder = 0),
    FortuneWheelSegment(id = "2", amount = 1000, displayOrder = 1),
    FortuneWheelSegment(id = "3", amount = 2000, displayOrder = 2),
    FortuneWheelSegment(id = "4", amount = 5000, displayOrder = 3),
    FortuneWheelSegment(id = "5", amount = 1500, displayOrder = 4),
)

const val LINEAR_ROTATION_DURATION = 800

/** Ghost layer (rotation offset in degrees, base alpha), closest trail first. */
private val GhostLayers = listOf(2f to 0.8f, 4f to 0.6f, 6f to 0.4f, 8f to 0.3f)

/**
 * The fortune wheel. [ghostLayers] (0–4) is how many trailing copies make up the
 * motion blur; [drawOutline] toggles the segment outline drawArc.
 * [previewRotation] freezes the wheel at that angle with the ghost layers fully
 * visible, as if it were spinning clockwise — used to show the layers stacking.
 * [overshootSeed] makes the landing overshoot deterministic (wheels in sync).
 */
@Composable
fun FortuneWheel(
    modifier: Modifier = Modifier,
    indicatorPosition: FortuneWheelIndicatorPosition = FortuneWheelIndicatorPosition.TOP,
    segments: List<FortuneWheelSegment>,
    targetSegmentIndex: Int?,
    startSpinning: Boolean,
    spinKey: Int = 0,
    motionBlurEnabled: Boolean = true,
    ghostLayers: Int = GhostLayers.size,
    drawOutline: Boolean = true,
    previewRotation: Float? = null,
    overshootSeed: Int? = null,
    onSpinComplete: () -> Unit,
) {
    val segmentCount = segments.size
    val sweepAngle = 360f / segmentCount
    val textMeasurer = rememberTextMeasurer()

    // Not keyed on spinKey: a re-spin continues from the current angle, no snap to 0.
    val rotateAnimatable = remember { Animatable(0f) }
    val currentTargetIndex by rememberUpdatedState(targetSegmentIndex)
    val latestOnSpinComplete by rememberUpdatedState(onSpinComplete)
    val indicatorHitAnimatable = remember { Animatable(0f) }
    var motionBlurVelocity by remember { mutableFloatStateOf(0f) }
    var activeSegmentIndex by remember { mutableIntStateOf(0) }
    val segmentHighlightFractions = remember(segmentCount) { List(segmentCount) { Animatable(0f) } }

    LaunchedEffect(activeSegmentIndex) {
        segmentHighlightFractions.forEachIndexed { index, animatable ->
            launch {
                if (index == activeSegmentIndex) {
                    animatable.animateTo(1f, tween(durationMillis = 50, easing = FastOutSlowInEasing))
                } else {
                    animatable.animateTo(0f, tween(durationMillis = 150, easing = EaseOut))
                }
            }
        }
    }

    LaunchedEffect(startSpinning, spinKey) {
        if (startSpinning) {
            // Phase 1: constant velocity — each 360° takes exactly LINEAR_ROTATION_DURATION ms.
            while (currentTargetIndex == null) {
                rotateAnimatable.animateTo(
                    targetValue = rotateAnimatable.value + 360f,
                    animationSpec = tween(durationMillis = LINEAR_ROTATION_DURATION, easing = LinearEasing),
                )
            }

            // Phase 2: decelerate with overshoot then settle.
            val targetIndex = currentTargetIndex ?: return@LaunchedEffect
            val currentValue = rotateAnimatable.value
            val targetRotation = (-targetIndex * sweepAngle).mod(360f)
            val currentNormalized = currentValue.mod(360f)
            val distanceToTarget = (targetRotation - currentNormalized).mod(360f)
            val additionalRotations = 360f
            val finalTarget = currentValue + additionalRotations + distanceToTarget

            val overshootRandom = overshootSeed?.let { Random(it) } ?: Random.Default
            val overshootAngle = sweepAngle / 2f + overshootRandom.nextInt(5, 30).toFloat()
            val overshootTarget = finalTarget + overshootAngle
            val phase1Velocity = 360f / LINEAR_ROTATION_DURATION

            // Quadratic ease-out whose initial slope matches phase 1 velocity — no jump.
            val quadraticEaseOut = CubicBezierEasing(0.33f, 0.66f, 0.66f, 1.0f)
            val overshootDistance = additionalRotations + distanceToTarget + overshootAngle
            val decelerateDuration = (2f * overshootDistance / phase1Velocity).toInt()

            rotateAnimatable.animateTo(
                targetValue = overshootTarget,
                animationSpec = tween(durationMillis = decelerateDuration, easing = quadraticEaseOut),
            )
            val settleDuration = (overshootAngle * 45).toInt()
            rotateAnimatable.animateTo(
                targetValue = finalTarget,
                animationSpec = tween(durationMillis = settleDuration, easing = EaseInOut),
            )
            latestOnSpinComplete()
        }
    }

    LaunchedEffect(rotateAnimatable, sweepAngle) {
        var lastBoundary = 0
        var prevRotation = 0f
        var prevDirection = 0
        snapshotFlow { rotateAnimatable.value }.collect { rotation ->
            val boundary = ((rotation + sweepAngle / 2) / sweepAngle).toInt()
            val currentDirection = when {
                rotation > prevRotation -> 1
                rotation < prevRotation -> -1
                else -> prevDirection
            }
            val boundaryChanged = boundary != lastBoundary
            lastBoundary = boundary
            prevRotation = rotation
            if (currentDirection != 0) prevDirection = currentDirection
            if (boundaryChanged) {
                activeSegmentIndex = (-boundary).mod(segmentCount)
                launch {
                    indicatorHitAnimatable.animateTo(30f * -currentDirection, tween(100, easing = FastOutLinearInEasing))
                    indicatorHitAnimatable.animateTo(0f, tween(50, easing = FastOutLinearInEasing))
                }
            }
        }
    }

    // Velocity (deg/s) tracked frame-by-frame for the motion blur.
    LaunchedEffect(rotateAnimatable) {
        var prevValue = rotateAnimatable.value
        var prevTimeMs = System.currentTimeMillis()
        snapshotFlow { rotateAnimatable.value }.collect { value ->
            val nowMs = System.currentTimeMillis()
            val dt = nowMs - prevTimeMs
            if (dt > 0) motionBlurVelocity = (value - prevValue) / dt * 1000f
            prevValue = value
            prevTimeMs = nowMs
        }
    }

    // Blur fades in from half the phase-1 speed and is full at phase-1 speed.
    val phase1VelocityDegPerSec = 360f / LINEAR_ROTATION_DURATION * 1000f
    val motionBlurThreshold = phase1VelocityDegPerSec / 2f
    val absVelocity = abs(motionBlurVelocity)
    val blurMultiplier = if (previewRotation != null) 1f else {
        ((absVelocity - motionBlurThreshold) / motionBlurThreshold).coerceIn(0f, 1f)
    }
    val spinDirection = if (previewRotation != null || motionBlurVelocity >= 0f) 1f else -1f
    val visualRotation = previewRotation ?: (rotateAnimatable.value % 360f)

    val drawWheelContent: DrawScope.() -> Unit = {
        val dotRadius = 4.dp.toPx()
        val dotMargin = 6.dp.toPx()
        val outlineWidthPx = 2.dp.toPx()
        val availableRadius = min(size.width, size.height) / 2f
        val radius = availableRadius - dotRadius - dotMargin
        val startAngleOffset = when (indicatorPosition) {
            FortuneWheelIndicatorPosition.TOP -> -90f
            FortuneWheelIndicatorPosition.BOTTOM -> 90f
            FortuneWheelIndicatorPosition.LEFT -> 180f
            FortuneWheelIndicatorPosition.RIGHT -> 0f
        } - sweepAngle / 2f

        segments.forEach { segment ->
            drawWheelSegment(
                segment = segment,
                startAngle = startAngleOffset + (segment.displayOrder * sweepAngle),
                sweepAngle = sweepAngle,
                radius = radius,
                outlineWidth = outlineWidthPx,
                textMeasurer = textMeasurer,
                indicatorPosition = indicatorPosition,
                segmentInnerColor = WheelColors.neutral6_a76,
                segmentOuterColor = WheelColors.bgPagePrimary_a100,
                drawOutline = drawOutline,
            )
        }
        drawHub(radius = radius)
        segments.forEachIndexed { index, _ ->
            drawIndicatorDot(
                startAngle = startAngleOffset + (index * sweepAngle),
                radius = radius,
                dotRadius = dotRadius,
                dotMargin = dotMargin,
            )
        }
    }

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        // Main wheel (layer0) — always fully opaque.
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                    rotationZ = visualRotation
                }
                .padding(16.dp),
            onDraw = drawWheelContent,
        )

        // Motion blur ghost layers trailing behind the spin direction.
        if (motionBlurEnabled && blurMultiplier > 0f) {
            GhostLayers.take(ghostLayers.coerceIn(0, GhostLayers.size)).forEach { (offset, baseAlpha) ->
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                            rotationZ = visualRotation - offset * spinDirection
                            alpha = baseAlpha * blurMultiplier
                        }
                        .padding(16.dp),
                    onDraw = drawWheelContent,
                )
            }
        }

        // Outer ring — sweep-gradient reflection.
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .padding(28.dp)
                .drawWithCache {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val ringRadius = min(size.width, size.height) / 2f
                    val ringPaint = org.jetbrains.skia.Paint().apply {
                        isAntiAlias = true
                        mode = PaintMode.STROKE
                        strokeWidth = 2.dp.toPx()
                        maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, blurSigma(2.dp.toPx()))
                        shader = Shader.makeSweepGradient(
                            cx, cy,
                            intArrayOf(
                                WheelColors.fgNeutralSecondary.toArgb(),
                                WheelColors.bgIntNeutralSecondary.toArgb(),
                                WheelColors.bgIntNeutralSecondary.toArgb(),
                                WheelColors.fgNeutralSecondary.toArgb(),
                                WheelColors.bgIntNeutralSecondary.toArgb(),
                                WheelColors.bgIntNeutralSecondary.toArgb(),
                                WheelColors.fgNeutralSecondary.toArgb(),
                            ),
                            floatArrayOf(0f, 0.1f, 0.4f, 0.5f, 0.6f, 0.9f, 1f),
                        )
                    }
                    onDrawBehind {
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawCircle(cx, cy, ringRadius, ringPaint)
                        }
                    }
                },
        )

        // Indicator (rounded triangle).
        Box(
            modifier = Modifier
                .align(
                    when (indicatorPosition) {
                        FortuneWheelIndicatorPosition.TOP -> Alignment.TopCenter
                        FortuneWheelIndicatorPosition.BOTTOM -> Alignment.BottomCenter
                        FortuneWheelIndicatorPosition.LEFT -> Alignment.CenterStart
                        FortuneWheelIndicatorPosition.RIGHT -> Alignment.CenterEnd
                    },
                )
                .graphicsLayer {
                    rotationZ = when (indicatorPosition) {
                        FortuneWheelIndicatorPosition.TOP -> 90f
                        FortuneWheelIndicatorPosition.BOTTOM -> -90f
                        FortuneWheelIndicatorPosition.LEFT -> 0f
                        FortuneWheelIndicatorPosition.RIGHT -> 180f
                    } + indicatorHitAnimatable.value
                    translationY = -8.dp.toPx()
                }
                .drawWithCache {
                    val roundedPolygon = RoundedPolygon(
                        numVertices = 3,
                        radius = size.minDimension / 2,
                        centerX = size.width / 2,
                        centerY = size.height / 2,
                        rounding = CornerRounding(size.minDimension / 10f, smoothing = 0.1f),
                    )
                    val roundedPolygonPath = roundedPolygon.toComposePath()
                    onDrawBehind {
                        drawPath(roundedPolygonPath, color = WheelColors.neutral6_a100)
                    }
                }
                .size(40.dp),
        )
    }
}

/**
 * The SpinWheelScreen behaviour without the buttons: every time [spinTrigger]
 * changes (and is > 0) the wheel spins for [spinMillis] at full speed, then
 * lands on a random segment. Clicking the wheel spins it again.
 */
@Composable
fun SpinningFortuneWheel(
    spinTrigger: Int,
    modifier: Modifier = Modifier,
    motionBlurEnabled: Boolean = true,
    ghostLayers: Int = 4,
    spinMillis: Long = 1600,
    endless: Boolean = false,
) {
    var spinning by remember { mutableStateOf(false) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    var spinKey by remember { mutableIntStateOf(0) }
    var clicks by remember { mutableIntStateOf(0) }

    LaunchedEffect(spinTrigger, clicks) {
        if (spinTrigger <= 0 && clicks == 0) return@LaunchedEffect
        spinKey++
        targetIndex = null
        spinning = true
        if (endless) return@LaunchedEffect
        delay(spinMillis)
        targetIndex = Random.nextInt(DemoWheelSegments.size)
    }

    FortuneWheel(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) { if (!spinning) clicks++ },
        segments = DemoWheelSegments,
        targetSegmentIndex = targetIndex,
        startSpinning = spinning,
        spinKey = spinKey,
        motionBlurEnabled = motionBlurEnabled,
        ghostLayers = ghostLayers,
        onSpinComplete = {
            spinning = false
            targetIndex = null
        },
    )
}

/**
 * Drives one or more wheels through an endless spin → land → pause cycle. Every
 * wheel given the same state gets the same spinKey, target and overshoot seed,
 * so they spin, stop and restart in lockstep.
 */
@Stable
class WheelLoopState internal constructor() {
    var spinKey by mutableIntStateOf(0)
        internal set
    var spinning by mutableStateOf(false)
        internal set
    var targetIndex by mutableStateOf<Int?>(null)
        internal set
}

/**
 * While [running], spins for [spinMillis] at full speed, lands on a random
 * segment, waits [pauseMillis] and spins again. [spinMillis] is kept off a
 * multiple of LINEAR_ROTATION_DURATION so the target is never set right on a
 * phase-1 lap boundary (both wheels see it on the same lap).
 */
@Composable
fun rememberWheelLoop(
    running: Boolean,
    spinMillis: Long = 2000,
    pauseMillis: Long = 2500,
): WheelLoopState {
    val state = remember { WheelLoopState() }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (true) {
            state.targetIndex = null
            state.spinKey++
            state.spinning = true
            delay(spinMillis)
            state.targetIndex = Random(state.spinKey).nextInt(DemoWheelSegments.size)
            snapshotFlow { state.spinning }.first { !it }
            delay(pauseMillis)
        }
    }
    return state
}

/** A wheel driven by a shared [WheelLoopState]. */
@Composable
fun LoopingFortuneWheel(
    loop: WheelLoopState,
    modifier: Modifier = Modifier,
    motionBlurEnabled: Boolean = true,
) {
    FortuneWheel(
        modifier = modifier,
        segments = DemoWheelSegments,
        targetSegmentIndex = loop.targetIndex,
        startSpinning = loop.spinning,
        spinKey = loop.spinKey,
        motionBlurEnabled = motionBlurEnabled,
        overshootSeed = loop.spinKey,
        onSpinComplete = { loop.spinning = false },
    )
}

/**
 * Wheel frozen at [rotation] with [ghostLayers] drawn at full strength, so each
 * layer's offset angle and alpha can be seen stacked on the main wheel.
 */
@Composable
fun FrozenMotionBlurWheel(
    ghostLayers: Int,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
) {
    FortuneWheel(
        modifier = modifier,
        segments = DemoWheelSegments,
        targetSegmentIndex = null,
        startSpinning = false,
        ghostLayers = ghostLayers,
        previewRotation = rotation,
        onSpinComplete = {},
    )
}

/** Static wheel used to preview drawWheelSegment, with or without the outline. */
@Composable
fun StaticFortuneWheel(drawOutline: Boolean, modifier: Modifier = Modifier) {
    FortuneWheel(
        modifier = modifier,
        segments = DemoWheelSegments,
        targetSegmentIndex = null,
        startSpinning = false,
        drawOutline = drawOutline,
        onSpinComplete = {},
    )
}

private fun DrawScope.drawWheelSegment(
    segment: FortuneWheelSegment,
    indicatorPosition: FortuneWheelIndicatorPosition,
    startAngle: Float,
    sweepAngle: Float,
    radius: Float,
    outlineWidth: Float,
    textMeasurer: TextMeasurer,
    segmentInnerColor: Color,
    segmentOuterColor: Color,
    drawOutline: Boolean,
) {
    val center = Offset(size.width / 2f, size.height / 2f)

    // 1. Shadow glow on the two radial segment lines.
    drawIntoCanvas { canvas ->
        val shadowPaint = Paint().apply {
            color = WheelColors.neutral6_a100
            isAntiAlias = true
            blendMode = BlendMode.Screen
        }
        shadowPaint.asFrameworkPaint().maskFilter =
            MaskFilter.makeBlur(FilterBlurMode.SOLID, blurSigma(4.dp.toPx()))
        val startRad = (startAngle * PI / 180f).toFloat()
        val endRad = ((startAngle + sweepAngle) * PI / 180f).toFloat()
        canvas.drawLine(
            p1 = center,
            p2 = Offset(center.x + radius * cos(startRad), center.y + radius * sin(startRad)),
            paint = shadowPaint,
        )
        canvas.drawLine(
            p1 = center,
            p2 = Offset(center.x + radius * cos(endRad), center.y + radius * sin(endRad)),
            paint = shadowPaint,
        )
    }

    // 2. Filled segment with a linear gradient along its radial axis.
    val gradientAngleRad = ((startAngle + sweepAngle / 2f) * PI / 180f).toFloat()
    val fillBrush = Brush.linearGradient(
        0f to lerp(segmentInnerColor, segmentOuterColor, 0.6f),
        0.7f to lerp(segmentInnerColor, segmentOuterColor, 0.9f),
        1f to segmentOuterColor,
        start = center,
        end = Offset(
            x = center.x + radius * cos(gradientAngleRad),
            y = center.y + radius * sin(gradientAngleRad),
        ),
    )
    drawArc(
        brush = fillBrush,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
    )

    // 3. Segment outline.
    if (drawOutline) {
        drawArc(
            color = WheelColors.neutral1_a76,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            // Round join: the default miter join turns the 72° tip at the
            // center into a spike that pokes into the opposite segments.
            style = Stroke(width = outlineWidth, join = StrokeJoin.Round),
        )
    }

    // 4. Price text.
    val textStyle = TextStyle(
        color = WheelColors.neutral6_a100,
        fontWeight = FontWeight(650),
        fontSize = if (sweepAngle >= 45f) 22.sp else 16.sp,
        letterSpacing = (-0.44).sp,
        shadow = Shadow(
            color = WheelColors.fgIntLightPrimary,
            offset = Offset(1.dp.toPx(), 1.dp.toPx()),
            blurRadius = 1.dp.toPx(),
        ),
    )
    val textLayoutResult = textMeasurer.measure(segment.label, textStyle)
    val textSize = textLayoutResult.size
    val middleAngle = startAngle + sweepAngle / 2f
    val textRadius = radius * 0.8f
    val angleInRadians = (middleAngle * PI / 180f).toFloat()
    val textX = center.x + textRadius * cos(angleInRadians) - textSize.width / 2f
    val textY = center.y + textRadius * sin(angleInRadians) - textSize.height / 2f
    rotate(
        degrees = middleAngle + when (indicatorPosition) {
            FortuneWheelIndicatorPosition.TOP -> 90f
            FortuneWheelIndicatorPosition.BOTTOM -> -90f
            FortuneWheelIndicatorPosition.LEFT -> -180f
            FortuneWheelIndicatorPosition.RIGHT -> 0f
        },
        pivot = Offset(textX + textSize.width / 2f, textY + textSize.height / 2f),
    ) {
        drawText(textLayoutResult = textLayoutResult, topLeft = Offset(textX, textY))
    }
}

/**
 * Center cap. All segment tips, their glow lines and outlines meet in one point;
 * covering it keeps the center clean (also while ghost layers are stacked).
 */
private fun DrawScope.drawHub(radius: Float) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val hubRadius = radius * 0.07f
    drawCircle(
        brush = Brush.radialGradient(
            0f to WheelColors.neutral4_a100,
            1f to WheelColors.bgIntNeutralSecondary,
            center = center - Offset(hubRadius * 0.3f, hubRadius * 0.3f),
            radius = hubRadius * 1.3f,
        ),
        radius = hubRadius,
        center = center,
    )
    drawCircle(
        color = WheelColors.neutral1_a76,
        radius = hubRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx()),
    )
}

private fun DrawScope.drawIndicatorDot(
    startAngle: Float,
    radius: Float,
    dotRadius: Float,
    dotMargin: Float,
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val dotDistance = radius + dotRadius + dotMargin
    val dotAngleInRadians = (startAngle * PI / 180f).toFloat()
    drawCircle(
        color = WheelColors.neutral4_a100,
        radius = dotRadius,
        center = Offset(center.x + dotDistance * cos(dotAngleInRadians), center.y + dotDistance * sin(dotAngleInRadians)),
    )
}

/** Android BlurMaskFilter takes a radius; Skia wants a sigma. */
private fun blurSigma(radius: Float): Float = if (radius > 0f) radius * 0.57735f + 0.5f else 0f

private fun RoundedPolygon.toComposePath(): Path {
    val path = Path()
    cubics.forEachIndexed { index, cubic ->
        if (index == 0) path.moveTo(cubic.anchor0X, cubic.anchor0Y)
        path.cubicTo(
            cubic.control0X, cubic.control0Y,
            cubic.control1X, cubic.control1Y,
            cubic.anchor1X, cubic.anchor1Y,
        )
    }
    path.close()
    return path
}
