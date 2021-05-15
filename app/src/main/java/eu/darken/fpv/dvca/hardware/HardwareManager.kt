package eu.darken.fpv.dvca.hardware

import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.coroutine.AppScope
import eu.darken.fpv.dvca.common.flow.HotDataFlow
import eu.darken.fpv.dvca.hardware.goggles.DjiFpvGogglesV1
import eu.darken.fpv.dvca.usb.core.DVCADeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareManager @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dvcaDeviceManager: DVCADeviceManager,
    djiFpvGogglesV1Factory: DjiFpvGogglesV1.Factory
) {

    private val factories = setOf<Hardware.Factory>(
        djiFpvGogglesV1Factory
    )

    private val internalState: HotDataFlow<Map<String, Hardware>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        Timber.tag(TAG).v("Internal init.")
        dvcaDeviceManager.devices.first()
            .mapNotNull { device ->
                val factory = factories.singleOrNull { it.canHandle(device) }
                if (factory == null) {
                    Timber.tag(TAG).i("No hardware factory to handle device: %s", device.label)
                    return@mapNotNull null
                }
                factory.create(device).also {
                    Timber.tag(TAG).i("Used %s to create %s", factory, it.label)
                }
            }
            .map { it.identifier to it }
            .toMap()
    }
    val availableHardware: Flow<Set<Hardware>> = internalState.data.map { it.values.toSet() }

    companion object {
        private val TAG = App.logTag("HW", "Manager")
    }
}