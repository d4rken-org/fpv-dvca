package eu.darken.fpv.dvca.dvr.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.common.BuildVersionWrap
import timber.log.Timber
import javax.inject.Inject


@Reusable
class DvrNotificationChannel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val manager: NotificationManager,
) {

    val channelId: String = "${context.packageName}.notification.channel.dvr"

    fun setup() {
        if (!BuildVersionWrap.hasAPILevel(26)) {
            Timber.v("Notification channel not necessary < API26")
            return
        }

        val serviceChannel = NotificationChannel(
            channelId,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )


        manager.createNotificationChannel(serviceChannel)
        Timber.v("Notification channel was setup().")
    }

}