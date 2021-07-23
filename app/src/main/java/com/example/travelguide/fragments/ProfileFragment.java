package com.example.travelguide.fragments;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.travelguide.R;
import com.example.travelguide.activities.EntryActivity;
import com.example.travelguide.adapters.ProfilePagerAdapter;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {

    interface sendImage {
        void sendImage(ImageView message);
    }

    public static final String TAG = "ProfileFragment";

    private ImageButton ibAvatar;
    private TextView tvProfile;
    private Button logOutBtn;

    private ProfilePagerAdapter profilePagerAdapter;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        ibAvatar = view.findViewById(R.id.ibAvatar);
        tvProfile = view.findViewById(R.id.tvProfile);
        logOutBtn = view.findViewById(R.id.logOutBtn);

        // sets username
        tvProfile.setText(ParseUser.getCurrentUser().getUsername());

        // gets profile image and load it
        ParseFile profileImg = ParseUser.getCurrentUser().getParseFile("avatar");

        if(profileImg != null)
            HelperClass.loadProfileImage(getContext(), profileImg, 500, 500, ibAvatar);

        // handles logout button click
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutUser();
            }
        });

        viewPager2 = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        profilePagerAdapter = new ProfilePagerAdapter(getActivity(), view.findViewById(R.id.expandedImgView), view.findViewById(R.id.expandedImgViewBG));
        viewPager2.setAdapter(profilePagerAdapter);

        // sets title of viewpager
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText(HelperClass.profileTabTitles[position])
        ).attach();
    }

    // logs out the user
    private void logOutUser() {

        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {

                // creates new intent to entry page and clears history
                Intent toEntry = new Intent(getContext(), EntryActivity.class);
                startActivity(toEntry);
                getActivity().finish();
            }
        });
    }
}