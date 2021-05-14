package eu.darken.fpv.dvca.common.smart

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eu.darken.fpv.dvca.App
import timber.log.Timber

abstract class SmartActivity : AppCompatActivity() {
    internal val tag: String =
        App.logTag("Activity", this.javaClass.simpleName + "(" + Integer.toHexString(hashCode()) + ")")

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(tag).v("onCreate(savedInstanceState=$savedInstanceState)")
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        Timber.tag(tag).v("onResume()")
        super.onResume()
    }

    override fun onPause() {
        Timber.tag(tag).v("onPause()")
        super.onPause()
    }

    override fun onDestroy() {
        Timber.tag(tag).v("onDestroy()")
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.tag(tag).v("onActivityResult(requestCode=%d, resultCode=%d, data=%s)", requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}