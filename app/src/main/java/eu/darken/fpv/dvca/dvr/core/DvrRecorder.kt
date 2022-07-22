package eu.darken.fpv.dvca.dvr.core

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import okio.Sink
import kotlin.time.Duration

interface DvrRecorder {

    fun record(storagePath: Uri): Session

    interface Session {
        val sink: Sink

        val stats: Flow<Stats>

        data class Stats(
            val length: Duration,
            val size: Long,
        )

        fun cancel()
    }

}