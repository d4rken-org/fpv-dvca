package eu.darken.fpv.dvca.gear

import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.coroutine.AppScope
import eu.darken.fpv.dvca.gear.goggles.djifpv.FpvGogglesV1
import eu.darken.fpv.dvca.usb.HWDevice
import eu.darken.fpv.dvca.usb.manager.HWDeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GearManager @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val deviceManager: HWDeviceManager,
    fpvGogglesV1Factory: FpvGogglesV1.Factory
) {

    private val mutex = Mutex()
    private val gearMap = mutableMapOf<String, Gear>()
    private val factories = setOf<Gear.Factory>(
        fpvGogglesV1Factory
    )
    private val internalGearFlow = MutableStateFlow(emptySet<Gear>())
    val availableGear: Flow<Set<Gear>> = internalGearFlow

    init {
        deviceManager.devices
            .onStart { Timber.tag(TAG).d("Observing devices.") }
            .onEach { devices ->
                Timber.tag(TAG).v("Devices changed! Updating gearmap.")
                updateGearMap(devices)
            }
            .catch { Timber.tag(TAG).e("Failed to handle device status change.") }
            .launchIn(appScope)
    }

    private suspend fun updateGearMap(devices: Set<HWDevice>) = mutex.withLock {
        Timber.tag(TAG).v("Updating gear...")

        gearMap.values.forEach { gear ->
            val match = devices.singleOrNull { it.identifier == gear.identifier }
            gear.updateDevice(match)
        }

        devices.forEach { device ->
            // Was already updated
            if (gearMap.containsKey(device.identifier)) return@forEach

            val newGear = device.createGear()
            if (newGear == null) {
                Timber.tag(TAG).d("Unknown device, couldn't create gear: %s", device.label)
                return@forEach
            } else {
                Timber.tag(TAG).i("Added new gear: %s", device.label)
            }
            gearMap[newGear.identifier] = newGear
        }

        Timber.tag(TAG).v("...gear update done.")
        internalGearFlow.value = gearMap.values.toSet()
    }

    private fun HWDevice.findFactory(): Gear.Factory? = factories.singleOrNull { it.canHandle(this) }

    private fun HWDevice.createGear(): Gear? {
        val factory = this.findFactory()
        if (factory == null) {
            Timber.tag(TAG).i("No gear factory to handle device: %s", this.label)
            return null
        }
        return factory.create(this@GearManager, this).also {
            Timber.tag(TAG).i("Used %s to create %s", factory, it.label)
        }
    }

    companion object {
        private val TAG = App.logTag("Gear", "Manager")
    }
}