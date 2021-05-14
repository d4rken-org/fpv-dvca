package eu.darken.fpv.dvca.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

fun <T> LiveData<T>.observe2(fragment: Fragment, callback: (T) -> Unit) {
    observe(fragment.viewLifecycleOwner, { callback.invoke(it) })
}

