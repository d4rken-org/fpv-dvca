package eu.darken.fpv.dvca.usb.connection

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.usb.connection.io.AndroidUSBInputStream2
import eu.darken.fpv.dvca.usb.connection.io.UsbDataSink
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.source
import timber.log.Timber
import java.io.Closeable

class DVCAConnection(
    private val rawDevice: UsbDevice,
    private val rawConnection: UsbDeviceConnection,
) : Closeable {

    val interfaceCount: Int
        get() = rawDevice.interfaceCount

    fun getInterface(index: Int): Interface {
        return Interface(
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

    class Interface(
        private val rawConnection: UsbDeviceConnection,
        private val rawInterface: UsbInterface,
    ) {

        val endpointCount: Int
            get() = rawInterface.endpointCount

        fun getEndpoint(index: Int): Endpoint = Endpoint(
            rawConnection = rawConnection,
            rawEndpoint = rawInterface.getEndpoint(index)
        )

        fun claim(forced: Boolean = false) {
            rawConnection.claimInterface(rawInterface, forced).also {
                Timber.tag(TAG).v("claim(forced=$forced): $it")
            }
        }

        fun release(): Boolean {
            return rawConnection.releaseInterface(rawInterface).also {
                Timber.tag(TAG).v("release(): $it")
            }
        }

        fun use(forced: Boolean = false, onUse: Interface.() -> Unit) = try {
            claim(forced = forced)
            onUse(this)
        } finally {
            release()
        }

        companion object {
            private val TAG = App.logTag("Usb", "Device", "Connection", "Interface")
        }

        class Endpoint(
            private val rawConnection: UsbDeviceConnection,
            private val rawEndpoint: UsbEndpoint
        ) {

            fun sink(): BufferedSink = UsbDataSink(
                rawEndpoint,
                rawConnection
            ).buffer()

            fun source(): BufferedSource = AndroidUSBInputStream2(
                rawEndpoint,
                rawConnection
            ).source().buffer()

            // TODO Tweak to gain better USB read performance and less delay
//            fun source(): BufferedSource = UsbDataSource(
//                rawEndpoint,
//                rawConnection
//            ).buffer()

            companion object {
                private val TAG = App.logTag("Usb", "Device", "Connection", "Interface", "Endpoint")
            }
        }
    }


}