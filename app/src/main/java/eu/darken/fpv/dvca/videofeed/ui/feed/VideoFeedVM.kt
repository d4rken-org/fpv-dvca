package eu.darken.fpv.dvca.videofeed.ui.feed

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.gear.GearManager
import eu.darken.fpv.dvca.gear.goggles.Goggles
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VideoFeedVM @Inject constructor(
    private val handle: SavedStateHandle,
    private val gearManager: GearManager
) : SmartVM() {

    private val goggles = gearManager.availableGear
        .map { it.filterIsInstance<Goggles>() }

    val goggle1 = goggles
        .map { gears -> gears.filter { it.isGearConnected } }
        .map { gears ->
            gears.minByOrNull { it.firstSeenAt }
        }
        .filterNotNull()

    val google1Feed = goggle1
        .onEach {
            Timber.tag(TAG).d("Goggle 1 available: %s", it.logId)
            if (it.videoFeed.first() == null) {
                Timber.tag(TAG).d("Enabling videofeed 1 for %s", it.logId)
                it.startVideoFeed()
            }
        }
        .flatMapMerge { it.videoFeed }
        .onEach { Timber.tag(TAG).d("Videofeed 1: %s", it) }
        .asLiveData2()

    val goggle2 = goggles
        .map { gears -> gears.filter { it.isGearConnected } }
        .map { gears ->
            if (gears.size < 2) {
                null
            } else {
                gears.maxByOrNull { it.firstSeenAt }
            }
        }
        .filterNotNull()

    val google2Feed = goggle2
        .onEach {
            Timber.tag(TAG).d("Goggle 2 available: %s", it.logId)
            if (it.videoFeed.first() == null) {
                Timber.tag(TAG).d("Enabling videofeed 2 for %s", it.logId)
                it.startVideoFeed()
            }
        }
        .flatMapMerge { it.videoFeed }
        .onEach { Timber.tag(TAG).d("Videofeed 2: %s", it) }
        .asLiveData2()

}