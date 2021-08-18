package eu.darken.fpv.dvca.feedplayer.ui.feed

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.BuildConfigWrap
import eu.darken.fpv.dvca.common.hideSystemUI
import eu.darken.fpv.dvca.common.navigation.doNavigate
import eu.darken.fpv.dvca.common.observe2
import eu.darken.fpv.dvca.common.saf.SAFPathPickerContract
import eu.darken.fpv.dvca.common.showSystemUI
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.VideofeedFragmentBinding
import eu.darken.fpv.dvca.feedplayer.core.player.exo.ExoFeedPlayer
import eu.darken.fpv.dvca.feedplayer.core.player.exo.RenderInfo
import eu.darken.fpv.dvca.gear.goggles.Goggles
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class FeedPlayerFragment : SmartFragment(R.layout.videofeed_fragment) {

    private val vm: FeedPlayerVM by viewModels()
    private val binding: VideofeedFragmentBinding by viewBindingLazy()

    @Inject lateinit var exoPlayer1: ExoFeedPlayer
    @Inject lateinit var exoPlayer2: ExoFeedPlayer

    private val versionTag: String by lazy {
        "DVCA ${BuildConfigWrap.VERSION_NAME}(${BuildConfigWrap.GITSHA})"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isInLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isInLandscape) enterImmersive()

        binding.apply {
            root.setOnClickListener {
                if (toolbar.isGone) exitImmersive() else enterImmersive()
            }
            toolbar.inflateMenu(R.menu.feedplayer_menu)
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.settings -> {
                        doNavigate(FeedPlayerFragmentDirections.actionVideoFeedFragmentToSettingsFragment())
                        true
                    }
                    R.id.info -> {
                        doNavigate(FeedPlayerFragmentDirections.actionVideoFeedFragmentToInfoFragment())
                        true
                    }
                    R.id.donate -> {
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/tydarken"))
                            .run { startActivity(this) }
                        true
                    }
                    else -> super.onOptionsItemSelected(it)
                }
            }

            playerContainer.orientation = when (isInLandscape) {
                true -> LinearLayout.HORIZONTAL
                false -> LinearLayout.VERTICAL
            }
        }

        // Player 1
        binding.apply {
            player1Placeholder.text = getString(R.string.video_feed_player_tease, "1")
            player1RecordFab.setOnClickListener { vm.onPlayer1RecordToggle() }

            vm.state.observe2(this@FeedPlayerFragment) { state ->
                val feed = state.feed1
                Timber.tag(TAG).d("google1Feed.observe2(): %s", feed)
                if (feed != null) {
                    exoPlayer1.start(
                        feed = feed,
                        surfaceView = player1Canvas,
                        renderInfoListener = { info ->
                            if (!isResumed) {
                                Timber.tag(TAG).v("View was null?")
                                return@start
                            }
                            binding.player1Metadata.text = createMetaDataDesc(info, feed)
                        }
                    )
                } else {
                    exoPlayer1.stop()
                }
                player1RecordFab.setImageResource(
                    if (state.recording1 != null) R.drawable.ic_round_stop_24 else R.drawable.ic_video_file_24
                )
                player1Placeholder.isGone = feed != null
                player1Canvas.isInvisible = feed == null
                player1Metadata.isInvisible = feed == null
                player1RecordFab.isGone = feed == null
            }
        }

        // Player 2
        binding.apply {
            player2Container.isGone = isInLandscape && !vm.isMultiplayerInLandscapeAllowed
            player2Placeholder.text = getString(R.string.video_feed_player_tease, "2")
            player2RecordFab.setOnClickListener { vm.onPlayer2RecordToggle() }

            if (isInLandscape && !vm.isMultiplayerInLandscapeAllowed) {
                exoPlayer2.stop()
                return@apply
            }

            vm.state.observe2(this@FeedPlayerFragment) { state ->
                val feed = state.feed1
                Timber.tag(TAG).d("google2Feed.observe2(): %s", feed)
                if (feed != null) {
                    exoPlayer2.start(
                        feed = feed,
                        surfaceView = player2Canvas,
                        renderInfoListener = { info ->
                            if (!isResumed) {
                                Timber.tag(TAG).v("View was null?")
                                return@start
                            }
                            binding.player2Metadata.text = createMetaDataDesc(info, feed)
                        }
                    )
                } else {
                    exoPlayer2.stop()
                }
                player2RecordFab.setImageResource(
                    if (state.recording2 != null) R.drawable.ic_round_stop_24 else R.drawable.ic_video_file_24
                )
                player2Placeholder.isGone = feed != null
                player2Canvas.isInvisible = feed == null
                player2Metadata.isInvisible = feed == null
                player2RecordFab.isGone = feed == null
            }
        }

        val safLauncher = registerForActivityResult(SAFPathPickerContract()) {
            Timber.i("Selected storage uri: %s", it)
            if (it == null) return@registerForActivityResult

            vm.onStoragePathSelected(it)
        }
        vm.dvrStoragePathEvent.observe2(this) { safLauncher.launch(null) }
    }

    private fun enterImmersive() {
        binding.apply {
            toolbar.isGone = true
            hideSystemUI(root)
        }
    }

    private fun exitImmersive() {
        binding.apply {
            toolbar.isGone = false
            showSystemUI(root)
        }
    }

    private fun createMetaDataDesc(info: RenderInfo, feed: Goggles.VideoFeed): String {
        val sb = StringBuilder()
        sb.append("$versionTag @ ${feed.deviceIdentifier}\n")
        sb.append(info.toString())
        sb.append(" [USB ${feed.videoUsbReadMbs} | BUFFER ${feed.videoBufferReadMbs} MB/s ~ ${feed.usbReadMode}]")

        return sb.toString()
    }

    override fun onDestroyView() {
        exoPlayer1.stop()
        exoPlayer2.stop()
        super.onDestroyView()
    }

    companion object {
        private val TAG = App.logTag("VideoFeed", "Fragment")
    }
}
