package eu.darken.fpv.dvca.gear.goggles.common

import eu.darken.androidstarter.common.logging.e
import eu.darken.androidstarter.common.logging.v
import eu.darken.androidstarter.common.logging.w
import okio.*
import kotlin.concurrent.thread

class HubSource(private val upstream: Source) {
    private val downStreams = mutableListOf<BufferedSink>()
    private val blackhole = blackholeSink().buffer()
    private var isRunning = true

    init {
        thread {
            v { "Starting to feed blackhole" }
            while (isRunning) {
                try {
                    blackhole.write(wrappedSource, 8192)
                } catch (err: Exception) {
                    e(throwable = err) { "Failed to feed blackhole" }
                    close()
                }
            }
            wrappedSource.close()
            v { "Finished feeding blackhole" }
        }
    }

    fun addDownStream(sink: BufferedSink) {
        v { "addDownStream(sink=$sink)" }
        downStreams.add(sink)
    }

    private val wrappedSource = object : Source {
        override fun read(sink: Buffer, byteCount: Long): Long {
            val bytesRead = try {
                upstream.read(sink, byteCount)
            } catch (e: Exception) {
                close()
                throw e
            }

            if (bytesRead == -1L) {
                close()
                return -1L
            }

            val offset = sink.size - bytesRead

            with(downStreams.listIterator()) {
                forEach {
                    try {
                        if (!it.isOpen) throw IOException("Sink was closed $it")
                        sink.copyTo(it.buffer, offset, bytesRead)
                        it.emitCompleteSegments()
                    } catch (e: Exception) {
                        w(throwable = e) { "Writing failed, removing from hub: $it" }
                        remove()
                        return@forEach
                    }
                }
                return bytesRead
            }
        }

        override fun close() {
            downStreams.iterator().forEach { it.tryClose() }
            upstream.close()
        }

        override fun timeout(): Timeout = upstream.timeout()
    }

    private fun Sink.tryClose() = try {
        close()
    } catch (e: Exception) {
        w { "Sink failed to close: already closed: $e" }
    }

    fun close() {
        v { "close() called on hub." }
        isRunning = false
        downStreams.iterator().forEach {
            if (it.isOpen) {
                v { "Closing downstream: $it" }
                it.tryClose()
            }
        }
        downStreams.clear()
    }
}

fun Source.hub(): HubSource = HubSource(this)