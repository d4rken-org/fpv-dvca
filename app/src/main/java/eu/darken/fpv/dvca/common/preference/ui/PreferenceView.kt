package eu.darken.fpv.dvca.common.preference.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isGone
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.databinding.ViewPreferenceBinding

open class PreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewPreferenceBinding = ViewPreferenceBinding.inflate(LayoutInflater.from(context), this)

    init {
        getContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).use {
            background = it.getDrawable(0)
        }

        binding.apply {
            getContext().obtainStyledAttributes(attrs, R.styleable.PreferenceView).use { typedArray ->
                icon.setImageResource(typedArray.getResourceId(R.styleable.PreferenceView_android_icon, 0))
                title.setText(typedArray.getResourceId(R.styleable.PreferenceView_android_title, 0))
                val descId = typedArray.getResourceId(R.styleable.PreferenceView_android_description, 0)
                if (descId != 0) description.setText(descId) else description.visibility = GONE
            }
        }
    }

    fun addExtra(view: View?) {
        binding.apply {
            extra.addView(view)
            extra.isGone = view == null
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setEnabledRecursion(this, enabled)
    }

    fun setIcon(@DrawableRes iconRes: Int) {
        binding.icon.setImageResource(iconRes)
    }

    fun setDescription(desc: String?) {
        binding.description.text = desc
    }

    companion object {
        private fun setEnabledRecursion(vg: ViewGroup, enabled: Boolean) {
            for (i in 0 until vg.childCount) {
                val child = vg.getChildAt(i)
                if (child is ViewGroup) setEnabledRecursion(child, enabled) else child.isEnabled = enabled
            }
        }
    }
}