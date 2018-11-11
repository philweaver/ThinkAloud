package com.switchamajig.thinkaloud

class Command (val executor: (_args: String?) -> Unit, val args: String?) {
    fun execute() {
        executor(args)
    }
}

class CommandParser (val commandProcessor: CommandProcessor, val logger: Logger) {
    val TAG = "CommandParser"
    val executorMap = HashMap<String, (_args: String?) -> Unit>()
    val executorRoughMap = HashMap<String, (_args: String?) -> Unit>()
    val argMap = HashMap<String, String>()

    fun parseResults(recoResults: List<String>) : Command? {
        if (recoResults.size < 1) {
            logger.log(TAG, "Empty reco results")
            return null
        }

        for (result in recoResults) {
            val command = parseResult(result, false)
            if (command != null) return command
        }
        for (result in recoResults) {
            val command = parseResult(result, true)
            if (command != null) return command
        }
        logger.log(TAG, "No command recognized")
        return null
    }

    private fun parseResult(recoResult : String, trySubstutions: Boolean) : Command? {
        val words = recoResult.split(" ", ignoreCase = true, limit = 2)
        if (words.size < 1) return null
        val arg = if (words.size > 1) words[words.size - 1] else null

        val executor = if (trySubstutions) executorRoughMap.get(words[0]) else executorMap.get(words[0])
        if (executor != null) {
            logger.log(TAG, "Command: " + words[0] + if (arg != null) " - " + arg else "")
        }
        return if (executor != null) Command(executor, arg) else null
    }

    init {
        executorMap.put("create", commandProcessor::create)
        executorMap.put("put", commandProcessor::put)
        executorMap.put("read", commandProcessor::read)
        executorMap.put("for", commandProcessor::iterate)
        executorMap.put("four", commandProcessor::iterate)

        executorRoughMap.putAll(executorMap)
        executorRoughMap.put("quick", commandProcessor::create)
        executorRoughMap.put("quit", commandProcessor::create)
        executorRoughMap.put("great", commandProcessor::create)

        executorMap.put("reed", commandProcessor::read)
        executorMap.put("weed", commandProcessor::read)
        executorMap.put("we", commandProcessor::read)
        executorMap.put("we've", commandProcessor::read)

        argMap.put("tense", "tens")
        argMap.put("tents", "tens")
        argMap.put("tenths", "tens")
        argMap.put("once", "ones")
        argMap.put("wands", "ones")
    }
}