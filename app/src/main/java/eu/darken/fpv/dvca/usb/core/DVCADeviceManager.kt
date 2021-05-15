package eu.darken.fpv.dvca.usb.core

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.collections.mutate
import eu.darken.fpv.dvca.common.coroutine.AppScope
import eu.darken.fpv.dvca.common.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DVCADeviceManager @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val usbManager: UsbManager,
    private val usbPermissionHandler: UsbPermissionHandler,
) {

    private val internalState: HotDataFlow<Map<String, DVCADevice>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        Timber.tag(TAG).v("Internal init.")
        usbManager.deviceList.values
            .map { it.toDVCADevice() }
            .map { it.identifier to it }
            .toMap()
    }
    val devices: Flow<Set<DVCADevice>> = internalState.data.map { it.values.toSet() }

    suspend fun requestPermission(device: DVCADevice): Boolean {
        internalState.updateBlocking {
            val target = this[device.identifier] ?: throw UnknownDeviceException(device)

            mutate {
                this[target.identifier] = target.copy(hasRequestedPermission = true)
            }
        }

        val isGranted = usbPermissionHandler.requestPermission(device)
        internalState.updateBlocking {
            val target = this[device.identifier]

            if (target == null) {
                Timber.tag(TAG).w("Permission grant, but device can't be found: %s", device)
                return@updateBlocking this
            }

            mutate {
                this[target.identifier] = target.copy(
                    hasPermission = isGranted,
                    hasRequestedPermission = false,
                )
            }
        }

        return isGranted
    }

    fun onDeviceAttached(rawDevice: UsbDevice) = internalState.updateAsync(
        onError = { Timber.tag(TAG).e(it, "ATTACHED failed for %s", rawDevice) }
    ) {
        Timber.tag(TAG).v("onDeviceAttached(rawDevice=%s)", rawDevice.label)

        val added = usbManager.find(rawDevice.identifier) ?: throw UnknownDeviceException(rawDevice)

        mutate {
            this[added.identifier] = added
        }
    }

    fun onDeviceDetached(rawDevice: UsbDevice) = internalState.updateAsync(
        onError = { Timber.tag(TAG).e(it, "DETATCH failed for %s", rawDevice) }
    ) {
        Timber.tag(TAG).v("onDeviceAttached(rawDevice=%s)", rawDevice.label)

        val toRemove = this[rawDevice.identifier]

        if (toRemove == null) {
            Timber.tag(TAG).w("Failed to remove %s, already removed?", rawDevice.label)
            return@updateAsync this
        } else {
            Timber.tag(TAG).i("Removing detached devices %s", toRemove?.label)
        }

        mutate {
            remove(toRemove.identifier)
        }
    }

    fun refresh(rawDevice: UsbDevice) = internalState.updateAsync(
        onError = { Timber.tag(TAG).e(it, "REFRESH failed for %s", rawDevice) }
    ) {
        Timber.tag(TAG).v("onDeviceAttached(rawDevice=%s)", rawDevice.label)

        val added = usbManager.find(rawDevice.identifier) ?: throw UnknownDeviceException(rawDevice)

        mutate {
            this[added.identifier] = added
        }
    }

    suspend fun refresh(device: DVCADevice): DVCADevice {
        val updated = internalState.updateBlocking {
            val target = this[device.identifier] ?: throw UnknownDeviceException(device)

            val updated = usbManager.find(device.identifier) ?: throw UnknownDeviceException(device)

            mutate {
                this[target.identifier] = updated
            }
        }

        return updated[device.identifier] ?: throw UnknownDeviceException(device)
    }

    private fun UsbDevice.toDVCADevice(): DVCADevice = DVCADevice(
        raw = this,
        hasPermission = usbManager.hasPermission(this),
        hasRequestedPermission = false,
    )

    private fun UsbManager.find(identifier: String): DVCADevice? = deviceList.values.singleOrNull {
        it.identifier == identifier
    }?.toDVCADevice()

    companion object {
        private val TAG = App.logTag("Usb", "DeviceManager")
    }
}