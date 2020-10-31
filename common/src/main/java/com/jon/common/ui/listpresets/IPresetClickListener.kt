package com.jon.common.ui.listpresets

import com.jon.common.presets.OutputPreset

internal interface IPresetClickListener {
    fun onClickEditItem(preset: OutputPreset)
    fun onClickDeleteItem(preset: OutputPreset)
}
