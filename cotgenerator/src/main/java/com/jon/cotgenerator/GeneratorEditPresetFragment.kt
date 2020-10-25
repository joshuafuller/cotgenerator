package com.jon.cotgenerator

import androidx.navigation.fragment.navArgs
import com.jon.common.presets.OutputPreset
import com.jon.common.ui.editpreset.EditPresetFragment

class GeneratorEditPresetFragment : EditPresetFragment() {
    override val args: GeneratorEditPresetFragmentArgs by navArgs()

    override fun getFragmentArgumentPreset(): OutputPreset? {
        return args.presetArgument
    }
}