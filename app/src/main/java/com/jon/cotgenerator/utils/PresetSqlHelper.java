package com.jon.cotgenerator.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.enums.Protocol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PresetSqlHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "presets.db";
    private static final String TABLE = "Presets";
    private static final String PROTOCOL = "Protocol";
    private static final String ALIAS = "Alias";
    private static final String ADDRESS = "Address";
    private static final String PORT = "Port";

    private static final String CREATE_TABLE = String.format(
            "CREATE TABLE IF NOT EXISTS %s(PrimaryKey INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, %s TEXT, %s TEXT, %s TEXT, %s INTEGER);",
            TABLE, PROTOCOL, ALIAS, ADDRESS, PORT);
    private static final String DELETE_ALL = "DROP TABLE IF EXISTS " + TABLE;

    public PresetSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_ALL);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertPreset(OutputPreset preset) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PROTOCOL, preset.protocol.get());
        values.put(ALIAS, preset.alias);
        values.put(ADDRESS, preset.address);
        values.put(PORT, preset.port);
        db.insert(TABLE, null, values);
    }

    public List<OutputPreset> getAllPresets(Protocol protocol) {
        SQLiteDatabase db = getReadableDatabase();
        String[] selectColumns = { ALIAS, ADDRESS, PORT };
        String whereCondition = PROTOCOL + " = ?";
        String[] whereArgs = { protocol.get() };
        String sortOrder = ALIAS + " DESC";
        Cursor cursor = db.query(
                TABLE,           // The table to query
                selectColumns,   // The array of columns to return (pass null to get all)
                whereCondition,  // The columns for the WHERE clause
                whereArgs,       // The values for the WHERE clause
                null,   // don't group the rows
                null,    // don't filter by row groups
                sortOrder        // The sort order
        );
        List<OutputPreset> presets = new ArrayList<>();
        while (cursor.moveToNext()) {
            presets.add(new OutputPreset(
                    protocol,
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getInt(2)
            ));
        }
        cursor.close();
        return presets;
    }

    public static boolean deleteDatabase() {
        String dbPath = Environment.getDataDirectory() + "/data/" + BuildConfig.APPLICATION_ID + "/databases/";
        return new File(dbPath, DATABASE_NAME).delete();
    }
}
