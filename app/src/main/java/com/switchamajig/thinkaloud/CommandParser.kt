package com.switchamajig.thinkaloud

class Command (val executor: (_args: String?) -> Unit, val args: String?) {
    fun execute() {
        executor(args)
    }
}

class CommandParser (val mainActivity: MainActivity){
    val executorMap = HashMap<String, (_args: String?) -> Unit>()
    val executorRoughMap = HashMap<String, (_args: String?) -> Unit>()
    val argMap = HashMap<String, String>()

    fun parseResults(recoResults: List<String>) : Command? {
        if (recoResults.size < 1) return null
        for (result in recoResults) {
            val command = parseResult(result, false)
            if (command != null) return command
        }
        for (result in recoResults) {
            val command = parseResult(result, true)
            if (command != null) return command
        }
        return null
    }

    fun parseResult(recoResult : String, trySubstutions: Boolean) : Command? {
        val words = recoResult.split(" ", ignoreCase = true)
        if (words.size < 1) return null
        val arg = if (words.size > 1) words[words.size - 1] else null

        val executor = if (trySubstutions) executorRoughMap.get(words[0]) else executorMap.get(words[0])
        return if (executor != null) Command(executor, arg) else null
    }

    init {
        executorMap.put("create", mainActivity::create)
        executorMap.put("put", mainActivity::put)
        executorMap.put("read", mainActivity::read)
        executorMap.put("for", mainActivity::iterate)
        executorMap.put("four", mainActivity::iterate)

        executorRoughMap.putAll(executorMap)
        executorRoughMap.put("quick", mainActivity::create)
        executorRoughMap.put("quit", mainActivity::create)
        executorRoughMap.put("great", mainActivity::create)

        executorMap.put("reed", mainActivity::read)
        executorMap.put("weed", mainActivity::read)
        executorMap.put("we", mainActivity::read)
        executorMap.put("we've", mainActivity::read)

        argMap.put("tense", "tens")
        argMap.put("tents", "tens")
        argMap.put("tenths", "tens")
        argMap.put("once", "ones")
        argMap.put("wands", "ones")
    }
}