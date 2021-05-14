package eu.darken.fpv.dvca.bugreporting

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.common.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BugReporterSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val prefs by lazy {
        context.getSharedPreferences("bugreporter_settings", Context.MODE_PRIVATE)
    }

    val isEnabled = prefs.createFlowPreference("bugreporter.enabled", false)

}