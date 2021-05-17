package eu.darken.fpv.dvca.usb

import android.hardware.usb.UsbDevice
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.usb.connection.HWConnection
import eu.darken.fpv.dvca.usb.manager.HWDeviceManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class HWDevice(
    val hwManager: HWDeviceManager,
    val rawDevice: UsbDevice,
) {
    private val mutex = Mutex()

    val identifier: String
        get() = rawDevice.deviceName

    val label: String
        get() = "${rawDevice.manufacturerName} \"${rawDevice.productName}\" (${rawDevice.deviceName})"

    val hasPermission: Boolean
        get() = hwManager.hasPermission(this)

    private suspend fun requirePermission() {
        if (!hasPermission) hwManager.requestPermission(this)
    }

    private val openConnections = mutableSetOf<HWConnection>()

    suspend fun openConnection(): HWConnection = mutex.withLock {
        requirePermission()
        val connection = hwManager.openDevice(this)
        openConnections.add(connection)
        return connection
    }

    suspend fun release() = mutex.withLock {
        Timber.tag(TAG).d("Closing %d connections.", openConnections.size)
        openConnections.forEach { it.close() }
    }

    override fun toString(): String = label

    companion object {
        private val TAG = App.logTag("Usb", "Device")
    }
}