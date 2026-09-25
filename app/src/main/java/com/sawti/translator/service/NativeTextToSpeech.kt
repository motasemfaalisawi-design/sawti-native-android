package com.sawti.translator.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class NativeTextToSpeech(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.language = Locale.ENGLISH
        }
    }

    fun speak(text: String, langCode: String = "en") {
        if (!isReady || text.isEmpty()) return

        val locale = if (langCode.startsWith("ar")) {
            Locale("ar")
        } else {
            Locale.ENGLISH
        }

        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sawti_tts_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
