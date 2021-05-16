package eu.darken.fpv.dvca.usb.manager

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.common.collections.mutate
import eu.darken.fpv.dvca.common.coroutine.AppScope
import eu.darken.fpv.dvca.common.flow.HotDataFlow
import eu.darken.fpv.dvca.usb.DVCADevice
import eu.darken.fpv.dvca.usb.connection.DVCAConnection
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

    fun onDeviceAttached(rawDevice: UsbDevice) = internalState.updateAsync(
        onError = { Timber.tag(TAG).e(it, "ATTACHED failed for %s", rawDevice) }
    ) {
        Timber.tag(TAG).v("onDeviceAttached(rawDevice=%s)", rawDevice.label)

        val added = usbManager.find(rawDevice.identifier) ?: throw UnknownDeviceException(rawDevice)

        this[rawDevice.identifier]?.let {
            Timber.tag(TAG).w("Double attach? Releasing previous device: %s", it)
            it.release()
        }

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
            Timber.tag(TAG).i("Removing detached devices %s", toRemove.label)
            toRemove.release()
        }

        mutate {
            remove(toRemove.identifier)
        }
    }

    private fun UsbDevice.toDVCADevice(): DVCADevice = DVCADevice(
        dvcaManager = this@DVCADeviceManager,
        rawDevice = this,
    )

    private fun UsbManager.find(identifier: String): DVCADevice? = deviceList.values.singleOrNull {
        it.identifier == identifier
    }?.toDVCADevice()

    internal suspend fun openDevice(device: DVCADevice): DVCAConnection {
        Timber.tag(TAG).d("Opening a connection to %s", device)
        val rawConnection = usbManager.openDevice(device.rawDevice)
        return DVCAConnection(
            rawDevice = device.rawDevice,
            rawConnection = rawConnection
        ).also { Timber.tag(TAG).d("Connection to %s opened: %s", device.label, it) }
    }

    fun hasPermission(device: DVCADevice): Boolean {
        return usbManager.hasPermission(device.rawDevice)
    }

    suspend fun requestPermission(device: DVCADevice): Boolean {
        return usbPermissionHandler.requestPermission(device)
    }

    companion object {
        private val TAG = App.logTag("Usb", "Manager")
    }
}