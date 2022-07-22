package eu.darken.fpv.dvca.feedplayer.ui.vr

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.gear.GearManager
import eu.darken.fpv.dvca.gear.goggles.Goggles
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VrPlayerVM @Inject constructor(
    gearManager: GearManager,
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

    companion object {
        private val TAG = App.logTag("VrFeed", "VM")
    }
}