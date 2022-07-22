package eu.darken.fpv.dvca.gear.goggles.djifpv

import android.net.Uri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import eu.darken.fpv.dvca.feedplayer.core.common.H264Extractor2
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.usb.connection.HWEndpoint

class ExoMediaSourceFactory(
    private val tag: String
) {
    fun create(
        videoFeed: Goggles.VideoFeed,
        usbReadMode: HWEndpoint.ReadMode,
    ): MediaSource {
        val dataSourceFactory: DataSource.Factory = DataSource.Factory { ExoDataSource(videoFeed, tag) }

        val extractorPreset = when (usbReadMode) {
            HWEndpoint.ReadMode.DIRECT -> SAMPLING_DIRECT
            HWEndpoint.ReadMode.BUFFER_BLOCKING -> SAMPLING_BUFFERED_PIPE
            HWEndpoint.ReadMode.BUFFER_NOTBLOCKING -> SAMPLING_BUFFERED_RING
        }
        val extractorFactory = ExtractorsFactory {
            arrayOf(H264Extractor2(extractorPreset))
        }
        return ProgressiveMediaSource.Factory(
            dataSourceFactory,
            extractorFactory
        ).createMediaSource(MediaItem.fromUri(Uri.EMPTY))
    }

    companion object {
        /**
         * Some thoughts
         * In buffered USB mode, stats showed that at 131072 framesize and 1000ms sampletime we got 30fps in buffer
         * Halfing the sample time to 500ms yielded ~60fps in buffer
         * Random thought: Max frame size is correlated to sample time, higher sample time, smaller frame buffer?
         */

        private const val DEFAULT_FRAMEBUFFER_SIZE = 131072L
        private const val DEFAULT_SAMPLE_TIME = 500L

        /**
         * Works nice with direct usb reader
         */
        private val SAMPLING_DIRECT = H264Extractor2.Preset(
            frameBufferSize = DEFAULT_FRAMEBUFFER_SIZE / 2,
            sampleTime = (DEFAULT_SAMPLE_TIME * 2),
        )

        /**
         * Works nice with pipe based usb reader
         */
        private val SAMPLING_BUFFERED_PIPE = H264Extractor2.Preset(
            frameBufferSize = DEFAULT_FRAMEBUFFER_SIZE / 4,
            sampleTime = DEFAULT_SAMPLE_TIME,
        )

        /**
         * Works nice with ring buffer usb reader
         */
        private val SAMPLING_BUFFERED_RING = H264Extractor2.Preset(
            frameBufferSize = DEFAULT_FRAMEBUFFER_SIZE / 4,
            sampleTime = DEFAULT_SAMPLE_TIME / 2,
        )
    }

}