package eu.darken.fpv.dvca.main.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.smart.SmartActivity

@AndroidEntryPoint
class MainActivity : SmartActivity() {

    private val vm: MainActivityVM by viewModels()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.BaseAppTheme)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        if (!vm.hasOnboarding.value) {
            navController.navigate(R.id.onboardingFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp() || super.onSupportNavigateUp()

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
}
