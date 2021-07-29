package com.example.travelguide.adapters;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import com.example.travelguide.R;
import com.example.travelguide.fragments.ProfileGuideFragment;
import com.example.travelguide.helpers.HelperClass;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    private static final String TAG = "ProfilePagerAdapter";

    private ImageView expandedImgViewID;
    private View expandedImgViewBgID;
    private String userID;

    @Deprecated public static final int USE_SET_USER_VISIBLE_HINT = 0;

    public ProfilePagerAdapter(@NonNull @NotNull FragmentActivity fragmentActivity, ImageView expandedImgViewID, View expandedImgViewBgID, String userID) {
        super(fragmentActivity);

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
        return 2;
    }
}
