package com.jon.common.ui.listpresets

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jon.common.R
import com.jon.common.di.UiResources
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.IPresetRepository
import com.jon.common.utils.*
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class ListPresetsFragment : Fragment(),
        PresetClickListener {
    private data class PresetRecyclerInfo(@IdRes val recyclerViewId: Int, @IdRes val emptyMessageId: Int, var adapter: ListPresetsAdapter? = null)

    private val recyclerViewMap: Map<Protocol, PresetRecyclerInfo> = hashMapOf(
            Protocol.SSL to PresetRecyclerInfo(R.id.listPresetsSslRecyclerView, R.id.sslNoneFound),
            Protocol.TCP to PresetRecyclerInfo(R.id.listPresetsTcpRecyclerView, R.id.tcpNoneFound),
            Protocol.UDP to PresetRecyclerInfo(R.id.listPresetsUdpRecyclerView, R.id.udpNoneFound)
    )

    private val navController by lazy { findNavController() }

    @Inject
    lateinit var presetRepository: IPresetRepository

    @Inject
    lateinit var uiResources: UiResources

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.toolbar_header_list_presets)
        return inflater.inflate(R.layout.fragment_list_presets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        for ((protocol, info) in recyclerViewMap) {
            val recyclerView = view.findViewById<RecyclerView>(info.recyclerViewId)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            info.adapter = ListPresetsAdapter(requireContext(), this)
            recyclerView.adapter = info.adapter
            val emptyMessage = view.findViewById<View>(info.emptyMessageId).also { it.visibility = View.VISIBLE }
            presetRepository.getCustomByProtocol(protocol).observe(viewLifecycleOwner) { presets ->
                Timber.i("getByProtocol(${protocol}) = ${presets.size}")
                info.adapter?.updatePresets(presets)
                emptyMessage.visibility = if (presets.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        initialiseFab()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.list_presets_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_all) {
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Presets")
                    .setMessage("Remove all custom presets? The built-in defaults will still remain.")
                    .setPositiveButton(android.R.string.ok) { _, _ -> presetRepository.deleteDatabase() }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClickEditItem(preset: OutputPreset) {
        navController.safelyNavigate(uiResources.listToEditDirections(preset))
    }

    override fun onClickDeleteItem(preset: OutputPreset) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Preset")
                .setMessage("Are you sure you want to delete " + preset.alias + "?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ -> presetRepository.deletePreset(preset) }
                .show()
    }

    private fun initialiseFab() {
        /* Add the two items */
        val speedDial = requireView().findViewById<SpeedDialView>(R.id.fab)
        speedDial.mainFabClosedIconColor = ContextCompat.getColor(requireContext(), R.color.black)
        speedDial.buildAndAddAction(R.id.new_preset_create, R.drawable.edit, R.string.fab_create_new_preset)
        speedDial.buildAndAddAction(R.id.new_preset_import, R.drawable.import_file, R.string.fab_import_new_preset)

        /* React to an item being tapped */
        speedDial.setOnActionSelectedListener {
            speedDial.close()
            return@setOnActionSelectedListener when (it.id) {
                R.id.new_preset_create -> {
                    /* Create new preset, so launch the activity to enter the required values */
                    navController.safelyNavigate(uiResources.listToEditDirections(null))
                    true
                }
                R.id.new_preset_import -> {
                    /* Import preset from file, so launch file browser activity */
                    showImportDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showImportDialog() {
        Notify.toast(requireContext(), "Select either a .pref or a .zip file")
        MaterialFilePicker()
                .withSupportFragment(this)
                .withCloseMenu(true)
                .withPath(Paths.EXTERNAL_DIRECTORY.absolutePath)
                .withRootPath(Paths.EXTERNAL_DIRECTORY.absolutePath)
                .withFilter(Pattern.compile(".*\\.(zip|pref)$"))
                .withFilterDirectories(false)
                .withTitle("Import ATAK Preference File")
                .withRequestCode(IMPORT_FILE_REQUEST_CODE)
                .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        super.onActivityResult(requestCode, resultCode, result)
        if (result == null || requestCode != IMPORT_FILE_REQUEST_CODE) {
            Notify.orange(requireView(), "Nothing imported!")
        } else if (resultCode != Activity.RESULT_OK) {
            Notify.red(requireView(), "Failed importing file!")
        } else {
            val path = result.getStringExtra(FilePickerActivity.RESULT_FILE_PATH) ?: return
            PresetPreferenceFileParser(requireContext(), path).parse {
                if (it == null) {
                    Notify.red(requireView(), "Failed importing from $path")
                } else {
                    navController.safelyNavigate(uiResources.listToEditDirections(it))
                }
            }
        }
    }

    private companion object {
        val IMPORT_FILE_REQUEST_CODE = GenerateInt.next()
    }

    private fun SpeedDialView.buildAndAddAction(@IdRes idRes: Int, @DrawableRes drawableRes: Int, @StringRes stringRes: Int) {
        val accent = ContextCompat.getColor(context, uiResources.accentColourId)
        val textColour = ContextCompat.getColor(context, R.color.black)
        addActionItem(
                SpeedDialActionItem.Builder(idRes, drawableRes)
                        .setLabel(stringRes)
                        .setFabBackgroundColor(textColour)
                        .setFabImageTintColor(accent)
                        .setLabelBackgroundColor(accent)
                        .setLabelColor(textColour)
                        .setLabelClickable(true)
                        .create()
        )
    }
}
