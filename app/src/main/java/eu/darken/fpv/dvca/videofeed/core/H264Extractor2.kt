package eu.darken.fpv.dvca.videofeed.core

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.extractor.*
import com.google.android.exoplayer2.extractor.SeekMap.Unseekable
import com.google.android.exoplayer2.extractor.ts.H264Reader
import com.google.android.exoplayer2.extractor.ts.SeiReader
import com.google.android.exoplayer2.extractor.ts.TsPayloadReader
import com.google.android.exoplayer2.extractor.ts.TsPayloadReader.TrackIdGenerator
import com.google.android.exoplayer2.util.ParsableByteArray

/**
 * Extracts data from H264 bitstreams.
 * Based on https://github.com/fpvout/DigiView-Android/blob/3d27932eb6786a23e296c7025f4f9b3c29c1faa1/app/src/main/java/com/fpvout/digiview/H264Extractor.java
 */
class H264Extractor2 : Extractor {
    private val reader: H264Reader = H264Reader(SeiReader(emptyList()), false, true)
    private val sampleData: ParsableByteArray = ParsableByteArray(MAX_SYNC_FRAME_SIZE)
    private var startedPacket = false
    private var firstSampleTimestampUs: Long = 0
    override fun sniff(input: ExtractorInput): Boolean = true

    override fun init(output: ExtractorOutput) {
        reader.createTracks(output, TrackIdGenerator(0, 1))
        output.endTracks()
        output.seekMap(Unseekable(C.TIME_UNSET))
    }

    override fun seek(position: Long, timeUs: Long) {
        startedPacket = false
        reader.seek()
    }

    override fun read(input: ExtractorInput, seekPosition: PositionHolder): Int {
        val bytesRead = input.read(sampleData.data, 0, MAX_SYNC_FRAME_SIZE)
        if (bytesRead == C.RESULT_END_OF_INPUT) return Extractor.RESULT_END_OF_INPUT

        sampleData.reset(bytesRead)

        reader.apply {
            packetStarted(firstSampleTimestampUs, TsPayloadReader.FLAG_DATA_ALIGNMENT_INDICATOR)
            consume(sampleData)
            packetFinished()
        }

        firstSampleTimestampUs += 200

        return Extractor.RESULT_CONTINUE
    }

    override fun release() {
        // Do nothing.
    }

    companion object {
        val FACTORY = ExtractorsFactory { arrayOf<Extractor>(H264Extractor2()) }
        private const val MAX_SYNC_FRAME_SIZE = 30 * 1024
    }

}