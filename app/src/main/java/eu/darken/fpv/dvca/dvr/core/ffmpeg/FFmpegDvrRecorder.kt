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
import okio.Sink
import okio.appendingSink
import java.io.File
import javax.inject.Inject

class FFmpegDvrRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) : DvrRecorder {

    override fun record(safUri: Uri): DvrRecorder.Session {
        val inPipe = FFmpegKitConfig.registerNewFFmpegPipe(context)
        val outFile = FFmpegKitConfig.getSafParameterForWrite(context, safUri)
        i(TAG) { "Starting FFmpeg: IN=$inPipe OUT=$outFile" }

        val ffmpegSession = FFmpegKit.executeAsync(
            "-fflags nobuffer -f:v h264 -probesize 8192 -i $inPipe -f mpegts -vcodec copy -preset ultrafast $outFile"
        ) {
            v(TAG) { "Session completed:\n$it " }
        }

        return object : DvrRecorder.Session {
            override val sink: Sink = File(inPipe).appendingSink()
            override fun cancel() {
                i(TAG) { "Canceling session" }
                v(TAG) { "Cancelled sesion:\n$ffmpegSession" }
                ffmpegSession.cancel()
            }
        }
    }

    companion object {
        private val TAG = App.logTag("DVR", "Recorder", "FFmpeg")
    }
}