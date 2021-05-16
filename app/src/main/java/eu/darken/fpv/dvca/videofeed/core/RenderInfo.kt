package eu.darken.fpv.dvca.videofeed.core

data class RenderInfo(
    val frames: Int,
    val buffers: Int,
    val dropped: Int
) {
    override fun toString(): String {
        return "FPS[screen=$frames, buffer=$buffers, drop=$dropped]"
    }
}