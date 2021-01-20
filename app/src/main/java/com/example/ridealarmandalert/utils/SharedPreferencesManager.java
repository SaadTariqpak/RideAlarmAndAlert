package com.example.ridealarmandalert.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private SharedPreferences sharedPreferences;
    private Context context;
    private SharedPreferences.Editor editor;

    public static final String MyPREFERENCES = "RideAlarmApp";
    public static final String USERNAME = "RideAlarmAppUsername";
    public static final String USERUNIQUEID = "RideAlarmAppUserid";


    public static final String CAT = "userCategory";

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public void setPreferences(String prefName, String value) {
        editor.putString(prefName, value);
        editor.commit();
    }

    public void setPreferences(String prefName, int value) {
        editor.putInt(prefName, value);
        editor.commit();
    }

    public SharedPreferences.Editor getPreferenceEditor() {
        return editor;
    }

    public SharedPreferences getPreferencesManager() {
        return sharedPreferences;
    }
}
