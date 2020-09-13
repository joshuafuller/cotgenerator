package com.jon.cotbeacon

import com.jon.common.ui.main.MainFragment

class BeaconFragment : MainFragment() {
    companion object {
        fun getInstance(): MainFragment = BeaconFragment()
    }
}
