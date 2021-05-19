package eu.darken.fpv.dvca.gear.goggles.djifpv

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.usb.connection.HWConnection
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import eu.darken.fpv.dvca.videofeed.core.player.exo.H264Extractor2
import okio.BufferedSink
import okio.BufferedSource
import timber.log.Timber

class FpvGogglesV1VideoFeed(
    private val connection: HWConnection,
    override val usbReadMode: HWEndpoint.ReadMode,
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

            intf.claim(forced = true)

            val cmdSink = cmdEndpoint.sink()
            val videoSource = videoEndpoint.source(readMode = usbReadMode)

            try {
                Timber.tag(TAG).v("Waiting for video feed to start.")
                val readBytes = videoSource.readByteArray(DEFAULT_FRAME_SIZE)
                Timber.tag(TAG).v("Video feed has started, we got %d bytes", readBytes.size)
            } catch (e: Exception) {
                // java.io.InterruptedIOException: timeout ?
                Timber.tag(TAG).v(e, "Failed to open video feed, needs magic packet.")

                cmdSink.apply {
                    Timber.tag(TAG).d("Writing new magic packet.")
                    write(MAGIC_FPVOUT_PACKET)
                    flush()
                }
            }

            this@FpvGogglesV1VideoFeed.cmdSink = cmdSink
            this@FpvGogglesV1VideoFeed.videoSource = videoSource

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

    override val exoMediaSource: MediaSource = run {
        val dataSourceFactory: DataSource.Factory = DataSource.Factory { exoDataSource }

        val extractorPreset = when (usbReadMode) {
            HWEndpoint.ReadMode.UNBUFFERED_DIRECT -> LONG_SAMPLING
            HWEndpoint.ReadMode.BUFFERED_BLOCKING -> MEDIUM_SAMPLING
            HWEndpoint.ReadMode.BUFFERED_NOT_BLOCKING -> SHORT_SAMPLING
        }
        val extractorFactory = ExtractorsFactory {
            arrayOf(H264Extractor2(extractorPreset))
        }
        ProgressiveMediaSource.Factory(
            dataSourceFactory,
            extractorFactory
        ).createMediaSource(MediaItem.fromUri(Uri.EMPTY))
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

        /**
         * Some thoughts
         * In buffered USB mode, stats showed that at 131072 framesize and 1000ms sampletime we got 30fps in buffer
         * Halfing the sample time to 500ms yielded ~60fps in buffer
         * Random thought: Max frame size is correlated to sample time, higher sample time, smaller frame buffer?
         */

        internal const val DEFAULT_FRAME_SIZE = 131072L
        private const val DEFAULT_SAMPLE_TIME = 500L

        /**
         * Works nice with direct usb reader
         */
        private val LONG_SAMPLING = H264Extractor2.Preset(
            sampleTime = DEFAULT_SAMPLE_TIME * 4,
            frameBufferSize = (DEFAULT_FRAME_SIZE * 0.25).toLong(),
        )

        /**
         * Works nice with pipe based usb reader
         */
        private val MEDIUM_SAMPLING = H264Extractor2.Preset(
            frameBufferSize = DEFAULT_FRAME_SIZE,
            sampleTime = DEFAULT_SAMPLE_TIME,
        )

        /**
         * Works nice with ring buffer usb reader
         */
        private val SHORT_SAMPLING = H264Extractor2.Preset(
            frameBufferSize = DEFAULT_FRAME_SIZE * 2,
            sampleTime = DEFAULT_SAMPLE_TIME / 2,
        )
    }
}