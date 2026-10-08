package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.blobs.Blob
import dev.piotrprus.canvascreations.presentation.demos.blobs.BlobBuildDemo
import dev.piotrprus.canvascreations.presentation.demos.blobs.BlobLayer
import dev.piotrprus.canvascreations.presentation.demos.blobs.BlobPalettes
import dev.piotrprus.canvascreations.presentation.demos.blobs.BlobPlayground
import dev.piotrprus.canvascreations.presentation.demos.blobs.BlobPresets
import dev.piotrprus.canvascreations.presentation.demos.blobs.Gold
import dev.piotrprus.canvascreations.presentation.demos.blobs.blobBuildCode
import dev.piotrprus.canvascreations.presentation.demos.blobs.playgroundLayers
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.LabeledFrame
import dev.piotrprus.canvascreations.presentation.ui.MorphingCodeBlock
import dev.piotrprus.canvascreations.presentation.ui.PhotoImage
import dev.piotrprus.canvascreations.presentation.ui.SlideSubtitle
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle
import dev.piotrprus.canvascreations.presentation.ui.highlightKotlin
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

// Blobs: how KBlobs (github.com/PiotrPrus/KBlobs) morphs a circle, then Lumeo,
// the app it was built for. Every blob here runs the KBlobs core ported in
// demos/blobs/KBlobs.kt; the build-up slide runs exactly the code it shows.

private const val APP_STORE_URL = "https://apps.apple.com/app/id6811937018"
private const val GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=dev.piotrprus.lumeo"

private val Sand = Color(0xFFE8D5A9)
private val Clay = Color(0xFFC98B67)
private val Slate = Color(0xFF8FA3BF)

/** Section title. */
val blobsTitleSlide = Slide {
    SlideSurface {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Blobs",
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(DeckColors.accent, DeckColors.accentAlt)),
                ),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * From a circle to a blob, the code growing next to a live preview:
 * 0 circle · 1 points on it · 2 path through them · 3 move them over time · 4 smooth curve.
 */
val blobBuildSlide = Slide(steps = 4) {
    val heading = when (step) {
        0 -> "A circle" to "where every blob starts"
        1 -> "Points on the circle" to "evenly spaced, each with its own direction"
        2 -> "A path through the points" to "moveTo, lineTo, close"
        3 -> "Move the points" to "each one along its radius, over time"
        else -> "Smooth it" to "sample the curve between the points"
    }
    SlideSurface {
        Crossfade(targetState = heading, animationSpec = tween(500)) { (title, subtitle) ->
            Column {
                SlideTitle(title)
                SlideSubtitle(subtitle)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(50.dp),
        ) {
            MorphingCodeBlock(
                code = blobBuildCode(step),
                fontSize = 21.sp,
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
            LabeledFrame(label = "Preview") {
                Box(
                    modifier = Modifier.size(480.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BlobBuildDemo(step = step)
                }
            }
        }
    }
}

/** Same algorithm, different settings — one live blob per BlobLayer tweak. */
val blobVariationsSlide = Slide(steps = 1) {
    SlideSurface {
        SlideTitle("Same blob, other settings")
        Spacer(modifier = Modifier.height(30.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            VariationRow {
                Variation("Few points", "segments = 3") {
                    listOf(BlobLayer(color = Gold, segments = 3, amplitude = (-20).dp..26.dp))
                }
                Variation("Many points", "segments = 16") {
                    listOf(BlobLayer(color = Gold, segments = 16, amplitude = (-10).dp..12.dp))
                }
                Variation("Bigger amplitude", "amplitude = (-24).dp..44.dp") {
                    listOf(BlobLayer(color = Gold, segments = 6, amplitude = (-24).dp..44.dp))
                }
            }
            Reveal(at = 1) {
                VariationRow {
                    Variation("Outlines, colors", "strokeWidth = 3.dp") {
                        listOf(Sand, Clay, Slate, Gold).mapIndexed { i, color ->
                            BlobLayer(
                                color = color,
                                segments = 5 + i,
                                amplitude = (-10).dp..16.dp,
                                strokeWidth = 3.dp,
                                rotation = i * 40f,
                            )
                        }
                    }
                    Variation("Layers", "BlobPresets.aura(body = Gold)") {
                        BlobPresets.aura(body = Gold, reach = 40.dp)
                    }
                    Variation("Layers + blur", "blur = 14.dp") {
                        listOf(Slate, Clay, Gold).mapIndexed { i, color ->
                            BlobLayer(
                                color = color,
                                alpha = 0.8f,
                                segments = 5 + 2 * i,
                                amplitude = (-4).dp..(40 - 10 * i).dp,
                                rotation = i * 60f,
                                blur = 14.dp,
                            )
                        } + BlobLayer(color = Sand, amplitude = 0.dp..0.dp)
                    }
                }
            }
        }
    }
}

private const val KBLOBS_DEMO_URL = "https://piotrprus.github.io/KBlobs/demo/"

/** Interactive: drag the sliders live during the talk. */
val blobPlaygroundSlide = Slide {
    SlideSurface {
        SlideTitle("Play with it")
        Box(modifier = Modifier.fillMaxSize().padding(top = 20.dp)) {
            BlobPlayground(modifier = Modifier.fillMaxSize())
            // The audience plays along on the web version of the same library.
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .background(Color.White, RoundedCornerShape(18.dp))
                        .padding(20.dp),
                ) {
                    Image(
                        painter = rememberQrCodePainter(KBLOBS_DEMO_URL),
                        contentDescription = "QR code to the KBlobs web demo",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Play along",
                    color = DeckColors.textPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "piotrprus.github.io/KBlobs/demo",
                    color = DeckColors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                )
            }
        }
    }
}

/** Lumeo: the recording screen, its orb is a KBlobs blob. QR codes to both stores. */
val lumeoSlide = Slide {
    SlideSurface {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PhotoImage(
                resourcePath = "images/lumeo_capture.png",
                contentDescription = "Lumeo recording screen",
                modifier = Modifier
                    .height(920.dp)
                    .aspectRatio(900f / 2004f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, DeckColors.codeBorder, RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Fit,
            )
            Column(modifier = Modifier.width(900.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(124.dp), contentAlignment = Alignment.Center) {
                        Blob(
                            layers = playgroundLayers(
                                points = 6,
                                amplitude = 18f,
                                spin = 10f,
                                layers = 3,
                                palette = BlobPalettes.first(),
                            ),
                            modifier = Modifier.size(84.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Lumeo",
                        color = DeckColors.textPrimary,
                        fontSize = 110.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Thoughts in, illumination out.",
                    color = Sand,
                    fontSize = 44.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "A voice-first second brain in Compose Multiplatform.\n" +
                        "Its listening orb is a layered blob.",
                    color = DeckColors.textSecondary,
                    fontSize = 32.sp,
                    lineHeight = 46.sp,
                )
                Spacer(modifier = Modifier.height(64.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(64.dp)) {
                    StoreQr(label = "App Store", url = APP_STORE_URL)
                    StoreQr(label = "Google Play", url = GOOGLE_PLAY_URL)
                }
            }
        }
    }
}

val blobSlides: List<Slide> = listOf(
    blobsTitleSlide,
    blobBuildSlide,
    blobVariationsSlide,
    blobPlaygroundSlide,
    lumeoSlide,
)

/** The closing slide, kept last in the deck. */
val endSlides: List<Slide> = listOf(theEndSlide)

@Composable
private fun VariationRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        content()
    }
}

@Composable
private fun Variation(title: String, code: String, layers: () -> List<BlobLayer>) {
    Column(
        modifier = Modifier.width(520.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(width = 520.dp, height = 250.dp), contentAlignment = Alignment.Center) {
            Blob(layers = remember { layers() }, modifier = Modifier.size(150.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = DeckColors.textPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = highlightKotlin(code),
            fontFamily = FontFamily.Monospace,
            fontSize = 24.sp,
        )
    }
}

@Composable
private fun StoreQr(label: String, url: String, size: Dp = 280.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(24.dp),
        ) {
            Image(
                painter = rememberQrCodePainter(url),
                contentDescription = "QR code to Lumeo on $label",
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = label,
            color = DeckColors.textPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
