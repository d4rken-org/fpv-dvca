package eu.darken.fpv.dvca.feedplayer.core.exo

import android.content.Context
import android.view.SurfaceView
import com.google.android.exoplayer2.*
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.gear.goggles.Goggles
import timber.log.Timber
import javax.inject.Inject

class ExoFeedPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val tag = App.logTag("VideoFeed", "Player", hashCode().toString())

    private val loadControl = DefaultLoadControl.Builder().apply {
        setBufferDurationsMs(100, 1000, 100, 100)
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
        renderInfoListener: ((RenderInfo) -> Unit)?
    ) {
        Timber.tag(tag).d("start(source=%s, view=%s)", feed, surfaceView)

        if (isPlaying) {
            Timber.tag(tag).w("Already playing? Stopping!")
            stop()
        }

        renderInfoListeners.add { info ->
            surfaceView.post { renderInfoListener?.invoke(info) }
        }

        player.apply {
            setVideoSurfaceView(surfaceView)
            setMediaSource(feed.exoMediaSource)

            addListener(object : Player.Listener {
                override fun onPlayerError(error: ExoPlaybackException) {
                    Timber.tag(tag).w(error, "Playback error")
                    player.stop()
                    player.prepare()
                    player.play()
                }
            })

            prepare()
            play()
        }
    }

    fun stop() {
        Timber.tag(tag).d("stop()")
        player.apply {
            stop()
            clearMediaItems()
        }
        renderInfoListeners.clear()
    }
}