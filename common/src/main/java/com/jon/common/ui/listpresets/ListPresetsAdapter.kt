package com.jon.common.ui.listpresets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jon.common.R
import com.jon.common.presets.OutputPreset

internal class ListPresetsAdapter(
        context: Context,
        private val clickListener: IPresetClickListener
) : RecyclerView.Adapter<PresetViewHolder>() {

    private val presets = ArrayList<OutputPreset>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val view = inflater.inflate(R.layout.preset_list_item, parent, false)
        return PresetViewHolder(view, clickListener, presets)
    }

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        val preset = presets[position]
        holder.alias.text = preset.alias
        holder.address.text = preset.address
        holder.port.text = preset.port.toString()
    }

    override fun getItemCount() = presets.size

    fun updatePresets(newPresets: List<OutputPreset>) {
        presets.clear()
        presets.addAll(newPresets)
        notifyDataSetChanged()
    }
}
