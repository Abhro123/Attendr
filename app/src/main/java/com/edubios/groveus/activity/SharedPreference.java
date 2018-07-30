package com.edubios.groveus.activity;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference
{
    private static final String SHARED_PREF_NAME = "fcmsharedprefdemo";
    private static final String KEY_ACCESS_TOKEN = "token";

    private static SharedPreference mInstance;
    private static Context mCtx;

    private SharedPreference(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPreference getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPreference(context);
        }
        return mInstance;
    }
    //this method will save the device token to shared preferences
    public boolean storeToken(String token){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }

    //this method will fetch the device token from shared preferences
    public String getToken(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_MULTI_PROCESS);
        String token=sharedPreferences.getString(KEY_ACCESS_TOKEN,"");
        return token;
    }
}