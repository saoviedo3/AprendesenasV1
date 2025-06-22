package com.espe.aprendesenasv1

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // NavController y AppBarConfiguration
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfig = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.lettersFragment),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfig)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.nav_open_drawer,
            R.string.nav_close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // tras toggle.syncState()
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            // cerramos el drawer
            drawerLayout.closeDrawer(GravityCompat.START)

            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    // volvemos a inicio
                    navController.navigate(
                        R.id.homeFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(navController.graph.startDestinationId, false)
                            .build()
                    )
                    true
                }
                R.id.lettersFragment -> {
                    // navegamos siempre a Letras, limpiando el backstack
                    navController.navigate(
                        R.id.lettersFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            // vaciamos hasta el root (home), para no duplicar entries
                            .setPopUpTo(navController.graph.startDestinationId, false)
                            .build()
                    )
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }
}
