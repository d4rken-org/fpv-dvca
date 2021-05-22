package eu.darken.fpv.dvca.usb.connection.io.read

import android.hardware.usb.UsbEndpoint
import android.os.SystemClock
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.usb.connection.HWConnection
import eu.darken.fpv.dvca.usb.connection.io.HasUsbStats
import okio.Buffer
import okio.Cursor
import okio.Source
import okio.Timeout
import timber.log.Timber

class UsbDataSourceDirect(
    private val connection: HWConnection,
    private val sender: UsbEndpoint,
) : Source, HasUsbStats {

    private val tag = App.logTag("Usb", "UsbDataSourceDirect", connection.deviceIdentifier)
    private var bufferBytesRead = 0L
    private var bufferBytesLast = SystemClock.elapsedRealtime()
    private val transferBuffer = ByteArray(2 * 131072)

    private var usbReadRate: Double = -1.0
    override val usbReadMbs: Double
        get() = usbReadRate

    override fun read(sink: Buffer, byteCount: Long): Long {
        val readBytes = connection.bulkTransfer(sender, transferBuffer, byteCount.toInt(), 200)

        sink.write(transferBuffer, 0, readBytes)
        sink.flush()

        bufferBytesRead += readBytes
        val nowMs = SystemClock.elapsedRealtime()
        if (nowMs - bufferBytesLast > 5_000) {
            val mbPerSecond = (bufferBytesRead / (1000 * 1000)) / 5.0

            bufferBytesLast = nowMs
            bufferBytesRead = 0L
            Timber.tag(tag).v("Reading $mbPerSecond MB/s from buffer")

            usbReadRate = mbPerSecond
        }

        return readBytes.toLong()
    }

    override fun cursor(): Cursor? = null

    override fun timeout(): Timeout = Timeout.NONE

    override fun close() {
        Timber.tag(tag).v("close()")
        usbReadRate = -1.0
    }
}