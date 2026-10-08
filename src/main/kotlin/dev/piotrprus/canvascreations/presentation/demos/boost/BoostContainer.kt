package dev.piotrprus.canvascreations.presentation.demos.boost

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.skiaShader
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.PaintMode

/*
 * Port of TiltAndroid uikit/container/BoostContainer.kt → OuterGlowBorder.
 * Android-only API substitutions (everything else is unchanged):
 *  - BlurMaskFilter(radius, Blur.X)  → MaskFilter.makeBlur(FilterBlurMode.X, BlurMaskFilter(radius))
 *  - android.graphics.Paint.Style.STROKE → PaintMode.STROKE (property `mode`)
 *  - android.graphics.BlendMode.X  → org.jetbrains.skia.BlendMode.X
 *  - Compose Shader → .skiaShader for the framework paint
 * Presentation-only: each draw call is gated by [LocalBoostLayers] so the
 * slides can build the ring up layer by layer.
 */

/** Android's BlurMaskFilter takes a radius; Skia takes a sigma (same conversion Android does). */
private fun BlurMaskFilter(radius: Float): Float = radius * 0.57735f + 0.5f

@Composable
fun OuterGlowBorder(sizeDp: Dp, activated: Boolean) {
    val layers = LocalBoostLayers.current
    val infiniteTransition = rememberInfiniteTransition(label = "infinite glow")
    val rotateAnim = infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = InfiniteRepeatableSpec(
            repeatMode = RepeatMode.Restart,
            animation = tween(durationMillis = 2500, easing = LinearEasing)
        ), label = "rotate glow"
    )

    Box(
        modifier = Modifier
            .size(sizeDp)
    ) {
        val density = LocalDensity.current
        val sizePx = with(density) { sizeDp.toPx() }
        val isDark = RetroTheme.colors === DarkRetroColors
        val ringColor = if (isDark) Color.White else RetroTheme.colors.fg.neutral.primary
        val ringBlend = if (isDark) {
            org.jetbrains.skia.BlendMode.OVERLAY
        } else {
            org.jetbrains.skia.BlendMode.MULTIPLY
        }
        val layer1Color = ringColor
        val blurWidthLayer1 = 2.dp
        val blurWidthPxLayer1 = with(density) { blurWidthLayer1.toPx() }
        val center = (sizePx + blurWidthPxLayer1) / 2

        val nativePaintLayer1 = remember(isDark, density) {
            Paint().asFrameworkPaint().apply {
                this.maskFilter = MaskFilter.makeBlur(FilterBlurMode.INNER, BlurMaskFilter(blurWidthPxLayer1))
                color = layer1Color.toArgb()
                mode = PaintMode.STROKE
                blendMode = ringBlend
                alpha = (255 * 0.4).toInt()
            }
        }

        val layer2Color = ringColor
        val blurWidthLayer2 = 2.dp
        val blurWidthPxLayer2 = with(density) { blurWidthLayer2.toPx() }

        val nativePaintLayer2 = remember(isDark, density) {
            Paint().asFrameworkPaint().apply {
                this.maskFilter = MaskFilter.makeBlur(FilterBlurMode.INNER, BlurMaskFilter(blurWidthPxLayer2))
                color = layer2Color.toArgb()
                mode = PaintMode.STROKE
                blendMode = ringBlend
                alpha = (255 * 0.6).toInt()
            }
        }

        val layer3Padding = with(density) { 5.dp.toPx() }
        val layer3Width = with(density) { 2.dp.toPx() }
        val nativePaintLayer3 = remember(isDark, density) {
            Paint().asFrameworkPaint().apply {
                this.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, BlurMaskFilter(layer3Width))
                mode = PaintMode.STROKE
                blendMode = if (isDark) {
                    org.jetbrains.skia.BlendMode.COLOR_DODGE
                } else {
                    org.jetbrains.skia.BlendMode.MULTIPLY
                }
                shader = SweepGradientShader(
                    center = Offset(center, center),
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        ringColor,
                        ringColor,
                        Color.Transparent,
                        Color.Transparent,
                        ringColor,
                        ringColor,
                        Color.Transparent,
                        Color.Transparent
                    ),
                    colorStops = listOf(
                        0f,
                        0.55f,
                        0.6f,
                        0.70f,
                        0.75f,
                        0.8f,
                        0.84f,
                        0.87f,
                        0.91f,
                        1f
                    )
                ).skiaShader
            }
        }

        val blurWidthLayer4 = 3.dp
        val blurWidthPxLayer4 = with(density) { blurWidthLayer4.toPx() }

        val infoColor = RetroTheme.colors.fg.info.primary
        val orange = RetroTheme.colors.decorative.orange
        val pineapple = RetroTheme.colors.decorative.pineapple
        val lime = RetroTheme.colors.decorative.lime
        val sea = RetroTheme.colors.decorative.sea
        val violet = RetroTheme.colors.decorative.violet
        val vibrantPink = RetroTheme.colors.decorative.vibrantPink
        val nativePaintLayer4 = remember(isDark, density) {
            Paint().asFrameworkPaint().apply {
                this.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, BlurMaskFilter(blurWidthPxLayer4))
                mode = PaintMode.STROKE
                strokeWidth = blurWidthPxLayer4
                blendMode = if (isDark) {
                    org.jetbrains.skia.BlendMode.SCREEN
                } else {
                    org.jetbrains.skia.BlendMode.MULTIPLY
                }
                shader = LinearGradientShader(
                    from = Offset(0f, 0f), to = Offset(sizePx, sizePx), colors = listOf(
                        orange,
                        pineapple,
                        lime,
                        sea,
                        infoColor,
                        violet,
                        vibrantPink
                    )
                ).skiaShader
            }
        }

        Canvas(modifier = Modifier
            .wrapContentSize(unbounded = true)
            .size(sizeDp + blurWidthLayer1),
            onDraw = {
                this.drawIntoCanvas { canvas ->
                    if (activated && BoostLayer.GlowLayer4 in layers) {
                        withTransform({
                            rotate(
                                degrees = if (BoostLayer.Rotation in layers) 360f * rotateAnim.value else 0f,
                            )
                        }) {
                            canvas.nativeCanvas.drawCircle(
                                center,
                                center,
                                sizePx / 2 + blurWidthPxLayer4,
                                nativePaintLayer4
                            )
                        }
                    }
                    if (BoostLayer.GlowLayer1 in layers) {
                        canvas.nativeCanvas.drawCircle(center, center, sizePx / 2, nativePaintLayer1)
                    }
                    if (BoostLayer.GlowLayer2 in layers) {
                        canvas.nativeCanvas.drawCircle(center, center, sizePx / 2, nativePaintLayer2)
                    }
                    if (BoostLayer.GlowLayer3 in layers) {
                        canvas.nativeCanvas.drawArc(
                            0f + layer3Padding,
                            0f + layer3Padding,
                            size.width - layer3Padding,
                            size.height - layer3Padding,
                            0f,
                            360f,
                            false,
                            nativePaintLayer3
                        )
                    }
                }
            })
    }
}
