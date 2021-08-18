package eu.darken.fpv.dvca.common

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

// Can't be const because that prevents them from being mocked in tests
@Suppress("MayBeConstant")
object BuildVersionWrap {
    val SDK_INT = Build.VERSION.SDK_INT

    @ChecksSdkIntAtLeast(parameter = 0)
    fun hasAPILevel(level: Int): Boolean = SDK_INT >= level
}
