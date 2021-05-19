package eu.darken.fpv.dvca.videofeed.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.navigation.popBackStack
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.SettingsFragmentBinding
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import eu.darken.fpv.dvca.videofeed.core.VideoFeedSettings
import javax.inject.Inject


@AndroidEntryPoint
class FeedSettingsFragment : SmartFragment(R.layout.settings_fragment) {

    private val vmFeed: FeedSettingsVM by viewModels()
    private val binding: SettingsFragmentBinding by viewBindingLazy()

    @Inject lateinit var videoFeedSettings: VideoFeedSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            feedModeDefault.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    val options = HWEndpoint.ReadMode.values().map { it.name }.toTypedArray()
                    setSingleChoiceItems(
                        options,
                        options.indexOf(videoFeedSettings.feedModeDefault.value.name)
                    ) { dif, id ->
                        videoFeedSettings.feedModeDefault.update {
                            HWEndpoint.ReadMode.values().single { it.name == options[id] }
                        }
                        dif.dismiss()
                    }

                }.show()
            }

        }
    }


    companion object {
        private val TAG = App.logTag("Feed", "Settings")
    }
}
