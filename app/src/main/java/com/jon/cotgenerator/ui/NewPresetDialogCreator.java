package com.jon.cotgenerator.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.enums.Protocol;
import com.jon.cotgenerator.presets.PresetRepository;
import com.jon.cotgenerator.utils.InputValidator;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.PrefUtils;

class NewPresetDialogCreator {
    static void show(Context context, View root, SharedPreferences prefs, PresetRepository repository) {
        show(context, root, prefs, repository, null, null, null);
    }

    private static void show(Context context, View root, SharedPreferences prefs, PresetRepository repository, @Nullable String previousAlias,
                             @Nullable String previousAddress, @Nullable String previousPort) {
        View view = View.inflate(context, R.layout.dialog_add_new_preset, null);
        Spinner protocolSpinner = view.findViewById(R.id.presetProtocol);
        EditText aliasEditText = view.findViewById(R.id.presetAlias);
        EditText addressEditText = view.findViewById(R.id.presetAddress);
        EditText portEditText = view.findViewById(R.id.presetPort);

        /* Set the spinner based on which protocol is selected in the main settings screen. 0 is TCP, 1 is UDP */
        protocolSpinner.setSelection(Protocol.fromPrefs(prefs) == Protocol.TCP ? 0 : 1);

        /* Set previous values, if any */
        setEditTextIfValid(aliasEditText, previousAlias);
        setEditTextIfValid(addressEditText, previousAddress);
        setEditTextIfValid(portEditText, previousPort);

        DialogInterface.OnClickListener onPositiveButtonListener = (dialog, i) -> {
            String reason;
            if (getString(aliasEditText).length() == 0) {
                reason = "empty alias";
            } else if (!InputValidator.validateHostname(getString(addressEditText))) {
                reason = "invalid destination address";
            } else if (!InputValidator.validateInt(getString(portEditText), 1, 65535)) {
                reason = "port number must be an integer between 1 and 65535";
            } else {
                /* All valid input, so take it and back out */
                dialog.dismiss();
                String protocol = protocolSpinner.getSelectedItem().toString();
                repository.insertPreset(protocol, getString(aliasEditText), getString(addressEditText), getInt(portEditText));
                int currentValue = PrefUtils.parseInt(prefs, Key.NEW_PRESET_ADDED);
                prefs.edit().putString(Key.NEW_PRESET_ADDED, Integer.toString(currentValue + 1)).apply();
                return;
            }
            /* Validation error, so inform the user and show another dialog with the previous entries */
            Notify.red(root, "Invalid input: " + reason);
            dialog.dismiss();
            show(context, root, prefs, repository, getString(aliasEditText), getString(addressEditText), getString(portEditText));
        };

        /* Build and show the dialog */
        new MaterialAlertDialogBuilder(context, R.style.Dialog)
                .setView(view)
                .setTitle(R.string.addNewPreset)
                .setPositiveButton("ADD", onPositiveButtonListener)
                .setNegativeButton(android.R.string.cancel, (dialog, i) -> dialog.dismiss())
                .show();
    }

    private static void setEditTextIfValid(EditText editText, @Nullable String value) {
        if (value != null) {
            editText.setText(value);
        }
    }

    private static String getString(EditText editText) {
        return editText.getText().toString().trim();
    }

    private static int getInt(EditText editText) {
        return Integer.parseInt(editText.getText().toString().trim());
    }
}
