package com.jon.common.ui.listpresets

import androidx.recyclerview.widget.RecyclerView
import com.jon.common.databinding.PresetListItemBinding
import com.jon.common.presets.OutputPreset

internal class PresetViewHolder(
        binding: PresetListItemBinding,
        private val clickListener: IPresetClickListener,
        private val presets: List<OutputPreset>,
) : RecyclerView.ViewHolder(binding.root) {

    val alias = binding.alias
    val address = binding.address
    val port = binding.port

    init {
        binding.editButton.setOnClickListener { clickListener.onClickEditItem(presets[adapterPosition]) }
        binding.deleteButton.setOnClickListener { clickListener.onClickDeleteItem(presets[adapterPosition]) }
    }
}
