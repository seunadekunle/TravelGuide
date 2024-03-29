package com.example.travelguide.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.travelguide.R;
import com.example.travelguide.adapters.ProfilePagerAdapter;
import com.example.travelguide.helpers.DeviceDimenHelper;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    private ImageButton ibAvatar;
    private TextView tvProfile;
    private TextView tvDate;
    private ImageView ivExpanded;
    private View imageBG;
    private ImageButton ibSettings;

    private int frameID;
    private FragmentManager fragmentManager;
    private ChangeAvatarFragment changeAvatarFragment;
    private SettingsFragment settingsFragment;
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
                    parseUser = object;

                    displayUserDetails();
                    hidePrivateInfo();
                });
            }
        }
    }

    public void displayUserDetails() {

        // sets username
        tvProfile.setText(parseUser.getUsername());
        // shows date user joined
        tvDate.setText(getDateString(parseUser.getCreatedAt()));

        loadAvatar();
    }

    // returns formatted date object
    private String getDateString(Date date) {

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
        return "Joined " + dateFormat.format(date);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ibAvatar = view.findViewById(R.id.ibAvatar);
        tvProfile = view.findViewById(R.id.tvProfile);
        tvDate = view.findViewById(R.id.tvDate);
        ibSettings = view.findViewById(R.id.ibSettings);
        ivExpanded = view.findViewById(R.id.expandedImgView);
        imageBG = view.findViewById(R.id.expandedImgViewBG);

        fragmentManager = getChildFragmentManager();
        changeAvatarFragment = ChangeAvatarFragment.newInstance(true);
        settingsFragment = new SettingsFragment();

        ibAvatar.setOnClickListener(v ->
                HelperClass.addFragment(fragmentManager, R.id.childFrame, changeAvatarFragment, changeAvatarFragment.TAG, true, true));


        // handles setting page click
        ibSettings.setOnClickListener(v -> HelperClass.addFragment(fragmentManager, R.id.childFrame, settingsFragment, SettingsFragment.TAG, true, true));

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
        ibSettings.setVisibility(View.GONE);
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
        profilePagerAdapter = new ProfilePagerAdapter(fragmentManager, getLifecycle(), ivExpanded, imageBG, userID);
        viewPager2.setAdapter(profilePagerAdapter);

        viewPager2.setSaveEnabled(false);
        viewPager2.setCurrentItem(0);
    }

    // loads the avatar for the profile image
    public void loadAvatar() {

        String profileUrl = null;
        profileUrl = Objects.requireNonNull(parseUser.getParseFile("avatar")).getUrl();

        // sets profile image dimension based on screen size
        int profileDimen = (int) (DeviceDimenHelper.getDisplayHeightPixels(requireContext()) / 6.5);
        // gets profile image and load it
        HelperClass.loadProfileImage(profileUrl, getContext(), profileDimen, profileDimen, ibAvatar);
    }
}