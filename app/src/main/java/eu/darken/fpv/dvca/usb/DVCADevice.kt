package eu.darken.fpv.dvca.usb

import android.hardware.usb.UsbDevice
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.usb.connection.DVCAConnection
import eu.darken.fpv.dvca.usb.manager.DVCADeviceManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class DVCADevice(
    val dvcaManager: DVCADeviceManager,
    val rawDevice: UsbDevice,
) {
    private val mutex = Mutex()

    val identifier: String
        get() = rawDevice.deviceName

    val label: String
        get() = "${rawDevice.manufacturerName} \"${rawDevice.productName}\" (${rawDevice.deviceName})"

    val hasPermission: Boolean
        get() = dvcaManager.hasPermission(this)

    private suspend fun requirePermission() {
        if (!hasPermission) dvcaManager.requestPermission(this)
    }

    private val openConnections = mutableSetOf<DVCAConnection>()

    suspend fun openConnection(): DVCAConnection = mutex.withLock {
        requirePermission()
        val connection = dvcaManager.openDevice(this)
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