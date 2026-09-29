package com.example.oxplayer.ui

import android.app.Activity
import android.provider.Settings
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.oxplayer.OxPlayerApp
import com.example.oxplayer.data.MediaItem
import com.example.oxplayer.engine.PlayerEngine
import org.videolan.libvlc.util.VLCVideoLayout
import kotlin.math.roundToInt

/**
 * Gesture map (matches common player conventions):
 *  - Vertical drag on right half  -> volume
 *  - Vertical drag on left half   -> screen brightness
 *  - Horizontal drag anywhere     -> seek
 *  - Double tap left/right        -> seek -10s / +10s
 *  - Pinch                        -> zoom video surface
 */
@Composable
fun PlayerScreen(item: MediaItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as OxPlayerApp
    val engine = remember { PlayerEngine(app.libVLC) }

    var isAudioOnly by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var videoScale by remember { mutableStateOf(1f) }
    var seekFeedback by remember { mutableStateOf<String?>(null) }

    val activity = context as? Activity

    DisposableEffect(item) {
        onDispose { engine.release() }
    }

    Box(Modifier.fillMaxSize()) {

        // --- Video surface (hidden visually but still decoding audio when audio-only) ---
        AndroidView(
            factory = { ctx ->
                VLCVideoLayout(ctx).also { layout ->
                    engine.attachVideoOutput(layout)
                    engine.load(item.uri, startAudioOnly = isAudioOnly)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayerScale(videoScale)
        )

        if (isAudioOnly) {
            // Audio-only mode: show a simple now-playing surface instead of blank/frozen video
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(96.dp))
                Spacer(Modifier.height(16.dp))
                Text(item.title, color = Color.White)
            }
        }

        // --- Gesture capture layer ---
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            val isRightSide = offset.x > size.width / 2
                            val delta = if (isRightSide) 10_000L else -10_000L
                            engine.seekBy(delta)
                            seekFeedback = if (isRightSide) "+10s" else "-10s"
                        },
                        onTap = { /* toggle controls visibility here if you add an overlay */ }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val isRightSide = change.position.x > size.width / 2
                        if (isRightSide) {
                            adjustVolume(context, -dragAmount)
                        } else {
                            activity?.let { adjustBrightness(it, -dragAmount) }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaMs = (dragAmount * 200).roundToInt().toLong()
                        engine.seekBy(deltaMs)
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        videoScale = (videoScale * zoom).coerceIn(1f, 3f)
                    }
                }
        )

        seekFeedback?.let {
            LaunchedEffect(it) {
                kotlinx.coroutines.delay(600)
                seekFeedback = null
            }
            Text(
                it,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- Bottom controls ---
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                engine.togglePlayPause()
                isPlaying = !isPlaying
            }) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }

            FilterChip(
                selected = isAudioOnly,
                onClick = {
                    isAudioOnly = engine.toggleAudioOnly()
                },
                label = { Text(if (isAudioOnly) "Audio only" else "Video") },
                leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) }
            )
        }
    }
}

private fun adjustVolume(context: android.content.Context, delta: Float) {
    val am = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    val max = am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
    val current = am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
    val step = (delta / 30f) * max
    val target = (current + step).roundToInt().coerceIn(0, max)
    am.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, target, 0)
}

private fun adjustBrightness(activity: Activity, delta: Float) {
    val window = activity.window
    val current = window.attributes.screenBrightness.let { if (it < 0) 0.5f else it }
    val step = delta / 600f
    val target = (current + step).coerceIn(0.05f, 1f)
    val params = window.attributes
    params.screenBrightness = target
    window.attributes = params
}

// Small helper so the AndroidView call above stays readable
private fun Modifier.graphicsLayerScale(scale: Float): Modifier =
    this.then(Modifier.scale(scale))

private fun Modifier.scale(scale: Float): Modifier =
    androidx.compose.ui.draw.scale(scale)
