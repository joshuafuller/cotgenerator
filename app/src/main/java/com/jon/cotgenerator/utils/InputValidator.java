package com.jon.cotgenerator.utils;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class InputValidator {
    private InputValidator() { }

    public static boolean validateInt(final String str, final Integer min, final Integer max) {
        try {
            int number = Integer.parseInt(str);
            return (min == null || number >= min) && (max == null || number <= max);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean validateDouble(final String str, final Double min, final Double max) {
        try {
            double number = Double.parseDouble(str);
            return (min == null || number >= min) && (max == null || number <= max);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static class ValidateHostnameTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                InetAddress.getByName(params[0]);
                return true;
            } catch (UnknownHostException e) {
                return false;
            }
        }
    }

    public static boolean validateHostname(final String host) {
        try {
            return new ValidateHostnameTask().execute(host).get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }
}
