package eu.darken.fpv.dvca.feedplayer.ui.vr

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import eu.darken.androidstarter.common.logging.d
import eu.darken.androidstarter.common.logging.w
import eu.darken.fpv.dvca.App
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.feedplayer.core.vr.gl.EglCore
import eu.darken.fpv.dvca.feedplayer.core.vr.gl.FullFrameRect
import eu.darken.fpv.dvca.feedplayer.core.vr.gl.Texture2dProgram
import eu.darken.fpv.dvca.feedplayer.core.vr.gl.WindowSurface

/**
 * Based on
 * https://github.com/fpvout/DigiView-Android/blob/c95962f640236be7f1970dbdb4e3134a0fb28e61/app/src/main/java/com/fpvout/digiview/vr/views/VrView.java
 */
class VrView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), SurfaceTexture.OnFrameAvailableListener {
    private val transformMatrix = FloatArray(16)

    private var surfaceViewLeft: SurfaceView
    private var surfaceViewRight: SurfaceView

    private lateinit var rightEyeSurface: WindowSurface
    private lateinit var leftEyeSurface: WindowSurface

    private val eglCore = EglCore()

    private val fullFrameBlit by lazy { FullFrameRect(Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT)) }
    private val textureId by lazy { fullFrameBlit.createTextureObject() }
    private val videoSurfaceTexture by lazy {
        SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener(this@VrView)
        }
    }

    lateinit var surface: Surface

    private var isReleased = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_vr, this)
        surfaceViewLeft = findViewById(R.id.eye_left)
        surfaceViewRight = findViewById(R.id.eye_right)

        surfaceViewLeft.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                leftEyeSurface = WindowSurface(eglCore, holder.surface, false).apply {
                    makeCurrent()
                }
                surface = Surface(videoSurfaceTexture)
                d(TAG) { "EYE-Left is ready" }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
        surfaceViewRight.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                rightEyeSurface = WindowSurface(eglCore, holder.surface, false)
                d(TAG) { "EYE-Right is ready" }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
    }

    override fun onFinishInflate() {
        d(TAG) { "onFinishInflate()" }
        super.onFinishInflate()
        d(TAG) { "onFinishInflate()'ed" }
    }

    fun release() {
        d(TAG) { "release()" }
        isReleased = true

        surface.release()
        videoSurfaceTexture.release()
        leftEyeSurface.release()
        rightEyeSurface.release()
        fullFrameBlit.release(false)
        eglCore.release()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        if (isReleased) {
            w(TAG) { "Skipping frame drawing, surface is released." }
            return
        }

        leftEyeSurface.drawFrame()
        rightEyeSurface.drawFrame()
    }

    private fun WindowSurface.drawFrame() {
        makeCurrent()

        videoSurfaceTexture.apply {
            updateTexImage()
            getTransformMatrix(transformMatrix)
        }

        GLES20.glViewport(0, 0, width, height)

        fullFrameBlit.drawFrame(textureId, transformMatrix)
        swapBuffers()
    }

    companion object {
        private val TAG = App.logTag("VrFeed", "VrView")
    }
}

