package com.example.travelguide.fragments;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
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
import androidx.fragment.app.FragmentManager;
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

import java.util.Objects;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    private ImageButton ibAvatar;
    private TextView tvProfile;
    private Button logOutBtn;

    private int fragmentID;
    private FragmentManager fragmentManager;
    private ChangeAvatarFragment changeAvatarFragment;

    private ProfilePagerAdapter profilePagerAdapter;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;

    private static final String ARG_ID = "id";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public static ProfileFragment newInstance(int fragmentID) {

        Bundle args = new Bundle();
        args.putInt(ARG_ID, fragmentID);

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null){

            // sets entry state depending on instance variable
            fragmentID = getArguments().getInt(ARG_ID);
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        ibAvatar = view.findViewById(R.id.ibAvatar);
        tvProfile = view.findViewById(R.id.tvProfile);
        logOutBtn = view.findViewById(R.id.logOutBtn);

        fragmentManager = requireActivity().getSupportFragmentManager();
        changeAvatarFragment = new ChangeAvatarFragment();
        // sets username
        tvProfile.setText(ParseUser.getCurrentUser().getUsername());

        // gets profile image and load it
        HelperClass.loadProfileImage(getContext(), 500, 500, ibAvatar);

        ibAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelperClass.showFragment(fragmentManager, fragmentID, changeAvatarFragment, changeAvatarFragment.TAG);
            }
        });

        // handles logout button click
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutUser();
            }
        });

        viewPager2 = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        profilePagerAdapter = new ProfilePagerAdapter(requireActivity(), view.findViewById(R.id.expandedImgView), view.findViewById(R.id.expandedImgViewBG));
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
                requireActivity().finish();
            }
        });
    }
}