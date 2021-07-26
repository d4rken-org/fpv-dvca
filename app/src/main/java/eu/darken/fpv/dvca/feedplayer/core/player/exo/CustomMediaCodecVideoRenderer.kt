package eu.darken.fpv.dvca.feedplayer.core.player.exo

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer
import com.google.android.exoplayer2.video.VideoRendererEventListener

class CustomMediaCodecVideoRenderer : MediaCodecVideoRenderer {

    constructor(context: Context, mediaCodecSelector: MediaCodecSelector) :
            super(context, mediaCodecSelector)

    constructor(context: Context, mediaCodecSelector: MediaCodecSelector, allowedJoiningTimeMs: Long) :
            super(context, mediaCodecSelector, allowedJoiningTimeMs)

    constructor(
        context: Context,
        mediaCodecSelector: MediaCodecSelector,
        allowedJoiningTimeMs: Long,
        eventHandler: Handler?,
        eventListener: VideoRendererEventListener?,
        maxDroppedFramesToNotify: Int
    ) : super(context, mediaCodecSelector, allowedJoiningTimeMs, eventHandler, eventListener, maxDroppedFramesToNotify)

    constructor(
        context: Context,
        mediaCodecSelector: MediaCodecSelector,
        allowedJoiningTimeMs: Long,
        enableDecoderFallback: Boolean,
        eventHandler: Handler?,
        eventListener: VideoRendererEventListener?,
        maxDroppedFramesToNotify: Int
    ) : super(
        context,
        mediaCodecSelector,
        allowedJoiningTimeMs,
        enableDecoderFallback,
        eventHandler,
        eventListener,
        maxDroppedFramesToNotify
    )

    constructor(
        context: Context,
        codecAdapterFactory: MediaCodecAdapter.Factory,
        mediaCodecSelector: MediaCodecSelector,
        allowedJoiningTimeMs: Long,
        enableDecoderFallback: Boolean,
        eventHandler: Handler?,
        eventListener: VideoRendererEventListener?,
        maxDroppedFramesToNotify: Int
    ) : super(
        context,
        codecAdapterFactory,
        mediaCodecSelector,
        allowedJoiningTimeMs,
        enableDecoderFallback,
        eventHandler,
        eventListener,
        maxDroppedFramesToNotify
    )

    lateinit var renderInfoListener: (RenderInfo) -> Unit


    private var lastUpdate: Long = 0

    private var drawnFps = 0
    private var droppedFrames = 0
    private var buffersProcessed = 0
    override fun render(positionUs: Long, elapsedRealtimeUs: Long) {
        drawnFps++

        val now = SystemClock.elapsedRealtime()
        if (now - lastUpdate > 1000) {
            lastUpdate = now
            renderInfoListener(
                RenderInfo(
                    frames = drawnFps,
                    buffers = buffersProcessed,
                    dropped = droppedFrames,
                )
            )
            drawnFps = 0
            buffersProcessed = 0
            droppedFrames = 0
        }

        super.render(positionUs, elapsedRealtimeUs)
    }


    override fun onProcessedOutputBuffer(presentationTimeUs: Long) {
        buffersProcessed++
        super.onProcessedOutputBuffer(presentationTimeUs)
    }

    override fun dropOutputBuffer(codec: MediaCodecAdapter, index: Int, presentationTimeUs: Long) {
        droppedFrames++
        super.dropOutputBuffer(codec, index, presentationTimeUs)
    }
}