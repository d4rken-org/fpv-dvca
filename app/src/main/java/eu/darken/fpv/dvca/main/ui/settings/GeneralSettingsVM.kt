package eu.darken.fpv.dvca.main.ui.settings

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.fpv.dvca.common.livedata.SingleLiveEvent
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.dvr.GeneralDvrSettings
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import eu.darken.fpv.dvca.videofeed.core.GeneralFeedSettings
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsVM @Inject constructor(
    private val generalFeedSettings: GeneralFeedSettings,
    private val dvrSettings: GeneralDvrSettings,
) : SmartVM() {

    val currentReadMode = generalFeedSettings.feedModeDefault.flow.asLiveData2()

    fun updateFeedSetting(mode: HWEndpoint.ReadMode) {
        generalFeedSettings.feedModeDefault.update { mode }
    }

    val currentStoragePath = dvrSettings.dvrStoragePath.flow.asLiveData2()
    val launchSAFPickerEvent = SingleLiveEvent<Uri?>()

    fun changeStoragePath() {
        launchSAFPickerEvent.postValue(dvrSettings.dvrStoragePath.value)
    }

    fun onNewStoragePath(path: Uri) {
        dvrSettings.dvrStoragePath.update { path }
    }
}