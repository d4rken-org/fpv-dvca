package eu.darken.fpv.dvca.feedplayer.core.player.exo

data class RenderInfo(
    val frames: Int,
    val buffers: Int,
    val dropped: Int
) {
    override fun toString(): String {
        return "FPS[screen=$frames, buffer=$buffers, drop=$dropped]"
    }
}