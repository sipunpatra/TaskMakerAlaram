package com.pratice.myworld

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

class AlarmReceiver: BroadcastReceiver(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var contextApp: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        contextApp = context
        val message = intent?.getStringExtra("message") ?: "Please complete your task"

        // Show a toast
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        // Speak with TTS
        tts = TextToSpeech(context, this)
        tts?.setSpeechRate(1.0f)
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }
}