package eu.darken.fpv.dvca.onboarding.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.navigation.doNavigate
import eu.darken.fpv.dvca.common.observe2
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.OnboardingFragmentBinding

@AndroidEntryPoint
class OnboardingFragment : SmartFragment(R.layout.onboarding_fragment) {

    private val vm: OnboardingVM by viewModels()
    private val binding: OnboardingFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.actionStart.setOnClickListener {
            vm.finishOnboarding()
        }

        vm.navEvents.observe2(this) { doNavigate(it) }
    }

}
