package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;

import java.util.List;

abstract class CotGenerator {
    final SharedPreferences prefs;

    CotGenerator(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    static CotGenerator getFromPrefs(SharedPreferences prefs) {
        return new FakeCotGenerator(prefs);
    }

    protected abstract List<CursorOnTarget> generate();
    protected abstract List<CursorOnTarget> initialise();
    protected abstract List<CursorOnTarget> update();

    protected abstract void clear();
}
