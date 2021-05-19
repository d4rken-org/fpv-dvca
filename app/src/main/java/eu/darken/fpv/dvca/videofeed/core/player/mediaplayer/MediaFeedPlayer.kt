package eu.darken.fpv.dvca.videofeed.core.player.mediaplayer

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaDataSource
import android.media.MediaPlayer
import android.os.PowerManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.videofeed.core.player.FeedPlayer
import eu.darken.fpv.dvca.videofeed.core.player.exo.RenderInfo
import okio.buffer
import javax.inject.Inject

class MediaFeedPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) : FeedPlayer {

    private val player = MediaPlayer()

    override val isPlaying: Boolean
        get() = TODO("Not yet implemented")

    @SuppressLint("NewApi")
    override fun start(feed: Goggles.VideoFeed, surfaceView: SurfaceView, renderInfoListener: (RenderInfo) -> Unit) {
        feed.open()

        feed.source.buffer().let {

            player.setDataSource(object : MediaDataSource() {
                override fun close() {
//                    it.close()
                }

                override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
                    return it.read(buffer, offset, size)
                }

                override fun getSize(): Long {
                    return Long.MAX_VALUE
                }

            })
        }
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                player.setDisplay(holder)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

        })

        player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        player.prepareAsync()
        player.start()
    }

    override fun stop() {
//        TODO("Not yet implemented")
    }

}