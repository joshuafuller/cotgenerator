package com.jon.common.ui.listpresets

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.jon.common.R
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.PresetRepository
import com.jon.common.ui.IntentIds
import com.jon.common.ui.ServiceBoundActivity
import com.jon.common.ui.editpreset.EditPresetActivity
import com.jon.common.utils.Protocol
import com.jon.common.variants.Variant
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

open class ListPresetsActivity : ServiceBoundActivity(), PresetClickListener {
    private data class PresetRecyclerInfo(@IdRes val recyclerViewId: Int, @IdRes val emptyMessageId: Int, var adapter: ListPresetsAdapter? = null)

    private val recyclerViewMap: Map<Protocol, PresetRecyclerInfo> = hashMapOf(
            Protocol.SSL to PresetRecyclerInfo(R.id.listPresetsSslRecyclerView, R.id.sslNoneFound),
            Protocol.TCP to PresetRecyclerInfo(R.id.listPresetsTcpRecyclerView, R.id.tcpNoneFound),
            Protocol.UDP to PresetRecyclerInfo(R.id.listPresetsUdpRecyclerView, R.id.udpNoneFound)
    )
    private val compositeDisposable = CompositeDisposable()
    private val repository = PresetRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_presets)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setTitle(R.string.toolbarHeaderListPresets)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        for ((protocol, info) in recyclerViewMap) {
            val recyclerView = findViewById<RecyclerView>(info.recyclerViewId)
            recyclerView.layoutManager = LinearLayoutManager(this)
            compositeDisposable.add(repository.getCustomByProtocol(protocol)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { presets: List<OutputPreset> ->
                        info.adapter = ListPresetsAdapter(this, presets.toMutableList(), this)
                        recyclerView.adapter = info.adapter
                        findViewById<View>(info.emptyMessageId).visibility = if (presets.isEmpty()) View.VISIBLE else View.GONE
                    })
        }
        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fab)
        fab.setOnClickListener { startActivity(Intent(this, EditPresetActivity::class.java)) }
        fab.setTextColor(ContextCompat.getColor(this, Variant.getIconColourId()))
        fab.setIconTintResource(Variant.getIconColourId())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_presets_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val resId = item.itemId
        if (resId == android.R.id.home) {
            onBackPressed()
        } else if (resId == R.id.delete_all) {
            MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Presets")
                    .setMessage("Clear all listed presets? The built-in defaults will still remain.")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        repository.deleteDatabase()
                        refresh()
                    }.setNegativeButton(android.R.string.cancel, null)
                    .show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClickEditItem(preset: OutputPreset) {
        /* Build an intent containing all the values we'll need to populate the EditPreset screen */
        val intent = Intent(this, EditPresetActivity::class.java).apply {
            putExtra(IntentIds.EXTRA_EDIT_PRESET_PROTOCOL, preset.protocol.toString())
            putExtra(IntentIds.EXTRA_EDIT_PRESET_ALIAS, preset.alias)
            putExtra(IntentIds.EXTRA_EDIT_PRESET_ADDRESS, preset.address)
            putExtra(IntentIds.EXTRA_EDIT_PRESET_PORT, preset.port)
            if (preset.protocol == Protocol.SSL) {
                putExtra(IntentIds.EXTRA_EDIT_PRESET_CLIENT_BYTES, String(preset.clientCert!!))
                putExtra(IntentIds.EXTRA_EDIT_PRESET_CLIENT_PASSWORD, preset.clientCertPassword)
                putExtra(IntentIds.EXTRA_EDIT_PRESET_TRUST_BYTES, String(preset.trustStore!!))
                putExtra(IntentIds.EXTRA_EDIT_PRESET_TRUST_PASSWORD, preset.trustStorePassword)
            }
        }
        startActivity(intent)
    }

    override fun onClickDeleteItem(preset: OutputPreset) {
        MaterialAlertDialogBuilder(this)
                .setTitle("Delete Preset")
                .setMessage("Are you sure you want to delete " + preset.alias + "?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    repository.deletePreset(preset)
                    refresh()
                }.show()
    }

    private fun refresh() {
        for ((protocol, info) in recyclerViewMap) {
            compositeDisposable.add(repository.getCustomByProtocol(protocol)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { presets: List<OutputPreset> ->
                        info.adapter?.updatePresets(presets)
                        findViewById<View>(info.emptyMessageId).visibility = if (presets.isEmpty()) View.VISIBLE else View.GONE
                    }
            )
        }
    }
}
