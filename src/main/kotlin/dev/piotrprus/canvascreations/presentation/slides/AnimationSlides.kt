package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.gradient.BoostedUsername
import dev.piotrprus.canvascreations.presentation.demos.gradient.MeshStage
import dev.piotrprus.canvascreations.presentation.demos.gradient.MidPointGradientTitle
import dev.piotrprus.canvascreations.presentation.demos.gradient.RotatingMeshText
import dev.piotrprus.canvascreations.presentation.demos.gradient.TwoColorGradientTitle
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.LabeledFrame
import dev.piotrprus.canvascreations.presentation.ui.MediaFrame
import dev.piotrprus.canvascreations.presentation.ui.MorphingCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.PhotoImage
import dev.piotrprus.canvascreations.presentation.ui.SlideSubtitle
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle
import dev.piotrprus.canvascreations.presentation.ui.StepCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.lines

// Animating the gradient, then the production example — every preview is live.

private const val MID_POINT_CODE = """
val infiniteTransition = rememberInfiniteTransition(label = "infinite")
val midPoint = infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = InfiniteRepeatableSpec(
        repeatMode = RepeatMode.Reverse,
        animation = tween(
            durationMillis = 3000,
            easing = EaseInOut
        )
    ), label = "midPoint"
)
"""

private const val MID_POINT_DRAW_CODE = """
.drawWithCache {
    val brush = Brush.linearGradient(
        colorStops = arrayOf(0f to Magenta, midPoint.value to Cyan, 1f to Magenta),
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )
    onDrawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
    }
}
"""

private const val COLOR_ANIM_CODE = """
val colorAnim = infiniteTransition.animateColor(
    initialValue = Cyan,
    targetValue = Yellow,
    animationSpec = InfiniteRepeatableSpec(
        repeatMode = RepeatMode.Reverse,
        animation = tween(
            durationMillis = 3000,
            easing = EaseInOut
        )
    ),
    label = "color"
)
"""

private const val COLOR_ANIM_2_CODE = """
val colorAnim2 = infiniteTransition.animateColor(
    initialValue = Magenta,
    targetValue = Red,
    animationSpec = InfiniteRepeatableSpec(
        repeatMode = RepeatMode.Reverse,
        animation = tween(
            durationMillis = 3000,
            easing = EaseInOut
        )
    ),
    label = "color2"
)
"""

private const val COLOR_BRUSH_CODE = """
val brush = Brush.linearGradient(
    colorStops = arrayOf(0f to colorAnim.value, 1f to colorAnim2.value),
    start = Offset(0f, 0f),
    end = Offset(size.width, size.height)
)
"""

/**
 * The production GradientTextTest at each stage of the walk-through; built from
 * one template so every version shares the exact same surrounding lines.
 */
private fun meshCode(offscreen: Boolean, drawContent: Boolean, srcAtop: Boolean): String = buildList {
    add("@Composable")
    add("fun GradientTextTest(modifier: Modifier = Modifier) {")
    add("    val infiniteTransition = rememberInfiniteTransition(label = \"infinite\")")
    add("    val rotationValue = infiniteTransition.animateFloat(")
    add("        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(")
    add("            tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Restart")
    add("        ), label = \"rotation\"")
    add("    )")
    add("    val imageBitmap = ImageBitmap.imageResource(id = R.drawable.flair_mesh_orange)")
    add("    Box(modifier = modifier")
    if (offscreen) {
        add("        .graphicsLayer {")
        add("            compositingStrategy = CompositingStrategy.Offscreen")
        add("        }")
    }
    add("        .drawWithContent {")
    if (drawContent) add("            drawContent()")
    add("            rotate(rotationValue.value) {")
    add("                drawImage(")
    add("                    imageBitmap,")
    add(if (srcAtop) "                    blendMode = BlendMode.SrcAtop" else "                    blendMode = BlendMode.Src")
    add("                )")
    add("            }")
    add("        }")
    add("    ) {")
    add("        Text(")
    add("            text = \"Bringing Your Canvas Creations\\n to Life with Jetpack Compose\",")
    add("            modifier = Modifier")
    add("        )")
    add("    }")
    add("}")
}.joinToString("\n")

private val MESH_SRC_CODE = meshCode(offscreen = false, drawContent = false, srcAtop = false)
private val MESH_OFFSCREEN_CODE = meshCode(offscreen = true, drawContent = false, srcAtop = false)
private val MESH_DRAW_CONTENT_CODE = meshCode(offscreen = true, drawContent = true, srcAtop = false)
private val MESH_SRC_ATOP_CODE = meshCode(offscreen = true, drawContent = true, srcAtop = true)

/** 0-based indices of [count] lines starting at the first line containing [marker]. */
private fun String.linesAt(marker: String, count: Int = 1): Set<Int> {
    val start = lines().indexOfFirst { marker in it }
    require(start >= 0) { "Marker '$marker' not found" }
    return (start until start + count).toSet()
}

/** Animate the middle color stop, call it out, then the live result. */
val midPointAnimationSlide = Slide(steps = 3) {
    SlideSurface {
        SlideTitle("Animation")
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                StepCodeBlock(
                    code = MID_POINT_CODE,
                    highlight = if (step == 2) emptySet() else null,
                    fontSize = 20.sp,
                )
                Reveal(at = 1) {
                    StepCodeBlock(
                        code = MID_POINT_DRAW_CODE,
                        highlight = if (step == 2) lines(2) else null,
                        fontSize = 20.sp,
                    )
                }
            }
            Reveal(at = 3, modifier = Modifier.align(Alignment.CenterVertically)) {
                LabeledFrame(
                    label = "Preview",
                    frameModifier = Modifier.width(560.dp).height(240.dp),
                ) {
                    MidPointGradientTitle(fontSize = 28.sp)
                }
            }
        }
    }
}

/** Two animated colors feeding the gradient, then the live result on top. */
val colorAnimationSlide = Slide(steps = 4) {
    SlideSurface {
        SlideTitle("Animation")
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    StepCodeBlock(
                        code = COLOR_ANIM_CODE,
                        highlight = if (step == 3) emptySet() else null,
                        dimAll = step >= 4,
                        fontSize = 24.sp,
                    )
                    Reveal(at = 1) {
                        StepCodeBlock(
                            code = COLOR_ANIM_2_CODE,
                            highlight = if (step == 3) emptySet() else null,
                            dimAll = step >= 4,
                            fontSize = 24.sp,
                        )
                    }
                }
                Reveal(at = 2) {
                    StepCodeBlock(
                        code = COLOR_BRUSH_CODE,
                        highlight = if (step == 3) lines(1) else null,
                        dimAll = step >= 4,
                        fontSize = 24.sp,
                    )
                }
            }
            Reveal(at = 4, modifier = Modifier.align(Alignment.Center)) {
                MediaFrame(modifier = Modifier.width(1200.dp).height(300.dp)) {
                    TwoColorGradientTitle(fontSize = 56.sp)
                }
            }
        }
    }
}

/** The boosted username in the TILT app, live, and the mesh bitmap behind it. */
val productionExampleSlide = Slide(steps = 1) {
    SlideSurface {
        SlideTitle("Animation")
        SlideSubtitle("production example")
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(120.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabeledFrame(
                label = "PreviewBoostedRow",
                frameModifier = Modifier.width(860.dp).height(330.dp),
            ) {
                BoostedUsername(name = "PiotrTilt", fontSize = 110.sp, iconSize = 150.dp)
            }
            // Make room for the drawable the production code spins behind the text.
            AnimatedVisibility(
                visible = step >= 1,
                enter = expandHorizontally() + fadeIn(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PhotoImage(
                        resourcePath = "images/anim_flair_mesh_orange.png",
                        contentDescription = "flair_mesh_orange",
                        modifier = Modifier.size(520.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "R.drawable.flair_mesh_orange",
                        color = DeckColors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 26.sp,
                    )
                }
            }
        }
    }
}

/**
 * Walking through the production code. Each → highlights the next part; after
 * each fix the live result of exactly that code plays over the dimmed snippet.
 */
val productionCodeSlide = Slide(steps = 11) {
    val code = when {
        step <= 6 -> MESH_SRC_CODE
        step <= 8 -> MESH_OFFSCREEN_CODE
        step == 9 -> MESH_DRAW_CONTENT_CODE
        else -> MESH_SRC_ATOP_CODE
    }
    val highlight = when (step) {
        1 -> code.linesAt("@Composable", 3)
        2 -> code.linesAt("val rotationValue", 5)
        3 -> code.linesAt("val imageBitmap")
        4 -> code.linesAt("Box(modifier", code.lines().size - code.linesAt("Box(modifier").first())
        5 -> code.linesAt("blendMode =")
        7 -> code.linesAt(".graphicsLayer", 3)
        9 -> code.linesAt("drawContent()")
        10 -> code.linesAt("drawContent()") + code.linesAt("blendMode =")
        else -> null
    }
    val preview = when (step) {
        6 -> MeshStage.Src
        8 -> MeshStage.Offscreen
        11 -> MeshStage.SrcAtop
        else -> null
    }
    SlideSurface {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.width(440.dp)) {
                SlideTitle("Animation")
                SlideSubtitle("production example")
            }
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                MorphingCodeBlock(
                    code = code,
                    highlight = highlight,
                    highlightAdded = false,
                    dimAll = preview != null,
                    fontSize = 21.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
                Crossfade(
                    targetState = preview,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) { stage ->
                    if (stage != null) {
                        MediaFrame(modifier = Modifier.width(936.dp).height(560.dp)) {
                            RotatingMeshText(stage = stage, fontSize = 40.sp)
                        }
                    }
                }
            }
        }
    }
}

val animationSlides: List<Slide> = listOf(
    midPointAnimationSlide,
    colorAnimationSlide,
    productionExampleSlide,
    productionCodeSlide,
)
