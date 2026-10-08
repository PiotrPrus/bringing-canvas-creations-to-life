package dev.piotrprus.canvascreations.presentation.demos.blobs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import kotlin.math.roundToInt

// Interactive blob for the talk. Controls use raw pointer input only — nothing
// here is focusable, so arrow keys / space keep reaching the window's
// onKeyEvent and navigating the deck, and mouse drags never advance a slide.

/** Body gradient + aura color; muted, no neon. */
class BlobPalette(val name: String, val light: Color, val mid: Color, val dark: Color, val aura: Color)

val BlobPalettes = listOf(
    BlobPalette("Gold", Color(0xFFF6E7C1), Gold, Color(0xFF9A7638), Color(0xFFD9BC7C)),
    BlobPalette("Ivory", Color(0xFFFFFFFF), Color(0xFFE4DDD0), Color(0xFFA39C8F), Color(0xFFE9E2D3)),
    BlobPalette("Slate", Color(0xFFD5DEEB), Color(0xFF8FA3BF), Color(0xFF4F5F78), Color(0xFF9DB0CB)),
    BlobPalette("Clay", Color(0xFFF1D2BE), Color(0xFFC98B67), Color(0xFF85523A), Color(0xFFD39A78)),
)

/** A still-ish lit body wrapped in [layers] translucent layers, like Lumeo's orb. */
fun playgroundLayers(
    points: Int,
    amplitude: Float,
    spin: Float,
    layers: Int,
    palette: BlobPalette,
): List<BlobLayer> {
    val moving = (0 until layers).map { i ->
        // Outermost first, so the denser inner layers draw over it.
        val depth = if (layers == 1) 1f else 1f - i.toFloat() / layers
        BlobLayer(
            color = palette.aura,
            alpha = 0.18f + 0.2f * (1f - depth),
            segments = points + i,
            amplitude = (-6).dp..(amplitude * depth).dp,
            tempo = 0.7f + 0.3f * i,
            rotation = i * 47f,
            spin = spin * (1f + 0.3f * i),
        )
    }
    val body = BlobLayer(
        brush = Brush.linearGradient(listOf(palette.light, palette.mid, palette.dark)),
        segments = points,
        amplitude = (-amplitude * 0.08f).dp..(amplitude * 0.08f).dp,
        tempo = 0.6f,
        spin = spin,
    )
    return moving + body
}

@Composable
fun BlobPlayground(modifier: Modifier = Modifier) {
    var points by remember { mutableFloatStateOf(6f) }
    var amplitude by remember { mutableFloatStateOf(64f) }
    var spin by remember { mutableFloatStateOf(12f) }
    var layers by remember { mutableFloatStateOf(3f) }
    var paletteIndex by remember { mutableIntStateOf(0) }
    val palette = BlobPalettes[paletteIndex]

    val blobLayers = remember(points.roundToInt(), amplitude.roundToInt(), spin.roundToInt(), layers.roundToInt(), paletteIndex) {
        playgroundLayers(points.roundToInt(), amplitude.roundToInt().toFloat(), spin.roundToInt().toFloat(), layers.roundToInt(), palette)
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .drawBehind {
                    // Faint navy glow behind the blob, as in Lumeo.
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF1B2540).copy(alpha = 0.9f), Color.Transparent),
                            center = center,
                            radius = size.minDimension * 0.55f,
                        ),
                        radius = size.minDimension * 0.55f,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Blob(layers = blobLayers, modifier = Modifier.size(400.dp))
        }
        Column(
            modifier = Modifier
                .width(440.dp)
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .padding(horizontal = 36.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            ControlRow("segments", points.roundToInt().toString()) {
                DeckSlider(points, { points = it }, 3f..16f)
            }
            ControlRow("amplitude", "${amplitude.roundToInt()}.dp") {
                DeckSlider(amplitude, { amplitude = it }, 0f..120f)
            }
            ControlRow("spin", "${spin.roundToInt()}°/s") {
                DeckSlider(spin, { spin = it }, -90f..90f)
            }
            ControlRow("layers", layers.roundToInt().toString()) {
                DeckSlider(layers, { layers = it }, 1f..5f)
            }
            ControlRow("color", palette.name) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    BlobPalettes.forEachIndexed { index, p ->
                        val selected = index == paletteIndex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(
                                    2.dp,
                                    if (selected) DeckColors.textPrimary else Color.Transparent,
                                    CircleShape,
                                )
                                .padding(5.dp)
                                .background(p.mid, CircleShape)
                                .pointerInput(index) { detectTapGestures { paletteIndex = index } },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlRow(label: String, value: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = label,
                color = DeckColors.textSecondary,
                fontSize = 26.sp,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = value,
                color = DeckColors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
            )
        }
        content()
    }
}

/** Minimal slider: press or drag anywhere on it. Not focusable on purpose. */
@Composable
private fun DeckSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
) {
    val latestOnChange by rememberUpdatedState(onValueChange)
    val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(range) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun update(x: Float) {
                        val f = (x / size.width).coerceIn(0f, 1f)
                        latestOnChange(range.start + f * (range.endInclusive - range.start))
                    }
                    update(down.position.x)
                    down.consume()
                    drag(down.id) { change ->
                        update(change.position.x)
                        change.consume()
                    }
                }
            }
            .drawBehind {
                val trackHeight = 4.dp.toPx()
                val y = size.height / 2f
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.14f),
                    topLeft = Offset(0f, y - trackHeight / 2f),
                    size = Size(size.width, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2f),
                )
                drawRoundRect(
                    color = Gold,
                    topLeft = Offset(0f, y - trackHeight / 2f),
                    size = Size(size.width * fraction, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2f),
                )
                drawCircle(Gold, radius = 11.dp.toPx(), center = Offset(size.width * fraction, y))
            },
    )
}
