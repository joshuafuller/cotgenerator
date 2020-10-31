package com.jon.common.repositories

import android.location.Location
import androidx.lifecycle.LiveData

interface IGpsRepository {
    fun setLocation(location: Location)
    fun getLocation(): LiveData<Location?>
    fun latitude(): Double
    fun longitude(): Double
    fun altitude(): Double
    fun bearing(): Double
    fun speed(): Double
    fun circularError90(): Double
    fun linearError90(): Double
    fun hasGpsFix(): Boolean
}
