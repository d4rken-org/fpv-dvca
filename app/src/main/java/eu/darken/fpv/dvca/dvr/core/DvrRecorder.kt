package eu.darken.fpv.dvca.dvr.core

import android.net.Uri
import okio.Source

interface DvrRecorder {

    fun record(source: Source, target: Uri): Session

    interface Session {

        fun stop()

    }

}