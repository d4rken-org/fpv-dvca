package eu.darken.fpv.dvca.videofeed.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.observe2
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.VideofeedFragmentBinding
import eu.darken.fpv.dvca.gear.GearManager
import eu.darken.fpv.dvca.gear.goggles.Goggles
import eu.darken.fpv.dvca.videofeed.core.FPVFeedPlayer
import eu.darken.fpv.dvca.videofeed.core.RenderInfo
import javax.inject.Inject


@AndroidEntryPoint
class VideoFeedFragment : SmartFragment(R.layout.videofeed_fragment) {

    private val vm: VideoFeedVM by viewModels()
    private val binding: VideofeedFragmentBinding by viewBindingLazy()

    @Inject lateinit var feedPlayer: FPVFeedPlayer
    @Inject lateinit var gearManager: GearManager

    private var reconnectToast: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.feedAvailability.observe2(this) { feed ->
            if (feed != null) {
                reconnectToast?.dismiss()
                reconnectToast = null

                feedPlayer.start(
                    feed = feed,
                    surfaceView = binding.videoCanvas,
                    renderInfoListener = { info -> updateMetaData(info, feed) }
                )
            } else {
                feedPlayer.stop()

                reconnectToast?.dismiss()
                Snackbar.make(
                    requireView(),
                    getString(R.string.status_message_waiting_for_device),
                    Snackbar.LENGTH_INDEFINITE
                ).also {
                    reconnectToast = it
                }.show()
            }
        }
    }

    private fun updateMetaData(info: RenderInfo, feed: Goggles.VideoFeed) {
        val sb = StringBuilder()
        sb.append(info.toString())
        sb.append(", USB ${feed.videoUsbReadMbs} MB/s")
        sb.append(", Buffer ${feed.videoBufferReadMbs} MB/s")

        binding.videoMetadata.text = sb.toString()
    }

    override fun onDestroyView() {
        feedPlayer.stop()
        super.onDestroyView()
    }

    companion object {
        private val TAG = App.logTag("VideoFeed", "Fragment")
    }
}
