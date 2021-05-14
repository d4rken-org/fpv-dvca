package eu.darken.fpv.dvca.common.debugging

import android.util.Log
import com.bugsnag.android.Event
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BugsnagTree @Inject constructor() : Timber.Tree() {

    // Adding one to the initial size accounts for the add before remove.
    private val buffer: Deque<String> = ArrayDeque(BUFFER_SIZE + 1)

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        var line = message
        line = System.currentTimeMillis().toString() + " " + priorityToString(priority) + "/" + tag + ": " + line
        synchronized(buffer) {
            buffer.addLast(line)
            if (buffer.size > BUFFER_SIZE) {
                buffer.removeFirst()
            }
        }
    }

    fun injectLog(event: Event) {
        synchronized(buffer) {
            var i = 100
            buffer.forEach { event.addMetadata("Log", String.format(Locale.US, "%03d", i++), it) }
            event.addMetadata("Log", String.format(Locale.US, "%03d", i), Log.getStackTraceString(event.originalError))
        }
    }

    companion object {
        private const val BUFFER_SIZE = 200

        private fun priorityToString(priority: Int): String = when (priority) {
            Log.ERROR -> "E"
            Log.WARN -> "W"
            Log.INFO -> "I"
            Log.DEBUG -> "D"
            Log.VERBOSE -> "V"
            else -> priority.toString()
        }
    }
}
