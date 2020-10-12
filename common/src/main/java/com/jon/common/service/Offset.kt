package com.jon.common.service

import com.jon.common.utils.Bearing
import com.jon.common.utils.GeometryUtils.arcdistance

data class Offset(
        var R: Double,    /* travel distance in metres */
        var theta: Double /* bearing in degrees */
) {
    companion object {
        fun from(startPoint: Point): Builder {
            return Builder(startPoint)
        }
    }

    class Builder(private val startPoint: Point) {
        private var endPoint: Point? = null

        private fun setStart(start: Point): Builder {
            return this
        }

        private fun setEnd(end: Point): Builder {
            endPoint = end
            return this
        }

        fun to(endPoint: Point): Offset {
            return Offset(
                    R = arcdistance(startPoint, endPoint),
                    theta = Bearing.from(startPoint).to(endPoint)
            )
        }
    }
}