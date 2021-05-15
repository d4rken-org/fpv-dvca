package eu.darken.fpv.dvca.onboarding.ui

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.fpv.dvca.common.livedata.SingleLiveEvent
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.onboarding.core.OnboardingSettings
import javax.inject.Inject

@HiltViewModel
class OnboardingVM @Inject constructor(
    private val handle: SavedStateHandle,
    private val onboardingSettings: OnboardingSettings,
) : SmartVM() {

    val navEvents = SingleLiveEvent<NavDirections>()

    fun finishOnboarding() {
        onboardingSettings.isOnboarded.update { true }
        navEvents.postValue(OnboardingFragmentDirections.actionOnboardingFragmentToVideoFeedFragment())
    }
}