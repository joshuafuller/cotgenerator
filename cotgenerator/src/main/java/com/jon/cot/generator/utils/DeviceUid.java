package com.jon.cot.generator.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import timber.log.Timber;

public class DeviceUid {
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
                Timber.i("Reading UID from file...");
                uid = readUid(context);
                Timber.i("Successfully read UID %s", uid);
            } else {
                Timber.i("Writing new UID to file...");
                uid = writeUid(context);
                Timber.i("Successfully written UID %s", uid);
            }
        } catch (IOException e) {
            uid = UUID.randomUUID().toString();
            Timber.e(e);
            Timber.e("Failed to read/write UID from/to file. Using a temporary one instead: %s", uid);
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
