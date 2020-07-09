package com.jon.cotgenerator.utils;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

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

    public static boolean validateString(final String str) {
        return (str != null && !str.isEmpty());
    }

    public static boolean validateString(final String str, final String regexPattern) {
        if (!validateString(str)) return false;
        return Pattern.compile(regexPattern).matcher(str).find();
    }

    public static boolean validateCallsign(String callsign) {
        /* These characters break TAK parsing when using XML, but not protobuf. I'll block them from both just to be safe */
        final Character[] disallowedCharacters = new Character[] {
                '<', '>',
        };
        for (Character character : disallowedCharacters) {
            if (callsign.indexOf(character) != -1) return false;
        }
        return true;
    }

    /* We don't care about the call to InetAddress.getByName() returning anything, all we want is to catch any exceptions from calling it */
    @SuppressWarnings("ResultOfMethodCallIgnored")
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
