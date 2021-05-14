package eu.darken.fpv.dvca.bugreporting

import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.InstallId
import eu.darken.fpv.dvca.common.debugging.BugsnagErrorHandler
import eu.darken.fpv.dvca.common.debugging.BugsnagTree
import eu.darken.fpv.dvca.common.debugging.NOPBugsnagErrorHandler
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class BugReporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bugReporterSettings: BugReporterSettings,
    private val installId: InstallId,
    private val bugsnagTree: Provider<BugsnagTree>,
    private val bugsnagErrorHandler: Provider<BugsnagErrorHandler>,
    private val nopBugsnagErrorHandler: Provider<NOPBugsnagErrorHandler>,
) {

    fun setup() {
        val isEnabled = bugReporterSettings.isEnabled.value
        Timber.tag(TAG).d("setup(): isEnabled=$isEnabled")

        try {
            val bugsnagConfig = Configuration.load(context).apply {
                if (bugReporterSettings.isEnabled.value) {
                    Timber.plant(bugsnagTree.get())
                    setUser(installId.id, null, null)
                    autoTrackSessions = true
                    addOnError(bugsnagErrorHandler.get())
                    Timber.tag(TAG).i("Bugsnag setup done!")
                } else {
                    autoTrackSessions = false
                    addOnError(nopBugsnagErrorHandler.get())
                    Timber.tag(TAG).i("Installing Bugsnag NOP error handler due to user opt-out!")
                }
            }

            Bugsnag.start(context, bugsnagConfig)
        } catch (e: IllegalStateException) {
            Timber.tag(TAG).w("Bugsnag API Key not configured.")
        }
    }

    companion object {
        private val TAG = App.logTag("BugReporter")
    }
}