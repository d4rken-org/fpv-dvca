package eu.darken.fpv.dvca.videofeed.core

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import eu.darken.fpv.dvca.App
import timber.log.Timber
import javax.inject.Inject

@FragmentScoped
class FPVFeedPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val loadControl = DefaultLoadControl.Builder().apply {
        setBufferDurationsMs(1, 20000, 1, 1)
//        setTargetBufferBytes(0)
//        setBackBuffer(50, true)
//        setPrioritizeTimeOverSizeThresholds(true)
//        setAllocator(DefaultAllocator(true,512))
    }.build()

    private val renderersFactory = CustomRendererFactory(
        context,
        renderInfoListener = { info ->
            renderInfoListeners.forEach { it(info) }
        }
    )

    private val player = SimpleExoPlayer.Builder(context, renderersFactory).apply {
        setLoadControl(loadControl)
        setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
        setWakeMode(C.WAKE_MODE_LOCAL)
        setUseLazyPreparation(true)
    }.build()

    private val renderInfoListeners = mutableListOf<(RenderInfo) -> Unit>()

    val isPlaying: Boolean
        get() = player.run { isPlaying || isLoading }

    fun start(
        source: DataSource,
        surfaceView: SurfaceView,
        renderInfoListener: (RenderInfo) -> Unit
    ) {
        Timber.tag(TAG).d("start(source=%s, view=%s)", source, surfaceView)

        if (isPlaying) {
            Timber.tag(TAG).w("Already playing? Stopping!")
            stop()
        }

        val mediaSource: MediaSource = run {
            val dataSourceFactory: DataSource.Factory = DataSource.Factory { source }
            ProgressiveMediaSource.Factory(
                dataSourceFactory,
                H264Extractor2.FACTORY
            ).createMediaSource(MediaItem.fromUri(Uri.EMPTY))
        }

        renderInfoListeners.add { info ->
            surfaceView.post { renderInfoListener(info) }
        }

        player.apply {
            setVideoSurfaceView(surfaceView)
            setMediaSource(mediaSource)

            prepare()
            play()
        }
    }

    fun stop() {
        Timber.tag(TAG).d("stop()")
        player.apply {
            stop()
            clearMediaItems()
        }
        renderInfoListeners.clear()
    }

    companion object {
        private val TAG = App.logTag("Video", "Player")
    }
}