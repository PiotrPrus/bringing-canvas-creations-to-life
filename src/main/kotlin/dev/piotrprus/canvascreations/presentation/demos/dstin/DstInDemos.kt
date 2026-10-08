package dev.piotrprus.canvascreations.presentation.demos.dstin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

/** Background of every DstIn demo card: saturated enough that faded areas read as "gone" on a projector. */
val DstInCardBackground = Color(0xFF1A2236)

/** The single, quiet accent of the DstIn demos (verified badge, live values). */
val DstInAccent = Color(0xFFA9BCFF)

// Restrained, product-like neutrals on the navy card.
private val Placeholder = Color.White.copy(alpha = 0.16f)
private val PlaceholderHighlight = Color.White.copy(alpha = 0.38f)
private val Tile = Color.White.copy(alpha = 0.10f)
private val TileBorder = Color.White.copy(alpha = 0.14f)
private val TextSecondary = Color.White.copy(alpha = 0.72f)

val WallOfTextLorem = """
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sodales laoreet commodo. Phasellus a purus eu risus elementum consequat. Aenean eu elit ut nunc convallis laoreet non ut libero. Suspendisse interdum placerat risus vel ornare. Donec vehicula, turpis sed consectetur ullamcorper, ante nunc egestas quam, ultricies adipiscing velit enim at nunc. Aenean id diam neque. Praesent ut lacus sed justo viverra fermentum et ut sem. Fusce convallis gravida lacinia. Integer semper dolor ut elit sagittis lacinia.

Praesent sodales scelerisque eros at rhoncus. Duis posuere sapien vel ipsum ornare interdum at eu quam. Vestibulum vel massa erat. Aenean quis sagittis purus. Phasellus arcu purus, rutrum id consectetur non, bibendum at nibh. Duis nec erat dolor. Nulla vitae consectetur ligula. Quisque nec mi est. Ut quam ante, rutrum at pellentesque gravida, pretium in dui. Cras eget sapien velit. Suspendisse ut sem nec tellus vehicula eleifend sit amet quis velit.

Phasellus quis suscipit nisi. Nam elementum malesuada tincidunt. Curabitur iaculis pretium eros, malesuada faucibus leo eleifend a. Curabitur congue orci in neque euismod a blandit libero vehicula. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sodales laoreet commodo. Phasellus a purus eu risus elementum consequat.
""".trim()

/**
 * Exactly the `DstInWallOfText` snippet from the slides. [blend] = false is the
 * "what if we drop the blend mode" step: the gradient is simply painted on top.
 */
@Composable
fun DstInWallOfText(modifier: Modifier, text: String, blend: Boolean = true) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White,
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithCache {
                val brush = Brush.verticalGradient(
                    0f to Color.Black,
                    0.4f to Color.Black,
                    0.95f to Color.Transparent,
                    1f to Color.Transparent,
                )
                onDrawWithContent {
                    drawContent()
                    if (blend) {
                        drawRect(brush = brush, blendMode = BlendMode.DstIn)
                    } else {
                        drawRect(brush = brush)
                    }
                }
            },
    )
}

/** The versions of the animated snippet, in slide order. */
enum class WallStage {
    /** Two Animatables declared, brush unchanged. */
    Remember,

    /** `hFloat.value to Color.Black.copy(alpha = alpha.value)` inserted as a stop. */
    StopAdded,

    /** The fixed 0.4/0.95 stops removed. */
    Trimmed,

    /** Last stop follows the animated alpha too: everything starts hidden. */
    Brush,

    /** LaunchedEffect reveals the text. */
    AnimateIn,

    /** …and hides it again. */
    AnimateInOut,
}

/** Holder for the two Animatables of the snippet, so the slide can print their live values. */
class WallAnimation {
    val hFloat = Animatable(0f)
    val alpha = Animatable(0f)
}

/**
 * The animated `DstInWallOfText` at a given [stage] — each stage draws exactly
 * the brush (and runs exactly the LaunchedEffect) shown in that step's code.
 */
@Composable
fun AnimatedDstInWallOfText(
    stage: WallStage,
    animation: WallAnimation,
    modifier: Modifier,
    text: String,
) {
    val hFloat = animation.hFloat
    val alpha = animation.alpha
    if (stage >= WallStage.AnimateIn) {
        LaunchedEffect(Unit) {
            hFloat.animateTo(1f, tween(5000, easing = LinearEasing))
            alpha.animateTo(1f, tween(3000, easing = LinearEasing))
            if (stage == WallStage.AnimateInOut) {
                alpha.animateTo(0f, tween(1000, easing = LinearEasing))
                hFloat.animateTo(0f, tween(2000, easing = LinearEasing))
            }
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White,
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithCache {
                val brush = when (stage) {
                    WallStage.Remember -> Brush.verticalGradient(
                        0f to Color.Black,
                        0.4f to Color.Black,
                        0.95f to Color.Transparent,
                        1f to Color.Transparent,
                    )
                    WallStage.StopAdded -> Brush.verticalGradient(
                        0f to Color.Black,
                        hFloat.value to Color.Black.copy(alpha = alpha.value),
                        0.4f to Color.Black,
                        0.95f to Color.Transparent,
                        1f to Color.Transparent,
                    )
                    WallStage.Trimmed -> Brush.verticalGradient(
                        0f to Color.Black,
                        hFloat.value to Color.Black.copy(alpha = alpha.value),
                        1f to Color.Transparent,
                    )
                    else -> Brush.verticalGradient(
                        0f to Color.Black,
                        hFloat.value to Color.Black.copy(alpha = alpha.value),
                        1f to Color.Black.copy(alpha = alpha.value),
                    )
                }
                onDrawWithContent {
                    drawContent()
                    drawRect(brush = brush, blendMode = BlendMode.DstIn)
                }
            },
    )
}

/**
 * Replays the animated wall so the effect can be watched again and again:
 * every loop gets fresh Animatables and a fresh LaunchedEffect.
 */
@Composable
fun ReplayingDstInWall(
    stage: WallStage,
    modifier: Modifier = Modifier,
    showValues: Boolean = false,
) {
    var iteration by remember { mutableIntStateOf(0) }
    val loopMillis = when (stage) {
        WallStage.AnimateIn -> 10_000L
        WallStage.AnimateInOut -> 12_500L
        else -> null
    }
    if (loopMillis != null) {
        LaunchedEffect(stage) {
            while (true) {
                delay(loopMillis)
                iteration++
            }
        }
    }
    key(stage, iteration) {
        val animation = remember { WallAnimation() }
        Column(modifier = modifier) {
            AnimatedDstInWallOfText(
                stage = stage,
                animation = animation,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                text = WallOfTextLorem,
            )
            if (showValues) {
                Text(
                    text = "hFloat = %.2f    alpha = %.2f".format(Locale.US, animation.hFloat.value, animation.alpha.value),
                    color = DstInAccent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(vertical = 14.dp),
                )
            }
        }
    }
}

/** Offscreen layer + a DstIn gradient: the shared trick behind every example below. */
private fun Modifier.dstInFade(brush: (Size) -> Brush): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithCache {
        val mask = brush(size)
        onDrawWithContent {
            drawContent()
            drawRect(brush = mask, blendMode = BlendMode.DstIn)
        }
    }

/** Skeleton loading list that dissolves towards the bottom. */
@Composable
fun SkeletonFadeDemo(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer",
    )
    Column(
        modifier = modifier
            .padding(28.dp)
            .dstInFade {
                Brush.verticalGradient(
                    0f to Color.Black,
                    0.3f to Color.Black,
                    1f to Color.Transparent,
                )
            },
        verticalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        repeat(7) { index ->
            val brush = Brush.linearGradient(
                colors = listOf(Placeholder, PlaceholderHighlight, Placeholder),
                start = Offset(-600f + shimmer * 1200f, 0f),
                end = Offset(-200f + shimmer * 1200f, 0f),
                tileMode = TileMode.Clamp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(72.dp).background(brush, CircleShape))
                Spacer(modifier = Modifier.width(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(modifier = Modifier.width(240.dp).height(24.dp).background(brush, CircleShape))
                    Box(modifier = Modifier.width(140.dp).height(24.dp).background(brush, CircleShape))
                }
            }
        }
    }
}

private val ShopThumbs = listOf("👟", "🧢", "👕", "🎒", "🕶️", "⌚")

/** Seller card: the product carousel behind the avatar fades out at both edges. */
@Composable
fun FollowCardDemo(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "carousel")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "carousel",
    )
    val thumb = 110.dp
    val gap = 16.dp
    val setWidth = (thumb + gap) * ShopThumbs.size
    Column(
        modifier = modifier.padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumb)
                    .clipToBounds()
                    .dstInFade {
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.3f to Color.Black,
                            0.7f to Color.Black,
                            1f to Color.Transparent,
                        )
                    },
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentWidth(align = Alignment.Start, unbounded = true)
                        .offset { IntOffset(-(setWidth.toPx() * progress).roundToInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    repeat(3) {
                        ShopThumbs.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(thumb)
                                    .background(Tile, RoundedCornerShape(22.dp))
                                    .border(1.dp, TileBorder, RoundedCornerShape(22.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(text = emoji, fontSize = 52.sp)
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(DstInCardBackground, CircleShape)
                    .padding(8.dp)
                    .background(Color(0xFF2C3650), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🛍️", fontSize = 64.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Retro Kicks", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.size(30.dp).background(DstInAccent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✓", color = DstInCardBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(text = "Old-fashioned sport clothes", color = TextSecondary, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .background(Color.White, CircleShape)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Follow", color = DstInCardBackground, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Auction bid bar: the scrolling fee line fades out at both ends. */
@Composable
fun BidPlacingDemo(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Tile, RoundedCornerShape(20.dp))
                    .border(1.dp, TileBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🧥", fontSize = 56.sp)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Auction • Reset Timer", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "Leather Moto Jacket", color = TextSecondary, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Buyer Protection Fee 🛡  •  shipping + taxes  •  Buyer Protection Fee 🛡  •  shipping + taxes  •  ",
                    color = TextSecondary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier
                        .dstInFade {
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.15f to Color.Black,
                                0.85f to Color.Black,
                                1f to Color.Transparent,
                            )
                        }
                        .basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 0, repeatDelayMillis = 0),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$1", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Text(text = "Top Bid", color = TextSecondary, fontSize = 20.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Custom Bid", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, CircleShape)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Bid $2", color = DstInCardBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Dark rounded card the DstIn demos live on. */
@Composable
fun DstInCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DstInCardBackground)
            .border(2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
    ) {
        content()
    }
}

@Composable
fun DstInCaption(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 30.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

@Composable
fun DstInFillCard(modifier: Modifier, content: @Composable () -> Unit) {
    DstInCard(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}
