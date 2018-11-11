package com.switchamajig.thinkaloud

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RecoStarter {
    val TAG = "MainActivity"

    val handler = Handler()

    lateinit var recognizer: SpeechRecognizer
    lateinit var recoIntent: Intent
    lateinit var ttsController: TtsController
    lateinit var commandProcessor: CommandProcessor
    lateinit var parser: CommandParser
    lateinit var logger: Logger


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recoIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        // Not sure if the rest of this is needed
        recoIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recoIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.switchamajig.thinkaloud")
        recoIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5)
        recognizer.setRecognitionListener(recoListener())
    }

    override fun onResume() {
        super.onResume()
        logger = Logger(this)
        ttsController = TtsController(this, logger, this)
        commandProcessor = CommandProcessor(ttsController, this, logger)
        parser = CommandParser(commandProcessor, logger)

        yes.setOnClickListener({_ -> commandProcessor.onYesPressed()})
        no.setOnClickListener({_ -> commandProcessor.onNoPressed()})
    }

    override fun onPause() {
        super.onPause()
        logger.stop()
    }

    override fun startReco() {
        handler.post({
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                recognizer.startListening(recoIntent)
            } else {
                logger.log(TAG, "Speech reco not available")
            }
        })
    }


    inner class recoListener : RecognitionListener {
        override fun onReadyForSpeech(p0: Bundle?) {
            logger.log(TAG, "onReadyForSpeech")
        }

        override fun onRmsChanged(p0: Float) {
            //Log.v(TAG, "onRmsChanged")
        }

        override fun onBufferReceived(p0: ByteArray?) {
            logger.log(TAG, "onBufferReceived")
        }

        override fun onPartialResults(p0: Bundle?) {
            logger.log(TAG, "onPartialResults")
        }

        override fun onEvent(p0: Int, p1: Bundle?) {
            logger.log(TAG, "onEvent")
        }

        override fun onBeginningOfSpeech() {
            logger.log(TAG, "onBeginningOfSpeech")
        }

        override fun onEndOfSpeech() {
            logger.log(TAG, "onEndOfSpeech")
        }

        override fun onError(error: Int) {
            logger.log(TAG, "onError " + error)
        }

        override fun onResults(bundle: Bundle?) {
            if (bundle == null) {
                logger.log(TAG, "onResults: null bundle")
                return
            }
            val recoResults = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (recoResults == null) {
                logger.log(TAG, "onResults: null results")
                return
            }
            if (recoResults.size == 0) {
                logger.log(TAG, "onResults: empty results")
                return
            }
            logger.log(TAG, "" + recoResults.size + " Reco results")
            for (result in recoResults) {
                logger.log(TAG, "Reco result = " + result)
            }
            val command = parser.parseResults(recoResults)
            if (command != null) {
                command.execute()
            } else {
                ttsController.speak("I didn't catch that", false)
            }
        }

    }
}
