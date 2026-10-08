package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.dstin.BidPlacingDemo
import dev.piotrprus.canvascreations.presentation.demos.dstin.DstInAccent
import dev.piotrprus.canvascreations.presentation.demos.dstin.DstInCaption
import dev.piotrprus.canvascreations.presentation.demos.dstin.DstInCard
import dev.piotrprus.canvascreations.presentation.demos.dstin.DstInWallOfText
import dev.piotrprus.canvascreations.presentation.demos.dstin.FollowCardDemo
import dev.piotrprus.canvascreations.presentation.demos.dstin.ReplayingDstInWall
import dev.piotrprus.canvascreations.presentation.demos.dstin.SkeletonFadeDemo
import dev.piotrprus.canvascreations.presentation.demos.dstin.WallOfTextLorem
import dev.piotrprus.canvascreations.presentation.demos.dstin.WallStage
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.MorphingCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle

// BlendMode.DstIn — the fading wall of text, then animating it. Every preview
// on these slides is live Compose code: the snippets match demos/dstin/DstInDemos.kt.

private val BASE_STOPS = listOf(
    "0f to Color.Black",
    "0.4f to Color.Black",
    "0.95f to Color.Transparent",
    "1f to Color.Transparent",
)
private const val ANIMATED_STOP = "hFloat.value to Color.Black.copy(alpha = alpha.value)"
private val STOP_ADDED_STOPS = listOf(BASE_STOPS[0], ANIMATED_STOP) + BASE_STOPS.drop(1)
private val TRIMMED_STOPS = listOf(BASE_STOPS[0], ANIMATED_STOP, "1f to Color.Transparent")
private val BRUSH_STOPS = listOf(BASE_STOPS[0], ANIMATED_STOP, "1f to Color.Black.copy(alpha = alpha.value)")

/**
 * The DstInWallOfText snippet in every version shown on the slides, built from
 * one template so consecutive steps differ only by the lines that really change.
 */
private fun wallCode(
    stops: List<String> = BASE_STOPS,
    blend: Boolean = true,
    animatables: Boolean = false,
    effect: WallStage? = null,
): String = buildList {
    add("@Composable")
    add("fun DstInWallOfText(modifier: Modifier, text: String) {")
    if (animatables) {
        add("    val hFloat = remember { Animatable(0f) }")
        add("    val alpha = remember { Animatable(0f) }")
    }
    if (effect != null) {
        add("    LaunchedEffect(Unit) {")
        add("        hFloat.animateTo(1f, tween(5000, easing = LinearEasing))")
        add("        alpha.animateTo(1f, tween(3000, easing = LinearEasing))")
        if (effect == WallStage.AnimateInOut) {
            add("        alpha.animateTo(0f, tween(1000, easing = LinearEasing))")
            add("        hFloat.animateTo(0f, tween(2000, easing = LinearEasing))")
        }
        add("    }")
    }
    add("    Text(")
    add("        text = text,")
    add("        style = MaterialTheme.typography.bodyLarge,")
    add("        color = Color.White,")
    add("        modifier = modifier")
    add("            .graphicsLayer {")
    add("                compositingStrategy = CompositingStrategy.Offscreen")
    add("            }")
    add("            .drawWithCache {")
    add("                val brush = Brush.verticalGradient(")
    stops.forEach { add("                    $it,") }
    add("                )")
    add("                onDrawWithContent {")
    add("                    drawContent()")
    add(if (blend) "                    drawRect(brush = brush, blendMode = BlendMode.DstIn)" else "                    drawRect(brush = brush)")
    add("                }")
    add("            }")
    add("    )")
    add("}")
}.joinToString("\n")

/** Indices of lines containing any of [markers]. */
private fun String.linesWith(vararg markers: String): Set<Int> =
    lines().withIndex().filter { (_, line) -> markers.any { it in line } }.map { it.index }.toSet()

/** From the first line containing [start] to the next line that is exactly [endTrimmed] (trimmed). */
private fun String.block(start: String, endTrimmed: String): Set<Int> {
    val all = lines()
    val from = all.indexOfFirst { start in it }
    require(from >= 0) { "Marker '$start' not found" }
    val to = (from until all.size).first { all[it].trim() == endTrimmed }
    return (from..to).toSet()
}

private val WALL_CODE = wallCode()
private val WALL_NO_BLEND_CODE = wallCode(blend = false)
private val REMEMBER_CODE = wallCode(animatables = true)
private val STOP_CODE = wallCode(animatables = true, stops = STOP_ADDED_STOPS)
private val TRIMMED_CODE = wallCode(animatables = true, stops = TRIMMED_STOPS)
private val BRUSH_CODE = wallCode(animatables = true, stops = BRUSH_STOPS)
private val EFFECT_CODE = wallCode(animatables = true, stops = BRUSH_STOPS, effect = WallStage.AnimateIn)
private val FULL_CODE = wallCode(animatables = true, stops = BRUSH_STOPS, effect = WallStage.AnimateInOut)

/** Keynote 39: section divider, then "My favourite". */
val favouriteBlendModeSlide = Slide(steps = 1) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeckColors.background, Color(0xFF131A26)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Reveal(at = 1) {
                Text(
                    text = "My favourite",
                    color = DeckColors.textSecondary,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = "Blend Mode",
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(DeckColors.textPrimary, DstInAccent)),
                    fontSize = 130.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

/** Keynote 40: "DstIn", then its definition from the docs. */
val dstInDefinitionSlide = Slide(steps = 2) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeckColors.background, Color(0xFF131A26)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Reveal(at = 1) {
                Text(
                    text = "DstIn",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(listOf(DeckColors.textPrimary, DstInAccent)),
                        fontSize = 200.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Reveal(at = 2, modifier = Modifier.width(1100.dp)) {
                Text(
                    text = "Show the destination image, but only where the two images overlap. " +
                        "The source image is not rendered, it is treated merely as a mask. " +
                        "The color channels of the source are ignored, only the opacity has an effect.",
                    color = DeckColors.textPrimary,
                    fontSize = 38.sp,
                    lineHeight = 54.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** DstIn in production — every example is live Compose, in a restrained palette on the navy card. */
val dstInExamplesSlide = Slide {
    SlideSurface {
        SlideTitle("BlendMode - DstIn")
        Spacer(modifier = Modifier.height(36.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(44.dp),
        ) {
            ExampleColumn("Skeleton fade", Modifier.width(470.dp)) {
                DstInCard(modifier = Modifier.fillMaxSize()) { SkeletonFadeDemo() }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ExampleColumn("Follow card", Modifier.weight(1.25f)) {
                    DstInCard(modifier = Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { FollowCardDemo(Modifier.fillMaxWidth()) }
                    }
                }
                ExampleColumn("Bid placing", Modifier.weight(1f)) {
                    DstInCard(modifier = Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { BidPlacingDemo(Modifier.fillMaxWidth()) }
                    }
                }
            }
            ExampleColumn("Wall of text", Modifier.width(470.dp)) {
                DstInCard(modifier = Modifier.fillMaxSize()) {
                    ReplayingDstInWall(stage = WallStage.AnimateInOut, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/**
 * Walk through DstInWallOfText. Step 6 drops the blend mode to show the bare
 * gradient painted on top, step 7 brings it back.
 */
val dstInWallOfTextSlide = Slide(steps = 8) {
    val code = if (step == 6) WALL_NO_BLEND_CODE else WALL_CODE
    val highlight = when (step) {
        2 -> code.linesWith("Text(", "text = text", "style =", "color =")
        3 -> code.block(".graphicsLayer {", "}")
        4 -> code.block("val brush", ")")
        5 -> code.block("onDrawWithContent", "}")
        6 -> code.block("val brush", ")") + code.linesWith("drawRect(")
        7 -> code.linesWith("drawRect(")
        else -> null
    }
    SlideSurface {
        SlideTitle("BlendMode - DstIn")
        Spacer(modifier = Modifier.height(36.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            Reveal(at = 1, modifier = Modifier.weight(1f).fillMaxHeight()) {
                MorphingCodeBlock(
                    code = code,
                    highlight = highlight,
                    highlightAdded = false,
                    fontSize = 21.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            WallPreview(label = if (step == 6) "Preview - no blend mode" else "Preview") {
                DstInWallOfText(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    text = WallOfTextLorem,
                    blend = step != 6,
                )
            }
        }
    }
}

/** Animate the gradient stop with two Animatables; the preview runs each step's code live. */
val dstInAnimatedSlide = Slide(steps = 6) {
    val (code, highlight) = when (step) {
        0 -> REMEMBER_CODE to REMEMBER_CODE.linesWith("remember {")
        1 -> REMEMBER_CODE to REMEMBER_CODE.block("val brush", "}")
        2 -> STOP_CODE to STOP_CODE.linesWith("remember {", "hFloat.value to")
        3 -> TRIMMED_CODE to TRIMMED_CODE.block("val brush", ")")
        4 -> BRUSH_CODE to BRUSH_CODE.linesWith("hFloat.value to", "1f to Color.Black.copy")
        5 -> EFFECT_CODE to EFFECT_CODE.block("LaunchedEffect", "}")
        else -> FULL_CODE to FULL_CODE.block("LaunchedEffect", "}")
    }
    val stage = when (step) {
        0, 1 -> WallStage.Remember
        2 -> WallStage.StopAdded
        3 -> WallStage.Trimmed
        4 -> WallStage.Brush
        5 -> WallStage.AnimateIn
        else -> WallStage.AnimateInOut
    }
    SlideSurface {
        SlideTitle("DstIn - animated")
        Spacer(modifier = Modifier.height(36.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            MorphingCodeBlock(
                code = code,
                highlight = highlight,
                highlightAdded = false,
                fontSize = 19.sp,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            WallPreview(label = "Preview") {
                ReplayingDstInWall(stage = stage, modifier = Modifier.fillMaxSize(), showValues = true)
            }
        }
    }
}

val dstInSlides: List<Slide> = listOf(
    favouriteBlendModeSlide,
    dstInDefinitionSlide,
    dstInExamplesSlide,
    dstInWallOfTextSlide,
    dstInAnimatedSlide,
)

@Composable
private fun ExampleColumn(label: String, modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxHeight()) {
        DstInCaption(label, Modifier.padding(start = 8.dp, bottom = 12.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}

/** Fixed-size phone-ish frame the wall-of-text previews render into. */
@Composable
private fun WallPreview(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.width(470.dp).fillMaxHeight(), horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            color = DeckColors.textSecondary,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 8.dp, bottom = 12.dp),
        )
        DstInCard(modifier = Modifier.fillMaxWidth().weight(1f)) { content() }
    }
}
