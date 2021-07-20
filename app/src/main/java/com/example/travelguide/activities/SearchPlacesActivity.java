package com.example.travelguide.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travelguide.R;

public class SearchPlacesActivity extends AppCompatActivity {

    private static final String TAG = SearchPlacesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_places);
    }
}