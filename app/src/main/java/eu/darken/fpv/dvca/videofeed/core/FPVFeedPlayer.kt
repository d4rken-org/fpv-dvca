package eu.darken.fpv.dvca.videofeed.core

import android.content.Context
import android.view.SurfaceView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.gear.goggles.Goggles
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
        setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        setWakeMode(C.WAKE_MODE_LOCAL)
        setUseLazyPreparation(true)
    }.build()

    private val renderInfoListeners = mutableListOf<(RenderInfo) -> Unit>()

    val isPlaying: Boolean
        get() = player.run { isPlaying || isLoading }

    fun start(
        feed: Goggles.VideoFeed,
        surfaceView: SurfaceView,
        renderInfoListener: (RenderInfo) -> Unit
    ) {
        Timber.tag(TAG).d("start(source=%s, view=%s)", feed, surfaceView)

        if (isPlaying) {
            Timber.tag(TAG).w("Already playing? Stopping!")
            stop()
        }

        renderInfoListeners.add { info ->
            surfaceView.post { renderInfoListener(info) }
        }

        player.apply {
            setVideoSurfaceView(surfaceView)
            setMediaSource(feed.exoMediaSource)

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