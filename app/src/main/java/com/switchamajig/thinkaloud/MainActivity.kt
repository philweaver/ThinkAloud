package com.switchamajig.thinkaloud

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.ArrayMap
import android.util.Log

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    val ITERATING_UTTERANCE_ID = "iterating"
    val lists = ArrayMap<String, List<String>>()
    val parser = CommandParser(this)
    val emptyRunnable = Runnable({})
    val handler = Handler()

    lateinit var recognizer: SpeechRecognizer
    lateinit var recoIntent: Intent
    lateinit var tts: TextToSpeech
    lateinit var currentList: ArrayList<String>

    var ttsInitialized = false
    var listCounter = 0
    var lastCommand = emptyRunnable
    var currentListName = "start"
    var listIterator : ListIterator<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        yes.setOnClickListener({_ -> onYesPressed()})
        no.setOnClickListener({_ -> onNoPressed()})
        recoIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        // Not sure if the rest of this is needed
        recoIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recoIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.switchamajig.thinkaloud")
        recoIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5)
        recognizer.setRecognitionListener(recoListener())

        tts = TextToSpeech(this, {i -> ttsInitialized = (i != TextToSpeech.ERROR)})
        tts.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onDone(id: String?) {
                if (ITERATING_UTTERANCE_ID.equals(id)) handler.post{startReco()}
            }

            override fun onError(utteranceId: String) {
                Log.v(TAG, "tts error")
            }

            override fun onStart(utteranceId: String) {
            }
        })

        currentList = ArrayList()
        lists.put(currentListName, currentList)
    }

    fun onYesPressed() {
        Log.v(TAG, "onYesPressed")
        lastCommand.run()
        lastCommand = emptyRunnable
        if (!ttsInitialized) {
            Log.e(TAG, "TTS not initialized")
            return
        }

        val localListIterator = listIterator
        if (localListIterator != null) {
            if (localListIterator.hasNext()) {
                tts.speak(localListIterator.next(), TextToSpeech.QUEUE_ADD, null, ITERATING_UTTERANCE_ID)
            } else {
                tts.speak("End of list", TextToSpeech.QUEUE_ADD, null, "id")
                listIterator = null
                return
            }
        } else {
            startReco()
        }
    }

    fun startReco() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer.startListening(recoIntent)
        } else {
            Log.e(TAG, "Speech reco not available")
        }
    }

    fun onNoPressed() {
        lastCommand = emptyRunnable
        tts.speak("Canceled", TextToSpeech.QUEUE_ADD, null, "id")
        val localListIterator = listIterator
        if (localListIterator != null) {
            // Canceled command while iterating - retry
            localListIterator.previous()
            onYesPressed()
        }
    }

    fun create(name: String?) {
        listCounter++
        currentListName = if (name != null) name else String.format("List%d", listCounter)
        tts.speak("Creating list called " + currentListName, TextToSpeech.QUEUE_ADD, null, "id")
        lastCommand = Runnable({
            currentList = ArrayList()
            lists.put(currentListName, currentList)
        })
    }

    fun put(name: String?) {
        if (name == null) {
            tts.speak("I didn't hear what you want to put", TextToSpeech.QUEUE_ADD, null, "id")
            return
        }
        tts.speak("Putting " + name, TextToSpeech.QUEUE_ADD, null, "id")
        lastCommand = Runnable({
            currentList.add(name)
        })
    }

    fun read(name: String?) {
        val listName = if ((name != null) && lists.contains(name)) name else currentListName
        val listToRead = lists.get(listName)
        if (listToRead == null) return

        tts.speak("Reading " + listName, TextToSpeech.QUEUE_ADD, null, "command")
        for (item in listToRead) {
            tts.speak(item, TextToSpeech.QUEUE_ADD, null, "id")
            Log.v(TAG, "TTS: " + item)
        }
    }

    fun iterate(name: String?) {
        if (name == null) {
            tts.speak("I didn't hear which list you want to go through", TextToSpeech.QUEUE_ADD, null, "id")
            return
        }
        val listToIterate = lists.get(name)
        if (listToIterate == null) {
            tts.speak("Can't go through over " + name + ". It does not exist", TextToSpeech.QUEUE_ADD, null, "command")
            return
        }
        if (listToIterate.size == 0) {
            tts.speak("Can't go through over empty list " + name, TextToSpeech.QUEUE_ADD, null, "command")
            return
        }
        tts.speak("Doing a command for everything in " + name, TextToSpeech.QUEUE_ADD, null, "command")
        lastCommand = Runnable({
            listIterator = listToIterate.listIterator()
        })
    }

    inner class recoListener : RecognitionListener {
        override fun onReadyForSpeech(p0: Bundle?) {
            Log.v(TAG, "onReadyForSpeech")
        }

        override fun onRmsChanged(p0: Float) {
            //Log.v(TAG, "onRmsChanged")
        }

        override fun onBufferReceived(p0: ByteArray?) {
            Log.v(TAG, "onBufferReceived")
        }

        override fun onPartialResults(p0: Bundle?) {
            Log.v(TAG, "onPartialResults")
        }

        override fun onEvent(p0: Int, p1: Bundle?) {
            Log.v(TAG, "onEvent")
        }

        override fun onBeginningOfSpeech() {
            Log.v(TAG, "onBeginningOfSpeech")
        }

        override fun onEndOfSpeech() {
            Log.v(TAG, "onEndOfSpeech")
        }

        override fun onError(p0: Int) {
            Log.v(TAG, "onError")
        }

        override fun onResults(bundle: Bundle?) {
            if (bundle == null) {
                Log.e(TAG, "onResults: null bundle")
                return
            }
            val recoResults = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (recoResults == null) {
                Log.e(TAG, "onResults: null results")
                return
            }
            if (recoResults.size == 0) {
                Log.e(TAG, "onResults: empty results")
                return
            }
            Log.v(TAG, "" + recoResults.size + " Reco results")
            for (result in recoResults) {
                Log.v(TAG, "Reco result = " + result)
            }
            val command = parser.parseResults(recoResults)
            if (command != null) {
                command.execute()
            } else {
                tts.speak("I didn't catch that", TextToSpeech.QUEUE_ADD, null, "nocommand")
            }
        }

    }
}
