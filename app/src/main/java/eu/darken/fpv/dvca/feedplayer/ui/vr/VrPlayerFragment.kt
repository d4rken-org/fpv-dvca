package eu.darken.fpv.dvca.feedplayer.ui.vr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.hideSystemUI
import eu.darken.fpv.dvca.common.navigation.doNavigate
import eu.darken.fpv.dvca.common.observe2
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.VrplayerFragmentBinding
import eu.darken.fpv.dvca.feedplayer.core.vr.VrFeedPlayer
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class VrPlayerFragment : SmartFragment(R.layout.vrplayer_fragment) {

    private val vm: VrPlayerVM by viewModels()
    private val binding: VrplayerFragmentBinding by viewBindingLazy()

    @Inject lateinit var vrFeedPlayer: VrFeedPlayer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideSystemUI(binding.root)

        binding.apply {
            actionExitVrMode.setOnClickListener {
                doNavigate(VrPlayerFragmentDirections.actionVrFragmentToVideoFeedFragment())
            }
        }

        vm.video1.observe2(this@VrPlayerFragment) { feed ->
            Timber.tag(TAG).d("video1.observe2(): %s", feed)
            if (feed != null) {
                vrFeedPlayer.start(
                    feed = feed,
                    vrView = binding.vrSurface,
                )
            } else {
                vrFeedPlayer.stop()
            }
        }
    }

    override fun onDestroyView() {
        vrFeedPlayer.stop()
        binding.vrSurface.release()
        super.onDestroyView()
    }

    companion object {
        private val TAG = App.logTag("VrFeed", "Fragment")
    }
}
