package eu.darken.fpv.dvca.gear.goggles.djifpv

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import eu.darken.fpv.dvca.gear.goggles.Goggles
import okio.BufferedSource
import okio.Pipe
import okio.buffer
import timber.log.Timber

class ExoDataSource(
    private val videoFeed: Goggles.VideoFeed,
    private val tag: String,
) : DataSource {
    private var exoBuffer: BufferedSource? = null

    override fun getUri(): Uri? = Uri.EMPTY

    override fun addTransferListener(transferListener: TransferListener) {}

    override fun open(dataSpec: DataSpec): Long {
        Timber.tag(tag).v("open(dataSpec=%s) this=%s", dataSpec, this)
        val pipe = Pipe(16320)
        videoFeed.source.addDownStream(pipe.sink.buffer())
        exoBuffer = pipe.source.buffer()

        return if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else {
            C.LENGTH_UNSET.toLong()
        }
    }

    override fun read(target: ByteArray, offset: Int, length: Int): Int {
        return exoBuffer?.read(target, offset, length) ?: -1
    }

    override fun close() {
        Timber.tag(tag).d("close(), source, this=%s", this)
        exoBuffer?.close()
        exoBuffer = null
    }
}