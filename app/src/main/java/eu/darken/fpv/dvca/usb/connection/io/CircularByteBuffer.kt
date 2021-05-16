package eu.darken.fpv.dvca.usb.connection.io
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
/**
 * Helper class used to store data bytes as a circular buffer.
 */

/**
 * Instantiates a new `CircularByteBuffer` with the given capacity
 * in bytes.
 *
 * @param size Circular byte buffer size in bytes.
 * @throws IllegalArgumentException if `size < 1`.
 */
class CircularByteBuffer(size: Int) {
    // Variables.
    private val buffer: ByteArray = ByteArray(size).also {
        require(size >= 1) { "Buffer size must be greater than 0." }
    }

    /**
     * Returns the current read index.
     *
     * @return readIndex The current read index.
     */
    private var readIndex: Int = 0

    /**
     * Returns the current write index.
     *
     * @return writeIndex The current write index.
     */
    private var writeIndex: Int = 0
    private var empty = true

    /**
     * Writes the given amount of bytes to the circular byte buffer.
     *
     * @param data     Bytes to write.
     * @param offset   Offset inside data where bytes to write start.
     * @param numBytes Number of bytes to write.
     * @return The number of bytes actually written.
     * @throws IllegalArgumentException if `offset < 0` or
     * if `numBytes < 1`.
     * @throws NullPointerException     if `data == null`.
     * @see .read
     * @see .skip
     */
    @Synchronized fun write(data: ByteArray?, offset: Int, numBytes: Int): Int {
        var toWrite = numBytes
        if (data == null) throw NullPointerException("Data cannot be null.")
        require(offset >= 0) { "Offset cannot be negative." }
        require(toWrite >= 1) { "Number of bytes to write must be greater than 0." }

        // Check if there are enough bytes to write.
        val availableBytes = data.size - offset
        if (toWrite > availableBytes) toWrite = availableBytes

        // Check where we should start writing.
        if (toWrite < buffer.size - writeIndex) {
            System.arraycopy(data, offset, buffer, writeIndex, toWrite)
            writeIndex += toWrite
        } else {
            System.arraycopy(data, offset, buffer, writeIndex, buffer.size - writeIndex)
            System.arraycopy(data, offset + buffer.size - writeIndex, buffer, 0, toWrite - (buffer.size - writeIndex))
            writeIndex = toWrite - (buffer.size - writeIndex)
            if (readIndex < writeIndex) readIndex = writeIndex
        }

        // Check if we were able to write all the bytes.
        if (toWrite > getCapacity()) toWrite = getCapacity()
        empty = false
        return toWrite
    }

    /**
     * Reads the given amount of bytes to the given array from the circular byte
     * buffer.
     *
     * @param data     Byte buffer to place read bytes in.
     * @param offset   Offset inside data to start placing read bytes in.
     * @param numBytes Number of bytes to read.
     * @return The number of bytes actually read.
     * @throws IllegalArgumentException if `offset < 0` or
     * if `numBytes < 1`.
     * @throws NullPointerException     if `data == null`.
     * @see .skip
     * @see .write
     */
    @Synchronized fun read(data: ByteArray?, offset: Int, numBytes: Int): Int {
        if (data == null) throw NullPointerException("Data cannot be null.")
        require(offset >= 0) { "Offset cannot be negative." }
        require(numBytes >= 1) { "Number of bytes to read must be greater than 0." }

        // If we are empty, return 0.
        if (empty) return 0

        // If we try to place bytes in an index bigger than buffer index, return 0 read bytes.
        if (offset >= data.size) return 0
        if (data.size - offset < numBytes) return read(data, offset, data.size - offset)
        if (availableToRead() < numBytes) return read(data, offset, availableToRead())
        readIndex = if (numBytes < buffer.size - readIndex) {
            System.arraycopy(buffer, readIndex, data, offset, numBytes)
            readIndex + numBytes
        } else {
            System.arraycopy(buffer, readIndex, data, offset, buffer.size - readIndex)
            System.arraycopy(
                buffer,
                0,
                data,
                offset + buffer.size - readIndex,
                numBytes - (buffer.size - readIndex)
            )
            numBytes - (buffer.size - readIndex)
        }

        // If we have read all bytes, set the buffer as empty.
        if (readIndex == writeIndex) empty = true
        return numBytes
    }

    /**
     * Skips the given number of bytes from the circular byte buffer.
     *
     * @param numBytes Number of bytes to skip.
     * @return The number of bytes actually skipped.
     * @throws IllegalArgumentException if `numBytes < 1`.
     * @see .read
     * @see .write
     */
    @Synchronized fun skip(numBytes: Int): Int {
        require(numBytes >= 1) { "Number of bytes to skip must be greater than 0." }

        // If we are empty, return 0.
        if (empty) return 0
        if (availableToRead() < numBytes) return skip(availableToRead())
        readIndex = if (numBytes < buffer.size - readIndex) {
            readIndex + numBytes // If we have skipped all bytes, set the buffer as empty.
        } else {
            // If we have skipped all bytes, set the buffer as empty.
            numBytes - (buffer.size - readIndex)
        }

        if (readIndex == writeIndex) empty = true
        return numBytes
    }

    /**
     * Returns the available number of bytes to read from the byte buffer.
     *
     * @return The number of bytes in the buffer available for reading.
     * @see .getCapacity
     * @see .read
     */
    fun availableToRead(): Int {
        if (empty) return 0
        return if (readIndex < writeIndex) {
            writeIndex - readIndex
        } else {
            buffer.size - readIndex + writeIndex
        }
    }

    /**
     * Returns the circular byte buffer capacity.
     *
     * @return The circular byte buffer capacity.
     */
    fun getCapacity(): Int = buffer.size

    /**
     * Clears the circular buffer.
     */
    fun clearBuffer() {
        empty = true
        readIndex = 0
        writeIndex = 0
    }
}