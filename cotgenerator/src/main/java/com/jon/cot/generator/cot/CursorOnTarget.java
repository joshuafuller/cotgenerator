package com.jon.cot.generator.cot;

import android.os.Build;

import com.jon.cot.generator.BuildConfig;
import com.jon.cot.generator.cot.proto.ContactOuterClass.Contact;
import com.jon.cot.generator.cot.proto.Cotevent.CotEvent;
import com.jon.cot.generator.cot.proto.DetailOuterClass.Detail;
import com.jon.cot.generator.cot.proto.GroupOuterClass.Group;
import com.jon.cot.generator.cot.proto.StatusOuterClass.Status;
import com.jon.cot.generator.cot.proto.Takmessage.TakMessage;
import com.jon.cot.generator.cot.proto.TakvOuterClass.Takv;
import com.jon.cot.generator.cot.proto.TrackOuterClass.Track;
import com.jon.cot.generator.utils.DataFormat;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CursorOnTarget {
    // Prepended to every protobuf packet
    private static final byte[] TAK_HEADER = new byte[] { (byte)0xbf, (byte)0x01, (byte)0xbf };

    public CotHow how = CotHow.MG;
    public CotType type = CotType.GROUND_COMBAT;

    // User info
    public String uid = null;    // unique ID of the device. Stays constant when changing callsign

    // Time info
    public UtcTimestamp time;    // time when the icon was created
    public UtcTimestamp start;   // time when the icon is considered valid
    public UtcTimestamp stale;   // time when the icon is considered invalid

    // Contact info
    public String callsign = null; // ATAK callsign

    // Position and movement info
    public double hae = 0.0;    // height above ellipsoid in metres
    public double lat = 0.0;    // latitude in decimal degrees
    public double lon = 0.0;    // longitude in decimal degrees
    public double ce = 0.0;     // circular (radial) error in metres. applies to 2D position only
    public double le = 0.0;     // linear error in metres. applies to altitude only
    public double course = 0.0; // ground bearing in decimal degrees
    public double speed = 0.0;  // ground velocity in m/s. Doesn't include altitude climb rate

    // Group
    public CotTeam team = CotTeam.CYAN;  // cyan, green, purple, etc
    public CotRole role = CotRole.TEAM_MEMBER;  // HQ, sniper, K9, etc

    // Location source
    public String altsrc = "GENERATED";
    public String geosrc = "GENERATED";

    // System info
    public Integer battery = 100; // internal battery charge percentage, scale of 1-100
    public final String device = String.format("%s %s", Build.MANUFACTURER.toUpperCase(), Build.MODEL.toUpperCase());
    public final String platform = "COT-GENERATOR";
    public final String os = String.valueOf(Build.VERSION.SDK_INT);
    public final String version = BuildConfig.VERSION_NAME;

    public CursorOnTarget() { /* blank */ }

    public byte[] toBytes(DataFormat dataFormat) {
        switch (dataFormat) {
            case XML: return toXml();
            case PROTOBUF: return toProtobuf();
            default: throw new IllegalArgumentException("Unknown data format");
        }
    }

    public void setStaleDiff(final long dt, final TimeUnit timeUnit) {
        int multiplier = getTimeUnitMultiplier(timeUnit);
        stale = new UtcTimestamp(start.toLong() + (multiplier * dt));
    }

    private int getTimeUnitMultiplier(TimeUnit timeUnit) {
        switch (timeUnit) {
            case MILLISECONDS: return 1;
            case SECONDS:      return 1000;
            case MINUTES:      return 60 * 1000;
            case HOURS:        return 60 * 60 * 1000;
            default:           throw new IllegalArgumentException("Give me a proper TimeUnit, not " + timeUnit.name());
        }
    }

    public byte[] toXml() {
        return String.format(Locale.ENGLISH,
                "<event version=\"2.0\" uid=\"%s\" type=\"%s\" time=\"%s\" start=\"%s\" stale=\"%s\" how=\"%s\"><point lat=\"%.7f\" " +
                        "lon=\"%.7f\" hae=\"%f\" ce=\"%f\" le=\"%f\"/><detail><track speed=\"%.7f\" course=\"%.7f\"/><contact callsign=\"%s\"/>" +
                        "<__group name=\"%s\" role=\"%s\"/><takv device=\"%s\" platform=\"%s\" os=\"%s\" version=\"%s\"/><status battery=\"%d\"/>" +
                        "<precisionlocation altsrc=\"%s\" geopointsrc=\"%s\" /></detail></event>",
                uid, type.get(), time.toString(), start.toString(), stale.toString(), how.get(), lat, lon, hae, ce, le, speed,
                course, callsign, team.get(), role.toString(), device, platform, os, version, battery, altsrc, geosrc)
                .getBytes();
    }

    public byte[] toProtobuf() {
        byte[] cotBytes = TakMessage.newBuilder()
                .setCotEvent(CotEvent.newBuilder()
                        .setType(type.get())
                        .setUid(uid)
                        .setHow(how.get())
                        .setSendTime(time.toLong())
                        .setStartTime(start.toLong())
                        .setStaleTime(stale.toLong())
                        .setLat(lat)
                        .setLon(lon)
                        .setHae(hae)
                        .setCe(ce)
                        .setLe(le)
                        .setDetail(Detail.newBuilder()
                                .setGroup(Group.newBuilder()
                                        .setName(team.get())
                                        .setRole(role.get())
                                        .build())
                                .setTakv(Takv.newBuilder()
                                        .setDevice(device)
                                        .setPlatform(platform)
                                        .setOs(os)
                                        .setVersion(version)
                                        .build())
                                .setStatus(Status.newBuilder()
                                        .setBattery(battery)
                                        .build())
                                .setTrack(Track.newBuilder()
                                        .setCourse(course)
                                        .setSpeed(speed)
                                        .build())
                                .setContact(Contact.newBuilder()
                                        .setCallsign(callsign)
                                        .build())
                                .build())
                        .build())
                .build()
                .toByteArray();
        return concatBytes(TAK_HEADER, cotBytes);
    }

    private byte[] concatBytes(byte[] array1, byte[] array2) {
        byte[] concatted = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, concatted, 0, array1.length);
        System.arraycopy(array2, 0, concatted, array1.length, array2.length);
        return concatted;
    }
}
