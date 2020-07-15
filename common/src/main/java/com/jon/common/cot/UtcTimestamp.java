package com.jon.common.cot;

import androidx.annotation.NonNull;

import org.threeten.bp.Instant;

import java.util.concurrent.TimeUnit;

/* For "easier" translating between milliseconds and strings without littering annoying little
 * utility functions all over the place. */
public class UtcTimestamp {
    private String isoTimestamp;
    private long milliseconds;

    public long toLong() {
        return milliseconds;
    }

    @NonNull
    @Override
    public String toString() {
        return isoTimestamp;
    }

    public static UtcTimestamp now() {
        return new UtcTimestamp(System.currentTimeMillis());
    }

    public UtcTimestamp(String str) {
        milliseconds = stringToLong(str);
        isoTimestamp = str;
    }

    public UtcTimestamp(long ms) {
        milliseconds = ms;
        isoTimestamp = longToString(ms);
    }

    public UtcTimestamp add(long differenceMilliseconds) {
        return new UtcTimestamp(milliseconds + differenceMilliseconds);
    }

    public UtcTimestamp add(long dt, TimeUnit timeUnit) {
        return add(timeUnit.toMillis(dt));
    }

    private long stringToLong(String timestamp) {
        return Instant.parse(timestamp).toEpochMilli();
    }

    private String longToString(long ms) {
        return Instant.ofEpochMilli(ms).toString();
    }
}
