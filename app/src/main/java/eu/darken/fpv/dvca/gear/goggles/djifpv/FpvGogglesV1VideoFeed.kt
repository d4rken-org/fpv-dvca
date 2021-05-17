package eu.darken.fpv.dvca.gear.goggles.djifpv

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.usb.connection.HWConnection
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import okio.BufferedSink
import okio.BufferedSource
import timber.log.Timber

class FpvGogglesV1VideoFeed(
    private val connection: HWConnection,
    private val usbReadMode: HWEndpoint.ReadMode,
) : Goggles.VideoFeed {
    private val intf = connection.getInterface(3)
    private val cmdEndpoint = intf.getEndpoint(0)
    private val videoEndpoint = intf.getEndpoint(1)
    private var cmdSink: BufferedSink? = null
    private var videoSource: BufferedSource? = null

    override val exoDataSource: DataSource = object : DataSource {
        override fun getUri(): Uri? = Uri.EMPTY

        override fun addTransferListener(transferListener: TransferListener) {}

        override fun open(dataSpec: DataSpec): Long {
            Timber.tag(TAG).v("open(dataSpec=%s) this=%s", dataSpec, this)

            intf.claim(forced = false)

            var newCmdSink = cmdEndpoint.sink()
            var newVideoSource = videoEndpoint.source(readMode = usbReadMode)

            try {
                Timber.tag(TAG).v("Waiting for video feed to start.")
                newCmdSink.apply {
                    Timber.tag(TAG).d("Writing magic packet.")
                    write(MAGIC_FPVOUT_PACKET)
                    flush()
                }
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
                    Timber.tag(TAG).d("Writing new magic packet.")
                    write(MAGIC_FPVOUT_PACKET)
                    flush()
                }

                Thread.sleep(100)

                newVideoSource = videoEndpoint.source(readMode = usbReadMode)

                Timber.tag(TAG).d("Video feed restart attempt done.")
            }

            cmdSink = newCmdSink
            videoSource = newVideoSource

            return if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
                dataSpec.length
            } else {
                C.LENGTH_UNSET.toLong()
            }
        }

        override fun read(target: ByteArray, offset: Int, length: Int): Int {
            return videoSource?.read(target, offset, length) ?: -1
        }

        override fun close() {
            Timber.tag(TAG).d("close(), source, this=%s", this)
            cmdSink?.close()
            cmdSink = null
            videoSource?.close()
            videoSource = null
        }
    }

    override val videoUsbReadMbs: Double
        get() = videoEndpoint.readStats.usbReadMbs
    override val videoBufferReadMbs: Double
        get() = videoEndpoint.readStats.bufferReadMbs

    override fun close() {
        Timber.tag(TAG).v("close() feed, this=%s", this)
        exoDataSource.close()
        intf.release()
    }

    companion object {
        private val TAG = App.logTag("Gear", "FpvGogglesV1", "VideoFeed")

        private val MAGIC_FPVOUT_PACKET = "RMVT".toByteArray()
    }
}