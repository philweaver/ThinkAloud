package com.switchamajig.thinkaloud

import android.util.ArrayMap

class CommandProcessor(val ttsController: TtsController, val recoStarter: RecoStarter, val logger: Logger) {
    val TAG = "CommandProcessor"
    val lists = ArrayMap<String, List<String>>()
    val emptyRunnable = Runnable({})

    var listCounter = 0
    var lastCommand = emptyRunnable
    var currentListName = "start"
    var listIterator : ListIterator<String>? = null
    var currentList = ArrayList<String>();

    init {
        lists.put(currentListName, currentList)
    }

    fun onYesPressed() {
        logger.log(TAG, "onYesPressed")
        lastCommand.run()
        lastCommand = emptyRunnable

        val localListIterator = listIterator
        if (localListIterator != null) {
            if (localListIterator.hasNext()) {
                ttsController.speak(localListIterator.next(), true)
            } else {
                ttsController.speak("End of list", false)
                listIterator = null
                return
            }
        } else {
            recoStarter.startReco()
        }
    }

    fun onNoPressed() {
        lastCommand = emptyRunnable
        ttsController.speak("Canceled", false)
        val localListIterator = listIterator
        if (localListIterator != null) {
            // Canceled command while iterating - retry
            localListIterator.previous()
            onYesPressed()
        }
    }

    fun create(name: String?) {
        listCounter++
        currentListName = if (name != null) name else String.format("%d", listCounter)
        ttsController.speak("Creating list called " + currentListName, false)
        lastCommand = Runnable({
            currentList = ArrayList()
            lists.put(currentListName, currentList)
        })
    }

    fun put(name: String?) {
        if (name == null) {
            ttsController.speak("I didn't hear what you want to put", false)
            return
        }
        ttsController.speak("Putting " + name, false)
        lastCommand = Runnable({
            currentList.add(name)
        })
    }

    fun read(name: String?) {
        val listName = if ((name != null) && lists.contains(name)) name else currentListName
        val listToRead = lists.get(listName)
        if (listToRead == null) return

        ttsController.speak("Reading " + listName, false)
        for (item in listToRead) {
            ttsController.speak(item, false)
        }
    }

    fun iterate(name: String?) {
        if (name == null) {
            ttsController.speak("I didn't hear which list you want to go through", false)
            return
        }
        val listToIterate = lists.get(name)
        if (listToIterate == null) {
            ttsController.speak("Can't go through over " + name + ". It does not exist", false)
            return
        }
        if (listToIterate.size == 0) {
            ttsController.speak("Can't go through over empty list " + name, false)
            return
        }
        ttsController.speak("Doing a command for everything in " + name, false)
        lastCommand = Runnable({
            listIterator = listToIterate.listIterator()
        })
    }

}