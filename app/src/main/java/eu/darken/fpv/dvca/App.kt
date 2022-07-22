package eu.darken.fpv.dvca

import android.app.Application
import com.getkeepsafe.relinker.ReLinker
import dagger.hilt.android.HiltAndroidApp
import eu.darken.fpv.dvca.bugreporting.BugReporter
import eu.darken.fpv.dvca.dvr.core.DvrController
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var bugReporter: BugReporter
    @Inject lateinit var dvrController: DvrController

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        ReLinker
            .log { message -> Timber.tag(TAG).d("ReLinker: %s", message) }
            .loadLibrary(this, "bugsnag-plugin-android-anr")

        bugReporter.setup()

        dvrController.setup()

        Timber.tag(TAG).d("onCreate() done!")
    }

    companion object {
        private const val TAG = "DVCA"

        fun logTag(vararg tags: String): String {
            val sb = StringBuilder("$TAG:")
            for (i in tags.indices) {
                sb.append(tags[i])
                if (i < tags.size - 1) sb.append(":")
            }
            return sb.toString()
        }
    }
}
