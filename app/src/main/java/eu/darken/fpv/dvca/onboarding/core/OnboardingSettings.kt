package eu.darken.fpv.dvca.onboarding.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.common.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val prefs by lazy {
        context.getSharedPreferences("onboarding_settings", Context.MODE_PRIVATE)
    }

    val isOnboarded = prefs.createFlowPreference("onboarding.done", false)

}