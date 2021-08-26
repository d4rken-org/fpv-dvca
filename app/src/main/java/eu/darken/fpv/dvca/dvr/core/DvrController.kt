package eu.darken.fpv.dvca.dvr.core

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.google.android.exoplayer2.util.MimeTypes
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.androidstarter.common.logging.d
import eu.darken.androidstarter.common.logging.i
import eu.darken.androidstarter.common.logging.v
import eu.darken.androidstarter.common.logging.w
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.coroutine.AppScope
import eu.darken.fpv.dvca.common.flow.HotDataFlow
import eu.darken.fpv.dvca.dvr.GeneralDvrSettings
import eu.darken.fpv.dvca.dvr.core.ffmpeg.FFmpegDvrRecorder
import eu.darken.fpv.dvca.dvr.core.service.DvrServiceController
import eu.darken.fpv.dvca.gear.goggles.Goggles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import okio.buffer
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DvrController @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val serviceController: DvrServiceController,
    private val fFmpegDvrRecorder: FFmpegDvrRecorder,
    private val dvrSettings: GeneralDvrSettings,
) {

    private val internalData: HotDataFlow<Set<DvrRecording>> = HotDataFlow(
        scope = appScope
    ) {
        emptySet()
    }
    val recordings: Flow<Set<DvrRecording>> = internalData.data

    fun setup() {
        internalData.data
            .onEach {
                d(TAG) { "Current active recordings: $it" }
                if (it.isNotEmpty() && !serviceController.isRunning) {
                    serviceController.startService()
                }
            }
            .launchIn(appScope)
    }


    suspend fun toggle(goggle: Goggles): DvrRecording {
        var affected: DvrRecording? = null
        internalData.updateBlocking {
            val existing = singleOrNull { it.goggle == goggle }
            if (existing != null) {
                i(TAG) { "Stopping DvrRecording: $existing" }
                existing.feedJob.cancel()

                // TODO send notification for result

                affected = existing
                this.minus(existing)
            } else {
                val target = dvrSettings.dvrStoragePath.value!!
                val videoFile = DocumentFile.fromTreeUri(context, target)!!
                    .createFile(MimeTypes.VIDEO_H264, "DVCA-${System.currentTimeMillis()}.mp4")!!

                var currentDvrSession: DvrRecorder.Session? = null

                // While the recording is running, we keep collecting, otherwise the feed is stopped.
                val feedJob = goggle.videoFeed
                    .onEach { feed ->
                        v(TAG) { "Received DVR feed for $goggle: $feed" }
                        currentDvrSession?.cancel()

                        val dvrSession = fFmpegDvrRecorder.record(videoFile.uri)
                        feed.source.addSideSink(dvrSession.sink.buffer())
                        currentDvrSession = dvrSession
                    }
                    .onCompletion { d(TAG) { "Feed job was cancelled: $it" } }
                    .catch { w(TAG, it) { "DVR feed failed for $goggle" } }
                    .launchIn(goggle.gearScope)

                val newRecording = DvrRecording(
                    goggle = goggle,
                    feedJob = feedJob,
                )

                i(TAG) { "Started DvrRecording: $newRecording" }
                affected = newRecording
                this.plus(newRecording)
            }
        }

        v(TAG) { "toggled: $affected" }
        return affected!!
    }

    data class DvrRecording(
        val goggle: Goggles,
        val feedJob: Job,
        val recordingId: String = UUID.randomUUID().toString(),
        val startedAt: Instant = Instant.now(),
    ) {
        val isFinished = feedJob.isCompleted
    }


    companion object {
        private val TAG = App.logTag("DVR", "Controller")
    }
}