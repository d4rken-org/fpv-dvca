package eu.darken.fpv.dvca.gear.goggles

import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import eu.darken.fpv.dvca.gear.Gear
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import kotlinx.coroutines.flow.Flow
import okio.Source

interface Goggles : Gear {

    val videoFeed: Flow<VideoFeed>

    interface VideoFeed {
        val source: Source
        val exoDataSource: DataSource
        val exoMediaSource: MediaSource

        val usbReadMode: HWEndpoint.ReadMode

        val videoUsbReadMbs: Double
        val videoBufferReadMbs: Double

        val deviceIdentifier: String

        fun open()

        fun close()
    }
}