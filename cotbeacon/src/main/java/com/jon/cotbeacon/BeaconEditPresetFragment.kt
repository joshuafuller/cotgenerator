package com.jon.cotbeacon

import androidx.navigation.fragment.navArgs
import com.jon.common.presets.OutputPreset
import com.jon.common.ui.editpreset.EditPresetFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BeaconEditPresetFragment : EditPresetFragment() {
    override val args: BeaconEditPresetFragmentArgs by navArgs()

    override fun getFragmentArgumentPreset(): OutputPreset? {
        return args.presetArgument
    }
}