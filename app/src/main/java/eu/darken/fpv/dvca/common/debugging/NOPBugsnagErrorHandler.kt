package eu.darken.fpv.dvca.common.debugging

import com.bugsnag.android.Event
import com.bugsnag.android.OnErrorCallback
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NOPBugsnagErrorHandler @Inject constructor() : OnErrorCallback {

    override fun onError(event: Event): Boolean {
        Timber.w(event.originalError, "Skipping bugtracking due to user opt-out.")
        return false
    }

}