package eu.darken.fpv.dvca.main.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.navigation.popBackStack
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.InfoFragmentBinding


@AndroidEntryPoint
class InfoFragment : SmartFragment(R.layout.info_fragment) {

    private val vm: InfoVM by viewModels()
    private val binding: InfoFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            infoSource.setOnClickListener {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/d4rken/fpv-dvca"))
                    .run { startActivity(this) }

            }
            infoDiscord.setOnClickListener {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/q5gHFXAs9e"))
                    .run { startActivity(this) }
            }
            authorTwitter.setOnClickListener {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/d4rken"))
                    .run { startActivity(this) }
            }
        }
    }

    companion object {
        private val TAG = App.logTag("Info", "Fragment")
    }
}
