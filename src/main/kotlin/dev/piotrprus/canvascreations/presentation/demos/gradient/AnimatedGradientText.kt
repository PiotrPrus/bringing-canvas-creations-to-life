package dev.piotrprus.canvascreations.presentation.demos.gradient

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.demos.TALK_TITLE
import dev.piotrprus.canvascreations.presentation.ui.rememberResourceImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/** The talk title as plain white text — the starting point every effect builds on. */
@Composable
fun TitleText(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 52.sp,
    text: String = TALK_TITLE,
) {
    Text(
        text = text,
        color = Color.White,
        lineHeight = fontSize * 1.25f,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize,
        modifier = modifier,
    )
}

/** Live version of the midPoint snippet: the Cyan stop travels between two Magenta ends. */
@Composable
fun MidPointGradientTitle(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 52.sp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val midPoint = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            repeatMode = RepeatMode.Reverse,
            animation = tween(durationMillis = 3000, easing = EaseInOut),
        ),
        label = "midPoint",
    )
    TitleText(
        fontSize = fontSize,
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                onDrawWithContent {
                    val brush = Brush.linearGradient(
                        colorStops = arrayOf(0f to Color.Magenta, midPoint.value to Color.Cyan, 1f to Color.Magenta),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    )
                    drawContent()
                    drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
                }
            },
    )
}

/** Live version of the two animateColor snippets feeding one linear gradient. */
@Composable
fun TwoColorGradientTitle(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 52.sp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val spec = InfiniteRepeatableSpec<Color>(
        repeatMode = RepeatMode.Reverse,
        animation = tween(durationMillis = 3000, easing = EaseInOut),
    )
    val colorAnim = infiniteTransition.animateColor(Color.Cyan, Color.Yellow, spec, label = "color")
    val colorAnim2 = infiniteTransition.animateColor(Color.Magenta, Color.Red, spec, label = "color2")
    TitleText(
        fontSize = fontSize,
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                onDrawWithContent {
                    val brush = Brush.linearGradient(
                        colorStops = arrayOf(0f to colorAnim.value, 1f to colorAnim2.value),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    )
                    drawContent()
                    drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
                }
            },
    )
}

/** The stages of the production snippet, in the order the slides fix them. */
enum class MeshStage {
    /** drawImage with BlendMode.Src, no offscreen layer, no drawContent(). */
    Src,

    /** + CompositingStrategy.Offscreen — the image is now clipped to the layer. */
    Offscreen,

    /** + drawContent() — still BlendMode.Src, so the image replaces the text. */
    DrawContent,

    /** BlendMode.SrcAtop — the image only shows where the text is. */
    SrcAtop,
}

/** Rotation used by every mesh example: one full turn every 4 s. */
@Composable
fun rememberMeshRotation(): () -> Float {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val rotationValue = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation",
    )
    return { rotationValue.value }
}

/**
 * Live GradientTextTest from the production walk-through, at a given [stage].
 * The mesh bitmap spins around the centre of the Box and is sized to cover it
 * at any angle.
 */
@Composable
fun RotatingMeshText(
    stage: MeshStage,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp,
) {
    val rotation = rememberMeshRotation()
    val imageBitmap = rememberResourceImage("images/anim_flair_mesh_orange.png")
    Box(
        modifier = modifier
            .then(
                if (stage == MeshStage.Src) {
                    Modifier
                } else {
                    Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                },
            )
            .drawWithContent {
                if (stage >= MeshStage.DrawContent) drawContent()
                rotate(rotation()) {
                    drawMeshImage(
                        image = imageBitmap,
                        blendMode = if (stage == MeshStage.SrcAtop) BlendMode.SrcAtop else BlendMode.Src,
                    )
                }
            },
    ) {
        TitleText(fontSize = fontSize)
    }
}

/** Draws [image] centred, scaled so it covers the whole draw area at any rotation. */
fun DrawScope.drawMeshImage(
    image: androidx.compose.ui.graphics.ImageBitmap,
    blendMode: BlendMode,
) {
    val side = (hypot(size.width, size.height) * 1.05f).roundToInt()
    drawImage(
        image = image,
        dstOffset = IntOffset(
            x = (center.x - side / 2f).roundToInt(),
            y = (center.y - side / 2f).roundToInt(),
        ),
        dstSize = IntSize(side, side),
        blendMode = blendMode,
    )
}

/**
 * The TILT "boosted" username row: star icon + name, both painted with the
 * spinning flair_mesh_orange bitmap through BlendMode.SrcAtop.
 */
@Composable
fun BoostedUsername(
    name: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 96.sp,
    iconSize: Dp = 120.dp,
) {
    val rotation = rememberMeshRotation()
    val imageBitmap = rememberResourceImage("images/anim_flair_mesh_orange.png")
    Row(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                rotate(rotation()) {
                    drawMeshImage(imageBitmap, BlendMode.SrcAtop)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(iconSize / 4),
    ) {
        ShootingStar(modifier = Modifier.size(iconSize))
        Text(
            text = name,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** White shooting-star glyph (star + three trails); the mesh tints it. */
@Composable
private fun ShootingStar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val starCenter = Offset(w * 0.62f, w * 0.36f)
        val outer = w * 0.3f
        val inner = outer * 0.48f
        val star = Path().apply {
            for (i in 0 until 10) {
                val radius = if (i % 2 == 0) outer else inner
                val angle = -PI / 2 + i * PI / 5
                val x = starCenter.x + radius * cos(angle).toFloat()
                val y = starCenter.y + radius * sin(angle).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(star, Color.White)
        val stroke = w * 0.07f
        listOf(
            Offset(w * 0.42f, w * 0.52f) to Offset(w * 0.14f, w * 0.8f),
            Offset(w * 0.52f, w * 0.66f) to Offset(w * 0.3f, w * 0.9f),
            Offset(w * 0.3f, w * 0.44f) to Offset(w * 0.06f, w * 0.66f),
        ).forEach { (from, to) ->
            drawLine(Color.White, from, to, strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}
