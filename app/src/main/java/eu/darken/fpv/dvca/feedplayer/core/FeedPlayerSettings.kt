package eu.darken.fpv.dvca.feedplayer.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.common.preferences.FlowPreference
import eu.darken.fpv.dvca.common.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedPlayerSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val prefs by lazy {
        context.getSharedPreferences("feedplayer_general_prefs", Context.MODE_PRIVATE)
    }

    val isLandscapeMultiplayerEnabled: FlowPreference<Boolean> = prefs.createFlowPreference(
        key = "feedplayer.multiplayer.landscape",
        defaultValue = false
    )
}