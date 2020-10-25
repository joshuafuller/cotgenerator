package com.jon.common.ui.listpresets

import android.os.Build
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jon.common.R
import com.jon.common.presets.OutputPreset

internal class PresetViewHolder(
        itemView: View,
        private val clickListener: PresetClickListener,
        private val presets: List<OutputPreset>
) : RecyclerView.ViewHolder(itemView) {

    val alias: TextView = itemView.findViewById(R.id.presetListItemAlias)
    val address: TextView = itemView.findViewById(R.id.presetListItemAddress)
    val port: TextView = itemView.findViewById(R.id.presetListItemPort)

    private val edit: ImageButton = itemView.findViewById(R.id.presetListItemEdit)
    private val delete: ImageButton = itemView.findViewById(R.id.presetListItemDelete)

    private fun setImageButtonTint(button: ImageButton) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val icon = button.drawable.apply {
                val colour = itemView.context.getColor(R.color.black)
                setTint(colour)
            }
            button.setImageDrawable(icon)
        }
    }

    init {
        edit.setOnClickListener { clickListener.onClickEditItem(presets[adapterPosition]) }
        delete.setOnClickListener { clickListener.onClickDeleteItem(presets[adapterPosition]) }
        setImageButtonTint(edit)
        setImageButtonTint(delete)
    }
}
