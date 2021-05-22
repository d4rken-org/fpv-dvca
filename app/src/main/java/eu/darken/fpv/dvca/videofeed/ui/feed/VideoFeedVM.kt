package eu.darken.fpv.dvca.videofeed.ui.feed

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.gear.GearManager
import eu.darken.fpv.dvca.gear.goggles.Goggles
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VideoFeedVM @Inject constructor(
    private val handle: SavedStateHandle,
    private val gearManager: GearManager
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

    companion object {
        private val TAG = App.logTag("VideoFeed", "VM")
    }

}