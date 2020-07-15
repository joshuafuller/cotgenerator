package com.jon.common.cot;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UtcTimestampTest {
    /* List of pre-calculated translations between epoch milliseconds and ISO 8601 string */
    private static final List<PrecalculatedResult> PRECALCULATED_RESULTS = new ArrayList<PrecalculatedResult>() {{
        add(new PrecalculatedResult(1606836655789L, "2020-12-01T15:30:55.789Z")); // March 2020
        add(new PrecalculatedResult(1624583396623L, "2021-06-25T01:09:56.623Z")); // June 2021
        add(new PrecalculatedResult(1663110062235L, "2022-09-13T23:01:02.235Z")); // September 2022
        add(new PrecalculatedResult(1701444655789L, "2023-12-01T15:30:55.789Z")); // December 2023
    }};

    private static final String BASELINE_ISO = "2020-07-15T14:26:53.123Z";

    @Test
    public void constructor_LongToString() {
        for (PrecalculatedResult precalculated : PRECALCULATED_RESULTS) {
            UtcTimestamp timestamp = new UtcTimestamp(precalculated.ms);
            assertThat(timestamp.toString(), equalTo(precalculated.iso));
        }
    }

    @Test
    public void constructor_StringToLong() {
        for (PrecalculatedResult precalculated : PRECALCULATED_RESULTS) {
            UtcTimestamp timestamp = new UtcTimestamp(precalculated.iso);
            assertThat(timestamp.toLong(), equalTo(precalculated.ms));
        }
    }

    @Test
    public void now() {
        long nowMs = System.currentTimeMillis();
        UtcTimestamp nowUtc = UtcTimestamp.now();
        int dt = (int) (nowUtc.toLong() - nowMs);
        assertThat(dt, lessThan(10));
    }

    @Test
    public void add_OneHour() {
        UtcTimestamp base = new UtcTimestamp(BASELINE_ISO);
        UtcTimestamp added = base.add(1, TimeUnit.HOURS);
        assertThat(added.toString(), equalTo("2020-07-15T15:26:53.123Z"));
    }

    @Test
    public void add_NegativeOneDay() {
        UtcTimestamp base = new UtcTimestamp(BASELINE_ISO);
        UtcTimestamp added = base.add(-1, TimeUnit.DAYS);
        assertThat(added.toString(), equalTo("2020-07-14T14:26:53.123Z"));
    }

    @Test
    public void add_Nothing() {
        UtcTimestamp base = new UtcTimestamp(BASELINE_ISO);
        UtcTimestamp added = base.add(0, TimeUnit.SECONDS);
        assertThat(added.toString(), equalTo(BASELINE_ISO));
    }

    private static class PrecalculatedResult {
        long ms;
        String iso;

        PrecalculatedResult(long ms, String iso) {
            this.ms = ms;
            this.iso = iso;
        }
    }
}
