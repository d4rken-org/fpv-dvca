package eu.darken.fpv.dvca.common

import eu.darken.fpv.dvca.BuildConfig


// Can't be const because that prevents them from being mocked in tests
@Suppress("MayBeConstant")
object BuildConfigWrap {

    val DEBUG: Boolean = BuildConfig.DEBUG
    val BUILD_TYPE: String = BuildConfig.BUILD_TYPE

    val VERSION_CODE: Long = BuildConfig.VERSION_CODE.toLong()
}
