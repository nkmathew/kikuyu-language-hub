package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class SoundManager(private val context: Context) : TextToSpeech.OnInitListener {
    
    companion object {
        private const val TAG = "SoundManager"
        private const val MAX_STREAMS = 5
        private const val DEFAULT_PRIORITY = 1
        private const val DEFAULT_RATE = 1.0f
        private const val TTS_QUEUE_FLUSH = TextToSpeech.QUEUE_FLUSH
        private const val TTS_QUEUE_ADD = TextToSpeech.QUEUE_ADD
    }
    
    private var soundPool: SoundPool? = null
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isTtsInitialized = false
    private var ttsLanguageSupported = false
    
    // Sound IDs
    private var swipeSoundId: Int = 0
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var buttonSoundId: Int = 0
    private var achievementSoundId: Int = 0
    
    init {
        initializeSoundPool()
        initializeTTS()
    }
    
    private fun initializeSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            soundPool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build()
            
            loadSounds()
            isInitialized = true
            Log.d(TAG, "SoundManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundPool", e)
            isInitialized = false
        }
    }
    
    private fun loadSounds() {
        soundPool?.let { pool ->
            try {
                // For now, we'll create silent placeholders
                // In a real implementation, you would load actual sound files from assets
                swipeSoundId = 1
                correctSoundId = 2
                wrongSoundId = 3
                buttonSoundId = 4
                achievementSoundId = 5
                
                Log.d(TAG, "Sound placeholders loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sounds", e)
            }
        }
    }
    
    fun playSwipeSound() {
        if (isInitialized && soundPool != null) {
            soundPool?.play(swipeSoundId, 1.0f, 1.0f, DEFAULT_PRIORITY, 0, DEFAULT_RATE)
        }
    }
    
    fun playCorrectSound() {
        if (isInitialized && soundPool != null) {
            soundPool?.play(correctSoundId, 1.0f, 1.0f, DEFAULT_PRIORITY, 0, DEFAULT_RATE)
        }
    }
    
    fun playWrongSound() {
        if (isInitialized && soundPool != null) {
            soundPool?.play(wrongSoundId, 1.0f, 1.0f, DEFAULT_PRIORITY, 0, DEFAULT_RATE)
        }
    }
    
    fun playButtonSound() {
        if (isInitialized && soundPool != null) {
            soundPool?.play(buttonSoundId, 1.0f, 1.0f, DEFAULT_PRIORITY, 0, DEFAULT_RATE)
        }
    }
    
    fun playAchievementSound() {
        if (isInitialized && soundPool != null) {
            soundPool?.play(achievementSoundId, 1.0f, 1.0f, DEFAULT_PRIORITY, 0, DEFAULT_RATE)
        }
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        // In a real implementation, you'd add a preference setting
        // For now, we'll just log the setting
        Log.d(TAG, "Sound enabled: $enabled")
    }
    
    private fun initializeTTS() {
        try {
            tts = TextToSpeech(context, this)
            Log.d(TAG, "TTS initialization started")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TTS", e)
            isTtsInitialized = false
        }
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { textToSpeech ->
                // Try to set Kikuyu locale first, fallback to Swahili, then English
                val kikuyuLocale = Locale("ki", "KE") // Kikuyu (Kenya)
                val swahiliLocale = Locale("sw", "KE") // Swahili (Kenya)
                val englishLocale = Locale("en", "US") // English (US)
                
                val kikuyuResult = textToSpeech.setLanguage(kikuyuLocale)
                if (kikuyuResult == TextToSpeech.LANG_MISSING_DATA || kikuyuResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Kikuyu language not supported, trying Swahili")
                    
                    val swahiliResult = textToSpeech.setLanguage(swahiliLocale)
                    if (swahiliResult == TextToSpeech.LANG_MISSING_DATA || swahiliResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Swahili language not supported, using English")
                        
                        val englishResult = textToSpeech.setLanguage(englishLocale)
                        if (englishResult == TextToSpeech.LANG_MISSING_DATA || englishResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e(TAG, "English language not supported")
                            ttsLanguageSupported = false
                        } else {
                            Log.i(TAG, "TTS initialized with English")
                            ttsLanguageSupported = true
                        }
                    } else {
                        Log.i(TAG, "TTS initialized with Swahili")
                        ttsLanguageSupported = true
                    }
                } else {
                    Log.i(TAG, "TTS initialized with Kikuyu")
                    ttsLanguageSupported = true
                }
                
                // Configure TTS settings
                textToSpeech.setSpeechRate(0.8f) // Slightly slower for learning
                textToSpeech.setPitch(1.0f)
                
                // Set up utterance progress listener
                textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "TTS started: $utteranceId")
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "TTS completed: $utteranceId")
                    }
                    
                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "TTS error: $utteranceId")
                    }
                })
                
                isTtsInitialized = true
                Log.d(TAG, "TTS initialization completed successfully")
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
            isTtsInitialized = false
        }
    }
    
    // Pronunciation methods
    fun speakEnglish(text: String) {
        if (!isTtsInitialized || !ttsLanguageSupported) {
            Log.w(TAG, "TTS not available for English pronunciation")
            return
        }
        
        tts?.let { textToSpeech ->
            // Temporarily switch to English for English phrases
            val englishLocale = Locale("en", "US")
            textToSpeech.setLanguage(englishLocale)
            
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "english_$text")
            
            textToSpeech.speak(text, TTS_QUEUE_FLUSH, params, "english_$text")
            Log.d(TAG, "Speaking English: $text")
        }
    }
    
    fun speakKikuyu(text: String) {
        if (!isTtsInitialized || !ttsLanguageSupported) {
            Log.w(TAG, "TTS not available for Kikuyu pronunciation")
            return
        }
        
        tts?.let { textToSpeech ->
            // Try Kikuyu first, fallback to Swahili for closest pronunciation
            val kikuyuLocale = Locale("ki", "KE")
            val swahiliLocale = Locale("sw", "KE")
            
            val kikuyuResult = textToSpeech.setLanguage(kikuyuLocale)
            if (kikuyuResult == TextToSpeech.LANG_MISSING_DATA || kikuyuResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(swahiliLocale)
                Log.d(TAG, "Using Swahili pronunciation for Kikuyu text")
            }
            
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "kikuyu_$text")
            
            textToSpeech.speak(text, TTS_QUEUE_FLUSH, params, "kikuyu_$text")
            Log.d(TAG, "Speaking Kikuyu: $text")
        }
    }
    
    fun speakPhrase(phrase: Phrase, language: String = "both") {
        when (language.lowercase()) {
            "english" -> speakEnglish(phrase.english)
            "kikuyu" -> speakKikuyu(phrase.kikuyu)
            "both" -> {
                speakEnglish(phrase.english)
                // Add delay between languages
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    speakKikuyu(phrase.kikuyu)
                }, 2000) // 2 second delay
            }
        }
    }
    
    fun stopSpeaking() {
        if (isTtsInitialized) {
            tts?.stop()
            Log.d(TAG, "TTS stopped")
        }
    }
    
    fun isSpeaking(): Boolean {
        return if (isTtsInitialized) {
            tts?.isSpeaking ?: false
        } else false
    }
    
    fun setSpeechRate(rate: Float) {
        if (isTtsInitialized) {
            tts?.setSpeechRate(rate.coerceIn(0.1f, 3.0f))
            Log.d(TAG, "Speech rate set to: $rate")
        }
    }
    
    fun setPitch(pitch: Float) {
        if (isTtsInitialized) {
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
            Log.d(TAG, "Pitch set to: $pitch")
        }
    }
    
    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isTtsInitialized = false
        
        soundPool?.release()
        soundPool = null
        isInitialized = false
        
        Log.d(TAG, "SoundManager released")
    }
}