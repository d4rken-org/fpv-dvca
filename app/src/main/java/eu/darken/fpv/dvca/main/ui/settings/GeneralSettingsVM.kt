package eu.darken.fpv.dvca.main.ui.settings

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.fpv.dvca.common.livedata.SingleLiveEvent
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.dvr.GeneralDvrSettings
import eu.darken.fpv.dvca.feedplayer.core.FeedPlayerSettings
import eu.darken.fpv.dvca.gear.goggles.VideoFeedSettings
import eu.darken.fpv.dvca.usb.connection.HWEndpoint
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsVM @Inject constructor(
    private val videoFeedSettings: VideoFeedSettings,
    private val feedPlayerSettings: FeedPlayerSettings,
    private val dvrSettings: GeneralDvrSettings,
) : SmartVM() {

    val currentReadMode = videoFeedSettings.feedModeDefault.flow.asLiveData2()

    fun updateFeedSetting(mode: HWEndpoint.ReadMode) {
        videoFeedSettings.feedModeDefault.update { mode }
    }

    val isMultiplayerLandscapeEnabled = feedPlayerSettings.isLandscapeMultiplayerEnabled.flow.asLiveData2()

    fun updateMultiplayerLandscape(enabled: Boolean) {
        feedPlayerSettings.isLandscapeMultiplayerEnabled.update { enabled }
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