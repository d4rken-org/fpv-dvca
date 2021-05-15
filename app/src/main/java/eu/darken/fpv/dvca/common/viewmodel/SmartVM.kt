package eu.darken.fpv.dvca.common.viewmodel

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import eu.darken.fpv.dvca.common.coroutine.DefaultDispatcherProvider
import eu.darken.fpv.dvca.common.coroutine.DispatcherProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import timber.log.Timber
import kotlin.coroutines.CoroutineContext


abstract class SmartVM(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : VM() {

    fun <T> Flow<T>.asLiveData2() = this.asLiveData(context = dispatcherProvider.Default)

    fun launch(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = dispatcherProvider.Default,
        errorHandler: CoroutineExceptionHandler? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val combinedContext = errorHandler?.let { context + it } ?: context
        try {
            scope.launch(context = combinedContext, block = block)
        } catch (e: CancellationException) {
            Timber.w(e, "launch()ed coroutine was canceled (scope=%s).", scope)
        }
    }

    fun <T> Flow<T>.launchInViewModel() = this.launchIn(viewModelScope + dispatcherProvider.Default)

}