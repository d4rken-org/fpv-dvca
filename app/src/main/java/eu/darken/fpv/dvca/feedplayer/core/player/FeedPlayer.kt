package eu.darken.fpv.dvca.feedplayer.core.player

import android.view.SurfaceView
import eu.darken.fpv.dvca.feedplayer.core.player.exo.RenderInfo
import eu.darken.fpv.dvca.gear.goggles.Goggles

interface FeedPlayer {

    val isPlaying: Boolean

    fun start(
        feed: Goggles.VideoFeed,
        surfaceView: SurfaceView,
        renderInfoListener: (RenderInfo) -> Unit
    )

    fun stop()
}