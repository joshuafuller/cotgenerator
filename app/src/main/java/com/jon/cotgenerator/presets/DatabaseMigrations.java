package com.jon.cotgenerator.presets;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

class DatabaseMigrations {
    private DatabaseMigrations() { /* blank */ }

    public static Migration[] getAllMigrations() {
        return new Migration[] {
                V1_TO_V2,
        };
    }

    /* 06 July 2020: Added SSL client certificate and truststore storage. */
    public static final Migration V1_TO_V2 = new Migration(1, 2) {
        @Override public void migrate(SupportSQLiteDatabase database) {
            /* Four new optional columns, only needed for SSL presets */
            database.execSQL("ALTER TABLE Presets ADD ClientCert BLOB DEFAULT NULL");
            database.execSQL("ALTER TABLE Presets ADD ClientCertPassword TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE Presets ADD TrustStore BLOB DEFAULT NULL");
            database.execSQL("ALTER TABLE Presets ADD TrustStorePassword TEXT DEFAULT NULL");
        }
    };
}
