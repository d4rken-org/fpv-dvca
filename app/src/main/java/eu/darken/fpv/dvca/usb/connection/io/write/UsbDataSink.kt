package eu.darken.fpv.dvca.usb.connection.io.write

import android.hardware.usb.UsbEndpoint
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.usb.connection.HWConnection
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
    private val connection: HWConnection,
    private val receiver: UsbEndpoint,
) : Sink {


    private val TAG = App.logTag("Usb", "DataSink", connection.deviceIdentifier)

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
}