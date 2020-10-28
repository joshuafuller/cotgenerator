package com.jon.cotgenerator

import android.view.MenuItem
import com.jon.common.ui.main.MainActivity
import com.jon.common.ui.main.MainFragmentDirections

class GeneratorActivity : MainActivity() {
    override val activityLayoutId = R.layout.generator_activity
    override val menuResourceId = R.menu.generator_main_menu
    override val navHostFragmentId = R.id.nav_host_fragment
    override val permissionRationaleId = R.string.permission_rationale

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.location ->
                navController.navigate(MainFragmentDirections.actionMainToLocation())
            R.id.about ->
                navController.navigate(MainFragmentDirections.actionMainToAbout())
        }
        return super.onOptionsItemSelected(item)
    }
}
