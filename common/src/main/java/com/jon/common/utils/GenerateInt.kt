package com.jon.common.utils

object GenerateInt {
    private val lock = Any()
    private var i = 3

    operator fun next(): Int {
        synchronized(lock) {
            return i++
        }
    }
}
