package com.jon.cotgenerator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.presets.OutputPreset;
import com.jon.cotgenerator.presets.PresetRepository;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.Protocol;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ListPresetsActivity
        extends ServiceBoundActivity
        implements ListPresetsAdapter.ItemClickListener {

    private final Map<Protocol, PresetRecyclerInfo> recyclerViewMap = new HashMap<Protocol, PresetRecyclerInfo>() {{
        put(Protocol.SSL, new PresetRecyclerInfo(R.id.listPresetsSslRecyclerView, R.id.listPresetsSslCreateButton));
        put(Protocol.TCP, new PresetRecyclerInfo(R.id.listPresetsTcpRecyclerView, R.id.listPresetsTcpCreateButton));
        put(Protocol.UDP, new PresetRecyclerInfo(R.id.listPresetsUdpRecyclerView, R.id.listPresetsUdpCreateButton));
    }};

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PresetRepository repository = PresetRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_presets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.toolbarHeaderListPresets);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        for (Map.Entry<Protocol, PresetRecyclerInfo> entry : recyclerViewMap.entrySet()) {
            final Protocol protocol = entry.getKey();
            final PresetRecyclerInfo info = entry.getValue();
            final RecyclerView recyclerView = findViewById(info.viewId);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            compositeDisposable.add(repository.getCustomByProtocol(protocol)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(presets -> {
                        info.adapter = new ListPresetsAdapter(this, presets);
                        info.adapter.setClickListener(this);
                        recyclerView.setAdapter(info.adapter);
                    }));
            Button createPresetButton = findViewById(info.buttonId);
            createPresetButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, EditPresetActivity.class);
                intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_PROTOCOL, protocol.get());
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_presets_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.delete_all:
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Delete Presets")
                        .setMessage("Clear all listed presets? The built-in defaults will still remain.")
                        .setPositiveButton(android.R.string.ok, (dialog, buttonId) -> {
                            repository.deleteDatabase();
                            refresh();
                        }).setNegativeButton(android.R.string.cancel, (dialog, buttonId) -> dialog.dismiss())
                        .show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickEditItem(OutputPreset preset) {
        /* Build an intent containing all the values we'll need to populate the EditPreset screen */
        Intent intent = new Intent(this, EditPresetActivity.class);
        intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_PROTOCOL, preset.protocol.get());
        intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_ALIAS, preset.alias);
        intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_ADDRESS, preset.address);
        intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_PORT, preset.port);
        if (preset.protocol == Protocol.SSL) {
            intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_CLIENT_BYTES, new String(preset.clientCert));
            intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_CLIENT_PASSWORD, preset.clientCertPassword);
            intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_TRUST_BYTES, new String(preset.trustStore));
            intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_TRUST_PASSWORD, preset.trustStorePassword);
        }
        startActivity(intent);
    }

    @Override
    public void onClickDeleteItem(OutputPreset preset) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Preset")
                .setMessage("Are you sure you want to delete " + preset.alias + "?")
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    repository.deletePreset(preset);
                    refresh();
                })
                .show();
    }

    private void refresh() {
        for (Map.Entry<Protocol, PresetRecyclerInfo> entry : recyclerViewMap.entrySet()) {
            final Protocol protocol = entry.getKey();
            final PresetRecyclerInfo info = entry.getValue();
            compositeDisposable.add(repository.getCustomByProtocol(protocol)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(presets -> info.adapter.updatePresets(presets)));
        }
    }

    private static class PresetRecyclerInfo {
        int viewId, buttonId;
        ListPresetsAdapter adapter;
        PresetRecyclerInfo(@IdRes int viewId, @IdRes int buttonId) {
            this.viewId = viewId;
            this.buttonId = buttonId;
        }
    }
}
