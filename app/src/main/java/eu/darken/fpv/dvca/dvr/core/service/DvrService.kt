package eu.darken.fpv.dvca.dvr.core.service

import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.androidstarter.common.logging.v
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.coroutine.DispatcherProvider
import eu.darken.fpv.dvca.common.smart.SmartService
import eu.darken.fpv.dvca.dvr.core.DvrController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class DvrService : SmartService() {

    @Inject lateinit var serviceNotification: DvrServiceNotification
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var dvrController: DvrController
    @Inject lateinit var dispatcherProvider: DispatcherProvider

    private val serviceScope by lazy {
        CoroutineScope(SupervisorJob() + dispatcherProvider.Default)
    }

    override fun onCreate() {
        isRunning = true
        super.onCreate()
        startForeground(NOTIFICATION_ID, serviceNotification.notification)

        dvrController.recordings
            .onEach { recordings ->
                v(TAG) { "Updating notifications" }
                serviceNotification.updateDvrInfo(recordings)
                notificationManager.notify(NOTIFICATION_ID, serviceNotification.notification)
                if (recordings.all { it.isFinished }) {
                    stopSelf()
                }
            }
            .catch { v(TAG, it) { "Notification updater cancelled." } }
            .launchIn(serviceScope)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        isRunning = false
        serviceScope.cancel("Service is being destroyed.")
        super.onDestroy()
    }

    companion object {
        internal var isRunning = false
        private val TAG = App.logTag("DVR", "Service")
        private const val NOTIFICATION_ID = 3
    }
}

