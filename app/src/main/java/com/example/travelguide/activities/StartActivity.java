package com.example.travelguide.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.travelguide.R;
import com.example.travelguide.databinding.ActivityStartBinding;
import com.example.travelguide.fragments.ComposeFragment;
import com.example.travelguide.fragments.LoginFragment;
import com.example.travelguide.helpers.HelperClass;
import com.parse.ParseUser;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = StartActivity.class.getSimpleName();

    private ActivityStartBinding binding;

    private Button loginBtn;
    private Button signupBtn;
    private int entryFrameId;

    private FragmentManager fragmentManager;
    private LoginFragment loginFragment;

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

        fragmentManager = getSupportFragmentManager();
        loginFragment = new LoginFragment();

        entryFrameId = R.id.entryForm;
        loginBtn = binding.loginBtn;
        signupBtn = binding.signupBtn;

        // on click listener for login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // goes to login fragment
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(entryFrameId, loginFragment);
                HelperClass.finishTransaction(ft, TAG, (Fragment) loginFragment);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    // navigates to the Map Stream view
    public void navigateToMapView() {
        Intent toMap = new Intent(StartActivity.this, MapsActivity.class);
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