package com.jon.common.cot;

import com.jon.common.AppSpecific;
import com.jon.common.TestingRepo;
import com.jon.common.utils.DataFormat;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CursorOnTargetTest {
    private CursorOnTarget cot;

    @Before
    public void initialise() {
        AppSpecific.setReferenceRepo(new TestingRepo());
        cot = new CursorOnTarget();
        cot.time = cot.start = UtcTimestamp.now();
        cot.stale = cot.start.add(10, TimeUnit.MINUTES);
    }

    @Test
    public void toBytes_Xml() {
        final String xml = new String(cot.toBytes(DataFormat.XML));
        assertThat(xml, containsXml("event", "uid", cot.uid));
        assertThat(xml, containsXml("event", "type", cot.type.get()));
        assertThat(xml, containsXml("event", "time", cot.time.toString()));
        assertThat(xml, containsXml("event", "start", cot.start.toString()));
        assertThat(xml, containsXml("event", "stale", cot.stale.toString()));
        assertThat(xml, containsXml("event", "how", cot.how.get()));
        assertThat(xml, containsXml("point", "lat", String.format("%.7f", cot.lat)));
        assertThat(xml, containsXml("point", "lon", String.format("%.7f", cot.lon)));
        assertThat(xml, containsXml("point", "hae", String.format("%f", cot.hae)));
        assertThat(xml, containsXml("point", "ce", String.format("%f", cot.ce)));
        assertThat(xml, containsXml("point", "le", String.format("%f", cot.le)));
        assertThat(xml, containsXml("track", "speed", String.format("%.7f", cot.speed)));
        assertThat(xml, containsXml("track", "course", String.format("%.7f", cot.course)));
        assertThat(xml, containsXml("contact", "callsign", cot.callsign));
        assertThat(xml, containsXml("__group", "name", cot.team.get()));
        assertThat(xml, containsXml("__group", "role", cot.role.get()));
        assertThat(xml, containsXml("takv", "device", cot.device));
        assertThat(xml, containsXml("takv", "platform", cot.platform));
        assertThat(xml, containsXml("takv", "os", cot.os));
        assertThat(xml, containsXml("takv", "version", cot.version));
        assertThat(xml, containsXml("status", "battery", String.format("%d", cot.battery)));
        assertThat(xml, containsXml("precisionlocation", "altsrc", cot.altsrc));
        assertThat(xml, containsXml("precisionlocation", "geopointsrc", cot.geosrc));
    }

    @Test
    public void toBytes_Protobuf() {
        final byte[] protobuf = cot.toBytes(DataFormat.PROTOBUF);
        assertThat(protobuf[0], equalTo((byte) 0xbf));
        assertThat(protobuf[1], equalTo((byte) 0x01));
        assertThat(protobuf[2], equalTo((byte) 0xbf));
        assertThat(protobuf.length, lessThan(cot.toBytes(DataFormat.XML).length));
    }

    @Test
    public void setStaleDiff_OneHour() {
        cot.setStaleDiff(1, TimeUnit.HOURS);
        assertThat(cot.stale.toLong() - cot.start.toLong(), equalTo(TimeUnit.HOURS.toMillis(1L)));
    }

    @Test
    public void setStaleDiff_1MillionDays() {
        cot.setStaleDiff(1_000_000L, TimeUnit.DAYS);
        assertThat(cot.stale.toLong() - cot.start.toLong(), equalTo(TimeUnit.DAYS.toMillis(1_000_000L)));
    }

    @Test
    public void setStaleDiff_Zero() {
        cot.setStaleDiff(0, TimeUnit.MILLISECONDS);
        assertThat(cot.stale.toLong() - cot.start.toLong(), equalTo(0L));
    }

    @Test
    public void setStaleDiff_Negative() {
        cot.setStaleDiff(-1, TimeUnit.HOURS);
        assertThat(cot.stale.toLong() - cot.start.toLong(), equalTo(TimeUnit.HOURS.toMillis(-1L)));
    }

    private Matcher<String> containsXml(String tag, String attribute, String value) {
        return matchesRegex(String.format(".*<%s.*?%s=\"%s\".*?>.*", tag, attribute, value));
    }

}
