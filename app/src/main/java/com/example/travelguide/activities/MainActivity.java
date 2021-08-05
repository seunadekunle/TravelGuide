package com.example.travelguide.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.travelguide.R;
import com.example.travelguide.fragments.ComposeFragment;
import com.example.travelguide.fragments.MapsFragment;
import com.example.travelguide.fragments.ProfileFragment;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.parse.ParseUser;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SmoothBottomBar smoothBottomBar;
    private FragmentManager fragmentManager;
    private int tabFrameID;
    private String fragmentTAG;

    // different fragments
    private ComposeFragment composeFragment;
    private ProfileFragment profileFragment;
    private MapsFragment mapsFragment;
    private Fragment shownFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getSupportActionBar().hide();
        fragmentManager = getSupportFragmentManager();
        tabFrameID = R.id.tabFrame;

        // creates new instance of the different fragments
        shownFragment = new Fragment();
        composeFragment = new ComposeFragment();
        mapsFragment = new MapsFragment();
        profileFragment = ProfileFragment.newInstance(tabFrameID, ParseUser.getCurrentUser().getObjectId());

        shownFragment = mapsFragment;
        showFragment(shownFragment, MapsFragment.TAG);
        // sets up bottom navbar
        smoothBottomBar = findViewById(R.id.bottomBar);

//        smoothBottomBar.setSelected(true);
//        smoothBottomBar.setItemActiveIndex(0);

        // sets bottom bar state
        smoothBottomBar.setOnItemSelectedListener((OnItemSelectedListener) i -> {

            // if the map fragment is shown but the user hasn't selected a tab yet
            if (mapsFragment.isVisible() && shownFragment == null) {
                // remove the fragment
                getSupportFragmentManager().beginTransaction().remove(mapsFragment).commit();
                getSupportFragmentManager().popBackStack();
            }

            // hide the fragment
            getSupportFragmentManager().beginTransaction().hide(shownFragment).commit();

            switch (i) {
                case 1:
                    shownFragment = composeFragment;
                    fragmentTAG = ComposeFragment.TAG;
                    break;
                case 2:
                    shownFragment = profileFragment;
                    fragmentTAG = ProfileFragment.TAG;
                    break;
                default:
                    shownFragment = mapsFragment;
                    fragmentTAG = MapsFragment.TAG;
                    break;
            }

            // replaces the fragment container
            showFragment(shownFragment, fragmentTAG);
            return true;
        });

        smoothBottomBar.setSelected(true);
    }

    private void showFragment(Fragment shownFragment, String tag) {

        // shows map fragment initially
        HelperClass.addFragment(fragmentManager, tabFrameID, shownFragment, tag, false, false);
    }

    // show profile fragment
    public void showProfileFragment() {
        getSupportFragmentManager().beginTransaction().show(profileFragment).commit();
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment;

        // sets currentFragment based on what's shown
        if (shownFragment != null) {
            currentFragment = shownFragment;
        } else {
            currentFragment = mapsFragment;
        }

        if (currentFragment instanceof MapsFragment) {

            // maps fragment manager
            FragmentManager mapsFragmentManager = mapsFragment.getChildFragmentManager();
            Fragment mapModalFragment = mapsFragment.getModalFragment();

            if (HelperClass.emptyBackStack(mapsFragmentManager)) {
                super.onBackPressed();
                return;
            }

            // if the modal fragment is visible
            if (mapsFragment.getModalFragment() != null && mapModalFragment.isVisible()) {

                if (mapsFragment.getSheetBehavior().getState() == BottomSheetBehavior.STATE_EXPANDED) {

                    if (mapsFragmentManager.getBackStackEntryCount() > 1) {
                        mapsFragmentManager.popBackStack();
                    } else {
                        // resets the modal state
                        mapsFragment.setSheetState(BottomSheetBehavior.STATE_COLLAPSED);
                        mapsFragment.showModalIndicator();
                        mapsFragment.showOverlayBtns();
                    }
                    return;
                }
                if (mapsFragment.getSheetBehavior().getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    // hide modal view
                    mapsFragment.hideModalFragment();
                    return;
                }

            } else {
                // shows last fragment
                mapsFragmentManager.popBackStack();
                // is back stack empty set addGuide button to be visible and refresh page
                if (mapsFragmentManager.getBackStackEntryCount() == 1) {
                    // reload data
                    mapsFragment.getGuides(false);
                    mapsFragment.showOverlayBtns();
                }
            }
            Log.i(TAG, "Maps");
        } else if (currentFragment instanceof ComposeFragment) {

            super.onBackPressed();
            Log.i(TAG, "ComposeFragment");
        } else {

            // profile fragment manager
            FragmentManager profileFragmentManager = profileFragment.getChildFragmentManager();

            if (HelperClass.emptyBackStack(profileFragmentManager)) {
                super.onBackPressed();
            } else {
                // if you are returning to profile fragment from ChangeAvatarFragment
                profileFragmentManager.popBackStack();
            }

            Log.i(TAG, "Profile");
        }
    }
}