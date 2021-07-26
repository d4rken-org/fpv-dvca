package eu.darken.fpv.dvca.common.preference.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.SwitchCompat

class SwitchPreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PreferenceView(
    context = context,
    attrs = attrs,
    defStyleAttr = defStyleAttr
) {
    private lateinit var toggle: SwitchCompat
    private var toggleListener: Listener? = null


    override fun onFinishInflate() {
        toggle = SwitchCompat(context)
        toggle.isClickable = false
        addExtra(toggle)
        setOnClickListener { view: View? -> if (toggleListener != null) performClick() }
        super.onFinishInflate()
    }

    var isChecked: Boolean
        get() = toggle.isChecked
        set(checked) {
            toggle.isChecked = checked
        }

    fun setOnCheckedChangedListener(listener: Listener) {
        toggleListener = listener
        setOnClickListener { _: View? ->
            toggle.isChecked = !toggle.isChecked
            listener.onCheckedChanged(this@SwitchPreferenceView, isChecked)
        }
    }

    interface Listener {
        fun onCheckedChanged(view: SwitchPreferenceView?, checked: Boolean)
    }
}