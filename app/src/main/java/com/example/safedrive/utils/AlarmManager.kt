package com.example.safedrive.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.safedrive.R // Ensure this matches your package

class AlarmManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playAlarm() {
        if (mediaPlayer == null) {
            // Make sure you have 'res/raw/alarm.mp3'
            mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
    }

    fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}