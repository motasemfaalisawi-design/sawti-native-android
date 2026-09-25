package com.sawti.translator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sawti.translator.service.NativeSpeechManager
import com.sawti.translator.service.NativeTextToSpeech
import com.sawti.translator.service.NativeTranslatorService
import com.sawti.translator.ui.MainTranslatorScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var speechManager: NativeSpeechManager
    private lateinit var translatorService: NativeTranslatorService
    private lateinit var ttsManager: NativeTextToSpeech

    private var hasMicPermission by mutableStateOf(false)
    private var sourceLang by mutableStateOf("ar")
    private var targetLang by mutableStateOf("en")
    private var translatedText by mutableStateOf("")

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            Toast.makeText(this, "تم منح إذن الميكروفون بنجاح", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "يحتاج التطبيق لإذن الميكروفون للترجمة الصوتية", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Native Services
        speechManager = NativeSpeechManager(this)
        translatorService = NativeTranslatorService()
        ttsManager = NativeTextToSpeech(this)

        // Setup real-time translation trigger
        speechManager.onFinalResult = { text ->
            if (text.isNotBlank()) {
                lifecycleScope.launch {
                    val result = translatorService.translate(
                        phrase = text,
                        sourceLang = sourceLang,
                        targetLang = targetLang
                    )
                    translatedText = result
                }
            }
        }

        // Check & request runtime permissions
        checkAndRequestPermission()

        setContent {
            val isListening by speechManager.isRecognizing.collectAsState()
            val spokenText by speechManager.spokenText.collectAsState()
            val rmsDb by speechManager.rmsDb.collectAsState()

            // Also translate live interim speech
            LaunchedEffect(spokenText) {
                if (spokenText.isNotBlank()) {
                    val result = translatorService.translate(
                        phrase = spokenText,
                        sourceLang = sourceLang,
                        targetLang = targetLang
                    )
                    translatedText = result
                }
            }

            MainTranslatorScreen(
                isListening = isListening,
                spokenText = spokenText,
                translatedText = translatedText,
                rmsDb = rmsDb,
                sourceLang = sourceLang,
                targetLang = targetLang,
                onToggleListening = {
                    if (!hasMicPermission) {
                        checkAndRequestPermission()
                    } else {
                        if (isListening) {
                            speechManager.stopListening()
                        } else {
                            val langCode = if (sourceLang == "ar") "ar-SA" else "en-US"
                            speechManager.startListening(langCode)
                        }
                    }
                },
                onSwapLanguages = {
                    val temp = sourceLang
                    sourceLang = targetLang
                    targetLang = temp
                    translatedText = ""
                    if (isListening) {
                        speechManager.stopListening()
                        val langCode = if (sourceLang == "ar") "ar-SA" else "en-US"
                        speechManager.startListening(langCode)
                    }
                },
                onSpeak = { text ->
                    ttsManager.speak(text, targetLang)
                }
            )
        }
    }

    private fun checkAndRequestPermission() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        hasMicPermission = granted
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
