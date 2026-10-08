package dev.piotrprus.canvascreations.presentation.demos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val TALK_TITLE = "Bringing Your Canvas Creations\n to Life with Jetpack Compose"

/**
 * Live version of the snippet on the BlendMode slide: gradient drawn SrcAtop over
 * text. With [blendMode] = null the gradient is drawn plainly — the
 * intermediate step where the rect simply covers the text.
 */
@Composable
fun GradientTitle(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 52.sp,
    blendMode: BlendMode? = BlendMode.SrcAtop,
) {
    Text(
        text = TALK_TITLE,
        lineHeight = fontSize * 1.25f,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize,
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithCache {
                val brush = Brush.linearGradient(
                    colors = listOf(Color.Cyan, Color.Magenta),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                )
                onDrawWithContent {
                    drawContent()
                    if (blendMode != null) {
                        drawRect(brush = brush, blendMode = blendMode)
                    } else {
                        drawRect(brush = brush)
                    }
                }
            },
    )
}

/** Live version of the drawWithCache snippet before the blend mode is added. */
@Composable
fun GradientRect(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .drawWithCache {
                val brush = Brush.linearGradient(
                    colors = listOf(Color.Cyan, Color.Magenta),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(brush = brush)
                }
            },
    )
}
