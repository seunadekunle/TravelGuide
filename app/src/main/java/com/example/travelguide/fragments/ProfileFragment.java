package com.example.travelguide.fragments;

import android.content.Intent;
import android.os.Build;
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
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    private ImageButton ibAvatar;
    private TextView tvProfile;
    private Button logOutBtn;
    private ImageView ivExpanded;
    private View imageBG;

    private int frameID;
    private FragmentManager fragmentManager;
    private ChangeAvatarFragment changeAvatarFragment;
    private ParseUser parseUser;
    private String userID;

    private ProfilePagerAdapter profilePagerAdapter;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;

    private static final String ARG_ID = "fragment_id";
    private static final String ARG_USER = "user_id";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public static ProfileFragment newInstance(int fragmentID, String userID) {

        Bundle args = new Bundle();

        args.putInt(ARG_ID, fragmentID);
        args.putString(ARG_USER, userID);

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ProfileFragment newInstance(String userID) {

        Bundle args = new Bundle();

        args.putString(ARG_USER, userID);

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            // sets entry state depending on instance variable
            frameID = getArguments().getInt(ARG_ID);
            userID = getArguments().getString(ARG_USER);
        }
    }

    // returns user based on ID
    public void setParseUser(String userID) {

        if (userID != null) {
            if (ParseUser.getCurrentUser().getObjectId().equals(userID)) {
                parseUser = ParseUser.getCurrentUser();
                displayUserDetails();
            } else {
                HelperClass.fetchUser(userID, (object, e) -> {
                    Log.i(TAG, object.getObjectId());
                    parseUser = object;

                    displayUserDetails();
                    hidePrivateInfo();
                });
            }
        }
    }

    public void displayUserDetails() {

        Log.i(TAG, parseUser.getUsername());
        // sets username
        tvProfile.setText(parseUser.getUsername());
        loadAvatar();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {


        ibAvatar = view.findViewById(R.id.ibAvatar);
        tvProfile = view.findViewById(R.id.tvProfile);
        logOutBtn = view.findViewById(R.id.logOutBtn);
        ivExpanded = view.findViewById(R.id.expandedImgView);
        imageBG = view.findViewById(R.id.expandedImgViewBG);

        fragmentManager = requireActivity().getSupportFragmentManager();
        changeAvatarFragment = ChangeAvatarFragment.newInstance(true);

        ibAvatar.setOnClickListener(v ->
                HelperClass.replaceFragment(fragmentManager, frameID, changeAvatarFragment, changeAvatarFragment.TAG));

        // handles logout button click
        logOutBtn.setOnClickListener(v -> logOutUser());

        viewPager2 = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        setParseUser(userID);
        loadViewPager();

        // sets title of viewpager
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    tab.setText(HelperClass.profileTabTitles[position]);
                }
        ).attach();
    }

    // hides ui information that only the logged in user can see
    private void hidePrivateInfo() {
        logOutBtn.setVisibility(View.GONE);
        ibAvatar.setClickable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ibAvatar.setForeground(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // reload data
        if (parseUser != null) {
            displayUserDetails();
        }

        loadViewPager();
    }

    public void loadViewPager() {
        profilePagerAdapter = new ProfilePagerAdapter(getChildFragmentManager(), getLifecycle(), ivExpanded, imageBG, userID);
        viewPager2.setAdapter(profilePagerAdapter);

        viewPager2.setSaveEnabled(false);
        viewPager2.setCurrentItem(0);
    }

    // loads the avatar for the profile image
    public void loadAvatar() {

        String profileUrl = null;
        profileUrl = parseUser.getParseFile("avatar").getUrl();

        // gets profile image and load it
        HelperClass.loadProfileImage(profileUrl, getContext(), 500, 500, ibAvatar);
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