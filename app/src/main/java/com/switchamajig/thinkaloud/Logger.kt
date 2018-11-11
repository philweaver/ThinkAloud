package com.switchamajig.thinkaloud

import android.content.Context
import android.util.Log
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Read the directory with:
 * adb shell "run-as com.switchamajig.thinkaloud ls /data/data/com.switchamajig.thinkaloud/files"
 *
 * Get a file with
 * adb shell "run-as com.switchamajig.thinkaloud cat /data/data/com.switchamajig.thinkaloud/files/18-11-10--16:02:14"
 *
 */
class Logger(val context: Context) {
    val lock = Any()
    var fileOutputStream = openOutputStream(context)
    var closed = false

    fun start() {
        synchronized(lock, {
            if (closed) {
                fileOutputStream = openOutputStream(context)
                closed = false
            }
        })
    }

    fun stop() {
        synchronized(lock, {
            if (!closed) {
                fileOutputStream.close()
                closed = true
            }
        })
    }

    fun log(tag: CharSequence, text: CharSequence) {
        Log.v(tag.toString(), text.toString())
        synchronized(lock, {
            if (!closed) {
                fileOutputStream.write(tag.toString().toByteArray())
                fileOutputStream.write(":".toByteArray())
                fileOutputStream.write(text.toString().toByteArray())
                fileOutputStream.write("\n".toByteArray())
            }
        })
    }

    private fun openOutputStream(localContext: Context) : FileOutputStream {
        return localContext.openFileOutput(SimpleDateFormat("yy-MM-dd--HH:mm:ss").format(Date()), Context.MODE_WORLD_READABLE)
    }
}