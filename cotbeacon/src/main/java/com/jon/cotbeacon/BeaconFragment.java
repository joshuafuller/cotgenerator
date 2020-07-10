package com.jon.cotbeacon;

import com.jon.common.ui.MainFragment;

public class BeaconFragment extends MainFragment {
    private BeaconFragment() { /* blank */ }

    public static MainFragment getInstance() {
        return new BeaconFragment();
    }
}
