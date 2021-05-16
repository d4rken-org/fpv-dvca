package eu.darken.fpv.dvca.usb.connection.io

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import eu.darken.fpv.dvca.App
import okio.Buffer
import okio.IOException
import okio.Sink
import okio.Timeout
import timber.log.Timber
import java.io.InterruptedIOException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class UsbDataSink constructor(
    private val receiver: UsbEndpoint,
    private val connection: UsbDeviceConnection,
) : Sink {

    private val writeQueue = LinkedBlockingQueue<ByteArray?>(512)
    private val timeout = Timeout()
    private var open = true

    init {
        thread {
            Timber.tag(TAG).d("Write worker starting...")
            while (open) {
                try {
                    val toWrite = writeQueue.poll(100, TimeUnit.MILLISECONDS)

                    if (toWrite == null || toWrite.isEmpty()) continue

                    Timber.tag(TAG).v("Writing %s", toWrite)

                    connection.bulkTransfer(receiver, toWrite, toWrite.size, 2000)
                } catch (e: InterruptedIOException) {
                    Timber.tag(TAG).w(e, "Failed to get data to write.")
                }
            }
            Timber.tag(TAG).w("Write worker quit...")
        }
    }

    override fun write(source: Buffer, byteCount: Long) {
        writeQueue.add(source.readByteArray())
    }

    override fun flush() {}

    override fun timeout(): Timeout = timeout

    @Throws(IOException::class)
    override fun close() {
        Timber.tag(TAG).v("close()")
        open = false
    }

    companion object {
        private val TAG = App.logTag("Usb", "DataSink")
    }
}