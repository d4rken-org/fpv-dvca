package eu.darken.fpv.dvca.dvr.core.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.common.BuildVersionWrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DvrServiceController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val intent by lazy {
        Intent(context, DvrService::class.java)
    }

    val isRunning: Boolean
        get() = DvrService.isRunning

    fun startService() {
        if (BuildVersionWrap.hasAPILevel(26)) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopService() {
        context.stopService(intent)
    }
}