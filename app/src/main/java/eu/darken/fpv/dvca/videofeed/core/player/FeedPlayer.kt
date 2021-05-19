package eu.darken.fpv.dvca.videofeed.core.player

import android.view.SurfaceView
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.videofeed.core.player.exo.RenderInfo

interface FeedPlayer {

    val isPlaying: Boolean

    fun start(
        feed: Goggles.VideoFeed,
        surfaceView: SurfaceView,
        renderInfoListener: (RenderInfo) -> Unit
    )

    fun stop()
}