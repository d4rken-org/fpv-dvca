package eu.darken.fpv.dvca.usb.connection

import android.hardware.usb.UsbInterface
import eu.darken.fpv.dvca.App
import timber.log.Timber

class HWInterface(
    private val connection: HWConnection,
    private val rawInterface: UsbInterface,
) {

    val endpointCount: Int
        get() = rawInterface.endpointCount

    fun getEndpoint(index: Int): HWEndpoint = HWEndpoint(
        connection = connection,
        rawEndpoint = rawInterface.getEndpoint(index)
    )

    fun claim(forced: Boolean = false) {
        connection.claimInterface(rawInterface, forced).also {
            Timber.tag(TAG).v("claim(forced=$forced): $it")
        }
    }

    fun release(): Boolean {
        return connection.releaseInterface(rawInterface).also {
            Timber.tag(TAG).v("release(): $it")
        }
    }

    fun use(forced: Boolean = false, onUse: HWInterface.() -> Unit) = try {
        claim(forced = forced)
        onUse(this)
    } finally {
        release()
    }

    companion object {
        private val TAG = App.logTag("Usb", "Device", "Connection", "Interface")
    }

}