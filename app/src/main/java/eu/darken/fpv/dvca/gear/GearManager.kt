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
            .catch { Timber.tag(TAG).e(it, "Failed to handle device status change.") }
            .launchIn(appScope)
    }

    private suspend fun updateGearMap(devices: Set<HWDevice>) = mutex.withLock {
        Timber.tag(TAG).v("Updating gear...")

        val latestGears = devices.mapNotNull { device ->
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

        val consumed = mutableSetOf<String>()
        val disconnected = mutableSetOf<String>()

        // Sort out still connected and disconnected ones
        gearMap.values.forEach { existing ->
            val match = latestGears.singleOrNull { it.identifier == existing.identifier }

            if (match != null) {
                Timber.tag(TAG).v("Still connected: %s", match.logId)
                consumed.add(match.identifier)
            } else {
                disconnected.add(existing.identifier)
            }
        }

        disconnected.forEach {
            val removed = gearMap.remove(it)!!
            removed.release()
            Timber.tag(TAG).i("Disconnected: %s", removed.logId)
        }

        // Add all new gears
        latestGears
            .filterNot { consumed.contains(it.identifier) }
            .forEach { maybeNew ->
                val newGearId = maybeNew.identifier

                Timber.tag(TAG).v("Adding new gear: %s", maybeNew.logId)
                gearMap[newGearId] = maybeNew
                consumed.add(maybeNew.identifier)
            }

        internalGearFlow.value = gearMap.values.toSet().also {
            Timber.tag(TAG).v("...gear update done: %s", it)
        }
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