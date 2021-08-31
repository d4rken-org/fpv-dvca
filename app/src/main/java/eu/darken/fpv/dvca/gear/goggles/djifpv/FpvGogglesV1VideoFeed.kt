package eu.darken.fpv.dvca.gear.goggles.djifpv

import com.google.android.exoplayer2.source.MediaSource
import dagger.assisted.AssistedInject
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.gear.goggles.common.HubSource
import eu.darken.fpv.dvca.gear.goggles.common.hub
import eu.darken.fpv.dvca.usb.connection.HWConnection
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import okio.*
import timber.log.Timber


class FpvGogglesV1VideoFeed @AssistedInject constructor(
    private val connection: HWConnection,
    override val usbReadMode: HWEndpoint.ReadMode,
) : Goggles.VideoFeed {
    private val tag = App.logTag("Gear", "FpvGogglesV1", "VideoFeed", hashCode().toString())

    private val intf = connection.getInterface(3)
    private val cmdEndpoint = intf.getEndpoint(0)
    private val videoEndpoint = intf.getEndpoint(1)
    private var cmdSink: BufferedSink? = null
    private var feedSource: HubSource? = null

    override val source: HubSource
        get() = feedSource!!

    override val deviceIdentifier: String
        get() = connection.deviceIdentifier

    override val exoMediaSource: MediaSource = ExoMediaSourceFactory(tag).create(this, usbReadMode)

    override val videoUsbReadMbs: Double
        get() = videoEndpoint.readStats.usbReadMbs
    override val videoBufferReadMbs: Double
        get() = videoEndpoint.readStats.bufferReadMbs

    fun open() {
        intf.claim(forced = false)

        val cmdSink = cmdEndpoint.sink()
        val videoSource = videoEndpoint.source(readMode = usbReadMode)

        try {
            Timber.tag(tag).v("Waiting for video feed to start.")
            val readBytes = videoSource.readByteArray(DEFAULT_FRAMEBUFFER_SIZE)
            Timber.tag(tag).v("Video feed has started, we got %d bytes", readBytes.size)
        } catch (e: Exception) {
            // java.io.InterruptedIOException: timeout ?
            Timber.tag(tag).v(e, "Failed to open video feed, needs magic packet.")

            cmdSink.apply {
                Timber.tag(tag).d("Writing new magic packet.")
                write(MAGIC_FPVOUT_PACKET)
                flush()
            }
        }

        this@FpvGogglesV1VideoFeed.cmdSink = cmdSink
        this@FpvGogglesV1VideoFeed.feedSource = videoSource.hub()
    }

    fun close() {
        Timber.tag(tag).v("close() feed, this=%s", this)
        cmdSink?.close()
        cmdSink = null
        feedSource?.close()
        feedSource = null

        intf.release()
    }

    override fun toString(): String {
        return "VideoFeed(identifier=${connection.deviceIdentifier}, mode=$usbReadMode)"
    }

    companion object {
        private val MAGIC_FPVOUT_PACKET = "RMVT".toByteArray()
        private const val DEFAULT_FRAMEBUFFER_SIZE = 131072L
    }
}