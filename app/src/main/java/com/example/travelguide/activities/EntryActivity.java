package com.example.travelguide.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.travelguide.R;
import com.example.travelguide.databinding.ActivityStartBinding;
import com.example.travelguide.fragments.EntryFormFragment;
import com.example.travelguide.helpers.HelperClass;
import com.parse.ParseUser;

public class EntryActivity extends AppCompatActivity {

    private static final String TAG = EntryActivity.class.getSimpleName();

    private ActivityStartBinding binding;

    private Button loginBtn;
    private Button signupBtn;
    private int entryFrameId;

    private FragmentManager fragmentManager;
    private EntryFormFragment loginFormFragment;
    private EntryFormFragment signUpFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // if there is a user logged in
        if (ParseUser.getCurrentUser() != null)
            navigateToMapView();

        getSupportActionBar().hide();

        entryFrameId = R.id.entryForm;
        loginBtn = binding.loginBtn;
        signupBtn = binding.signupBtn;

        fragmentManager = getSupportFragmentManager();
        loginFormFragment = EntryFormFragment.newInstance(entryFrameId, "Login");
        signUpFragment = EntryFormFragment.newInstance(entryFrameId, "Signup");


        // on click listener for login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "login");
                // goes to login fragment
                HelperClass.replaceFragment(fragmentManager, entryFrameId, loginFormFragment, loginFormFragment.TAG);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // goes to signup fragment
                HelperClass.replaceFragment(fragmentManager, entryFrameId, signUpFragment, signUpFragment.TAG);
            }
        });
    }

    // navigates to the Map Stream view
    public void navigateToMapView() {
        Intent toMap = new Intent(EntryActivity.this, MapsActivity.class);
        startActivity(toMap);
        finish();
    }

    // handles navigation across fragments
    @Override
    public void onBackPressed() {

        if (HelperClass.emptyBackStack(fragmentManager))
            super.onBackPressed();
        else
            fragmentManager.popBackStack();
    }
}