package com.jon.cotbeacon

import androidx.navigation.fragment.navArgs
import com.jon.common.presets.OutputPreset
import com.jon.common.ui.editpreset.EditPresetFragment

class BeaconEditPresetFragment : EditPresetFragment() {
    override val args: BeaconEditPresetFragmentArgs by navArgs()

    override fun getFragmentArgumentPreset(): OutputPreset? {
        return args.presetArgument
    }
}