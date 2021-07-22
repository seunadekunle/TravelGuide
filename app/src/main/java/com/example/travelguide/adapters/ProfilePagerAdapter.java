package com.example.travelguide.adapters;

import android.os.Bundle;
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
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.travelguide.R;
import com.example.travelguide.fragments.ProfileGuideFragment;
import com.example.travelguide.helpers.HelperClass;

import org.jetbrains.annotations.NotNull;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    private int expandedImgViewID;
    private int expandedImgViewBgID;

    public ProfilePagerAdapter(@NonNull @NotNull FragmentActivity fragmentActivity, int expandedImgViewID, int expandedImgViewBgID) {
        super(fragmentActivity);

        this.expandedImgViewID = expandedImgViewID;
        this.expandedImgViewBgID = expandedImgViewBgID;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        return ProfileGuideFragment.newInstance(HelperClass.profileTabTitles[position], expandedImgViewID, expandedImgViewBgID);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
