package com.switchamajig.thinkaloud

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener

/**
 * Wrap tts to ecapsulate details and log output
 */

interface RecoStarter {
    fun startReco()
}

class TtsController(val context: Context, val logger: Logger, val recoStarter: RecoStarter) {
    val TAG = "TTSController"
    val START_RECO_ID = "START_RECO_ID"
    val OTHER_ID = "OTHER_ID"

    var ttsInitialized = false
    val tts: TextToSpeech = TextToSpeech(context, {i -> ttsInitialized = (i != TextToSpeech.ERROR)})

    init {
        tts.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onDone(id: String?) {
                if (START_RECO_ID.equals(id)) recoStarter.startReco()
            }

            override fun onError(utteranceId: String) {
                logger.log(TAG, "tts error")
            }

            override fun onStart(utteranceId: String) {
            }
        })
    }

    fun speak(text: CharSequence, startRecoWhenDone: Boolean) {
        val id = if(startRecoWhenDone) START_RECO_ID else OTHER_ID
        logger.log(TAG, text.toString() + ":" + id)
        if (!ttsInitialized) {
            logger.log(TAG, "TTS not initialized")
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, id)
    }
}