package eu.darken.fpv.dvca

import android.app.Application
import com.getkeepsafe.relinker.ReLinker
import dagger.hilt.android.HiltAndroidApp
import eu.darken.fpv.dvca.bugreporting.BugReporter
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
open class App : Application() {

    @Inject lateinit var bugReporter: BugReporter

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        ReLinker
            .log { message -> Timber.tag(TAG).d("ReLinker: %s", message) }
            .loadLibrary(this, "bugsnag-plugin-android-anr")

        bugReporter.setup()

        Timber.tag(TAG).d("onCreate() done!")
    }

    companion object {
        internal val TAG = logTag("DVCA")

        fun logTag(vararg tags: String): String {
            val sb = StringBuilder()
            for (i in tags.indices) {
                sb.append(tags[i])
                if (i < tags.size - 1) sb.append(":")
            }
            return sb.toString()
        }
    }
}
