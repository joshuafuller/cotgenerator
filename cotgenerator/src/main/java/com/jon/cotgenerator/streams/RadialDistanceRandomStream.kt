package com.jon.cotgenerator.streams

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sqrt

class RadialDistanceRandomStream(random: Random, maxDistance: Double) : RandomStream<Double> {
    private val weightedElements = ArrayList<Double>()
    private val indexStream = IntRandomStream(random, 0, NUM_WEIGHTED_ELEMENTS - 1)

    init {
        for (i in 0 until NUM_WEIGHTED_ELEMENTS) {
            val r = i.toDouble() / NUM_WEIGHTED_ELEMENTS.toDouble()
            weightedElements.add(
                    sqrt(r) * maxDistance
            )
        }
    }

    override fun next(): Double {
        return weightedElements[indexStream.next()]
    }

    private companion object {
        const val NUM_WEIGHTED_ELEMENTS = 1000
    }
}
