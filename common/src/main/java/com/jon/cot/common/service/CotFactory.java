package com.jon.cot.common.service;

import android.content.SharedPreferences;

import com.jon.cot.common.cot.CursorOnTarget;

import java.util.List;

public abstract class CotFactory {
    protected final SharedPreferences prefs;

    protected CotFactory(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    protected abstract List<CursorOnTarget> generate();
    protected abstract List<CursorOnTarget> initialise();
    protected abstract List<CursorOnTarget> update();

    protected abstract void clear();
}
