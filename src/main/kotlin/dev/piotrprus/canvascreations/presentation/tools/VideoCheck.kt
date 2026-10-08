package dev.piotrprus.canvascreations.presentation.tools

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.delay
import java.io.File

/**
 * Dev tool: plays one video in a small window and logs the native player's
 * state for a few seconds — a smoke test that decoding works on this machine.
 */
fun main(args: Array<String>) = application {
    val path = args.getOrElse(0) { "src/main/resources/videos/tilt_hero.mp4" }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Video check",
        state = rememberWindowState(size = DpSize(800.dp, 450.dp), position = WindowPosition(40.dp, 40.dp)),
    ) {
        val player = rememberVideoPlayerState()
        LaunchedEffect(Unit) {
            player.volume = 0f
            player.openUri(File(path).toURI().toString())
            repeat(8) {
                delay(1000)
                println(
                    "t=${it + 1}s playing=${player.isPlaying} loading=${player.isLoading} " +
                        "hasMedia=${player.hasMedia} pos=${player.currentTime} dur=${player.duration} " +
                        "aspect=${player.aspectRatio} error=${player.error}",
                )
            }
            exitApplication()
        }
        VideoPlayerSurface(playerState = player, modifier = Modifier.fillMaxSize())
    }
}
