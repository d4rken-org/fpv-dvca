package testhelper


import android.util.Log

import timber.log.Timber

class JUnitTree : Timber.DebugTree {
    private val minlogLevel: Int

    constructor() {
        minlogLevel = Log.VERBOSE
    }

    constructor(minlogLevel: Int) {
        this.minlogLevel = minlogLevel
    }

    private fun priorityToString(priority: Int): String = when (priority) {
        Log.ERROR -> "E"
        Log.WARN -> "W"
        Log.INFO -> "I"
        Log.DEBUG -> "D"
        Log.VERBOSE -> "V"
        else -> priority.toString()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < minlogLevel) return
        println(System.currentTimeMillis().toString() + " " + priorityToString(priority) + "/" + tag + ": " + message)
    }
}