package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.GradientTitle
import dev.piotrprus.canvascreations.presentation.demos.gradient.MidPointGradientTitle
import dev.piotrprus.canvascreations.presentation.demos.gradient.TitleText
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.LabeledFrame
import dev.piotrprus.canvascreations.presentation.ui.MediaFrame
import dev.piotrprus.canvascreations.presentation.ui.MorphingCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.PhotoImage
import dev.piotrprus.canvascreations.presentation.ui.SlideSubtitle
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle
import dev.piotrprus.canvascreations.presentation.ui.VideoClip

// Building the gradient text step by step: one Text, one modifier at a time.

private const val TEXT_CODE = """
Text(
    text = "Bringing Your Canvas Creations\n to Life…",
    lineHeight = 50.sp,
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp,
    modifier = modifier
)
"""

private const val OFFSCREEN_CODE = """
Text(
    text = "Bringing Your Canvas Creations\n to Life…",
    lineHeight = 50.sp,
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp,
    modifier = modifier
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
)
"""

private const val BRUSH_CODE = """
Text(
    text = "Bringing Your Canvas Creations\n to Life…",
    lineHeight = 50.sp,
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp,
    modifier = modifier
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithCache {
            val brush = Brush.linearGradient(
                colors = listOf(Cyan, Magenta),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        }
)
"""

private const val DRAW_RECT_CODE = """
Text(
    text = "Bringing Your Canvas Creations\n to Life…",
    lineHeight = 50.sp,
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp,
    modifier = modifier
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithCache {
            val brush = Brush.linearGradient(
                colors = listOf(Cyan, Magenta),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
            onDrawWithContent {
                drawContent()
                drawRect(brush = brush)
            }
        }
)
"""

private const val BLEND_MODE_CODE = """
Text(
    text = "Bringing Your Canvas Creations\n to Life…",
    lineHeight = 50.sp,
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp,
    modifier = modifier
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithCache {
            val brush = Brush.linearGradient(
                colors = listOf(Cyan, Magenta),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
            onDrawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
            }
        }
)
"""

private const val OFFSCREEN_KDOC =
    "Rendering of content will always be rendered into an offscreen buffer first, then drawn to " +
        "the destination regardless of the other parameters configured on the graphics layer. " +
        "This is useful for leveraging different blending algorithms for masking content."

/** The animated gradient title, live — the effect this whole section builds. */
val gradientTitleLiveSlide = Slide {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0D12), Color(0xFF131A2B)))),
        contentAlignment = Alignment.Center,
    ) {
        MidPointGradientTitle(fontSize = 96.sp)
    }
}

/**
 * One Text, one modifier at a time: the snippet grows in place (new lines
 * expand in highlighted, the code scrolls to them) while the preview on the
 * right renders exactly the code currently on screen.
 *
 * 0 plain Text · 1 + graphicsLayer Offscreen · 2 Offscreen KDoc ·
 * 3 + drawWithCache brush · 4 + drawRect · 5 + BlendMode.SrcAtop
 */
val gradientTextBuildSlide = Slide(steps = 5) {
    val code = when (step) {
        0 -> TEXT_CODE
        1, 2 -> OFFSCREEN_CODE
        3 -> BRUSH_CODE
        4 -> DRAW_RECT_CODE
        else -> BLEND_MODE_CODE
    }
    val heading = when {
        step <= 2 -> "Compositing strategy" to "in graphicsLayer"
        step <= 4 -> "drawWithCache" to "build the brush once, draw every frame"
        else -> "BlendMode" to "SrcAtop: source only where the destination is"
    }
    SlideSurface {
        Crossfade(targetState = heading, animationSpec = tween(500)) { (title, subtitle) ->
            Column {
                SlideTitle(title)
                SlideSubtitle(subtitle)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
            Box(modifier = Modifier.width(1150.dp).height(680.dp)) {
                MorphingCodeBlock(
                    code = code,
                    fontSize = 24.sp,
                    modifier = Modifier.fillMaxSize(),
                )
                // The KDoc sits on top of the code, at its bottom edge.
                OffscreenKDoc(
                    visible = step == 2,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                )
            }
            LabeledFrame(
                label = "Preview",
                frameModifier = Modifier.width(500.dp).height(220.dp),
            ) {
                Crossfade(targetState = step.coerceAtLeast(3), animationSpec = tween(600)) { previewStep ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when (previewStep) {
                            3 -> TitleText(fontSize = 28.sp)
                            4 -> GradientTitle(fontSize = 28.sp, blendMode = null)
                            else -> GradientTitle(fontSize = 28.sp)
                        }
                    }
                }
            }
        }
    }
}

/** Scrolling through the BlendMode documentation. */
val blendModeDocsSlide = Slide {
    SlideSurface {
        SlideTitle("BlendMode")
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            MediaFrame(modifier = Modifier.height(720.dp).aspectRatio(1900f / 1398f)) {
                VideoClip(
                    resourcePath = "videos/rec_2024-08-24_06_58_55.mov",
                    modifier = Modifier.fillMaxSize(),
                    loop = false,
                )
            }
        }
    }
}

/** The SRC_ATOP diagram from the docs. */
val srcAtopSlide = Slide {
    SlideSurface {
        SlideTitle("BlendMode")
        Spacer(modifier = Modifier.height(40.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MediaFrame(modifier = Modifier.height(720.dp).aspectRatio(724f / 608f)) {
                PhotoImage(
                    resourcePath = "images/blendmode_src_atop.png",
                    contentDescription = "BlendMode.SrcAtop diagram",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Composable
private fun OffscreenKDoc(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        // Let the code finish scrolling before the explanation lands.
        enter = fadeIn(tween(500, delayMillis = 700)) +
            slideInVertically(tween(500, delayMillis = 700)) { it / 8 },
        exit = fadeOut(tween(300)),
    ) {
        KDocCallout(
            term = "CompositingStrategy.Offscreen",
            text = OFFSCREEN_KDOC,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Definition box in the code-adjacent style: the term in mono, then its KDoc. */
@Composable
private fun KDocCallout(term: String, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF1C222C), RoundedCornerShape(14.dp))
            .border(1.dp, DeckColors.codeBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 32.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = term,
            color = DeckColors.textSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = text,
            color = DeckColors.textPrimary,
            fontSize = 26.sp,
            lineHeight = 38.sp,
        )
    }
}

val basicsSlides: List<Slide> = listOf(
    gradientTitleLiveSlide,
    gradientTextBuildSlide,
    blendModeDocsSlide,
    srcAtopSlide,
)
