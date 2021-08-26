package eu.darken.fpv.dvca.dvr.core

import android.net.Uri
import okio.Sink

interface DvrRecorder {

    fun record(storagePath: Uri): Session

    interface Session {
        val sink: Sink

        fun cancel()
    }

}