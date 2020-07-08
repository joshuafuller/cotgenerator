package com.jon.cotgenerator.presets;

import androidx.room.Room;

import com.jon.cotgenerator.CotApplication;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.utils.FileUtils;
import com.jon.cotgenerator.utils.Protocol;

import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Observable;

public class PresetRepository {
    private static final List<OutputPreset> UDP_DEFAULTS = new ArrayList<OutputPreset>() {{
        add(new OutputPreset(
                Protocol.UDP,
                CotApplication.getContext().getString(R.string.udpDefaultSa),
                CotApplication.getContext().getString(R.string.udpDefaultSaIp),
                Integer.parseInt(CotApplication.getContext().getString(R.string.udpDefaultSaPort))
        ));
    }};
    private static final List<OutputPreset> TCP_DEFAULTS = new ArrayList<OutputPreset>() {{
        add(new OutputPreset(
                Protocol.TCP,
                CotApplication.getContext().getString(R.string.tcpFreetakserver),
                CotApplication.getContext().getString(R.string.tcpFreetakserverIp),
                Integer.parseInt(CotApplication.getContext().getString(R.string.tcpFreetakserverPort))
        ));
    }};
    private static final List<OutputPreset> SSL_DEFAULTS = new ArrayList<OutputPreset>() {{
        add(new OutputPreset(
                Protocol.SSL,
                CotApplication.getContext().getString(R.string.sslTakserver),
                CotApplication.getContext().getString(R.string.sslTakserverIp),
                Integer.parseInt(CotApplication.getContext().getString(R.string.sslTakserverPort)),
                FileUtils.toByteArraySafe(R.raw.discord_client),
                "atakatak",
                FileUtils.toByteArraySafe(R.raw.discord_truststore),
                "atakatak"
        ));
    }};

    private static PresetRepository instance;
    private PresetDatabase database;
    private final Executor executor = Executors.newSingleThreadExecutor();

    synchronized public static PresetRepository getInstance() {
        if (instance == null) {
            instance = new PresetRepository();
        }
        return instance;
    }

    private PresetRepository() {
        database = Room.databaseBuilder(CotApplication.getContext(), PresetDatabase.class, PresetDatabase.FILENAME)
                .addMigrations(DatabaseMigrations.getAllMigrations())
                .build();
    }

    public void insertPreset(OutputPreset preset) {
        executor.execute(() -> database.presetDao().insert(preset));
    }

    public OutputPreset getPreset(Protocol protocol, String address, int port) {
        return database.presetDao().getPreset(protocol.get(), address, port);
    }

    public Observable<List<OutputPreset>> getByProtocol(Protocol protocol) {
        /* First get a list of known default presets, which aren't stored in the database */
        final Observable<List<OutputPreset>> defaults = Observable.just(defaultsByProtocol(protocol));
        /* Then query the database to grab any user-entered presets */
        return database.presetDao()
                .getByProtocol(protocol.get())
                /* Merge the two observables together to return a single observable containing a list of OutputPresets */
                .zipWith(defaults, (fetchedPresets, defaultPresets) -> ListUtils.union(defaultPresets, fetchedPresets));
    }

    public void deleteDatabase() {
        executor.execute(() -> database.presetDao().deleteAll());
    }

    public List<OutputPreset> defaultsByProtocol(Protocol protocol) {
        switch (protocol) {
            case SSL: return SSL_DEFAULTS;
            case TCP: return TCP_DEFAULTS;
            case UDP: return UDP_DEFAULTS;
            default: throw new IllegalArgumentException("Unknown protocol: " + protocol);
        }
    }
}
