package com.jon.cotbeacon;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.jon.common.ui.ListPresetsActivity;

public class BeaconListPresetsActivity extends ListPresetsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Change the text colour over the FAB to contrast better. It's white on green by default, looks a bit crap */
        ExtendedFloatingActionButton fab = findViewById(com.jon.common.R.id.fab);
        fab.setTextColor(ContextCompat.getColor(this, R.color.black));
        fab.setIconTintResource(R.color.black);
    }
}
