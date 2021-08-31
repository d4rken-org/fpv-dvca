package eu.darken.fpv.dvca.gear.goggles

import com.google.android.exoplayer2.source.MediaSource
import eu.darken.fpv.dvca.gear.Gear
import eu.darken.fpv.dvca.gear.goggles.common.HubSource
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import kotlinx.coroutines.flow.Flow

interface Goggles : Gear {

    val videoFeed: Flow<VideoFeed>

    interface VideoFeed {
        val source: HubSource

        val exoMediaSource: MediaSource

        val usbReadMode: HWEndpoint.ReadMode

        val videoUsbReadMbs: Double
        val videoBufferReadMbs: Double

        val deviceIdentifier: String

    }
}