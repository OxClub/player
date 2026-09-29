package com.example.oxplayer

import android.app.Application
import org.videolan.libvlc.LibVLC

/**
 * LibVLC should be created once per process, not once per screen.
 * Every player screen reuses this single instance.
 */
class OxPlayerApp : Application() {

    lateinit var libVLC: LibVLC
        private set

    override fun onCreate() {
        super.onCreate()
        val options = arrayListOf(
            "--no-drop-late-frames",
            "--no-skip-frames",
            "--avcodec-hw=any" // hardware decoding when the device supports it, falls back to software
        )
        libVLC = LibVLC(this, options)
    }
}
