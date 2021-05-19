package eu.darken.fpv.dvca.main.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.fpv.dvca.common.coroutine.DispatcherProvider
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.onboarding.core.OnboardingSettings
import javax.inject.Inject


@HiltViewModel
class MainActivityVM @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    handle: SavedStateHandle,
    private val onboardingSettings: OnboardingSettings,
) : SmartVM(dispatcherProvider = dispatcherProvider) {

    val hasOnboarding = onboardingSettings.isOnboarded


}