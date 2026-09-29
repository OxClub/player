package com.example.oxplayer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.oxplayer.data.MediaItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var nowPlaying by remember { mutableStateOf<MediaItem?>(null) }

                    val current = nowPlaying
                    if (current == null) {
                        LibraryScreen(onPlay = { nowPlaying = it })
                    } else {
                        PlayerScreen(item = current, onBack = { nowPlaying = null })
                    }
                }
            }
        }
    }
}
