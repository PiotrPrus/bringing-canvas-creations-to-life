package dev.piotrprus.canvascreations.presentation.demos.boost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.min

/**
 * The TILT boost bubble for the slides, drawn by the ported app code in
 * [FloatingBoostBubble] / [OuterGlowBorder]. All app sizes stay in their
 * original dp (78.dp bubble, 112.dp text ring); the bubble is scaled up by
 * swapping [LocalDensity] so the 112.dp container fills the smaller side of
 * [modifier] while blurs and text stay sharp. [layers] picks which draw calls
 * run, so the slides can build the bubble up one layer at a time.
 */
@Composable
fun BoostBubble(
    modifier: Modifier = Modifier,
    emoji: String = "✨",
    boostText: String = stringResource(id = R.string.boost_competition_profile_flair_circular_title),
    layers: Set<BoostLayer> = BoostLayer.All,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val outer = LocalDensity.current
        val scale = min(maxWidth, maxHeight) / RetroDimen.boostRotationContainerSize
        CompositionLocalProvider(
            LocalDensity provides Density(outer.density * scale, outer.fontScale),
            LocalBoostLayers provides layers,
        ) {
            if (BoostLayer.VibrantStars in layers) {
                // In the app this emitter is fullscreen and follows the travelling
                // bubble; here the bubble stays put, so give it a field around it.
                val field = RetroDimen.boostRotationContainerSize * 2
                BoostVibrantStarParticles(
                    bubbleCenterProvider = { DpOffset(field / 2, field / 2) },
                    modifier = Modifier.wrapContentSize(unbounded = true).size(field),
                )
            }
            Box(contentAlignment = Alignment.Center) {
                FloatingBoostBubble(boostText = boostText, emoji = emoji)
            }
        }
    }
}

/** Layer sets used by the build-up slides, in the order they are introduced. */
object BoostLayers {
    val Bubble = setOf(BoostLayer.Surface, BoostLayer.RadialOverlay, BoostLayer.Noise, BoostLayer.Emoji)
    val InnerGlow = Bubble + BoostLayer.GlowLayer1 + BoostLayer.GlowLayer2
    val Arc = InnerGlow + BoostLayer.GlowLayer3
    val Ring = Arc + BoostLayer.GlowLayer4
    val Rotating = Ring + BoostLayer.Rotation
}
