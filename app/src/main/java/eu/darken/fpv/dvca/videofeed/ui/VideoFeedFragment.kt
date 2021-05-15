package eu.darken.fpv.dvca.videofeed.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.VideofeedFragmentBinding
import eu.darken.fpv.dvca.gear.GearManager
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class VideoFeedFragment : SmartFragment(R.layout.videofeed_fragment) {

    private val vm: VideoFeedVM by viewModels()
    private val binding: VideofeedFragmentBinding by viewBindingLazy()

    @Inject lateinit var gearManager: GearManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.launch {
            gearManager.availableGear.collect {
                view.post {
                    binding.statusMessage.text = it.map { it.label }.toString()
                }
            }
        }
    }

}
