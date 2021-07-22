package com.example.travelguide.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.travelguide.R;
import com.example.travelguide.helpers.HelperClass;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {

    private ImageButton ibAvatar;
    private TextView tvProfile;

    public static final String TAG = "ProfileFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ibAvatar = view.findViewById(R.id.ibAvatar);
        tvProfile = view.findViewById(R.id.tvProfile);

        // sets username
        tvProfile.setText(ParseUser.getCurrentUser().getUsername());

        ParseFile profileImg = ParseUser.getCurrentUser().getParseFile("avatar");

        HelperClass.loadProfileImage(getContext(), profileImg, 500, 5000, ibAvatar);
    }
}