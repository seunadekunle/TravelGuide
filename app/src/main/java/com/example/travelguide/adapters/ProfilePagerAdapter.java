package com.example.travelguide.adapters;

import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.travelguide.fragments.ProfileGuideFragment;
import com.example.travelguide.helpers.HelperClass;

import org.jetbrains.annotations.NotNull;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    private static final String TAG = "ProfilePagerAdapter";

    private ImageView expandedImgViewID;
    private View expandedImgViewBgID;
    private String userID;

    @Deprecated
    public static final int USE_SET_USER_VISIBLE_HINT = 0;
    private static final int ITEM_COUNT = 2;

    public ProfilePagerAdapter(@NonNull @NotNull FragmentManager fragmentManager, Lifecycle lifecycle, ImageView expandedImgViewID, View expandedImgViewBgID, String userID) {
        super(fragmentManager, lifecycle);

        this.expandedImgViewID = expandedImgViewID;
        this.expandedImgViewBgID = expandedImgViewBgID;
        this.userID = userID;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {

        ProfileGuideFragment profileGuideFragment = ProfileGuideFragment.newInstance(HelperClass.profileTabTitles[position], userID);
        profileGuideFragment.setExpandedElements(expandedImgViewID, expandedImgViewBgID);

        return profileGuideFragment;
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }
}
