package eu.darken.fpv.dvca.main.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.fpv.dvca.R
import eu.darken.fpv.dvca.common.navigation.isGraphSet
import eu.darken.fpv.dvca.common.smart.SmartActivity

@AndroidEntryPoint
class MainActivity : SmartActivity() {

    private val vm: MainActivityVM by viewModels()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.BaseAppTheme)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        vm.state.observe(this) {
            if (it.ready && !navController.isGraphSet()) {
                val graph = navController.navInflater.inflate(R.navigation.main)

                navController.setGraph(graph, bundleOf("exampleArgument" to "hello"))
                setupActionBarWithNavController(navController)
            }
        }


        vm.onGo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> {
            // NOOP
            true
        }
        else -> NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }
}
