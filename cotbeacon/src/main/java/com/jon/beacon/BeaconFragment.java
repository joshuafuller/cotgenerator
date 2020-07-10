package com.jon.beacon;

import com.jon.common.ui.MainFragment;

public class BeaconFragment extends MainFragment {
    private BeaconFragment() { /* blank */ }

    public static MainFragment getInstance() {
        return new BeaconFragment();
    }
}
