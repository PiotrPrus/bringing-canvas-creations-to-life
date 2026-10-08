package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.GradientTitle
import dev.piotrprus.canvascreations.presentation.demos.wheel.FrozenMotionBlurWheel
import dev.piotrprus.canvascreations.presentation.demos.wheel.LoopingFortuneWheel
import dev.piotrprus.canvascreations.presentation.demos.wheel.SpinningFortuneWheel
import dev.piotrprus.canvascreations.presentation.demos.wheel.rememberWheelLoop
import dev.piotrprus.canvascreations.presentation.demos.wheel.StaticFortuneWheel
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.LabeledFrame
import dev.piotrprus.canvascreations.presentation.ui.MediaFrame
import dev.piotrprus.canvascreations.presentation.ui.MorphingCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.PhotoImage
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle

// The fortune wheel and its motion blur. Every wheel on these slides is the
// live port of the SpinWheel sample in demos/wheel/FortuneWheel.kt.

private val WheelBackground = Color(0xFF0D1117)

private val WHEEL_SEGMENT_CODE = wheelSegmentCode(outline = false)
private val WHEEL_OUTLINE_CODE = wheelSegmentCode(outline = true)

/** drawWheelSegment as in demos/wheel, optionally with the outline drawArc. */
private fun wheelSegmentCode(outline: Boolean): String = buildList {
    add("private fun DrawScope.drawWheelSegment(")
    add("    segment: FortuneWheelSegment,")
    add("    startAngle: Float,")
    add("    sweepAngle: Float,")
    add("    radius: Float,")
    add("    outlineWidth: Float,")
    add("    segmentInnerColor: Color,")
    add("    segmentOuterColor: Color,")
    add(") {")
    add("    val center = Offset(size.width / 2f, size.height / 2f)")
    add("    val gradientAngleRad = ((startAngle + sweepAngle / 2f) * PI / 180f).toFloat()")
    add("    val fillBrush = Brush.linearGradient(")
    add("        0f to lerp(segmentInnerColor, segmentOuterColor, 0.6f),")
    add("        0.7f to lerp(segmentInnerColor, segmentOuterColor, 0.9f),")
    add("        1f to segmentOuterColor,")
    add("        start = center,")
    add("        end = Offset(")
    add("            x = center.x + radius * cos(gradientAngleRad),")
    add("            y = center.y + radius * sin(gradientAngleRad),")
    add("        ),")
    add("    )")
    add("    // Draw segment")
    add("    drawArc(")
    add("        brush = fillBrush,")
    add("        startAngle = startAngle,")
    add("        sweepAngle = sweepAngle,")
    add("        useCenter = true,")
    add("        topLeft = Offset(center.x - radius, center.y - radius),")
    add("        size = Size(radius * 2f, radius * 2f),")
    add("    )")
    if (outline) {
        add("    // Draw the segment outline")
        add("    drawArc(")
        add("        color = WheelColors.neutral1_a76,")
        add("        startAngle = startAngle,")
        add("        sweepAngle = sweepAngle,")
        add("        useCenter = true,")
        add("        topLeft = Offset(center.x - radius, center.y - radius),")
        add("        size = Size(radius * 2f, radius * 2f),")
        add("        style = Stroke(width = outlineWidth, join = StrokeJoin.Round),")
        add("    )")
    }
    add("}")
}.joinToString("\n")

/** The motion blur code as it grows: main wheel, then one ghost layer per step. */
private fun motionBlurCode(layers: Int): String = buildList {
    add("// layer0: main wheel, always fully opaque")
    add("Canvas(")
    add("    modifier = Modifier")
    add("        .matchParentSize()")
    add("        .graphicsLayer {")
    add("            compositingStrategy = CompositingStrategy.Offscreen")
    add("            rotationZ = visualRotation")
    add("        }")
    add("        .padding(16.dp),")
    add("    onDraw = drawWheelContent")
    add(")")
    if (layers > 0) {
        add("if (motionBlurEnabled && blurMultiplier > 0f) {")
        listOf(
            Triple(2, "0.8f", "closest trail"),
            Triple(4, "0.6f", ""),
            Triple(6, "0.4f", ""),
            Triple(8, "0.3f", "farthest trail"),
        ).take(layers).forEachIndexed { index, (offset, alpha, note) ->
            val suffix = if (note.isEmpty()) "" else " ($note)"
            add("    // layer${index + 1}: $offset° offset, ${alpha.removeSuffix("f").toFloat().times(100).toInt()}% base opacity$suffix")
            add("    Canvas(")
            add("        modifier = Modifier")
            add("            .matchParentSize()")
            add("            .graphicsLayer {")
            add("                compositingStrategy = CompositingStrategy.Offscreen")
            add("                rotationZ = visualRotation - ${offset}f * spinDirection")
            add("                alpha = $alpha * blurMultiplier")
            add("            }")
            add("            .padding(16.dp),")
            add("        onDraw = drawWheelContent")
            add("    )")
        }
        add("}")
    }
}.joinToString("\n")

/** Section title. */
val fortuneWheelTitleSlide = Slide {
    SectionTitleSlide("Fortune wheel")
}

/** The wheel, no motion blur — spins on the next step (click it to spin again). */
val fortuneWheelVideoSlide = Slide(steps = 1) {
    SlideSurface {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WheelStage(size = 860.dp) {
                SpinningFortuneWheel(
                    spinTrigger = step,
                    motionBlurEnabled = false,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/** drawWheelSegment: the fill, then the outline drawArc grows in; preview follows. */
val drawWheelSegmentSlide = Slide(steps = 1) {
    SlideSurface {
        SlideTitle("drawWheelSegment")
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(60.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MorphingCodeBlock(
                code = if (step >= 1) WHEEL_OUTLINE_CODE else WHEEL_SEGMENT_CODE,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            LabeledFrame(label = "Preview") {
                Box(
                    modifier = Modifier.size(560.dp).background(WheelBackground).padding(24.dp),
                ) {
                    StaticFortuneWheel(drawOutline = step >= 1, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/**
 * Without vs with motion blur. Step 1 starts both wheels on the same endless
 * spin → stop → pause loop, in lockstep, so the audience can spot the
 * difference; step 2 reveals which one is which.
 */
val motionBlurComparisonSlide = Slide(steps = 2) {
    SlideSurface {
        val loop = rememberWheelLoop(running = step >= 1)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WheelWithLabel("No motion blur", showLabel = step >= 2) {
                LoopingFortuneWheel(
                    loop = loop,
                    motionBlurEnabled = false,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            WheelWithLabel("Motion blur", showLabel = step >= 2) {
                LoopingFortuneWheel(
                    loop = loop,
                    motionBlurEnabled = true,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/** Section title. */
val motionBlurTitleSlide = Slide {
    SectionTitleSlide("Motion blur")
}

/**
 * The trailing copies of the wheel, one ghost layer per step. While layers are
 * being added the preview is frozen with exactly the layers that are in the
 * code drawn at full strength, so each one visibly stacks on the last; the
 * final step spins it to show the result.
 */
val motionBlurCodeSlide = Slide(steps = 5) {
    val layers = step.coerceAtMost(4)
    val spinning = step >= 5
    SlideSurface {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(60.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MorphingCodeBlock(
                code = motionBlurCode(layers = layers),
                fontSize = 22.sp,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            val label = when {
                spinning -> "Preview · spinning"
                layers == 0 -> "Preview"
                else -> "Preview · $layers ghost layer${if (layers > 1) "s" else ""}"
            }
            LabeledFrame(label = label) {
                Box(
                    modifier = Modifier.size(600.dp).background(WheelBackground).padding(24.dp),
                ) {
                    if (spinning) {
                        SpinningFortuneWheel(
                            spinTrigger = 1,
                            endless = true,
                            ghostLayers = layers,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        FrozenMotionBlurWheel(
                            ghostLayers = layers,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

/** The blog post about it. */
val motionBlurArticleSlide = Slide {
    SlideSurface {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MediaFrame(modifier = Modifier.height(800.dp).aspectRatio(723f / 766f)) {
                PhotoImage(
                    resourcePath = "images/wheel_article.png",
                    contentDescription = "Motion Blur for a Spinning Wheel in Jetpack Compose",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            PhotoImage(
                resourcePath = "images/wheel_article_qr.png",
                contentDescription = "QR code to the article",
                modifier = Modifier.size(500.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

/** The end. */
val theEndSlide = Slide {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A0D12), Color(0xFF131A2B))),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GradientTitle(fontSize = 84.sp)
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "The end",
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(DeckColors.accentAlt, DeckColors.accent)),
                ),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 110.dp, bottom = 90.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Piotr Prus",
                    color = DeckColors.textPrimary,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "@TILT",
                    color = DeckColors.textSecondary,
                    fontSize = 34.sp,
                )
            }
            PhotoImage(
                resourcePath = "images/piotr_avatar.png",
                contentDescription = "Piotr Prus",
                modifier = Modifier.size(260.dp).clip(CircleShape),
            )
        }
    }
}

val fortuneWheelSlides: List<Slide> = listOf(
    fortuneWheelTitleSlide,
    fortuneWheelVideoSlide,
    drawWheelSegmentSlide,
    motionBlurComparisonSlide,
    motionBlurTitleSlide,
    motionBlurCodeSlide,
    motionBlurArticleSlide,
)

/** Dark rounded stage the wheel sits on, matching the SpinWheel sample background. */
@Composable
private fun WheelStage(size: Dp, content: @Composable () -> Unit) {
    MediaFrame(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier.fillMaxSize().background(WheelBackground).padding(32.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun WheelWithLabel(label: String, showLabel: Boolean, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        WheelStage(size = 720.dp, content = content)
        Spacer(modifier = Modifier.height(28.dp))
        // Space is always reserved so the wheels don't shift when the labels appear.
        val alpha by animateFloatAsState(if (showLabel) 1f else 0f, tween(400))
        Text(
            text = label,
            color = DeckColors.textPrimary,
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.graphicsLayer { this.alpha = alpha },
        )
    }
}

/** Large gradient headline, Keynote's section-title look. */
@Composable
private fun SectionHeadline(text: String) {
    Text(
        text = text,
        style = TextStyle(
            brush = Brush.horizontalGradient(listOf(DeckColors.accent, DeckColors.accentAlt)),
        ),
        fontSize = 96.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SectionTitleSlide(text: String) {
    SlideSurface {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SectionHeadline(text)
        }
    }
}
