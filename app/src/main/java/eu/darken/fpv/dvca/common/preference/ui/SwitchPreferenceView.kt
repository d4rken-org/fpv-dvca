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
    private var toggleListener: ((SwitchPreferenceView, Boolean) -> Unit)? = null


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

    fun setOnCheckedChangedListener(listener: (SwitchPreferenceView, Boolean) -> Unit) {
        toggleListener = listener
        setOnClickListener {
            toggle.isChecked = !toggle.isChecked
            listener(this@SwitchPreferenceView, isChecked)
        }
    }
}