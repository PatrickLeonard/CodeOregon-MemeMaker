package com.codeoregonapp.patrickleonard.mememaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.codeoregonapp.patrickleonard.mememaker.utils.StorageType;

/**
 * Created by Evan Anger on 8/13/14.
 */
public class MemeMakerApplicationSettings {


    private final static String STORAGE_PREFERENCE = "storage";

    private SharedPreferences mSharedPreferences;

    public MemeMakerApplicationSettings(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getStoragePreference() {
        return mSharedPreferences.getString(STORAGE_PREFERENCE, StorageType.INTERNAL);
    }

    public void setStoragePreference(String storageType) {
        mSharedPreferences.edit().putString(STORAGE_PREFERENCE,storageType).apply();
    }

}
