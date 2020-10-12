package com.jon.cotbeacon

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.jon.common.ui.listpresets.ListPresetsActivity
import com.jon.common.variants.Variant

class BeaconListPresetsActivity : ListPresetsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Change the text colour over the FAB to contrast better. It's white on green by default, looks a bit crap */
        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fab)
        fab.setTextColor(ContextCompat.getColor(this, Variant.getIconColourId()))
        fab.setIconTintResource(Variant.getIconColourId())
    }
}
