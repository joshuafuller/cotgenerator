package com.jon.cotgenerator.presets;

import androidx.room.Room;

import com.jon.cotgenerator.CotApplication;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.enums.Protocol;

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
                CotApplication.getContext().getString(R.string.tcpTakserver),
                CotApplication.getContext().getString(R.string.tcpTakserverIp),
                Integer.parseInt(CotApplication.getContext().getString(R.string.tcpTakserverPort))
        ));
        add(new OutputPreset(
                Protocol.TCP,
                CotApplication.getContext().getString(R.string.tcpFreetakserver),
                CotApplication.getContext().getString(R.string.tcpFreetakserverIp),
                Integer.parseInt(CotApplication.getContext().getString(R.string.tcpFreetakserverPort))
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
        database = Room.databaseBuilder(CotApplication.getContext(), PresetDatabase.class, PresetDatabase.FILENAME).build();
    }

    public void insertPreset(String protocol, String alias, String address, int port) {
        executor.execute(() -> {
            OutputPreset preset = new OutputPreset(protocol, alias, address, port);
            database.presetDao().insert(preset);
        });
    }

    public Observable<List<OutputPreset>> getByProtocol(Protocol protocol) {
        final Observable<List<OutputPreset>> defaults = Observable.just(protocol == Protocol.TCP ? TCP_DEFAULTS : UDP_DEFAULTS);
        return database.presetDao()
                .getByProtocol(protocol.get())
                .zipWith(defaults, (fetchedPresets, defaultPresets) -> ListUtils.union(defaultPresets, fetchedPresets));
    }

    public List<OutputPreset> getDefaults(Protocol protocol) {
        return protocol == Protocol.TCP ? TCP_DEFAULTS : UDP_DEFAULTS;
    }

    public void deleteDatabase() {
        executor.execute(() -> database.presetDao().deleteAll());
    }
}
