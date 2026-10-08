package dev.piotrprus.canvascreations.presentation.slides

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.piotrprus.canvascreations.presentation.deck.Slide
import dev.piotrprus.canvascreations.presentation.demos.GradientTitle
import dev.piotrprus.canvascreations.presentation.ui.DeckColors
import dev.piotrprus.canvascreations.presentation.ui.MediaFrame
import dev.piotrprus.canvascreations.presentation.ui.PhotoImage
import dev.piotrprus.canvascreations.presentation.ui.VideoClip

// Keynote slides 1–4. Slide 3 (title repeated without the avatar) was only a
// Magic Move bridge into the video, so it is folded into the transition here.

/** Keynote 1. */
val titleSlide = Slide {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A0D12), Color(0xFF131A2B))),
            ),
    ) {
        GradientTitle(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            fontSize = 84.sp,
        )
        SpeakerBadge(modifier = Modifier.align(Alignment.BottomEnd))
    }
}

/** Keynote 2 — who I am and what TILT is: the hero video from tilt.app. */
val aboutMeSlide = Slide {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A0D12), Color(0xFF131A2B))),
            ),
    ) {
        MediaFrame(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 60.dp)
                .fillMaxHeight()
                .aspectRatio(720f / 1558f),
        ) {
            VideoClip(
                resourcePath = "videos/tilt_hero.mp4",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        SpeakerBadge(modifier = Modifier.align(Alignment.BottomEnd))
    }
}

/** Name + avatar in the bottom-right corner, shared by the first slides. */
@Composable
private fun SpeakerBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(end = 110.dp, bottom = 90.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        SpeakerName()
        Avatar(size = 260)
    }
}

@Composable
private fun SpeakerName() {
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
}

@Composable
private fun Avatar(size: Int) {
    PhotoImage(
        resourcePath = "images/piotr_avatar.png",
        contentDescription = "Piotr Prus",
        modifier = Modifier.size(size.dp).clip(CircleShape),
    )
}

val introSlides: List<Slide> = listOf(titleSlide, aboutMeSlide)
