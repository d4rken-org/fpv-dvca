package eu.darken.fpv.dvca.main.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.navigation.popBackStack
import eu.darken.fpv.dvca.common.observe2
import eu.darken.fpv.dvca.common.saf.SAFPathPickerContract
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.SettingsFragmentBinding
import eu.darken.fpv.dvca.dvr.core.DvrMode
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import timber.log.Timber


@AndroidEntryPoint
class GeneralSettingsFragment : SmartFragment(R.layout.settings_fragment) {

    private val vm: GeneralSettingsVM by viewModels()
    private val binding: SettingsFragmentBinding by viewBindingLazy()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.apply {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
        }

        vm.currentReadMode.observe2(this) { readMode ->
            binding.feedModeDefault.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    val options = HWEndpoint.ReadMode.values().map { it.name }.toTypedArray()
                    setSingleChoiceItems(
                        options,
                        options.indexOf(readMode.name)
                    ) { dif, id ->
                        vm.updateFeedSetting(HWEndpoint.ReadMode.values().single { it.name == options[id] })

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.msg_restart_for_effect),
                            Toast.LENGTH_LONG
                        ).show()
                        dif.dismiss()
                    }

                }.show()
            }
        }

        vm.isMultiplayerLandscapeEnabled.observe2(this) { isEnabled ->
            binding.playbackMultiplayerLandscape.isChecked = isEnabled
        }
        binding.playbackMultiplayerLandscape.setOnCheckedChangedListener { _, checked ->
            vm.updateMultiplayerLandscape(checked)
        }

        vm.currentStoragePath.observe2(this@GeneralSettingsFragment) {
            binding.dvrPath.apply {
                setOnClickListener { vm.changeStoragePath() }
                val nicePath = it?.run {
                    "$authority:$path"
                }
                setDescription("${getString(R.string.dvr_path_description)}\n$nicePath")
            }
        }
        val safLauncher = registerForActivityResult(SAFPathPickerContract()) {
            Timber.i("Selected storage uri: %s", it)
            if (it == null) return@registerForActivityResult

            vm.onNewStoragePath(it)
        }
        vm.launchSAFPickerEvent.observe2(this) {
            safLauncher.launch(it)
        }

        vm.currentDvrMode.observe2(this) { readMode ->
            binding.dvrModeDefault.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    val options = DvrMode.values().map { it.name }.toTypedArray()
                    setSingleChoiceItems(
                        options,
                        options.indexOf(readMode.name)
                    ) { dif, id ->
                        vm.updateDvrModeDefault(DvrMode.values().single { it.name == options[id] })

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.msg_restart_for_effect),
                            Toast.LENGTH_LONG
                        ).show()
                        dif.dismiss()
                    }

                }.show()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        private val TAG = App.logTag("Feed", "Settings")
    }
}
