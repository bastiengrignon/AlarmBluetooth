package com.party.map.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AlarmManager {
    private static final String TABLE_NAME = "alarme";
    private static final String KEY_ID_ALARM = "id_alarme";
    private static final String KEY_HOUR_ALARM = "hour_alarme";
    private static final String KEY_IS_ALARM_ENABLED = "alarm_enabled";
    static final String CREATE_TABLE_ALARM = "CREATE TABLE"
            + TABLE_NAME
            + " (" + " "
            + KEY_ID_ALARM
            + "INTEGER primary key,"
            + " "
            + KEY_HOUR_ALARM
            + " TEXT"
            + KEY_IS_ALARM_ENABLED
            + "INTEGER DEFAULT 0"
            + ");";

    private MySQLite maBaseSQLite;
    private SQLiteDatabase db;

    public AlarmManager(Context context) {
        maBaseSQLite = MySQLite.getInstance(context);
    }

    public void open() {
        db = maBaseSQLite.getWritableDatabase();
    }

    public void close() {
        db.close();
    }

    public long addAlarm(AlarmeItem alarmeItem) {
        ContentValues values = new ContentValues();
        values.put(KEY_HOUR_ALARM, alarmeItem.gethAlarm());
        values.put(KEY_IS_ALARM_ENABLED, alarmeItem.isAlarmEnabled());

        return db.insert(TABLE_NAME, null, values);
    }

    public int removeAlarm(AlarmeItem alarmeItem) {
        String where = KEY_ID_ALARM + " = ?";
        String[] whereArgs = {alarmeItem.getIdAlarm() + ""};

        return db.delete(TABLE_NAME, where, whereArgs);
    }

    public AlarmeItem getAlarm(int id) {
        AlarmeItem alarmeItem = new AlarmeItem();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID_ALARM + "=" + id, null);

        if (cursor.moveToFirst()) {
            alarmeItem.setIdAlarm(cursor.getInt(cursor.getColumnIndex(KEY_ID_ALARM)));
            alarmeItem.sethAlarm(cursor.getString(cursor.getColumnIndex(KEY_HOUR_ALARM)));
            //alarmeItem.setAlarmEnabled(cursor.getInt(cursor.getColumnIndex(KEY_IS_ALARM_ENABLED)));
            cursor.close();
        }
        return alarmeItem;
    }

    public Cursor getAlarms() {
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }


}
