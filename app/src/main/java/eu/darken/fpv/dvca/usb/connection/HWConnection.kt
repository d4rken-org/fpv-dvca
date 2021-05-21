package eu.darken.fpv.dvca.usb.connection

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
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

    fun getInterface(index: Int): HWInterface {

        return HWInterface(
            rawConnection = rawConnection,
            rawInterface = rawDevice.getInterface(index),
        )
    }

    override fun close() {
        Timber.tag(TAG).v("close()")
        rawConnection.close()
    }

    companion object {
        private val TAG = App.logTag("Usb", "Device", "Connection")
    }


}