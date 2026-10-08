package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.dance.BubbleLayers
import dev.piotrprus.canvascreations.presentation.demos.dance.DanceBubble
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.MediaFrame
import dev.piotrprus.canvascreations.presentation.ui.MorphingCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.PhotoImage
import dev.piotrprus.canvascreations.presentation.ui.SlideSubtitle
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle
import dev.piotrprus.canvascreations.presentation.ui.StepCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.VideoClip
import dev.piotrprus.canvascreations.presentation.ui.lines

// "Dance with Blends" — the TILT auction bubble built from two animated
// circles, a blur, an alpha layer and two blend modes. Every bubble on these
// slides is the live DanceBubble composable, driven by the slide step.

/** Keynote "Dance with Blends" gradient (green → blue). */
private val danceGradient = Brush.horizontalGradient(listOf(Color(0xFF00E640), Color(0xFF0091FF)))

/** Keynote "Scary math" gradient (cyan → purple). */
private val scaryGradient = Brush.horizontalGradient(listOf(Color(0xFF00C2FF), Color(0xFFB04CFF)))

private const val BUBBLE_OFFSET_CODE = """
private fun calculateBubbleOffset(
    angle: Int,
    radius: Dp,
    parentSize: Dp
): DpOffset {
    val angleRadians = Math.toRadians(angle.toDouble())
    val offsetX = cos(angleRadians) * (parentSize / 2 - radius)
    val offsetY = sin(angleRadians) * (parentSize / 2 - radius)
    return DpOffset(x = offsetX, y = offsetY)
}
"""

private const val BIG_CIRCLE_POSITION_CODE = """
val infiniteTransition = rememberInfiniteTransition(label = "infinite")
val animOffset1 by infiniteTransition.animateValue(
    initialValue = calculateBubbleOffset(angle = 45, radius = 0.dp, parentSize = rootCircleSize),
    targetValue = calculateBubbleOffset(angle = 45, radius = 0.dp, parentSize = rootCircleSize),
    typeConverter = DpOffset.VectorConverter,
    animationSpec = infiniteRepeatable(animation = keyframes {
        durationMillis = 7000
        calculateBubbleOffset(angle = 55, radius = 10.dp, parentSize = rootCircleSize) at 500
        calculateBubbleOffset(angle = 55, radius = rootCircleSize / 2, parentSize = rootCircleSize) at 1500
        calculateBubbleOffset(angle = 300, radius = 0.dp, parentSize = rootCircleSize) at 2500
        calculateBubbleOffset(angle = 270, radius = 0.dp, parentSize = rootCircleSize) at 3000
        calculateBubbleOffset(angle = 180, radius = 0.dp, parentSize = rootCircleSize) at 3500
        calculateBubbleOffset(angle = 120, radius = 0.dp, parentSize = rootCircleSize) at 4500
        calculateBubbleOffset(angle = 120, radius = 30.dp, parentSize = rootCircleSize) at 5500
        calculateBubbleOffset(angle = 180, radius = rootCircleSize / 2, parentSize = rootCircleSize) at 6000
    }), label = "offset1"
)
"""

private const val SMALL_CIRCLE_POSITION_CODE = """
val animOffset2 by infiniteTransition.animateValue(
    initialValue = calculateBubbleOffset(angle = 135, radius = 0.dp, parentSize = rootCircleSize),
    targetValue = calculateBubbleOffset(angle = 135, radius = 0.dp, parentSize = rootCircleSize),
    typeConverter = DpOffset.VectorConverter,
    animationSpec = infiniteRepeatable(animation = keyframes {
        durationMillis = 7000
        calculateBubbleOffset(angle = 180, radius = 10.dp, parentSize = rootCircleSize) at 500
        calculateBubbleOffset(angle = 200, radius = 0.dp, parentSize = rootCircleSize) at 1500
        calculateBubbleOffset(angle = 200, radius = rootCircleSize / 2, parentSize = rootCircleSize) at 2500
        calculateBubbleOffset(angle = 45, radius = 0.dp, parentSize = rootCircleSize) at 4500
        calculateBubbleOffset(angle = 120, radius = 30.dp, parentSize = rootCircleSize) at 5500
        calculateBubbleOffset(angle = 90, radius = 0.dp, parentSize = rootCircleSize) at 6000
    }), label = "offset2"
)
"""

private const val ANIMATE_COLOR_CODE = """
val animColor1 by infiniteTransition.animateColor(
    initialValue = CandyPink,
    targetValue = CandyPink,
    animationSpec = infiniteRepeatable(animation = keyframes {
        durationMillis = 7000
        Night at 2500
        Orange at 5500
    }), label = "color1"
)
"""

private const val WEB_CSS_CODE = """
&#filter-blur {
    filter: blur(10px);
}

&#alpha-layer {
    background-color: rgba(0, 0, 0, 0.6);
}

&#color-dodge {
    mix-blend-mode: color-dodge;
    background-color: rgba(0, 0, 0, 0.9);
    width: 250px;
    height: 250px;
}

&#color-burn {
    mix-blend-mode: color-burn;
    background-color: rgba(0, 0, 0, 0.9);
}
"""

/**
 * The "layers" snippet grows step by step; every version comes from one
 * template so the morphing code block only animates the lines that changed.
 */
private fun layersCode(layers: BubbleLayers): String = buildList {
    add("Box(")
    add("    modifier = Modifier")
    add("        .size(rootCircleSize)")
    add("        .clip(CircleShape)")
    if (layers.colorDodge) {
        add("        .background(color = Neutral2)")
        add("        .drawWithContent {")
        add("            drawContent()")
        add("            drawCircle(color = Neutral2, blendMode = BlendMode.ColorDodge)")
        if (layers.colorBurn) {
            add("            drawCircle(color = Neutral4, blendMode = BlendMode.ColorBurn)")
        }
        add("        },")
    } else {
        add("        .background(color = Neutral2),")
    }
    add("    contentAlignment = Alignment.Center")
    add(") {")
    add("    Box(")
    add("        modifier = Modifier")
    add("            .size(rootCircleSize)")
    add("            .clip(CircleShape)")
    if (layers.blur) {
        add("            .background(color = Neutral2)")
        add("            .blur(10.dp, BlurredEdgeTreatment.Unbounded),")
    } else {
        add("            .background(color = Neutral2),")
    }
    add("        contentAlignment = Alignment.Center")
    add("    ) {")
    add("        Box(")
    add("            modifier = Modifier")
    add("                .size(child1Size)")
    add("                .offset { IntOffset(animOffset1.x.roundToPx(), animOffset1.y.roundToPx()) }")
    add("                .background(color = animColor1, shape = CircleShape)")
    add("        )")
    add("        Box(")
    add("            modifier = Modifier")
    add("                .size(child2Size)")
    add("                .offset { IntOffset(animOffset2.x.roundToPx(), animOffset2.y.roundToPx()) }")
    add("                .background(color = Lime5, shape = CircleShape)")
    add("        )")
    if (layers.alphaLayer) {
        add("        Box(")
        add("            modifier = Modifier")
        add("                .padding(5.dp)")
        add("                .fillMaxSize()")
        add("                .clip(CircleShape)")
        add("                .background(color = NeutralBlack.copy(alpha = 0.6f))")
        add("        )")
    }
    add("    }")
    add("}")
}.joinToString("\n")

/** Section title, then the finished bubble and its raw dance — both live. */
private val danceTitleSlide = Slide(steps = 2) {
    SlideSurface {
        Box(modifier = Modifier.fillMaxSize()) {
            // The title floats from the centre to the top once the videos start.
            val titleOffset by animateDpAsState(
                targetValue = if (step == 0) 380.dp else 0.dp,
                animationSpec = tween(durationMillis = 600),
            )
            GradientHeadline(
                text = "Dance with Blends",
                brush = danceGradient,
                fontSize = 96.sp,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = titleOffset),
            )
            Reveal(at = 1, modifier = Modifier.align(Alignment.Center).padding(top = 120.dp)) {
                // First the finished bubble, then the raw dance underneath it.
                DanceBubble(
                    rootCircleSize = 620.dp,
                    layers = if (step >= 2) BubbleLayers.Base else BubbleLayers.Burn,
                    // The finished auction bubble carries the gavel; the raw dance drops it.
                    showGavel = step < 2,
                )
            }
        }
    }
}

/** Keynote 61. */
private val scaryMathSlide = Slide {
    SlideSurface {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientHeadline(text = "Scary math", brush = scaryGradient, fontSize = 160.sp)
                Text(text = " 🫣", fontSize = 140.sp)
            }
            Reveal(1) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Actually no need anymore",
                    color = DeckColors.textSecondary,
                    fontSize = 40.sp,
                )
            }
        }
    }
}

/** Keynote 62: circle diagram → polar coordinates → calculateBubbleOffset. */
private val circleEquationSlide = Slide(steps = 2) {
    SlideSurface {
        SlideTitle("The scary math")
        SlideSubtitle("Equation of a circle")
        Spacer(modifier = Modifier.height(50.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(60.dp),
        ) {
            MediaFrame(modifier = Modifier.width(520.dp).aspectRatio(590f / 334f)) {
                PhotoImage(
                    resourcePath = "images/dance_circle_equation.png",
                    contentDescription = "Equation of a circle",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            Reveal(at = 2) {
                StepCodeBlock(code = BUBBLE_OFFSET_CODE, fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
        Reveal(at = 1, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            MediaFrame(modifier = Modifier.width(1200.dp).aspectRatio(1070f / 172f)) {
                PhotoImage(
                    resourcePath = "images/dance_circle_point_formula.png",
                    contentDescription = "Coordinates of a point on a circle at a given angle",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

/** Keyframed offsets for the big, then the small circle — the bubble follows the code. */
private val circlePositionSlide = Slide(steps = 1) {
    SlideSurface {
        SlideTitle("Circle position")
        Crossfade(targetState = step >= 1) { small ->
            SlideSubtitle(if (small) "Small circle" else "Big circle")
        }
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = step >= 1) { small ->
                StepCodeBlock(
                    code = if (small) SMALL_CIRCLE_POSITION_CODE else BIG_CIRCLE_POSITION_CODE,
                    fontSize = 19.sp,
                )
            }
            DanceBubble(
                rootCircleSize = 300.dp,
                layers = BubbleLayers.Base.copy(smallCircle = step >= 1, animateColor = false),
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

/** Keyframed colour: the live bubble starts colouring once the step reveals it. */
private val animateColorSlide = Slide(steps = 1) {
    SlideSurface {
        SlideTitle("Animate color")
        SlideSubtitle("Big circle")
        Spacer(modifier = Modifier.height(60.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(80.dp),
        ) {
            StepCodeBlock(code = ANIMATE_COLOR_CODE, fontSize = 26.sp)
            DanceBubble(
                rootCircleSize = 480.dp,
                layers = BubbleLayers.Base.copy(animateColor = step >= 1),
            )
        }
    }
}

/**
 * The layer stack, growing one layer per step. The code morphs (new lines
 * slide in highlighted) and the live bubble next to it gains the same layer.
 */
private val layersSlide = Slide(steps = 5) {
    val layers = when (step) {
        0 -> BubbleLayers.Base
        1 -> BubbleLayers.Blur
        2 -> BubbleLayers.Alpha
        3 -> BubbleLayers.Dodge
        else -> BubbleLayers.Burn
    }
    SlideSurface {
        SlideTitle("The layers")
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                MorphingCodeBlock(
                    code = layersCode(layers),
                    dimAll = step >= 5,
                    fontSize = 19.sp,
                    modifier = Modifier.fillMaxSize(),
                )
                Reveal(at = 5, modifier = Modifier.align(Alignment.Center)) {
                    MediaFrame(modifier = Modifier.width(1000.dp).aspectRatio(1805f / 588f)) {
                        PhotoImage(
                            resourcePath = "images/dance_dodge_vs_burn_table.png",
                            contentDescription = "Color Dodge vs Color Burn",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.width(440.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(36.dp),
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                DanceBubble(rootCircleSize = 420.dp, layers = layers, showGavel = step >= 4)
                Crossfade(targetState = step) { current ->
                    when (current) {
                        3 -> Callout(
                            term = "Color Dodge",
                            text = " dramatically lightens the colors in the base layer — " +
                                "a bright, almost ethereal effect.",
                        )
                        4, 5 -> Callout(
                            term = "Color Burn",
                            text = " dramatically darkens the colors in the base layer — " +
                                "a deep, rich, moody atmosphere.",
                        )
                        else -> Box(modifier = Modifier.size(1.dp))
                    }
                }
            }
        }
    }
}

/** Every layer side by side, all live. */
private val danceSummarySlide = Slide {
    SlideSurface {
        SlideTitle("Dance with blends - summary")
        Spacer(modifier = Modifier.height(30.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(160.dp)) {
                SummaryTile(label = "base", layers = BubbleLayers.Base)
                SummaryTile(label = "+ blur", layers = BubbleLayers.Blur)
                SummaryTile(label = "+ alpha layer", layers = BubbleLayers.Alpha)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(160.dp)) {
                SummaryTile(label = "+ color dodge", layers = BubbleLayers.Dodge)
                SummaryTile(label = "+ color burn", layers = BubbleLayers.Burn, showGavel = true)
            }
        }
    }
}

/** Keynote 79–81: the same effect in CSS, credit to Henry Lim. */
private val webBonusSlide = Slide(steps = 2) {
    SlideSurface {
        SlideTitle("A bonus for Web developers")
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(100.dp),
        ) {
            BubbleVideo("videos/rec_2024-09-05_23_24_51.mov", size = 640.dp, aspect = 636f / 630f)
            Reveal(at = 1) {
                Crossfade(targetState = step >= 2) { credit ->
                    if (credit) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Created by Henry Lim",
                                color = DeckColors.textPrimary,
                                fontSize = 40.sp,
                            )
                            Spacer(modifier = Modifier.height(30.dp))
                            PhotoImage(
                                resourcePath = "images/dance_henry_lim_qr.png",
                                contentDescription = "QR code to Henry Lim's original",
                                modifier = Modifier.size(480.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    } else {
                        StepCodeBlock(code = WEB_CSS_CODE, fontSize = 26.sp)
                    }
                }
            }
        }
    }
}

val danceSlides: List<Slide> = listOf(
    danceTitleSlide,
    scaryMathSlide,
    circlePositionSlide,
    animateColorSlide,
    layersSlide,
    danceSummarySlide,
    webBonusSlide,
)

@Composable
private fun BubbleVideo(resource: String, size: Dp, aspect: Float, modifier: Modifier = Modifier) {
    MediaFrame(modifier = modifier.width(size).aspectRatio(aspect)) {
        VideoClip(resourcePath = resource, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun SummaryTile(label: String, layers: BubbleLayers, showGavel: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = DeckColors.textPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        DanceBubble(rootCircleSize = 300.dp, layers = layers, showGavel = showGavel)
    }
}

@Composable
private fun GradientHeadline(
    text: String,
    brush: Brush,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(brush = brush, fontSize = fontSize, fontWeight = FontWeight.Bold),
    )
}

/** Keynote's white-outlined definition box. */
@Composable
private fun Callout(term: String, text: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(term) }
            append(text)
        },
        color = DeckColors.textPrimary,
        fontSize = 26.sp,
        lineHeight = 36.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(440.dp)
            .background(DeckColors.background)
            .border(2.dp, DeckColors.textPrimary)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    )
}
