package eu.darken.fpv.dvca.gear.goggles.common

import eu.darken.androidstarter.common.logging.v
import eu.darken.androidstarter.common.logging.w
import okio.*
import java.io.IOException

class TeeSource(private val upstream: Source) : Source {

    private val sideStreams = mutableListOf<BufferedSink>()

    fun addSideSink(sink: BufferedSink) {
        v { "addSideSink(sink=$sink)" }
        sideStreams.add(sink)
    }

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = try {
            upstream.read(sink, byteCount)
        } catch (e: IOException) {
            sideStreams.iterator().forEach { it.tryClose() }
            throw e
        }

        if (bytesRead == -1L) {
            sideStreams.iterator().forEach { it.tryClose() }
            return -1L
        }

        val offset = sink.size - bytesRead

        with(sideStreams.listIterator()) {
            forEach {
                if (!it.isOpen) {
                    remove()
                    return@forEach
                }

                sink.copyTo(it.buffer, offset, bytesRead)
                it.emitCompleteSegments()
            }

            return bytesRead
        }
    }

    override fun close() {
        sideStreams.iterator().forEach { it.tryClose() }
        upstream.close()
    }

    override fun timeout(): Timeout = upstream.timeout()

    private fun Sink.tryClose() = try {
        close()
    } catch (e: Exception) {
        w { "Sink failed to close: already closed: $e" }
    }
}

fun Source.tee(): TeeSource = TeeSource(this)