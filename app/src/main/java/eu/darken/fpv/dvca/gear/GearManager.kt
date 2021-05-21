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

        val currentGears = devices.mapNotNull { device ->
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

        // Sort out those that didn't change
        currentGears.forEach { maybeNew ->
            val stillConnected = gearMap.values
                .filter { it.isGearConnected }
                .singleOrNull { it.identifier == maybeNew.identifier }

            if (stillConnected != null) {
                Timber.tag(TAG).v("Still connected: %s", stillConnected.logId)
                consumed.add(maybeNew.identifier)
            }
        }


        // Was this a reconnect on the same identifier?
        currentGears
            .filterNot { consumed.contains(it.identifier) }
            .forEach { maybeNew ->
                val reconnectFor = gearMap.values
                    .filter { !it.isGearConnected }
                    .firstOrNull { it.identifier == maybeNew.identifier }

                if (reconnectFor != null) {
                    Timber.tag(TAG).d("Found a reconnect, updating %s with %s", reconnectFor, maybeNew.device)
                    reconnectFor.updateDevice(maybeNew.device)
                    consumed.add(maybeNew.identifier)
                }
            }

        // YOLO any remaining matches, one is better than none?
        currentGears
            .filterNot { consumed.contains(it.identifier) }
            .forEach { maybeNew ->
                val ballParkMatch = gearMap.values
                    .filter { !it.isGearConnected }
                    .firstOrNull { it.gearName == maybeNew.gearName }

                if (ballParkMatch != null) {
                    Timber.tag(TAG).v("Ballpark matched %s with %s", ballParkMatch.logId, maybeNew.logId)
                    ballParkMatch.updateDevice(maybeNew.device)
                    consumed.add(maybeNew.identifier)
                }
            }

        // Add all new unknown gears
        currentGears
            .filterNot { consumed.contains(it.identifier) }
            .forEach { maybeNew ->
                val newGearId = maybeNew.identifier

                Timber.tag(TAG).v("Adding new gear: %s", maybeNew.logId)
                gearMap[newGearId] = maybeNew
                consumed.add(maybeNew.identifier)
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