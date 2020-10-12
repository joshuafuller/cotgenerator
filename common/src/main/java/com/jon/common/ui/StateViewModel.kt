package com.jon.common.ui

import androidx.lifecycle.ViewModel
import com.jon.common.service.ServiceState

class StateViewModel : ViewModel() {
    var currentState: ServiceState = ServiceState.STOPPED
}
