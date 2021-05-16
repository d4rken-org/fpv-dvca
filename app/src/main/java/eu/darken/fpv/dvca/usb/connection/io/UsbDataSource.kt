package eu.darken.fpv.dvca.usb.connection.io

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.os.SystemClock
import eu.darken.fpv.dvca.App
import okio.*
import timber.log.Timber
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class UsbDataSource(
    private val sender: UsbEndpoint,
    private val connection: UsbDeviceConnection,
) : Source {

    private val pipe = Pipe(16 * 1024 * 1024)
    private val source = pipe.source.also {
        timeout().timeout(200, TimeUnit.MILLISECONDS)
    }
    private val sink = pipe.sink.buffer().also {
        timeout().timeout(200, TimeUnit.MILLISECONDS)
    }
    private var open = true

    init {
        thread {
            Timber.tag(TAG).d("Read worker starting...")

            val readSize = sender.maxPacketSize * 3
            val transferBuffer = ByteArray(readSize)

            var bytesRead = 0L
            var lastRead = SystemClock.elapsedRealtime()

            while (open) {
                val receivedBytes = connection.bulkTransfer(
                    sender,
                    transferBuffer,
                    readSize,
                    100
                )

                if (receivedBytes > 0) {
                    try {
                        sink.write(transferBuffer, 0, receivedBytes)
                        sink.emit()
                    } catch (e: InterruptedIOException) {
                        Timber.tag(TAG).w(e, "Buffer is full.")
                    }
                }

                bytesRead += receivedBytes

                val nowMs = SystemClock.elapsedRealtime()
                if (nowMs - lastRead > 5_000) {
                    val mbPerSecond = (bytesRead / (1000 * 1000)) / 5.0

                    lastRead = nowMs
                    bytesRead = 0L
                    Timber.tag(TAG).v("Reading $mbPerSecond MB/s from USB")

                    usbReadRate = mbPerSecond
                }
            }
            Timber.tag(TAG).w("Read worker quit...")
            sink.close()
        }
    }

    private var bufferBytesRead = 0L
    private var bufferBytesLast = SystemClock.elapsedRealtime()

    override fun read(sink: Buffer, byteCount: Long): Long = source.read(sink, byteCount).also {
        bufferBytesRead += it

        val nowMs = SystemClock.elapsedRealtime()
        if (nowMs - bufferBytesLast > 5_000) {
            val mbPerSecond = (bufferBytesRead / (1000 * 1000)) / 5.0

            bufferBytesLast = nowMs
            bufferBytesRead = 0L
            Timber.tag(TAG).v("Reading $mbPerSecond MB/s from buffer")

            bufferReadRate = mbPerSecond
        }
    }

    override fun cursor(): Cursor? = source.cursor()

    override fun timeout(): Timeout = pipe.source.timeout()

    @Throws(IOException::class)
    override fun close() {
        Timber.tag(TAG).v("close()")
        open = false
    }

    companion object {
        private val TAG = App.logTag("Usb", "DataSource")
        var usbReadRate: Double = 0.0
        var bufferReadRate: Double = 0.0
    }
}