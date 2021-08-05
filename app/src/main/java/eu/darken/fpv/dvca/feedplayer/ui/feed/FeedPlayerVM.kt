package eu.darken.fpv.dvca.feedplayer.ui.feed

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.livedata.SingleLiveEvent
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.dvr.GeneralDvrSettings
import eu.darken.fpv.dvca.dvr.core.DvrController
import eu.darken.fpv.dvca.feedplayer.core.FeedPlayerSettings
import eu.darken.fpv.dvca.gear.GearManager
import eu.darken.fpv.dvca.gear.goggles.Goggles
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FeedPlayerVM @Inject constructor(
    private val handle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val gearManager: GearManager,
    private val dvrSettings: GeneralDvrSettings,
    private val feedPlayerSettings: FeedPlayerSettings,
    private val dvrController: DvrController,
) : SmartVM() {

    private val goggles = gearManager.availableGear
        .map { it.filterIsInstance<Goggles>() }

    private val goggle1 = goggles
        .map { gears ->
            gears.minByOrNull { it.firstSeenAt }
        }

    val google1Feed = goggle1
        .map { goggles1 ->
            if (goggles1 == null) return@map null
            Timber.tag(TAG).d("Goggle 1 available: %s", goggles1.logId)

            goggles1.videoFeed ?: goggles1.startVideoFeed().also {
                Timber.tag(TAG).d("Enabling videofeed 1 for %s", goggles1.logId)
            }
        }
        .onEach { Timber.tag(TAG).d("Videofeed 1: %s", it) }
        .asLiveData2()

    private val goggle2 = goggles
        .map { gears ->
            if (gears.size < 2) {
                null
            } else {
                gears.maxByOrNull { it.firstSeenAt }
            }
        }

    val google2Feed = goggle2
        .map { goggles2 ->
            if (goggles2 == null) return@map null
            Timber.tag(TAG).d("Goggle 2 available: %s", goggles2.logId)

            goggles2.videoFeed ?: goggles2.startVideoFeed().also {
                Timber.tag(TAG).d("Enabling videofeed 2 for %s", goggles2.logId)
            }
        }
        .onEach { Timber.tag(TAG).d("Videofeed 2: %s", it) }
        .asLiveData2()

    val isMultiplayerInLandscapeAllowed: Boolean
        get() = feedPlayerSettings.isLandscapeMultiplayerEnabled.value

    val dvrStoragePathEvent = SingleLiveEvent<Unit>()

    fun onPlayer1RecordToggle() = launch {
        if (pathSetup()) return@launch
        dvrController.toggleDvr()
    }

    fun onPlayer2RecordToggle() = launch {
        if (pathSetup()) return@launch
    }

    private fun pathSetup(): Boolean {
        if (dvrSettings.dvrStoragePath.value == null) {
            dvrStoragePathEvent.postValue(Unit)
            return true
        }
        return false
    }

    fun onStoragePathSelected(path: Uri) = launch {
        context.contentResolver.takePersistableUriPermission(
            path,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        dvrSettings.dvrStoragePath.update { path }
    }

    companion object {
        private val TAG = App.logTag("VideoFeed", "VM")
    }
}