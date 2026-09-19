package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class SpeechStatus {
    IDLE,
    PLAYING,
    PAUSED
}

class ArticleSpeechHelper(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _status = MutableStateFlow(SpeechStatus.IDLE)
    val status: StateFlow<SpeechStatus> = _status.asStateFlow()

    private val _currentParagraphIndex = MutableStateFlow(0)
    val currentParagraphIndex: StateFlow<Int> = _currentParagraphIndex.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private var paragraphs: List<String> = emptyList()
    private var articleTitle: String = ""

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale("en", "IN"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.language = Locale.US
                    }
                    engine.setSpeechRate(_speechRate.value)
                    isInitialized = true
                    setupUtteranceListener(engine)
                }
            } else {
                Log.w("ArticleSpeechHelper", "TTS Initialization failed")
            }
        }
    }

    private fun setupUtteranceListener(engine: TextToSpeech) {
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _status.value = SpeechStatus.PLAYING
            }

            override fun onDone(utteranceId: String?) {
                val nextIdx = _currentParagraphIndex.value + 1
                if (nextIdx < paragraphs.size) {
                    _currentParagraphIndex.value = nextIdx
                    speakCurrentParagraph()
                } else {
                    _status.value = SpeechStatus.IDLE
                    _currentParagraphIndex.value = 0
                }
            }

            override fun onError(utteranceId: String?) {
                _status.value = SpeechStatus.IDLE
            }
        })
    }

    fun startListening(title: String, paragraphList: List<String>) {
        if (!isInitialized) return
        articleTitle = title
        paragraphs = if (paragraphList.isNotEmpty()) paragraphList else listOf(title)
        _currentParagraphIndex.value = 0
        speakCurrentParagraph()
    }

    fun pause() {
        tts?.stop()
        _status.value = SpeechStatus.PAUSED
    }

    fun resume() {
        if (!isInitialized) return
        if (_status.value == SpeechStatus.PAUSED) {
            speakCurrentParagraph()
        }
    }

    fun stop() {
        tts?.stop()
        _status.value = SpeechStatus.IDLE
        _currentParagraphIndex.value = 0
    }

    fun nextParagraph() {
        if (_currentParagraphIndex.value < paragraphs.size - 1) {
            _currentParagraphIndex.value += 1
            if (_status.value == SpeechStatus.PLAYING) {
                speakCurrentParagraph()
            }
        }
    }

    fun previousParagraph() {
        if (_currentParagraphIndex.value > 0) {
            _currentParagraphIndex.value -= 1
            if (_status.value == SpeechStatus.PLAYING) {
                speakCurrentParagraph()
            }
        }
    }

    fun setSpeed(rate: Float) {
        _speechRate.value = rate
        tts?.setSpeechRate(rate)
        if (_status.value == SpeechStatus.PLAYING) {
            speakCurrentParagraph()
        }
    }

    private fun speakCurrentParagraph() {
        if (!isInitialized) return
        val idx = _currentParagraphIndex.value
        val textToSpeak = if (idx == 0 && articleTitle.isNotBlank() && !paragraphs[0].startsWith(articleTitle)) {
            "$articleTitle. ${paragraphs.getOrNull(idx) ?: ""}"
        } else {
            paragraphs.getOrNull(idx) ?: return
        }

        val params = android.os.Bundle()
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "varta_paragraph_$idx")
        _status.value = SpeechStatus.PLAYING
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
