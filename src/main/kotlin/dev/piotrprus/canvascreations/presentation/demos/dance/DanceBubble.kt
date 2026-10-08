package dev.piotrprus.canvascreations.presentation.demos.dance

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// TILT palette stand-ins, matched against the original recordings.
object DanceColors {
    val CandyPink = Color(0xFFFF4F86)
    val Night = Color(0xFF8058D8)
    val Orange = Color(0xFFF5A23C)
    val Lime5 = Color(0xFFB4F04A)
    val Neutral2 = Color(0xFF1C1C1E)
    val Neutral4 = Color(0xFFA2A2A8)
    val NeutralBlack = Color(0xFF000000)
}

/** Which layers of the "Dance with Blends" bubble are switched on. */
data class BubbleLayers(
    val bigCircle: Boolean = true,
    val smallCircle: Boolean = true,
    val animateColor: Boolean = true,
    val blur: Boolean = false,
    val alphaLayer: Boolean = false,
    val colorDodge: Boolean = false,
    val colorBurn: Boolean = false,
) {
    companion object {
        val Base = BubbleLayers()
        val Blur = Base.copy(blur = true)
        val Alpha = Blur.copy(alphaLayer = true)
        val Dodge = Alpha.copy(colorDodge = true)
        val Burn = Dodge.copy(colorBurn = true)
    }
}

/** Material "gavel" icon (24x24 viewport), built in code — no image assets. */
val GavelIcon: ImageVector = ImageVector.Builder(
    name = "GavelFill",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 256f,
    viewportHeight = 256f,
).addPath(
    // Phosphor Icons "gavel-fill".
    pathData = addPathNodes(
        "M52.69,99.31a16,16,0,0,1,0-22.63l64-64a16,16,0,0,1,22.63,22.63l-64,64a16,16,0,0,1-22.63,0Zm190.63,17.37a16,16,0,0,0-22.63,0l-64,64a16,16,0,0,0,0,22.63h0a16,16,0,0,0,22.63,0l64-64A16,16,0,0,0,243.32,116.68Zm-35.11-15.8L155.12,47.79a4,4,0,0,0-5.66,0L87.8,109.45a4,4,0,0,0,0,5.66L103,130.34,28.69,204.69a16,16,0,0,0,22.62,22.62L125.66,153l15.23,15.23a4,4,0,0,0,5.66,0l61.66-61.66A4,4,0,0,0,208.21,100.88Z",
    ),
    fill = SolidColor(Color.White),
).build()

/** The production design sizes; everything scales with the requested bubble size. */
private val DesignRootSize = 120.dp

private fun calculateBubbleOffset(angle: Int, radius: Dp, parentSize: Dp): DpOffset {
    val angleRadians = Math.toRadians(angle.toDouble())
    val offsetX = (parentSize / 2 - radius) * cos(angleRadians).toFloat()
    val offsetY = (parentSize / 2 - radius) * sin(angleRadians).toFloat()
    return DpOffset(x = offsetX, y = offsetY)
}

/**
 * Live port of the TILT auction bubble from the slides: two keyframed circles
 * dancing inside a clipped root circle, then (per [layers]) blur, an alpha
 * layer and ColorDodge / ColorBurn circles drawn over the content. Toggling a
 * layer animates it in, so the bubble follows the code on screen.
 */
@Composable
fun DanceBubble(
    rootCircleSize: Dp,
    layers: BubbleLayers,
    modifier: Modifier = Modifier,
    showGavel: Boolean = false,
) {
    val scale = rootCircleSize / DesignRootSize
    val child1Size = rootCircleSize * 0.75f
    val child2Size = rootCircleSize * 0.45f

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val animOffset1 by infiniteTransition.animateValue(
        initialValue = calculateBubbleOffset(angle = 45, radius = 0.dp, parentSize = rootCircleSize),
        targetValue = calculateBubbleOffset(angle = 45, radius = 0.dp, parentSize = rootCircleSize),
        typeConverter = DpOffset.VectorConverter,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 7000
            calculateBubbleOffset(angle = 55, radius = 10.dp * scale, parentSize = rootCircleSize) at 500
            calculateBubbleOffset(angle = 55, radius = rootCircleSize / 2, parentSize = rootCircleSize) at 1500
            calculateBubbleOffset(angle = 300, radius = 0.dp, parentSize = rootCircleSize) at 2500
            calculateBubbleOffset(angle = 270, radius = 0.dp, parentSize = rootCircleSize) at 3000
            calculateBubbleOffset(angle = 180, radius = 0.dp, parentSize = rootCircleSize) at 3500
            calculateBubbleOffset(angle = 120, radius = 0.dp, parentSize = rootCircleSize) at 4500
            calculateBubbleOffset(angle = 120, radius = 30.dp * scale, parentSize = rootCircleSize) at 5500
            calculateBubbleOffset(angle = 180, radius = rootCircleSize / 2, parentSize = rootCircleSize) at 6000
        }),
        label = "offset1",
    )
    val animOffset2 by infiniteTransition.animateValue(
        initialValue = calculateBubbleOffset(angle = 135, radius = 0.dp, parentSize = rootCircleSize),
        targetValue = calculateBubbleOffset(angle = 135, radius = 0.dp, parentSize = rootCircleSize),
        typeConverter = DpOffset.VectorConverter,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 7000
            calculateBubbleOffset(angle = 180, radius = 10.dp * scale, parentSize = rootCircleSize) at 500
            calculateBubbleOffset(angle = 200, radius = 0.dp, parentSize = rootCircleSize) at 1500
            calculateBubbleOffset(angle = 200, radius = rootCircleSize / 2, parentSize = rootCircleSize) at 2500
            calculateBubbleOffset(angle = 45, radius = 0.dp, parentSize = rootCircleSize) at 4500
            calculateBubbleOffset(angle = 120, radius = 30.dp * scale, parentSize = rootCircleSize) at 5500
            calculateBubbleOffset(angle = 90, radius = 0.dp, parentSize = rootCircleSize) at 6000
        }),
        label = "offset2",
    )
    val keyframedColor by infiniteTransition.animateColor(
        initialValue = DanceColors.CandyPink,
        targetValue = DanceColors.CandyPink,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 7000
            DanceColors.Night at 2500
            DanceColors.Orange at 5500
        }),
        label = "color1",
    )
    val animColor1 = if (layers.animateColor) keyframedColor else DanceColors.CandyPink

    // Layer switches fade in/out instead of popping.
    val blurRadius by animateDpAsState(if (layers.blur) 10.dp * scale else 0.dp, tween(700))
    val alphaLayer by animateFloatAsState(if (layers.alphaLayer) 1f else 0f, tween(700))
    val dodge by animateFloatAsState(if (layers.colorDodge) 1f else 0f, tween(700))
    val burn by animateFloatAsState(if (layers.colorBurn) 1f else 0f, tween(700))
    val big by animateFloatAsState(if (layers.bigCircle) 1f else 0f, tween(500))
    val small by animateFloatAsState(if (layers.smallCircle) 1f else 0f, tween(500))
    val gavel by animateFloatAsState(if (showGavel) 1f else 0f, tween(600))

    Box(modifier = modifier.requiredSize(rootCircleSize), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .requiredSize(rootCircleSize)
                .clip(CircleShape)
                .background(color = DanceColors.Neutral2)
                .drawWithContent {
                    drawContent()
                    if (dodge > 0f) {
                        drawCircle(
                            color = DanceColors.Neutral2.copy(alpha = dodge),
                            blendMode = BlendMode.ColorDodge,
                        )
                    }
                    if (burn > 0f) {
                        drawCircle(
                            color = DanceColors.Neutral4.copy(alpha = burn),
                            blendMode = BlendMode.ColorBurn,
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(rootCircleSize)
                    .clip(CircleShape)
                    .background(color = DanceColors.Neutral2)
                    .then(
                        if (blurRadius > 0.dp) {
                            Modifier.blur(blurRadius, BlurredEdgeTreatment.Unbounded)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(child1Size)
                        .offset { IntOffset(x = animOffset1.x.roundToPx(), y = animOffset1.y.roundToPx()) }
                        .background(color = animColor1.copy(alpha = big), shape = CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(child2Size)
                        .offset { IntOffset(x = animOffset2.x.roundToPx(), y = animOffset2.y.roundToPx()) }
                        .background(color = DanceColors.Lime5.copy(alpha = small), shape = CircleShape),
                )
                if (alphaLayer > 0f) {
                    Box(
                        modifier = Modifier
                            .padding(5.dp * scale)
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(color = DanceColors.NeutralBlack.copy(alpha = 0.6f * alphaLayer)),
                    )
                }
            }
        }
        if (gavel > 0f) {
            // Drawn above the blend layers so it stays crisp white.
            Image(
                imageVector = GavelIcon,
                contentDescription = "Gavel",
                modifier = Modifier
                    .size(rootCircleSize * 0.38f)
                    .graphicsLayer { alpha = gavel },
            )
        }
    }
}
