package com.jon.common;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder;

import org.mockito.Mockito;

abstract public class SharedPreferenceTest {
    protected SharedPreferences sharedPreferences;

    public void initSharedPrefs() {
        sharedPreferences = new SPMockBuilder().createSharedPreferences();
        Context context = Mockito.mock(Context.class);
        Mockito.when(PreferenceManager.getDefaultSharedPreferences(context))
                .thenReturn(sharedPreferences);
    }
}
