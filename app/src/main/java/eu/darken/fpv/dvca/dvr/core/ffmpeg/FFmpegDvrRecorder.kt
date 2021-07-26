package eu.darken.fpv.dvca.dvr.core.ffmpeg

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.dvr.core.DvrRecorder
import okio.Source
import okio.buffer
import okio.sink
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.concurrent.thread

class FFmpegDvrRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) : DvrRecorder {

    // TODO
    override fun record(source: Source, safUri: Uri): DvrRecorder.Session {

        val inPipe = FFmpegKitConfig.registerNewFFmpegPipe(context)

        val ffmpegTarget = FFmpegKitConfig.getSafParameterForWrite(context, safUri)

        var recording = true
        thread {
            FFmpegKit.execute("-fflags nobuffer -f:v h264 -probesize 8192 -i $inPipe -f mpegts -vcodec copy -preset ultrafast $ffmpegTarget")
        }
        thread {
            val ffmpegSink = FileOutputStream(inPipe).sink().buffer()
            source.use {
                val sour = it.buffer()
                while (recording) {
                    ffmpegSink.write(sour, 8192)
                }
            }
        }
        return object : DvrRecorder.Session {
            override fun stop() {
                recording = false
            }

        }
    }
}