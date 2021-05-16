package eu.darken.fpv.dvca.gear.goggles.djifpv

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.usb.connection.DVCAConnection
import eu.darken.fpv.dvca.usb.connection.io.AndroidUSBInputStream2
import eu.darken.fpv.dvca.usb.connection.io.UsbDataSource
import okio.BufferedSink
import okio.BufferedSource
import timber.log.Timber

class FpvGogglesV1VideoFeed(
    private val connection: DVCAConnection,
) : Goggles.VideoFeed, DataSource {
    private val intf = connection.getInterface(3)
    private val cmdEndpoint = intf.getEndpoint(0)
    private val videoEndpoint = intf.getEndpoint(1)
    private var cmdSink: BufferedSink? = null
    private var videoSource: BufferedSource? = null

    override val exoDataSource: DataSource = this

    override fun getUri(): Uri? = Uri.EMPTY

    override fun addTransferListener(transferListener: TransferListener) {
        // NOOP
    }

    override fun open(dataSpec: DataSpec): Long {
        Timber.tag(TAG).v("open(dataSpec=%s) this=%s", dataSpec, this)

        intf.claim(forced = true)

        var newCmdSink = cmdEndpoint.sink()
        var newVideoSource = videoEndpoint.source()

        try {
            Timber.tag(TAG).v("Waiting for video feed to start.")
            newVideoSource.readByteArray(64)
            Timber.tag(TAG).v("Waiting for video feed has started.")
        } catch (e: Exception) {
            // java.io.InterruptedIOException: timeout ?
            Timber.tag(TAG).v(e, "Failed to open video feed, needs magic packet.")

            newCmdSink.close()
            newVideoSource.close()

            intf.apply {
                release()
                claim(forced = true)
            }

            newCmdSink = cmdEndpoint.sink().apply {
                Timber.tag(TAG).d("Writing magic packet.")
                write(MAGIC_FPVOUT_PACKET)
                flush()
            }

            newVideoSource = videoEndpoint.source()

            Timber.tag(TAG).d("Video feed restart attempt done.")
        }


        this.cmdSink = newCmdSink
        this.videoSource = newVideoSource

        return if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else {
            C.LENGTH_UNSET.toLong()
        }
    }

    override fun read(target: ByteArray, offset: Int, length: Int): Int {
        return videoSource!!.read(target, offset, length)
    }

    override fun close() {
        Timber.tag(TAG).d("close() this=%s", this)
        cmdSink?.close()
        cmdSink = null
        videoSource?.close()
        videoSource = null

        intf.release()
    }

    companion object {
        private val TAG = App.logTag("Gear", "FpvGogglesV1", "VideoFeed")

        private val MAGIC_FPVOUT_PACKET = "RMVT".toByteArray()

        val usbReadRate: Double
            get() = when {
                UsbDataSource.usbReadRate != 0.0 -> UsbDataSource.usbReadRate
                else -> AndroidUSBInputStream2.usbReadRate
            }

        val bufferReadRate: Double
            get() = when {
                UsbDataSource.bufferReadRate != 0.0 -> UsbDataSource.bufferReadRate
                else -> AndroidUSBInputStream2.bufferReadRate
            }
    }
}