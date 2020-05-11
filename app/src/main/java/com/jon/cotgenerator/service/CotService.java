package com.jon.cotgenerator.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.ui.CotActivity;

public class CotService extends Service {
    private static final String TAG = CotService.class.getSimpleName();
    private static final String BASE_INTENT_ID = BuildConfig.APPLICATION_ID + ".CotService.";
    public static final String START_SERVICE = BASE_INTENT_ID + "START";
    public static final String STOP_SERVICE = BASE_INTENT_ID + "STOP";
    public static final String CLOSE_SERVICE_INTERNAL = BASE_INTENT_ID + "CLOSE_SERVICE_INTERNAL";
    public static final String START_EMERGENCY = BASE_INTENT_ID + "SEND_EMERGENCY";
    public static final String CANCEL_EMERGENCY = BASE_INTENT_ID + "CANCEL_EMERGENCY";

    private SharedPreferences prefs;
    private CotManager cotManager;

    @Override
    public IBinder onBind(Intent intent) {
        /* TODO: Return the communication channel to the service */
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cotManager = new CotManager(prefs);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_SERVICE:
                    cotManager.start();
                    startForegroundService();
                    break;
                case STOP_SERVICE:
                    cotManager.shutdown();
                    stopForegroundService();
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CLOSE_SERVICE_INTERNAL));
                    break;
                case START_EMERGENCY:
                    cotManager.startEmergency();
                    break;
                case CANCEL_EMERGENCY:
                    cotManager.cancelEmergency();
                    break;
            }
        }
        return Service.START_STICKY;
    }

    private void startForegroundService() {
        /* minimum importance -> no heads-up service notification */
        NotificationChannel channel = new NotificationChannel(
                BuildConfig.APPLICATION_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        channel.enableVibration(false);
        channel.setSound(null, null);
        channel.setShowBadge(false);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            Log.e(TAG, "NotificationManager == null");
            return;
        }
        manager.createNotificationChannel(channel);

        /* Intent to launch main activity when tapping the notification */
        Intent launchIntent = new Intent(this, CotActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent launchPendingIntent = PendingIntent.getActivity(
                this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.target)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(launchPendingIntent)
                .build();
        /* Not sure why the number 2 is important here, but putting 0 or 1 gives no notification */
        startForeground(3, notification);
    }

    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }
}
