package eu.darken.fpv.dvca.gear.goggles

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.common.preferences.FlowPreference
import eu.darken.fpv.dvca.common.preferences.createFlowPreference
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoFeedSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val prefs by lazy {
        context.getSharedPreferences("videofeed_general_prefs", Context.MODE_PRIVATE)
    }

    val feedModeDefault: FlowPreference<HWEndpoint.ReadMode> = prefs.createFlowPreference(
        key = "feedsettings.readmode.default",
        reader = { key ->
            getString(key, null).let { value ->
                HWEndpoint.ReadMode.values().singleOrNull { it.key == value }
            } ?: HWEndpoint.ReadMode.DIRECT
        },
        writer = { key, value ->
            putString(key, value.key)
        }
    )
}