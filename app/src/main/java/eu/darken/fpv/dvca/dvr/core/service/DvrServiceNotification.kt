package eu.darken.fpv.dvca.dvr.core.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.BuildVersionWrap
import eu.darken.fpv.dvca.dvr.core.DvrController
import eu.darken.fpv.dvca.main.ui.MainActivity
import javax.inject.Inject

@Reusable
class DvrServiceNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channel: DvrNotificationChannel,
) {

    private val appIntent: PendingIntent by lazy {
        val flags = if (BuildVersionWrap.hasAPILevel(23)) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            flags
        )
    }

    private val notificationBuilder: NotificationCompat.Builder by lazy {
        channel.setup()
        NotificationCompat.Builder(context, channel.channelId).apply {
            setSmallIcon(R.drawable.ic_video_file_24)
            setContentTitle("DVR Service")
            setContentText("// TODO")
            setContentIntent(appIntent)
        }
    }

    val notification: Notification
        get() = notificationBuilder.build()

    fun updateDvrInfo(recordings: Set<DvrController.DvrRecording>) {
        val activeRecordings = recordings.count { !it.isFinished }
        notificationBuilder.setContentText("$activeRecordings active recordings")
    }

}

