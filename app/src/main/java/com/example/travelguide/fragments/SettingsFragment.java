package com.example.travelguide.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.travelguide.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}