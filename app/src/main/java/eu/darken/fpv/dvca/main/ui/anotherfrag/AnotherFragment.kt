package eu.darken.fpv.dvca.main.ui.anotherfrag

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.smart.SmartFragment
import eu.darken.fpv.dvca.common.viewbinding.viewBindingLazy
import eu.darken.fpv.dvca.databinding.AnotherFragmentBinding

//import eu.darken.kotlinstarter.common.vdc.vdcsAssisted

@AndroidEntryPoint
class AnotherFragment : SmartFragment(R.layout.another_fragment) {

    private val vm: AnotherFragmentVM by viewModels()
    private val binding: AnotherFragmentBinding by viewBindingLazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

}
