package com.jon.common.utils

object GenerateInt {
    private var i = 3

    operator fun next(): Int {
        return i++
    }
}
