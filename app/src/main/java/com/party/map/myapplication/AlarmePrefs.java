package com.party.map.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AlarmePrefs {

    private static final String PREFS_ALARM = "current_alarm";
    private static SharedPreferences settings;
    private static Editor editor;
    private static final String KEY_PREFIX   = "KEY";
    private static final String KEY_H_ALARM  = "KEY_H_ALARM";
    private static final String KEY_ENABLED  = "KEY_ENABLED";

    AlarmePrefs(Context context) {
        if (settings == null) {
            settings = context.getSharedPreferences(PREFS_ALARM, Context.MODE_PRIVATE);
        }
        editor = settings.edit();
    }

    private String getFieldKey(int id, String fieldKey) {
        return KEY_PREFIX + id + "_" + fieldKey;
    }

    void setPrefsAlarm(AlarmeItem alarmeItem) {
        if (alarmeItem == null) {
            return;
        }
        int id = alarmeItem.getIdAlarm();
        editor.putString(  getFieldKey(id, KEY_H_ALARM), alarmeItem.gethAlarm() );
        editor.putBoolean( getFieldKey(id, KEY_ENABLED), alarmeItem.isAlarmEnabled() );
        editor.apply();
    }

    /**
     * Récupérer l'alarme
     * @param id : id de l'alarme
     * @return iten de l'alarme depuis l'id
     */
    AlarmeItem getPrefsAlarm(int id) {
        String hAlarm   = settings.getString(  getFieldKey(id, KEY_H_ALARM), "");
        boolean enabled = settings.getBoolean( getFieldKey(id, KEY_ENABLED), false);

        return new AlarmeItem(id, hAlarm, enabled);
    }

    void deleteAlarm(AlarmeItem alarmeItem) {
        if (alarmeItem == null) {
            return;
        }
        int id = alarmeItem.getIdAlarm();
        editor.remove( getFieldKey(id, KEY_H_ALARM) );
        editor.remove( getFieldKey(id, KEY_ENABLED) );
        editor.apply();
    }
}
