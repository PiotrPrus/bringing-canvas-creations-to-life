package dev.piotrprus.canvascreations.presentation.demos.boost

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.piotrprus.particleemitter.CanvasEmitterConfig
import dev.piotrprus.particleemitter.CanvasParticleEmitter
import dev.piotrprus.particleemitter.ParticleShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathBuilder
import org.jetbrains.skia.PathDirection
import org.jetbrains.skia.PathMeasure
import org.jetbrains.skia.RSXform
import org.jetbrains.skia.Rect
import org.jetbrains.skia.TextBlob

/*
 * Port of TiltAndroid boosts/ui/screen/subview/BoostCompetitionBubble.kt — the
 * drawing parts of FloatingBoost (the bubble Box) plus BoostBlurParticles,
 * BoostVibrantStarParticles, BreakParticles and CircleBubbleTextCounterClockwise.
 * The travel path, winner/leaderboard flow and AnimatedVisibility(RetroTransition)
 * wrappers are left out (they are always "visible, no winner" here).
 * Android-only API substitutions:
 *  - ImageBitmap.imageResource(context.resources, R.drawable.x) → ImageBitmap.imageResource(R.drawable.x)
 *  - resources.getFont(R.font.aspekta_700) → getFont(R.font.aspekta_700) (skia Typeface)
 *  - android.graphics.Path + RectF → skia PathBuilder.addOval(Rect, CLOCKWISE)
 *  - android.graphics.Paint(textSize, typeface, letterSpacing) → skia Font + Paint
 *  - Canvas.drawTextOnPath (no Skia equivalent) → [drawTextOnPath] below, built on
 *    PathMeasure + TextBlob.makeFromRSXform
 */

const val BOOST_BUBBLE_TEXT_ROTATION_DURATION = 8000

/** Layers the slides can switch on one by one. Presentation-only. */
enum class BoostLayer {
    Surface, RadialOverlay, Noise, Emoji,
    GlowLayer1, GlowLayer2, GlowLayer3, GlowLayer4, Rotation,
    CircleText, BlurParticles, BreakParticles, VibrantStars;

    companion object {
        val All: Set<BoostLayer> = entries.toSet()
    }
}

val LocalBoostLayers = staticCompositionLocalOf { BoostLayer.All }

/**
 * The bubble Box of FloatingBoost: circular text, blur particles, glow border,
 * break particles, the tappable bubble Canvas and the emoji. Tapping it runs the
 * same LaunchedEffect(localTapCount) as the app (inner glow progress + bounce).
 */
@Composable
fun FloatingBoostBubble(
    boostText: String,
    emoji: String,
    tapsToBreak: Int = 20,
    onTapBoost: (Int) -> Unit = {},
) {
    val layers = LocalBoostLayers.current
    val bubbleSurfaceColor = RetroTheme.colors.overMedia.surface
    val bubbleScale = remember { Animatable(1f) }
    val noiseImage =
        remember { ImageBitmap.imageResource(R.drawable.boost_noise) }
    var innerGlowProgress by remember { mutableFloatStateOf(0f) }
    var localTapCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(localTapCount) {
        if (localTapCount > 0) {
            launch {
                localTapCount.toFloat().div(tapsToBreak).coerceAtMost(1f)
                    .also { innerGlowProgress = it }
            }
            launch {
                bubbleScale.animateTo(1.2f, tween(80))
                bubbleScale.animateTo(1f, tween(70))
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        if (BoostLayer.CircleText in layers) {
            CircleBubbleTextCounterClockwise(
                winner = null,
                boostText = boostText,
                modifier = Modifier.graphicsLayer {
                    scaleX = bubbleScale.value
                    scaleY = bubbleScale.value
                },
            )
        } else {
            Spacer(Modifier.size(RetroDimen.boostRotationContainerSize))
        }

        if (BoostLayer.BlurParticles in layers) {
            BoostBlurParticles(modifier = Modifier.graphicsLayer {
                scaleX = bubbleScale.value
                scaleY = bubbleScale.value
            })
        }
        Box(modifier = Modifier.graphicsLayer {
            scaleX = bubbleScale.value
            scaleY = bubbleScale.value
        }, contentAlignment = Alignment.Center) {
            OuterGlowBorder(sizeDp = RetroDimen.boostBubbleSize, activated = true)
            if (BoostLayer.BreakParticles in layers) {
                BreakParticles(size = DpSize(37.dp, 37.dp), progress = innerGlowProgress)
            }
            Canvas(modifier = Modifier
                .padding(1.dp)
                .size(RetroDimen.boostBubbleSize)
                .clickable(
                    onClick = {
                        localTapCount++
                        onTapBoost(localTapCount)
                    },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ), onDraw = {
                if (BoostLayer.Surface in layers) {
                    drawCircle(
                        color = bubbleSurfaceColor.copy(alpha = 0.52f),
                        radius = size.height / 2,
                        blendMode = BlendMode.ColorDodge
                    )
                }
                if (BoostLayer.RadialOverlay in layers) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent, Color.White.copy(alpha = 0.5f)
                            )
                        ), radius = size.height / 2, blendMode = BlendMode.Overlay
                    )
                }
                if (BoostLayer.Noise in layers) {
                    drawImage(
                        image = noiseImage,
                        blendMode = BlendMode.Softlight,
                        alpha = 1f
                    )
                }
            })

            if (BoostLayer.Emoji in layers) {
                Text(
                    text = emoji,
                    style = RetroTheme.typography.largeTitle,
                    modifier = Modifier.zIndex(5f)
                )
            }
        }
    }
}

@Composable
fun BoostBlurParticles(modifier: Modifier = Modifier) {
    val ovalImage =
        remember { ImageBitmap.imageResource(R.drawable.oval_low_q) }

    CanvasParticleEmitter(
        modifier = modifier
            .size(RetroDimen.boostBubbleSize)
            .clip(CircleShape), CanvasEmitterConfig(
            particlePerSecond = 10,
            emitterCenter = DpOffset(
                x = RetroDimen.boostRotationContainerSize / 2,
                y = RetroDimen.boostRotationContainerSize / 2
            ),
            startRegionShape = CanvasEmitterConfig.Shape.OVAL,
            startRegionSize = DpSize(
                RetroDimen.boostBubbleSize,
                RetroDimen.boostBubbleSize
            ),
            particleShapes = listOf(ParticleShape.Image(ovalImage)),
            lifespanRange = IntRange(3500, 4500),
            colors = listOf(Color.White.copy(alpha = 0.5f)),
            blendMode = BlendMode.Screen,
            alphaEasing = FastOutSlowInEasing,
            scaleEasing = LinearEasing,
            particleSizes = listOf(DpSize(60.dp, 60.dp)),
            initialForce = IntRange(18, 51),
            spread = IntRange(-180, 180),
            fadeOutTime = IntRange(2500, 3000),
            rotationRange = IntRange(0, 0),
            scaleTime = IntRange(2500, 3000),
            targetScaleRange = IntRange(2, 5),
            startScaleRange = IntRange(1, 2),
        )
    )
}

@Composable
fun BoostVibrantStarParticles(
    bubbleCenterProvider: () -> DpOffset,
    modifier: Modifier = Modifier,
) {
    val particleColors = RetroTheme.colors.decorative.particleColors
    var particleColor by remember { mutableStateOf(particleColors.first()) }
    val fourStarImage =
        remember { ImageBitmap.imageResource(R.drawable.star_four) }

    LaunchedEffect(Unit) {
        var colorIndex = 1
        while (true) {
            delay(200)
            particleColor =
                particleColors[colorIndex.rem(particleColors.size)]
            colorIndex++
        }
    }

    CanvasParticleEmitter(
        modifier = modifier, CanvasEmitterConfig(
            particlePerSecond = 60,
            emitterCenter = bubbleCenterProvider(),
            startRegionShape = CanvasEmitterConfig.Shape.OVAL,
            startRegionSize = DpSize(
                RetroDimen.boostBubbleSize,
                RetroDimen.boostBubbleSize
            ),
            particleShapes = listOf(ParticleShape.Image(fourStarImage)),
            lifespanRange = IntRange(3250, 3250),
            colors = listOf(particleColor),
            blendMode = BlendMode.SrcOver,
            alphaEasing = EaseInCirc,
            scaleEasing = EaseInCubic,
            particleSizes = listOf(DpSize(7.dp, 7.dp)),
            initialForce = IntRange(14, 14),
            spread = IntRange(-180, 180),
            fadeOutTime = IntRange(10000, 10000),
            rotationRange = IntRange(0, 45),
            scaleTime = IntRange(5000, 5000),
            targetScaleRange = IntRange(0, 1),
            startScaleRange = IntRange(1, 2),
            hideInStartRegion = true,
        )
    )
}

@Composable
private fun BreakParticles(size: DpSize, progress: Float) {
    val ovalImage = remember { ImageBitmap.imageResource(R.drawable.oval_low_q) }
    CanvasParticleEmitter(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = progress
                scaleY = progress
            }, CanvasEmitterConfig(
            particlePerSecond = 30,
            emitterCenter = size.center,
            startRegionShape = CanvasEmitterConfig.Shape.OVAL,
            startRegionSize = size,
            particleShapes = listOf(ParticleShape.Image(ovalImage)),
            lifespanRange = IntRange(2000, 2000),
            colors = listOf(
                RetroTheme.colors.fg.info.primary,
                RetroTheme.colors.decorative.violet,
                RetroTheme.colors.decorative.vibrantPink
            ),
            blendMode = BlendMode.Screen,
            alphaEasing = EaseInCirc,
            scaleEasing = EaseInCubic,
            particleSizes = listOf(DpSize(40.dp, 40.dp)),
            initialForce = IntRange(2, 3),
            spread = IntRange(-180, 180),
            fadeOutTime = IntRange(2000, 2000),
            rotationRange = IntRange(0, 0),
            scaleTime = IntRange(2000, 2000),
            targetScaleRange = IntRange(2, 2),
            startScaleRange = IntRange(1, 2),
        )
    )
}

@Composable
fun CircleBubbleTextCounterClockwise(
    winner: String?,
    modifier: Modifier = Modifier,
    boostText: String = stringResource(id = R.string.boost_competition_profile_flair_circular_title)
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "infinite rotation")
    val rotateAnim = infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = InfiniteRepeatableSpec(
            repeatMode = RepeatMode.Restart,
            animation = tween(
                durationMillis = BOOST_BUBBLE_TEXT_ROTATION_DURATION,
                easing = LinearEasing
            )
        ), label = "rotate anim"
    )
    val textFont = remember { getFont(R.font.aspekta_700) }
    val text by remember(winner) { mutableStateOf(if (winner == null) boostText else "CLAIMED BY @${winner}") }

    Spacer(modifier = modifier
        .size(RetroDimen.boostRotationContainerSize)
        .graphicsLayer {
            rotationZ = rotateAnim.value * -360f
        }
        .drawWithCache {
            val drawScope = this
            val textHeightPx = with(density) { RetroDimen.boostRotationTextHeight.toPx() }
            val path = PathBuilder().apply {
                addOval(
                    Rect.makeLTRB(
                        textHeightPx / 2,
                        textHeightPx / 2,
                        drawScope.size.width - textHeightPx / 2,
                        drawScope.size.height - textHeightPx / 2

                    ), PathDirection.CLOCKWISE, 1
                )
            }.detach()
            val paint = Paint()
                .apply {
                    color = Color.White.toArgb()
                }
            val font = Font(textFont, textHeightPx)
            val letterSpacing = 0.07f
            onDrawBehind {
                this.drawIntoCanvas {
                    it.nativeCanvas.drawTextOnPath(
                        text.uppercase(), path, 0f, 0f, font, letterSpacing, paint
                    )
                }
            }
        })
}

/**
 * Skia has no `drawTextOnPath`; this mirrors android.graphics.Canvas.drawTextOnPath:
 * each glyph sits on the path at its advance position (plus [hOffset]), rotated
 * to the path tangent and pushed by [vOffset] along the normal. Characters the
 * font lacks (Aspekta has no "•") fall back to the default typeface, as Android does.
 */
fun SkiaCanvas.drawTextOnPath(
    text: String,
    path: Path,
    hOffset: Float,
    vOffset: Float,
    font: Font,
    letterSpacing: Float,
    paint: Paint,
) {
    val spacing = letterSpacing * font.size
    val measure = PathMeasure(path, false, 1f)
    val length = measure.length
    val runs = linkedMapOf<Font, Pair<MutableList<Short>, MutableList<RSXform>>>()
    var x = hOffset
    var i = 0
    while (i < text.length) {
        val cp = text.codePointAt(i)
        val ch = String(Character.toChars(cp))
        i += Character.charCount(cp)
        val glyphFont = if (font.getStringGlyphs(ch).firstOrNull()?.toInt() == 0) {
            FontMgr.default.matchFamilyStyleCharacter(null, FontStyle.BOLD, null, cp)
                ?.let { Font(it, font.size) } ?: font
        } else {
            font
        }
        val glyph = glyphFont.getStringGlyphs(ch).firstOrNull() ?: continue
        val w = glyphFont.getWidths(shortArrayOf(glyph)).first()
        val mid = x + w / 2
        if (mid in 0f..length) {
            val pos = measure.getPosition(mid)
            val tan = measure.getTangent(mid)
            if (pos != null && tan != null) {
                // Glyph origin = point on path - tangent * w/2 + normal * vOffset.
                val tx = pos.x - tan.x * w / 2 - tan.y * vOffset
                val ty = pos.y - tan.y * w / 2 + tan.x * vOffset
                val run = runs.getOrPut(glyphFont) { mutableListOf<Short>() to mutableListOf() }
                run.first += glyph
                run.second += RSXform(tan.x, tan.y, tx, ty)
            }
        }
        x += w + spacing
    }
    runs.forEach { (runFont, run) ->
        val blob = TextBlob.makeFromRSXform(run.first.toShortArray(), run.second.toTypedArray(), runFont)
            ?: return@forEach
        drawTextBlob(blob, 0f, 0f, paint)
    }
}
