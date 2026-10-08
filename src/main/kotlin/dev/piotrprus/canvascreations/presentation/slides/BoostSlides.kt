package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.Column
import dev.piotrprus.canvascreations.presentation.demos.boost.BoostBubble
import dev.piotrprus.canvascreations.presentation.demos.boost.BoostLayer
import dev.piotrprus.canvascreations.presentation.demos.boost.BoostLayers
import dev.piotrprus.canvascreations.presentation.demos.boost.R
import dev.piotrprus.canvascreations.presentation.demos.boost.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import dev.piotrprus.canvascreations.presentation.deck.SlideScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.MediaFrame
import dev.piotrprus.canvascreations.presentation.ui.PhotoImage
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle
import dev.piotrprus.canvascreations.presentation.ui.StepCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.VideoClip
import dev.piotrprus.canvascreations.presentation.ui.lines

// Keynote slides 82–101: the TILT "boost" bubble, built layer by layer with
// native BlurMaskFilter paints, then the canvas particle emitter behind its glow.
// Most of these slides place media at the same coordinates as the Keynote
// layout, so they use absolute positions on the 1920×1080 stage.

private val BoostGradient = listOf(Color(0xFF00E040), Color(0xFF00A8FF))
private val BlurGradient = listOf(Color(0xFFFF1FD0), Color(0xFFFFB800))

// Code excerpts below are verbatim from TiltAndroid:
// boosts/ui/screen/subview/BoostCompetitionBubble.kt (bubble Canvas) and
// uikit/container/BoostContainer.kt (OuterGlowBorder, called by the bubble).

private const val BUBBLE_CODE = """
drawCircle(
    color = bubbleSurfaceColor.copy(alpha = 0.52f),
    radius = size.height / 2,
    blendMode = BlendMode.ColorDodge
)
drawCircle(
    brush = Brush.radialGradient(
        colors = listOf(
            Color.Transparent, Color.White.copy(alpha = 0.5f)
        )
    ), radius = size.height / 2, blendMode = BlendMode.Overlay
)
drawImage(
    image = noiseImage,
    blendMode = BlendMode.Softlight,
    alpha = 1f
)
"""

private const val LAYER3_CODE = """
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
)
"""

private const val DRAW_ARC_CODE = """
val layer3Padding = with(density) { 5.dp.toPx() }
val layer3Width = with(density) { 2.dp.toPx() }
"""

private const val DRAW_ARC_CALL_CODE = """
canvas.nativeCanvas.drawCircle(center, center, sizePx / 2, nativePaintLayer1)
canvas.nativeCanvas.drawCircle(center, center, sizePx / 2, nativePaintLayer2)
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
"""

private const val LAYER4_CODE = """
val nativePaintLayer4 = remember(isDark) {
    Paint().asFrameworkPaint().apply {
        this.maskFilter = BlurMaskFilter(blurWidthPxLayer4, BlurMaskFilter.Blur.NORMAL)
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = blurWidthPxLayer4
        blendMode = if (isDark) {
            android.graphics.BlendMode.SCREEN
        } else {
            android.graphics.BlendMode.MULTIPLY
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
        )
    }
}
"""

private const val ROTATION_ANIM_CODE = """
val infiniteTransition = rememberInfiniteTransition(label = "infinite glow")
val rotateAnim = infiniteTransition.animateFloat(
    initialValue = 0f, targetValue = 1f, animationSpec = InfiniteRepeatableSpec(
        repeatMode = RepeatMode.Restart,
        animation = tween(durationMillis = 2500, easing = LinearEasing)
    ), label = "rotate glow"
)
"""

private const val ROTATION_CODE = """
if (activated) {
    withTransform({
        rotate(
            degrees = 360f * rotateAnim.value,
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
"""

private const val EMITTER_CONFIG_CODE = """
data class CanvasEmitterConfig(
    val configId: String = "",
    val particlePerSecond: Int,
    val emitterCenter: DpOffset,
    val startRegionShape: Shape,
    val startRegionSize: DpSize,
    val particleShapes: List<ParticleShape>,
    val lifespanRange: IntRange,
    val fadeOutTime: IntRange,
    val scaleTime: IntRange,
    val colors: List<Color>,
    val particleSizes: List<DpSize>,
    val spread: IntRange = IntRange(-180, 180),
    val blendMode: BlendMode = BlendMode.SrcOver,
    val translateEasing: Easing = LinearOutSlowInEasing,
    val alphaEasing: Easing = LinearEasing,
    val scaleEasing: Easing = LinearEasing,
    val flyDistancesDp: IntRange = IntRange(10, 100),
    val rotationRange: IntRange = IntRange(-180, 180),
    val startScaleRange: IntRange = IntRange(0,1),
    val targetScaleRange: IntRange = IntRange(1,2),
    val hideInStartRegion: Boolean = false,
)
"""

private const val EMITTER_USAGE_CODE = """
CanvasParticleEmitter(
    modifier = modifier
        .size(size)
        .graphicsLayer {
            scaleX = progress
            scaleY = progress
        }, CanvasEmitterConfig(
        particlePerSecond = 50,
        emitterCenter = size.center,
        startRegionShape = CanvasEmitterConfig.Shape.OVAL,
        startRegionSize = size,
        particleShapes = listOf(ParticleShape.Image(ovalImage)),
        lifespanRange = IntRange(2000, 2000),
        colors = listOf(
            Color.Blue,
            Color.Magenta,
            Color.Cyan
        ),
        blendMode = BlendMode.Screen,
        translateEasing = FastOutLinearInEasing,
        alphaEasing = EaseInCirc,
        scaleEasing = EaseInCubic,
        particleSizes = listOf(DpSize(40.dp, 40.dp)),
        flyDistancesDp = IntRange(3, 5),
        spread = IntRange(-180, 180),
        fadeOutTime = IntRange(2000, 2000),
        rotationRange = IntRange(0, 0),
        scaleTime = IntRange(2000, 2000),
        targetScaleRange = IntRange(2, 2),
        startScaleRange = IntRange(1, 2),
    )
)
"""

private const val BLUR_MODIFIER_CODE = """
Box(modifier = Modifier
    .size(10.dp)
    .clip(CircleShape)
    .blur(radius = 10.dp)
)
"""

private const val BLUR_MASK_CODE = """
val nativePaintLayer = remember {
    Paint().asFrameworkPaint().apply {
        this.maskFilter = BlurMaskFilter(10, BlurMaskFilter.Blur.INNER)
        color = Color.Magenta.toArgb()
        style = android.graphics.Paint.Style.FILL
        blendMode = android.graphics.BlendMode.SCREEN
        alpha = (255 * 0.5).toInt()
    }
}
"""

/** Keynote 82. */
val boostSectionSlide = Slide {
    SectionTitle("The boost", BoostGradient)
}

/** Keynote 83 — the three boost prizes, drawn live by the app's bubble code. */
val profileFlairSlide = Slide {
    StageSlide("The boost") {
        BoostBubble(
            modifier = Modifier.place(300, 420, 360, 360),
            emoji = "💰",
            boostText = stringResource(id = R.string.boost_competition_coupon_circular_title, "$5"),
        )
        BoostBubble(
            modifier = Modifier.place(780, 420, 360, 360),
            emoji = "✨",
            boostText = stringResource(id = R.string.boost_competition_profile_flair_circular_title),
        )
        BoostBubble(
            modifier = Modifier.place(1260, 420, 360, 360),
            emoji = "🚚",
            boostText = stringResource(id = R.string.boost_competition_coupon_free_shipping_circular_title),
        )
    }
}

/** Keynote 84 — the bubble Canvas: surface, radial overlay, noise, one draw call per step. */
val boostBubbleSlide = Slide(steps = 2) {
    StageSlide("The boost") {
        StepCodeBlock(
            code = BUBBLE_CODE,
            modifier = Modifier.offset(110.dp, 260.dp),
            highlight = when (step) {
                0 -> lines(0..4)
                1 -> lines(5..11)
                else -> lines(12..16)
            },
            fontSize = 26.sp,
        )
        BoostBubble(
            modifier = Modifier.place(1280, 300, 480, 480),
            layers = buildSet {
                add(BoostLayer.Emoji)
                add(BoostLayer.Surface)
                if (step >= 1) add(BoostLayer.RadialOverlay)
                if (step >= 2) add(BoostLayer.Noise)
            },
        )
    }
}

/**
 * Keynote 86–87 — layer 3: a sweep gradient used as a light streak. Each step
 * after the bubble labels the next colour stop around the circle.
 */
val sweepGradientSlide = Slide(steps = 10) {
    StageSlide("The boost") {
        val stop = step - 2
        StepCodeBlock(
            code = LAYER3_CODE,
            modifier = Modifier.offset(110.dp, 210.dp),
            highlight = if (stop in ColorStopOrder.indices) lines(3 + stop, 15 + stop) else null,
            fontSize = 19.sp,
        )
        BubbleReveal(
            at = 1,
            modifier = Modifier.place(StopsCenterX - StopsBubble / 2, StopsCenterY - StopsBubble / 2, StopsBubble, StopsBubble),
        ) {
            BoostBubble(modifier = Modifier.fillMaxSize(), layers = BoostLayers.Arc)
        }
        ColorStopOrder.forEachIndexed { index, stop ->
            Reveal(at = index + 2, modifier = Modifier.fillMaxSize()) {
                ColorStopLabel(stop)
            }
        }
    }
}

/** Keynote 88 — drawing layer 3 as an arc on top of layers 1 and 2. */
val drawArcSlide = Slide(steps = 1) {
    StageSlide("The boost") {
        Column(
            modifier = Modifier.offset(70.dp, 230.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            StepCodeBlock(code = DRAW_ARC_CODE, fontSize = 22.sp)
            StepCodeBlock(code = DRAW_ARC_CALL_CODE, fontSize = 22.sp)
        }
        BoostBubble(modifier = Modifier.place(1450, 230, 380, 380), layers = BoostLayers.InnerGlow)
        BubbleReveal(at = 1, modifier = Modifier.place(1450, 640, 380, 380)) {
            BoostBubble(modifier = Modifier.fillMaxSize(), layers = BoostLayers.Arc)
        }
    }
}

/** Keynote 89 — layer 4: the colourful outer ring. */
val gradientRingSlide = Slide(steps = 1) {
    StageSlide("The boost") {
        StepCodeBlock(
            code = LAYER4_CODE,
            modifier = Modifier.offset(70.dp, 220.dp),
            fontSize = 20.sp,
        )
        BoostBubble(modifier = Modifier.place(1450, 230, 380, 380), layers = BoostLayers.Arc)
        BubbleReveal(at = 1, modifier = Modifier.place(1450, 640, 380, 380)) {
            BoostBubble(modifier = Modifier.fillMaxSize(), layers = BoostLayers.Ring)
        }
    }
}

/** Keynote 90 — spinning the ring with withTransform. */
val ringRotationSlide = Slide(steps = 1) {
    StageSlide("The boost") {
        Column(
            modifier = Modifier.offset(70.dp, 220.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            StepCodeBlock(code = ROTATION_ANIM_CODE, fontSize = 20.sp)
            StepCodeBlock(code = ROTATION_CODE, fontSize = 20.sp)
        }
        BoostBubble(
            modifier = Modifier.place(1300, 300, 480, 480),
            layers = if (step >= 1) BoostLayers.Rotating else BoostLayers.Ring,
        )
    }
}

/**
 * Reveal for the bubble: scales it in instead of fading. A fade puts the bubble
 * in its own translucent layer, where the ColorDodge surface has no backdrop to
 * blend with and flashes as a flat grey disc until the fade ends.
 */
@Composable
private fun SlideScope.BubbleReveal(
    at: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (step >= at) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
    )
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        content = content,
    )
}

/** Keynote 91. */
val tapCompetitionSectionSlide = Slide {
    SectionTitle("The tap competition", BoostGradient)
}

/** Keynote 92 — the tapping competition recorded in the app. */
val tapCompetitionSlide = Slide {
    StageSlide("The tap competition") {
        PhoneVideo(
            resourcePath = "videos/demoBoost.mp4",
            modifier = Modifier.place(1240, 60, 432, 962),
        )
    }
}

/** Keynote 93–94 — the emitter config, then the glow emitter built from it. */
val canvasEmitterConfigSlide = Slide(steps = 1) {
    StageSlide("Canvas Particle Emitter") {
        Crossfade(targetState = step >= 1, modifier = Modifier.fillMaxSize()) { usage ->
            Box(modifier = Modifier.fillMaxSize()) {
                PhoneVideo(
                    resourcePath = if (usage) "videos/trimmedGlow.mp4" else "videos/glowParticles.mp4",
                    modifier = Modifier.place(258, 207, 378, 841),
                )
                if (usage) {
                    StepCodeBlock(
                        code = EMITTER_USAGE_CODE,
                        modifier = Modifier.offset(1000.dp, 24.dp),
                        fontSize = 20.sp,
                    )
                } else {
                    StepCodeBlock(
                        code = EMITTER_CONFIG_CODE,
                        modifier = Modifier.offset(820.dp, 210.dp),
                        fontSize = 21.sp,
                    )
                }
            }
        }
    }
}

/** Keynote 95. */
val blurSectionSlide = Slide {
    SectionTitle("How to make this blur?", BlurGradient)
}

/** Keynote 96 — the two obvious answers, both rejected. */
val blurOptionsSlide = Slide(steps = 6) {
    StageSlide("How to make this blur?") {
        Reveal(at = 1, modifier = Modifier.offset(789.dp, 396.dp)) {
            Callout("Blur modifier!")
        }
        Reveal(at = 2, modifier = Modifier.offset(176.dp, 330.dp)) {
            StepCodeBlock(code = BLUR_MODIFIER_CODE, fontSize = 24.sp)
        }
        Reveal(at = 3, modifier = Modifier.place(1138, 390, 64, 64)) {
            ThumbsDown()
        }
        Reveal(at = 4, modifier = Modifier.offset(1415.dp, 725.dp)) {
            Callout("Blur mask!")
        }
        Reveal(at = 5, modifier = Modifier.offset(170.dp, 600.dp)) {
            StepCodeBlock(code = BLUR_MASK_CODE, fontSize = 24.sp)
        }
        Reveal(at = 6, modifier = Modifier.place(1682, 720, 64, 64)) {
            ThumbsDown()
        }
    }
}

/** Keynote 97 — a blurred image is cheap once it is small. */
val blurSizesSlide = Slide(steps = 6) {
    StageSlide("How to make this blur?") {
        BlurPhotos.forEachIndexed { index, photo ->
            Reveal(at = index * 2 + 1, modifier = Modifier.place(photo.x, 254, photo.width, 722)) {
                MediaFrame(modifier = Modifier.fillMaxSize()) {
                    PhotoImage(
                        resourcePath = photo.resourcePath,
                        contentDescription = "Sunset photo, ${photo.label}",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Reveal(at = index * 2 + 2, modifier = Modifier.place(photo.x, 996, photo.width, 60)) {
                Text(
                    text = photo.label,
                    color = DeckColors.textPrimary,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

/** Keynote 98–99 — one pre-blurred 78×78 sprite does the job. */
val glowSpriteSlide = Slide(steps = 2) {
    StageSlide("Canvas Particle Emitter") {
        PhoneVideo(
            resourcePath = "videos/trimmedGlow.mp4",
            modifier = Modifier.place(258, 207, 378, 841),
        )
        Crossfade(targetState = step >= 2, modifier = Modifier.fillMaxSize()) { large ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (large) {
                    Sprite("images/boost_glow_dot_large.png", Modifier.place(917, 250, 612, 612))
                } else {
                    Sprite("images/boost_glow_dot.png", Modifier.place(1184, 504, 71, 72))
                    Reveal(at = 1, modifier = Modifier.offset(1242.dp, 600.dp)) {
                        Callout("78x78px\n2 KB")
                    }
                }
            }
        }
    }
}

/** Keynote 100 — stacking sprites builds the glow… *magic*. */
val glowBuildUpSlide = Slide(steps = 6) {
    StageSlide("Canvas Particle Emitter") {
        PhoneVideo(
            resourcePath = "videos/trimmedGlow.mp4",
            modifier = Modifier.place(258, 207, 378, 841),
        )
        GlowDots.forEachIndexed { index, dot ->
            Reveal(at = GlowDotsPerStep.indexOfFirst { it > index }, modifier = Modifier.place(dot.x, dot.y, 71, 72)) {
                Sprite("images/boost_dot_${dot.sprite}.png", Modifier.fillMaxSize())
            }
        }
        Reveal(at = 6, modifier = Modifier.place(1524, 683, 395, 395)) {
            MediaFrame(modifier = Modifier.fillMaxSize()) {
                VideoClip(resourcePath = "videos/mr-bean.mp4", modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/** Keynote 101 — the emitter in production, one recording per click. */
val emitterInProductionSlide = Slide(steps = 3) {
    StageSlide("Canvas Particle Emitter") {
        ProductionVideos.forEachIndexed { index, video ->
            Reveal(at = index, modifier = Modifier.place(video.x, 254, 319, 711)) {
                PhoneVideo(resourcePath = video.resourcePath, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

val boostSlides: List<Slide> = listOf(
    boostSectionSlide,
    profileFlairSlide,
    boostBubbleSlide,
    sweepGradientSlide,
    drawArcSlide,
    gradientRingSlide,
    ringRotationSlide,
    tapCompetitionSectionSlide,
    tapCompetitionSlide,
    canvasEmitterConfigSlide,
    blurSectionSlide,
    blurOptionsSlide,
    blurSizesSlide,
    glowSpriteSlide,
    glowBuildUpSlide,
    emitterInProductionSlide,
)

private class Placed(val x: Int, val y: Int, val width: Int, val height: Int)

/** Centre of the bubble on the sweep-gradient slide, in stage coordinates. */
private const val StopsCenterX = 1360
private const val StopsCenterY = 640

/** Size of the bubble on the sweep-gradient slide. */
private const val StopsBubble = 520

/** A colour stop and where its label sits, relative to the bubble centre. */
private class ColorStop(val value: String, val position: Float, val labelX: Int, val labelY: Int)

/** Colour stops in the order they are labelled (0f first, then around the top). */
private val ColorStopOrder = listOf(
    ColorStop("0f", 0f, 285, 0),
    ColorStop("0.55f", 0.55f, -285, -80),
    ColorStop("0.6f", 0.6f, -260, -215),
    ColorStop("0.7f", 0.7f, -160, -320),
    ColorStop("0.75f", 0.75f, -25, -345),
    ColorStop("0.8f", 0.8f, 105, -330),
    ColorStop("0.84f", 0.84f, 215, -295),
    ColorStop("0.87f", 0.87f, 295, -220),
    ColorStop("0.91f", 0.91f, 315, -120),
)

/** Tick at the stop's angle (0f is 3 o'clock, clockwise) plus its value label. */
@Composable
private fun ColorStopLabel(stop: ColorStop) {
    val angle = stop.position * 2f * PI.toFloat()
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val start = Offset(StopsCenterX + 192f * cos(angle), StopsCenterY + 192f * sin(angle))
            val end = Offset(StopsCenterX + 240f * cos(angle), StopsCenterY + 240f * sin(angle))
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = start * density,
                end = end * density,
                strokeWidth = 2.dp.toPx(),
            )
        }
        Text(
            text = stop.value,
            color = DeckColors.textPrimary,
            fontSize = 34.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset((StopsCenterX + stop.labelX - 55).dp, (StopsCenterY + stop.labelY - 24).dp)
                .size(110.dp, 48.dp),
        )
    }
}

private class BlurPhoto(val resourcePath: String, val label: String, val x: Int, val width: Int)

private val BlurPhotos = listOf(
    BlurPhoto("images/boost_photo_2mb.png", "2 MB", 220, 407),
    BlurPhoto("images/boost_photo_60kb.png", "60 KB", 715, 398),
    BlurPhoto("images/boost_photo_2kb.png", "2 KB", 1200, 414),
)

private class GlowDot(val sprite: Int, val x: Int, val y: Int)

/** Sprite positions from Keynote 100, in the order they appear. */
private val GlowDots = listOf(
    GlowDot(0, 1184, 504), GlowDot(0, 1194, 514),
    GlowDot(1, 1208, 504), GlowDot(2, 1208, 489), GlowDot(3, 1184, 489), GlowDot(4, 1172, 510), GlowDot(3, 1184, 526),
    GlowDot(2, 1208, 526), GlowDot(1, 1223, 514), GlowDot(5, 1227, 496), GlowDot(6, 1208, 480), GlowDot(6, 1178, 480),
    GlowDot(7, 1164, 496), GlowDot(8, 1164, 526), GlowDot(9, 1188, 539), GlowDot(6, 1208, 539), GlowDot(6, 1223, 480),
    GlowDot(6, 1178, 480), GlowDot(5, 1155, 500), GlowDot(1, 1227, 530), GlowDot(1, 1240, 504), GlowDot(1, 1208, 472),
)

/** How many of [GlowDots] are visible at each step of Keynote 100. */
private val GlowDotsPerStep = listOf(1, 2, 7, 12, 17, 22)

private class ProductionVideo(val resourcePath: String, val x: Int)

private val ProductionVideos = listOf(
    ProductionVideo("videos/boostFloating_keynote.mp4", 152),
    ProductionVideo("videos/boostTapping_keynote.mp4", 549),
    ProductionVideo("videos/boostGlow_keynote.mp4", 947),
    ProductionVideo("videos/flairSelection_keynote.mp4", 1344),
)

/** Absolute placement on the 1920×1080 stage, matching Keynote coordinates. */
private fun Modifier.place(x: Int, y: Int, width: Int, height: Int): Modifier =
    offset(x.dp, y.dp).size(width.dp, height.dp)

/** Slide chrome with a title; [content] is laid out in stage coordinates. */
@Composable
private fun StageSlide(title: String, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        SlideSurface { SlideTitle(title) }
        content()
    }
}

@Composable
private fun SectionTitle(text: String, colors: List<Color>) {
    SlideSurface {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = TextStyle(
                    brush = Brush.horizontalGradient(colors),
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
private fun Callout(text: String) {
    Text(
        text = text,
        color = DeckColors.textPrimary,
        fontSize = 46.sp,
        lineHeight = 64.sp,
    )
}

@Composable
private fun Sprite(resourcePath: String, modifier: Modifier) {
    PhotoImage(
        resourcePath = resourcePath,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun ThumbsDown() {
    PhotoImage(
        resourcePath = "images/boost_thumbs_down.png",
        contentDescription = "Thumbs down",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun PhoneVideo(resourcePath: String, modifier: Modifier) {
    MediaFrame(modifier = modifier) {
        VideoClip(
            resourcePath = resourcePath,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
