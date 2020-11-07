package com.jon.common.prefs

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageButton
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.jon.common.R

class RefreshCallsignPreference(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
) : EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, getDefaultAttr(context))

    constructor(context: Context)
            : this(context, null)

    init {
        isPersistent = true
        widgetLayoutResource = R.layout.refresh_callsign_preference
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val atakCallsigns = context.resources.getStringArray(R.array.atakCallsigns)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        with(holder.itemView) {
            val refreshButton = findViewById<ImageButton>(R.id.refresh_button)
            refreshButton.setOnClickListener {
                val newCallsign = atakCallsigns.random()
                text = newCallsign
                prefs.edit().putString(CommonPrefs.CALLSIGN.key, newCallsign).apply()
            }
        }
    }
}

/* Copied from androidx.core.content.res.TypedArrayUtils, since that method is package-private */
private fun getDefaultAttr(context: Context): Int {
    val value = TypedValue()
    val attr = R.attr.editTextPreferenceStyle
    context.theme.resolveAttribute(attr, value, true)
    return if (value.resourceId != 0) {
        attr
    } else {
        android.R.attr.editTextPreferenceStyle
    }
}
