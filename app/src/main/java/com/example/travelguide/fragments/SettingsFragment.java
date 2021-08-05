package com.example.travelguide.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelguide.R;
import com.example.travelguide.activities.EntryActivity;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    public static final String TAG = "SettingsFragment";
    private Button logOutBtn;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logOutBtn = view.findViewById(R.id.logOutBtn);
        // handles logout button click
        logOutBtn.setOnClickListener(v -> logOutUser());
    }

    // logs out the user
    private void logOutUser() {

        ParseUser.logOutInBackground(e -> {
            // creates new intent to entry page and clears history
            Intent toEntry = new Intent(getContext(), EntryActivity.class);
            startActivity(toEntry);
            requireActivity().finish();
        });
    }
}