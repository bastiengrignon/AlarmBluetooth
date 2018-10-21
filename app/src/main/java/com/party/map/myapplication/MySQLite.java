package com.party.map.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLite extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "db.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static MySQLite sInstance;

    static synchronized MySQLite getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MySQLite(context);
        }
        return sInstance;
    }

    private MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Création de la base de donnée
     * @param db : base de donnée à modifier
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AlarmManager.CREATE_TABLE_ALARM);
    }

    /**
     * Mise à jour de la base de donnée
     * @param db : base de donnée à changer
     * @param oldVersion : ancienne version de la base de donnée
     * @param newVersion : nouvelle version de la base de donnée
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}
