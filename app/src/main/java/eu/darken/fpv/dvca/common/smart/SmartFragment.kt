package eu.darken.fpv.dvca.common.smart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import eu.darken.fpv.dvca.App
import timber.log.Timber


abstract class SmartFragment(
    @LayoutRes layoutRes: Int
) : Fragment(layoutRes) {

    @Suppress("JoinDeclarationAndAssignment")
    internal val tag: String

    init {
        tag = App.logTag("Fragment", this.javaClass.simpleName + "(" + Integer.toHexString(hashCode()) + ")")
    }

    override fun onAttach(context: Context) {
        Timber.tag(tag).v("onAttach(context=$context)")
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(tag).v("onCreate(savedInstanceState=$savedInstanceState)")
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.tag(tag).v("onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
        super.onViewCreated(view, savedInstanceState)
    }

    @Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Timber.tag(tag).v("onActivityCreated(savedInstanceState=$savedInstanceState)")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        Timber.tag(tag).v("onResume()")
        super.onResume()
    }

    override fun onPause() {
        Timber.tag(tag).v("onPause()")
        super.onPause()
    }

    override fun onDestroyView() {
        Timber.tag(tag).v("onDestroyView()")
        super.onDestroyView()
    }

    override fun onDetach() {
        Timber.tag(tag).v("onDetach()")
        super.onDetach()
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
