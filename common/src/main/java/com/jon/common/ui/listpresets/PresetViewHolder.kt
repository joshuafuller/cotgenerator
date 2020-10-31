package com.jon.common.ui.listpresets

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jon.common.R
import com.jon.common.di.UiResources
import com.jon.common.presets.OutputPreset

internal class PresetViewHolder(
        itemView: View,
        private val clickListener: PresetClickListener,
        private val presets: List<OutputPreset>,
        uiResources: UiResources
) : RecyclerView.ViewHolder(itemView) {

    val alias: TextView = itemView.findViewById(R.id.presetListItemAlias)
    val address: TextView = itemView.findViewById(R.id.presetListItemAddress)
    val port: TextView = itemView.findViewById(R.id.presetListItemPort)

    private val edit: ImageButton = itemView.findViewById(R.id.presetListItemEdit)
    private val delete: ImageButton = itemView.findViewById(R.id.presetListItemDelete)

    init {
        val accent = ContextCompat.getColor(itemView.context, uiResources.accentColourId)
        edit.setOnClickListener { clickListener.onClickEditItem(presets[adapterPosition]) }
        delete.setOnClickListener { clickListener.onClickDeleteItem(presets[adapterPosition]) }
    }
}
