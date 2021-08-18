package eu.darken.fpv.dvca.dvr.core

import eu.darken.androidstarter.common.logging.d
import eu.darken.androidstarter.common.logging.i
import eu.darken.androidstarter.common.logging.v
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.coroutine.AppScope
import eu.darken.fpv.dvca.common.flow.HotDataFlow
import eu.darken.fpv.dvca.dvr.core.ffmpeg.FFmpegDvrRecorder
import eu.darken.fpv.dvca.dvr.core.service.DvrServiceController
import eu.darken.fpv.dvca.gear.goggles.Goggles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DvrController @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val serviceController: DvrServiceController,
    private val fFmpegDvrRecorder: FFmpegDvrRecorder
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
                // TODO send notification for result
                affected = existing
                this.minus(existing)
            } else {
                val newRecording = DvrRecording(
                    goggle = goggle,
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
        val recordingId: String = UUID.randomUUID().toString(),
        val startedAt: Instant = Instant.now(),
        val isFinished: Boolean = false,
    )


    companion object {
        private val TAG = App.logTag("DVR", "Controller")
    }
}