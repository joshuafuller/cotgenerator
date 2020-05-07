package com.jon.cotgenerator.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class DeviceUid {
    private static final String TAG = DeviceUid.class.getSimpleName();
    private static final String FILENAME = "uuid.txt";

    private DeviceUid() { }

    private static String uid;

    public static String get() {
        return uid;
    }

    public static void generate(Context context) {
        try {
            File file = new File(context.getFilesDir(), FILENAME);
            if (file.exists()) {
                Log.i(TAG, "Reading UID from file...");
                uid = readUid(context);
                Log.i(TAG, "Successfully read UID " + uid);
            } else {
                Log.i(TAG, "Writing new UID to file...");
                uid = writeUid(context);
                Log.i(TAG, "Successfully written UID " + uid);
            }
        } catch (IOException e) {
            e.printStackTrace();
            uid = UUID.randomUUID().toString();
            Log.e(TAG, "Failed to read/write UID from/to file. Using a temporary one instead: " + uid);
        }
    }

    private static String readUid(Context context) throws IOException {
        InputStream inputStream = context.openFileInput(FILENAME);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = bufferedReader.readLine();
        inputStream.close();
        return line;
    }

    private static String writeUid(Context context) throws IOException {
        String uuid = UUID.randomUUID().toString();
        FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
        fos.write(uuid.getBytes());
        fos.close();
        return uuid;
    }
}
