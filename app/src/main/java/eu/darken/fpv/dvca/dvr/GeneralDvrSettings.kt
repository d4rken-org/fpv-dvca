package eu.darken.fpv.dvca.dvr

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.common.preferences.FlowPreference
import eu.darken.fpv.dvca.common.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralDvrSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val prefs by lazy {
        context.getSharedPreferences("dvr_general_prefs", Context.MODE_PRIVATE)
    }

    val dvrStoragePath: FlowPreference<Uri?> = prefs.createFlowPreference(
        key = "dvr.storage.path",
        reader = { key ->
            getString(key, null)?.let { Uri.parse(it) }
        },
        writer = { key, value ->
            putString(key, value.toString())
        }
    )
}