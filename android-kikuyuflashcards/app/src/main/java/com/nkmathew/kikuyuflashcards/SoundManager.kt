package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry

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
    private var vibrator: Vibrator? = null
    private var isInitialized = false
    private var isTtsInitialized = false
    private var ttsLanguageSupported = false

    // Settings for sound and vibration
    private var soundEnabled = true
    private var vibrationEnabled = true
    
    // Sound IDs
    private var swipeSoundId: Int = -1
    private var correctSoundId: Int = -1
    private var wrongSoundId: Int = -1
    private var buttonSoundId: Int = -1
    private var achievementSoundId: Int = -1

    // Sound loaded flags
    private var swipeSoundLoaded = false
    private var correctSoundLoaded = false
    private var wrongSoundLoaded = false
    private var buttonSoundLoaded = false
    private var achievementSoundLoaded = false
    
    init {
        initializeSoundPool()
        initializeVibrator()
        initializeTTS()
        loadSettings()
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
                // Load Duolingo-style sound effects from assets
                correctSoundId = loadSoundFromAssets("sounds/duolingo-correct.mp3")
                wrongSoundId = loadSoundFromAssets("sounds/duolingo-incorrect.mp3")
                achievementSoundId = loadSoundFromAssets("sounds/duolingo-level-complete.mp3")

                // Set loaded flags
                correctSoundLoaded = correctSoundId != -1
                wrongSoundLoaded = wrongSoundId != -1
                achievementSoundLoaded = achievementSoundId != -1

                Log.d(TAG, "Sound effects loaded - Correct: $correctSoundLoaded, Wrong: $wrongSoundLoaded, Achievement: $achievementSoundLoaded")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sounds", e)
                // Set flags to false on error
                correctSoundLoaded = false
                wrongSoundLoaded = false
                achievementSoundLoaded = false
            }
        }
    }

    private fun initializeVibrator() {
        try {
            vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            Log.d(TAG, "Vibrator initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing vibrator", e)
            vibrator = null
        }
    }

    private fun loadSettings() {
        try {
            val prefs = context.getSharedPreferences("SoundSettings", Context.MODE_PRIVATE)
            soundEnabled = prefs.getBoolean("sound_enabled", true)
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true)
            Log.d(TAG, "Settings loaded - Sound: $soundEnabled, Vibration: $vibrationEnabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading settings", e)
            // Use default values
            soundEnabled = true
            vibrationEnabled = true
        }
    }

    fun saveSettings() {
        try {
            val prefs = context.getSharedPreferences("SoundSettings", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("sound_enabled", soundEnabled)
                .putBoolean("vibration_enabled", vibrationEnabled)
                .apply()
            Log.d(TAG, "Settings saved - Sound: $soundEnabled, Vibration: $vibrationEnabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving settings", e)
        }
    }

    private fun loadSoundFromAssets(filePath: String): Int {
        return try {
            val assetManager = context.assets
            val afd = assetManager.openFd(filePath)
            val soundId = soundPool?.load(afd, 1) ?: -1
            afd.close()
            soundId
        } catch (e: Exception) {
            Log.w(TAG, "Could not load sound from assets: $filePath", e)
            -1
        }
    }
    
    fun playSwipeSound() {
        // Sound effects disabled to avoid SoundPool errors
        Log.d(TAG, "Swipe sound skipped - sound effects disabled")
    }

    fun playCorrectSound() {
        if (soundEnabled) {
            if (isInitialized && correctSoundLoaded && correctSoundId != -1) {
                try {
                    soundPool?.play(correctSoundId, 1.0f, 1.0f, DEFAULT_PRIORITY, 0, 1.0f)
                    Log.d(TAG, "Played correct answer sound effect from assets")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not play correct sound effect from assets, falling back to system tones", e)
                    playSystemCorrectSound()
                }
            } else {
                Log.w(TAG, "Correct sound not loaded, using system tones")
                playSystemCorrectSound()
            }
        } else {
            Log.d(TAG, "Sound disabled - skipping correct sound")
        }
    }

    fun playWrongSound() {
        // Play sound if enabled
        if (soundEnabled) {
            if (isInitialized && wrongSoundLoaded && wrongSoundId != -1) {
                try {
                    soundPool?.play(wrongSoundId, 1.0f, 1.0f, DEFAULT_PRIORITY, 0, 1.0f)
                    Log.d(TAG, "Played wrong answer sound effect from assets")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not play wrong sound effect from assets, falling back to system tones", e)
                    playSystemWrongSound()
                }
            } else {
                Log.w(TAG, "Wrong sound not loaded, using system tones")
                playSystemWrongSound()
            }
        } else {
            Log.d(TAG, "Sound disabled - skipping wrong sound")
        }

        // Play vibration if enabled
        if (vibrationEnabled) {
            playWrongAnswerVibration()
        } else {
            Log.d(TAG, "Vibration disabled - skipping haptic feedback")
        }
    }

    fun playAchievementSound() {
        if (isInitialized && achievementSoundLoaded && achievementSoundId != -1) {
            try {
                soundPool?.play(achievementSoundId, 1.0f, 1.0f, DEFAULT_PRIORITY, 0, 1.0f)
                Log.d(TAG, "Played achievement sound effect from assets")
            } catch (e: Exception) {
                Log.w(TAG, "Could not play achievement sound effect from assets", e)
            }
        } else {
            Log.w(TAG, "Achievement sound not loaded")
        }
    }

    private fun playSystemCorrectSound() {
        try {
            val toneGenerator = android.media.ToneGenerator(
                android.media.AudioManager.STREAM_MUSIC,
                100
            )
            toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 150)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 150)
            }, 200)
        } catch (e: Exception) {
            Log.w(TAG, "Could not play system correct sound", e)
        }
    }

    private fun playSystemWrongSound() {
        try {
            val toneGenerator = android.media.ToneGenerator(
                android.media.AudioManager.STREAM_MUSIC,
                80
            )
            toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_NACK, 300)
        } catch (e: Exception) {
            Log.w(TAG, "Could not play system wrong sound", e)
        }
    }

    private fun playWrongAnswerVibration() {
        try {
            vibrator?.let { vib ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // Create a medium-loud vibration pattern for wrong answers
                    // Pattern: vibrate 200ms, pause 50ms, vibrate 100ms - distinctive error pattern
                    val vibrationPattern = longArrayOf(0, 200, 50, 100)
                    val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1)
                    vib.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(200) // Fallback for older Android versions
                }
                Log.d(TAG, "Played wrong answer vibration")
            } ?: Log.w(TAG, "Vibrator not available")
        } catch (e: Exception) {
            Log.w(TAG, "Could not play vibration", e)
        }
    }

    // Settings management methods
    fun isSoundEnabled(): Boolean = soundEnabled
    fun isVibrationEnabled(): Boolean = vibrationEnabled

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
        saveSettings()
        Log.d(TAG, "Sound enabled set to: $enabled")
    }

    fun setVibrationEnabled(enabled: Boolean) {
        vibrationEnabled = enabled
        saveSettings()
        Log.d(TAG, "Vibration enabled set to: $enabled")
    }

    fun playButtonSound() {
        // Sound effects disabled to avoid SoundPool errors
        Log.d(TAG, "Button sound skipped - sound effects disabled")
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
    
    fun speakPhrase(phrase: FlashcardEntry, language: String = "both") {
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