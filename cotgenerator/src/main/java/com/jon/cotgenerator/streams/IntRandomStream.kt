package com.jon.cotgenerator.streams

import java.util.*

class IntRandomStream(private val random: Random, private val min: Int, private val max: Int) : RandomStream<Int> {
    override fun next(): Int {
        return min + random.nextInt(max - min)
    }
}
