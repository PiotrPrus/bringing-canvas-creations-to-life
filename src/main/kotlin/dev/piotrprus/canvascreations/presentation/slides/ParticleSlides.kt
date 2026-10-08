package dev.piotrprus.canvascreations.presentation.slides

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.particles.AmbientParticlesDemo
import dev.piotrprus.canvascreations.presentation.demos.particles.FlameDemo
import dev.piotrprus.canvascreations.presentation.demos.particles.MagicWandDemo
import dev.piotrprus.canvascreations.presentation.demos.particles.MatrixRainDemo
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.SlideSurface
import dev.piotrprus.canvascreations.presentation.ui.SlideTitle
import dev.piotrprus.canvascreations.presentation.ui.highlightKotlin
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

// The ParticleEmitter library release, then a few effects built with it.
// Demos are copied from the ParticleEmitter talk and run on the published 1.1.0.

private const val LIBRARY_URL = "https://github.com/PiotrPrus/ParticleEmitter"

private const val DEPENDENCY_CODE = """
// build.gradle.kts
implementation("io.github.piotrprus:particle-emitter:1.1.0")
"""

/** The library is out — QR to GitHub, the dependency line, sparks rising behind. */
val libraryReleaseSlide = Slide {
    Box(modifier = Modifier.fillMaxSize()) {
        SlideSurface {}
        AmbientParticlesDemo()
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 110.dp, vertical = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ParticleEmitter 1.1.0 is out!",
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(DeckColors.accent, DeckColors.accentAlt)),
                ),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Compose Multiplatform · Android · iOS · Desktop · Web",
                color = DeckColors.textSecondary,
                fontSize = 36.sp,
            )
            Spacer(modifier = Modifier.height(80.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(70.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(380.dp)
                            .background(Color.White, RoundedCornerShape(32.dp))
                            .padding(32.dp),
                    ) {
                        Image(
                            painter = rememberQrCodePainter(LIBRARY_URL),
                            contentDescription = "QR code to the ParticleEmitter repository",
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "github.com/PiotrPrus/ParticleEmitter",
                        color = DeckColors.textPrimary,
                        fontSize = 32.sp,
                    )
                }
                Text(
                    text = highlightKotlin(DEPENDENCY_CODE.trimIndent()),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp,
                    lineHeight = 40.sp,
                    softWrap = false,
                    modifier = Modifier
                        .background(DeckColors.codeBackground, RoundedCornerShape(20.dp))
                        .border(1.dp, DeckColors.codeBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 40.dp, vertical = 32.dp),
                )
            }
        }
    }
}

/** Three effects, revealed one by one. */
val particleInspirationSlide = Slide(steps = 3) {
    SlideSurface {
        SlideTitle("Particles in Compose")
        Spacer(modifier = Modifier.height(50.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(50.dp),
        ) {
            Reveal(at = 1, modifier = Modifier.weight(1f)) {
                EffectCard("Campfire flame") { FlameDemo() }
            }
            Reveal(at = 2, modifier = Modifier.weight(1f)) {
                EffectCard("Matrix rain") { MatrixRainDemo() }
            }
            Reveal(at = 3, modifier = Modifier.weight(1f)) {
                EffectCard("Magic wand") { MagicWandDemo() }
            }
        }
    }
}

val particleSlides: List<Slide> = listOf(
    libraryReleaseSlide,
    particleInspirationSlide,
)

/** Square-ish dark stage with a label underneath. */
@Composable
private fun EffectCard(label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0A0D12))
                .border(1.dp, DeckColors.codeBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = label,
            color = DeckColors.textPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Full-height dark stage with a label underneath. */
@Composable
private fun EffectFrame(label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0A0D12))
                .border(1.dp, DeckColors.codeBorder, RoundedCornerShape(24.dp)),
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = label,
            color = DeckColors.textPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
