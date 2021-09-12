package eu.darken.fpv.dvca.feedplayer.core.vr

import android.content.Context
import com.google.android.exoplayer2.*
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.feedplayer.ui.vr.VrView
import eu.darken.fpv.dvca.gear.goggles.Goggles
import timber.log.Timber
import javax.inject.Inject

class VrFeedPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val tag = App.logTag("VrFeed", "Player", hashCode().toString())

    private val loadControl = DefaultLoadControl.Builder().apply {
        setBufferDurationsMs(100, 1000, 100, 100)
    }.build()

    private val renderersFactory = DefaultRenderersFactory(context)

    private val player = SimpleExoPlayer.Builder(context, renderersFactory).apply {
        setLoadControl(loadControl)
        setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        setWakeMode(C.WAKE_MODE_LOCAL)
        setUseLazyPreparation(true)
    }.build()

    val isPlaying: Boolean
        get() = player.run { isPlaying || isLoading }

    fun start(
        feed: Goggles.VideoFeed,
        vrView: VrView,
    ) {
        Timber.tag(tag).d("start(source=%s, surface=%s)", feed, vrView.surface)

        if (isPlaying) {
            Timber.tag(tag).w("Already playing? Stopping!")
            stop()
        }

        player.apply {
            setVideoSurface(vrView.surface)
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
    }
}