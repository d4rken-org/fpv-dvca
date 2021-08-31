package eu.darken.fpv.dvca.dvr.core.ffmpeg

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.androidstarter.common.logging.i
import eu.darken.androidstarter.common.logging.v
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.dvr.core.DvrRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.Sink
import okio.appendingSink
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration

class FFmpegDvrRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) : DvrRecorder {

    override fun record(storagePath: Uri): DvrRecorder.Session {
        val inPipe = FFmpegKitConfig.registerNewFFmpegPipe(context)
        val outFile = FFmpegKitConfig.getSafParameterForWrite(context, storagePath)
        i(TAG) { "Starting FFmpeg: IN=$inPipe OUT=$outFile" }

        val recordingStart = System.currentTimeMillis()

        val ffmpegSession = FFmpegKit.executeAsync(
            "-fflags nobuffer -f:v h264 -framerate 60 -probesize 10000000 -i $inPipe -f mpegts -vcodec copy -preset ultrafast $outFile"
        ) {
            v(TAG) { "Session completed:\n$it " }
        }

        val targetSink = File(inPipe).appendingSink()

        return object : DvrRecorder.Session {
            override val sink: Sink = targetSink

            override val stats: Flow<DvrRecorder.Session.Stats> = flow {
                while (true) {
                    emit(
                        DvrRecorder.Session.Stats(
                            length = Duration.milliseconds(System.currentTimeMillis() - recordingStart),
                            size = 0L
                        )
                    )
                    delay(1000)
                }
            }

            override fun cancel() {
                i(TAG) { "Cancelling session" }
                v(TAG) { "Closing ffmpegSink:\n$ffmpegSession" }
                targetSink.close()
                v(TAG) { "Cancelled session:\n$ffmpegSession" }
                ffmpegSession.cancel()
            }
        }
    }

    companion object {
        private val TAG = App.logTag("DVR", "Recorder", "FFmpeg")
    }
}