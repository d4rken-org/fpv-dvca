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
    private var storedSerial: String? = null
    val serialNumber: SerialNumber?
        get() {
            val number = storedSerial ?: try {
                rawDevice.serialNumber?.also { storedSerial = it }
            } catch (e: SecurityException) {
                null
            }

            return number?.let { SerialNumber(it) }
        }

    val identifier: String
        get() = rawDevice.deviceName

    val productName: String
        get() = rawDevice.productName ?: rawDevice.manufacturerName ?: rawDevice.deviceName

    val hasPermission: Boolean
        get() = hwManager.hasPermission(this)

    val logId: String
        get() = "${rawDevice.deviceName} ($productName)"

    private val openConnections = mutableSetOf<HWConnection>()

    suspend fun openConnection(): HWConnection = mutex.withLock {
        val connection = hwManager.openDevice(this)
        openConnections.add(connection)
        return connection
    }

    suspend fun release() = mutex.withLock {
        Timber.tag(TAG).d("Closing %d connections.", openConnections.size)
        openConnections.forEach { it.close() }
    }

    override fun toString(): String = logId

    companion object {
        private val TAG = App.logTag("Usb", "Device")
    }

    @JvmInline
    value class SerialNumber(val serialNumber: String)
}