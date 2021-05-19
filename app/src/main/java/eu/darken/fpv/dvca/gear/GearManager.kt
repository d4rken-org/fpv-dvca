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
    private val gearMap = mutableMapOf<HWDevice.SerialNumber, Gear>()
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
            .catch { Timber.tag(TAG).e(it, "Failed to handle device status change.") }
            .launchIn(appScope)
    }

    private suspend fun updateGearMap(devices: Set<HWDevice>) = mutex.withLock {
        Timber.tag(TAG).v("Updating gear...")

        val gears = devices.mapNotNull { device ->
            val gear = device.createGear()
            if (gear == null) {
                Timber.tag(TAG).d("Unknown device, couldn't create gear: %s", device.logId)
                return@mapNotNull null
            }

            Timber.tag(TAG).i("Known type of gear: %s", device.logId)

            if (!device.hasPermission) {
                // Need to be able to read the serial
                Timber.tag(TAG).d("Missing usb permission, requesting access now")
                deviceManager.requestPermission(device)
            }

            gear
        }

        gearMap.values.forEach { existing ->
            val match = gears.singleOrNull { it.serialNumber == existing.serialNumber }
            Timber.tag(TAG).v("Updating gear %s", existing.logId)
            existing.updateDevice(match?.device)
        }

        gears.forEach { gear ->
            val snOfNewGear = gear.serialNumber
            if (snOfNewGear == null) {
                Timber.tag(TAG).w("Serial number was unavailable, no permission? Can't add %s", gear.logId)
                return@forEach
            }

            // Was already updated
            if (gearMap.containsKey(snOfNewGear)) return@forEach

            gearMap[snOfNewGear] = gear
        }

        Timber.tag(TAG).v("...gear update done.")
        internalGearFlow.value = gearMap.values.toSet()
    }

    private fun HWDevice.findFactory(): Gear.Factory? = factories.singleOrNull { it.canHandle(this) }

    private fun HWDevice.createGear(): Gear? {
        val factory = this.findFactory()
        if (factory == null) {
            Timber.tag(TAG).i("No gear factory to handle device: %s", this.identifier)
            return null
        }
        return factory.create(this).also {
            Timber.tag(TAG).i("Used %s to create %s", factory, it)
        }
    }

    companion object {
        private val TAG = App.logTag("Gear", "Manager")
    }
}