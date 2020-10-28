package com.jon.cotgenerator

import android.view.MenuItem
import com.jon.common.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeneratorActivity : MainActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.location ->
                navController.navigate(activityResources.mainToLocationDirections)
            R.id.about ->
                navController.navigate(activityResources.mainToAboutDirections)
        }
        return super.onOptionsItemSelected(item)
    }
}
