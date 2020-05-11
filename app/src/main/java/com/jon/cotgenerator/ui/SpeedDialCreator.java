package com.jon.cotgenerator.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.jon.cotgenerator.R;
import com.jon.cotgenerator.service.CotService;
import com.jon.cotgenerator.utils.Notify;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

class SpeedDialCreator {
    private static final String TAG = SpeedDialCreator.class.getSimpleName();

    private SpeedDialCreator() {
    }

    static SpeedDialView getSpeedDial(Activity activity) {
        SpeedDialView speedDial = activity.findViewById(R.id.speedDial);
        speedDial.addActionItem(buildSpeedDialViewItem(activity, R.id.startEmergency, R.drawable.start, R.string.startEmergency));
        speedDial.addActionItem(buildSpeedDialViewItem(activity, R.id.cancelEmergency, R.drawable.stop, R.string.cancelEmergency));
        speedDial.setOnActionSelectedListener(actionItem -> {
            Intent intent = new Intent(activity, CotService.class);
            intent.setAction(actionItem.getId() == R.id.startEmergency ? CotService.START_EMERGENCY : CotService.CANCEL_EMERGENCY);
            activity.startService(intent);
            return false;
        });
        return speedDial;
    }

    static SpeedDialView getDisabledSpeedDial(Activity activity) {
        SpeedDialView speedDial = activity.findViewById(R.id.speedDialDisabled);
        speedDial.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                Notify.orange(activity.findViewById(android.R.id.content), "Press the start button first!");
                return false;
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
            }
        });
        return speedDial;
    }

    private static SpeedDialActionItem buildSpeedDialViewItem(Context context, int itemId, int iconId, int textId) {
        int textColour = context.getColor(R.color.colorAccent);
        int backgroundColour = context.getColor(R.color.white);
        return new SpeedDialActionItem.Builder(itemId, iconId)
                .setFabBackgroundColor(backgroundColour)
                .setFabImageTintColor(textColour)
                .setLabel(context.getString(textId))
                .setLabelColor(textColour)
                .setLabelBackgroundColor(backgroundColour)
                .setLabelClickable(true)
                .create();
    }
}
