package eu.darken.fpv.dvca.usb.connection

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.usb.manager.identifier
import timber.log.Timber
import java.io.Closeable

class HWConnection(
    private val rawDevice: UsbDevice,
    private val rawConnection: UsbDeviceConnection,
) : Closeable {

    val interfaceCount: Int
        get() = rawDevice.interfaceCount

    val deviceIdentifier: String
        get() = rawDevice.identifier

    fun getInterface(index: Int): HWInterface = HWInterface(
        connection = this,
        rawInterface = rawDevice.getInterface(index),
    )

    fun bulkTransfer(endpoint: UsbEndpoint, buffer: ByteArray, length: Int, timeout: Int): Int {
        return rawConnection.bulkTransfer(endpoint, buffer, length, timeout)
    }

    fun claimInterface(rawInterface: UsbInterface, forced: Boolean): Boolean {
        return rawConnection.claimInterface(rawInterface, forced)
    }

    fun releaseInterface(rawInterface: UsbInterface): Boolean {
        return rawConnection.releaseInterface(rawInterface)
    }

    override fun close() {
        Timber.tag(TAG).v("close()")
        rawConnection.close()
    }

    companion object {
        private val TAG = App.logTag("Usb", "Device", "Connection")
    }


}