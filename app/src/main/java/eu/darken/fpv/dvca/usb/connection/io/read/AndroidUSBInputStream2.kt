package eu.darken.fpv.dvca.usb.connection.io.read

/*
* Copyright 2019, Digi International Inc.
*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/.
*
* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
* WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
* MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
* ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
* WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
* ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
* OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.os.SystemClock
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.usb.connection.io.CircularByteBuffer
import eu.darken.fpv.dvca.usb.connection.io.HasUsbStats
import timber.log.Timber
import java.io.InputStream
import kotlin.concurrent.thread


/**
 * This class acts as a wrapper to read data from the USB Interface in Android
 * behaving like an `InputputStream` class.
 */

/**
 * Class constructor. Instantiates a new `AndroidUSBInputStream`
 * object with the given parameters.
 *
 * @see UsbDeviceConnection
 *
 * @see UsbEndpoint
 */
class AndroidUSBInputStream2(
    private val receiveEndPoint: UsbEndpoint,
    private val usbConnection: UsbDeviceConnection,
) : InputStream(), HasUsbStats {

    private var open = true
    private var readBuffer: CircularByteBuffer = CircularByteBuffer(32 * 1024 * 1024)

    private var usbReadRate: Double = 0.0
    override val usbReadMbs: Double
        get() = usbReadRate
    private var bufferReadRate: Double = 0.0
    override val bufferReadMbs: Double
        get() = bufferReadRate

    init {
        var bytesRead = 0L
        var lastRead = SystemClock.elapsedRealtime()

        thread {
            while (open) {
                val buffer = ByteArray(1024)
                val receivedBytes =
                    usbConnection.bulkTransfer(receiveEndPoint, buffer, buffer.size, READ_TIMEOUT)
                if (receivedBytes > 0) {
                    val data = ByteArray(receivedBytes)
                    System.arraycopy(buffer, 0, data, 0, receivedBytes)
                    readBuffer.write(buffer, 0, receivedBytes)
                }

                bytesRead += receivedBytes

                val nowMs = SystemClock.elapsedRealtime()
                if (nowMs - lastRead > 5_000) {
                    val mbPerSecond = (bytesRead / (1000 * 1000)) / 5.0
                    usbReadRate = mbPerSecond
                    lastRead = nowMs
                    bytesRead = 0L
                    Timber.tag(TAG).v("Reading $mbPerSecond MB/s from USB")
                }
            }
            usbReadRate = -1.0
            bufferReadRate = -1.0
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    override fun read(): Int {
        val buffer = ByteArray(1)
        read(buffer)
        return buffer[0].toInt() and 0xFF
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#read(byte[])
     */
    override fun read(buffer: ByteArray): Int = read(buffer, 0, buffer.size)

    private var bufferBytesRead = 0L
    private var bufferBytesLast = SystemClock.elapsedRealtime()

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val deadLine = System.currentTimeMillis() + READ_TIMEOUT
        var readBytes = 0
        while (System.currentTimeMillis() < deadLine && readBytes <= 0) {
            readBytes = readBuffer.read(buffer, offset, length)
        }
        if (readBytes <= 0) return -1

        val readData = ByteArray(readBytes)
        System.arraycopy(buffer, offset, readData, 0, readBytes)
        return readBytes.also {
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
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#available()
     */
    override fun available(): Int = readBuffer.availableToRead()

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#skip(long)
     */
    override fun skip(byteCount: Long): Long = if (byteCount <= 0) {
        0
    } else {
        readBuffer.skip(byteCount.toInt()).toLong()
    }

    /**
     * Stops the USB input stream read thread.
     *
     * @see .startReadThread
     */
    override fun close() {
        open = false
        super.close()
    }

    companion object {
        private const val READ_TIMEOUT = 200
        private val TAG = App.logTag("Usb", "AndroidUSBInputStream2")
    }
}