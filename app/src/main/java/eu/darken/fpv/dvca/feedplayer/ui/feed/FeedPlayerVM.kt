package eu.darken.fpv.dvca.feedplayer.ui.feed

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.androidstarter.common.logging.i
import eu.darken.androidstarter.common.logging.w
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.flow.combine
import eu.darken.fpv.dvca.common.livedata.SingleLiveEvent
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.dvr.GeneralDvrSettings
import eu.darken.fpv.dvca.dvr.core.DvrController
import eu.darken.fpv.dvca.feedplayer.core.FeedPlayerSettings
import eu.darken.fpv.dvca.gear.GearManager
import eu.darken.fpv.dvca.gear.goggles.Goggles
import kotlinx.coroutines.flow.*
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

    private val goggle1Feed: Flow<Goggles.VideoFeed?> = goggle1
        .flatMapLatest { goggles1 ->
            if (goggles1 == null) {
                Timber.tag(TAG).d("Goggle 1 unavailable")
                flowOf(null)
            } else {
                Timber.tag(TAG).d("Goggle 1 available: %s", goggles1.logId)
                goggles1.videoFeed
            }
        }
        .onEach { Timber.tag(TAG).d("Videofeed 1: %s", it) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)

    val video1 = goggle1Feed.asLiveData2()

    val dvr1 = combine(goggle1, dvrController.recordings) { g1, recordings ->
        recordings.singleOrNull { it.goggle == g1 }
    }.asLiveData2()

    private val goggle2 = goggles
        .map { gears ->
            if (gears.size < 2) {
                null
            } else {
                gears.maxByOrNull { it.firstSeenAt }
            }
        }

    private val google2Feed = goggle2
        .flatMapLatest { goggles2 ->
            if (goggles2 == null) {
                Timber.tag(TAG).d("Goggle 2 unavailable")
                flowOf(null)
            } else {
                Timber.tag(TAG).d("Goggle 2 available: %s", goggles2.logId)
                goggles2.videoFeed
            }
        }
        .onEach { Timber.tag(TAG).d("Videofeed 2: %s", it) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)

    val video2 = google2Feed.asLiveData2()

    val dvr2 = combine(goggle2, dvrController.recordings) { g2, recordings ->
        recordings.singleOrNull { it.goggle == g2 }
    }.asLiveData2()

    val isMultiplayerInLandscapeAllowed: Boolean
        get() = feedPlayerSettings.isLandscapeMultiplayerEnabled.value

    val dvrStoragePathEvent = SingleLiveEvent<Unit>()

    fun onPlayer1RecordToggle() = launch {
        if (pathSetup()) return@launch

        val goggle = goggle1.first()
        if (goggle == null) {
            w(TAG) { "Can't start Goggle 1 DVR, was null!" }
            return@launch
        } else {
            i(TAG) { "onPlayer1RecordToggle(): goggle=$goggle" }
        }

        val recording = dvrController.toggle(goggle)
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