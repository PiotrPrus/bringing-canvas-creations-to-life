package dev.piotrprus.canvascreations.presentation.demos.boost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Image
import org.jetbrains.skia.Typeface
import java.util.Base64

// Desktop stand-ins for the bits of the TILT Android uikit that
// BoostCompetitionBubble.kt and BoostContainer.kt read. Names and values are
// copied from RetroColors.kt (dark palette), RetroDimen.kt and RetroTypography.kt.

class ColorWithAlpha(private val baseColor: Color) {
    val base = baseColor
    val a100 = baseColor.copy(alpha = 1.0f)
    val a52 = baseColor.copy(alpha = 0.52f)
    val a26 = baseColor.copy(alpha = 0.26f)
}

class RetroColors(
    val bg: Bg,
    val fg: Fg,
    val decorative: Decorative,
    val overMedia: OverMedia,
) {
    class Bg(val page: Page) {
        class Page(val secondary: ColorWithAlpha)
    }

    class Fg(val neutral: Neutral, val info: Info) {
        class Neutral(val primary: Color)
        class Info(val primary: Color)
    }

    class Decorative(
        val violet: Color,
        val orange: Color,
        val vibrantPink: Color,
        val pineapple: Color,
        val lime: Color,
        val sea: Color,
        val particleColors: List<Color>,
    )

    class OverMedia(val surface: Color)
}

private object DarkPalette {
    val neutral2 = ColorWithAlpha(Color(0xff141415))
    val neutral6 = ColorWithAlpha(Color(0xfff7f7f8))
    val violet = Color(0xff9840ff)
    val vibrantPink = Color(0xffff7aff)
    val candyPink = Color(0xffff5794)
    val mandarine = Color(0xffff7b38)
    val orange = Color(0xffffb842)
    val pineapple = Color(0xffe5ff5e)
    val lime = Color(0xffbdff4f)
    val teal = Color(0xff7efcc5)
    val sea = Color(0xff94fffc)
    val sky = Color(0xff4ac2ff)
}

val DarkRetroColors: RetroColors = with(DarkPalette) {
    RetroColors(
        bg = RetroColors.Bg(page = RetroColors.Bg.Page(secondary = ColorWithAlpha(neutral2.base))),
        fg = RetroColors.Fg(
            neutral = RetroColors.Fg.Neutral(primary = neutral6.base),
            info = RetroColors.Fg.Info(primary = sky),
        ),
        decorative = RetroColors.Decorative(
            violet = violet,
            orange = orange,
            vibrantPink = vibrantPink,
            pineapple = pineapple,
            lime = lime,
            sea = sea,
            particleColors = listOf(
                violet, vibrantPink, candyPink, mandarine, orange,
                pineapple, lime, teal, sea, sky,
            ),
        ),
        overMedia = RetroColors.OverMedia(surface = neutral2.base),
    )
}

class RetroTypography(val largeTitle: TextStyle)

object RetroTheme {
    val colors: RetroColors
        @Composable @ReadOnlyComposable get() = DarkRetroColors

    val typography: RetroTypography
        @Composable @ReadOnlyComposable get() = Typography
}

private val Typography = RetroTypography(
    largeTitle = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.W700, lineHeight = 40.sp),
)

object RetroDimen {
    val padding1 = 1.dp
    val padding8 = 8.dp
    val boostRotationContainerSize = 112.dp
    val boostRotationTextHeight = 9.dp
    val boostBubbleSize = 78.dp
}

/** The app resources BoostCompetitionBubble uses, embedded from [BoostAssets.kt]. */
@Suppress("ClassName")
object R {
    object drawable {
        const val boost_noise = boost_noise_png
        const val oval_low_q = oval_low_q_png
        const val boost_sparkles = boost_sparkles_png
        const val star_four = star_four_png
    }

    object font {
        const val aspekta_700 = aspekta_700_otf
    }

    object string {
        const val boost_competition_profile_flair_circular_title = "PROFILE FLAIR • TAP TO CLAIM"
        const val boost_competition_coupon_free_shipping_circular_title = "FREE SHIPPING • TAP TO CLAIM"
        const val boost_competition_coupon_circular_title = "%s COUPON • TAP TO CLAIM"
    }
}

/** Desktop `ImageBitmap.imageResource(resources, id)`: decodes an embedded PNG. */
fun ImageBitmap.Companion.imageResource(id: String): ImageBitmap =
    Image.makeFromEncoded(Base64.getDecoder().decode(id)).toComposeImageBitmap()

/** Desktop `resources.getFont(id)`. */
fun getFont(id: String): Typeface =
    FontMgr.default.makeFromData(Data.makeFromBytes(Base64.getDecoder().decode(id)))
        ?: error("Cannot load font")

/** Desktop `stringResource(id, args)`. */
fun stringResource(id: String, vararg formatArgs: Any): String = id.format(*formatArgs)
