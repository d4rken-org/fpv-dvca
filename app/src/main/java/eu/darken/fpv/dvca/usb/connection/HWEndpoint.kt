package eu.darken.fpv.dvca.usb.connection

import android.hardware.usb.UsbEndpoint
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.usb.connection.io.HasUsbStats
import eu.darken.fpv.dvca.usb.connection.io.read.AndroidUSBInputStream2
import eu.darken.fpv.dvca.usb.connection.io.read.UsbDataSourceBuffered
import eu.darken.fpv.dvca.usb.connection.io.read.UsbDataSourceDirect
import eu.darken.fpv.dvca.usb.connection.io.write.UsbDataSink
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.source
import timber.log.Timber

class HWEndpoint(
    private val connection: HWConnection,
    private val rawEndpoint: UsbEndpoint
) {

    private var readStatsSource: HasUsbStats = HasUsbStats.DUMMY
    val readStats: HasUsbStats
        get() = readStatsSource

    private var writeStatsSource: HasUsbStats = HasUsbStats.DUMMY
    val writeStats: HasUsbStats
        get() = writeStatsSource

    fun sink(): BufferedSink = UsbDataSink(
        connection,
        rawEndpoint
    ).buffer()

    fun source(readMode: ReadMode = ReadMode.BUFFER_BLOCKING): BufferedSource = when (readMode) {
        ReadMode.BUFFER_NOTBLOCKING -> AndroidUSBInputStream2(connection, rawEndpoint).run {
            readStatsSource = this
            source()
        }
        ReadMode.BUFFER_BLOCKING -> UsbDataSourceBuffered(connection, rawEndpoint).apply {
            readStatsSource = this
        }
        ReadMode.DIRECT -> UsbDataSourceDirect(connection, rawEndpoint).apply {
            readStatsSource = this
        }
    }.buffer().also {
        Timber.tag(TAG).v("source(readMode=$readMode) created: %s", it)
    }

    enum class ReadMode(val key: String) {
        BUFFER_NOTBLOCKING("buffered_not_blocking"),
        BUFFER_BLOCKING("buffered_blocking"),
        DIRECT("unbuffered_direct")
    }

    companion object {
        private val TAG = App.logTag("Usb", "Device", "Connection", "Interface", "Endpoint")
    }
}