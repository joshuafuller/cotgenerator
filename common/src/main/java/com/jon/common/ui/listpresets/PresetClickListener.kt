package com.jon.common.ui.listpresets

import com.jon.common.presets.OutputPreset

internal interface PresetClickListener {
    fun onClickEditItem(preset: OutputPreset)
    fun onClickDeleteItem(preset: OutputPreset)
}
