package com.jon.common.cot

import java.time.Instant
import java.util.concurrent.TimeUnit

/* For easier translating between milliseconds and ISO timestamp strings. */
class UtcTimestamp(private var milliseconds: Long) {

    constructor(timestampString: String) : this(stringToLong(timestampString))

    fun milliseconds(): Long = milliseconds

    fun isoTimestamp(): String = longToString(milliseconds)

    fun add(differenceMilliseconds: Long): UtcTimestamp = UtcTimestamp(milliseconds + differenceMilliseconds)

    fun add(dt: Long, timeUnit: TimeUnit): UtcTimestamp = add(timeUnit.toMillis(dt))

    companion object {
        fun now(): UtcTimestamp = UtcTimestamp(System.currentTimeMillis())

        private fun stringToLong(timestamp: String): Long = Instant.parse(timestamp).toEpochMilli()

        private fun longToString(ms: Long): String = Instant.ofEpochMilli(ms).toString()
    }
}
