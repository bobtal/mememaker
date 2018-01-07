package com.teamtreehouse.mememaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.teamtreehouse.mememaker.utils.StorageType;

/**
 * Created by Boban Talevski on 10/18/2017.
 */

public class MemeMakerApplicationSettings {
    private static final String KEY_STORAGE_TYPE = "key_storage_type";
    SharedPreferences sharedPreferences;

    public MemeMakerApplicationSettings(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getStoragePreference() {
        return sharedPreferences.getString(KEY_STORAGE_TYPE, StorageType.INTERNAL);
    }

    public void setStoragePreference(String storageType) {
        sharedPreferences
                .edit()
                .putString(KEY_STORAGE_TYPE, storageType)
                .apply(); // could've used commit here instead
                // the difference is commit is synchronous which means it will block execution
                // of this line of code until it fully saves this field out
                // while apply is asynchronous which means it will do the saving on a
                // separate thread, thus allowing execution to continue and not wait for
                // the save to complete
                // If saving large data sets into shared preferences you could run into a problem
                // where you can get an "application not responding"

    }

}
