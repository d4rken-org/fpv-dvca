package eu.darken.fpv.dvca.common.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import eu.darken.fpv.dvca.App
import timber.log.Timber

abstract class VM : ViewModel() {
   private val TAG: String = App.logTag("VM", javaClass.simpleName)

    init {
        Timber.tag(TAG).v("Initialized")
    }

    @CallSuper
    override fun onCleared() {
        Timber.tag(TAG).v("onCleared()")
        super.onCleared()
    }
}