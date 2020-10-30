package com.jon.cotgenerator

import androidx.navigation.NavDirections
import com.jon.common.di.UiResources
import com.jon.common.presets.OutputPreset
import com.jon.common.ui.listpresets.ListPresetsFragmentDirections
import com.jon.common.ui.main.MainFragmentDirections
import javax.inject.Inject

class GeneratorUiResources @Inject constructor() : UiResources {
    override val activityLayoutId = R.layout.generator_activity
    override val settingsXmlId = R.xml.settings
    override val mainMenuId = R.menu.generator_main_menu
    override val navHostFragmentId = R.id.nav_host_fragment
    override val startStopButtonId = R.id.start_stop_button
    override val permissionRationaleId = R.string.permission_rationale
    override val accentColourId = R.color.colorAccent

    override val mainToLocationDirections = MainFragmentDirections.actionMainToLocation()
    override val mainToAboutDirections = MainFragmentDirections.actionMainToAbout()
    override val mainToListDirections = MainFragmentDirections.actionMainToListPresets()

    override fun listToEditDirections(preset: OutputPreset?): NavDirections {
        return ListPresetsFragmentDirections.actionListPresetsToGeneratorEditPreset(preset)
    }
}
