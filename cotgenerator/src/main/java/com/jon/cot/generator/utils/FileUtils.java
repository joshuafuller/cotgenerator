package com.jon.cot.generator.utils;

import androidx.annotation.RawRes;

import com.jon.cot.generator.CotApplication;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class FileUtils {
    private FileUtils() { /* blank */ }

    public static byte[] toByteArray(String filepath) throws IOException {
        return IOUtils.toByteArray(new FileInputStream(filepath));
    }

    public static byte[] toByteArraySafe(@RawRes int resId) {
        try {
            InputStream inputStream = CotApplication.getContext().getResources().openRawResource(resId);
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            Timber.e(e);
            Timber.e("Error loading raw resource, this shouldn't happen!");
            return null;
        }
    }
}
