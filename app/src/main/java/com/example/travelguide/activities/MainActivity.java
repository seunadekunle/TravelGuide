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
import com.parse.ParseUser;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SmoothBottomBar smoothBottomBar;
    private FragmentManager fragmentManager;
    private int tabFrameID;

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

        showFragment(mapsFragment);
        // sets up bottom bar
        smoothBottomBar = findViewById(R.id.bottomBar);

        // sets bottom bar state
        smoothBottomBar.setOnItemSelectedListener((OnItemSelectedListener) i -> {

            // if the map fragment is shown but the user hasn't selected a tab yet
            if (mapsFragment.isVisible() && shownFragment == null) {
                // remove the fragment
                getSupportFragmentManager().beginTransaction().hide(mapsFragment).commit();
            }

            // hide the fragment
            getSupportFragmentManager().beginTransaction().hide(shownFragment).commit();

            switch (i) {
                case 1:
                    shownFragment = composeFragment;
                    break;
                case 2:
                    shownFragment = profileFragment;
                    break;
                default:

                    shownFragment = mapsFragment;
                    break;
            }

            // replaces the fragment container
            showFragment(shownFragment);
            return true;
        });

        smoothBottomBar.setSelected(true);
    }

    private void showFragment(Fragment shownFragment) {
        // shows map fragment initially
        HelperClass.addFragment(fragmentManager, tabFrameID, shownFragment, shownFragment.getTag());
    }

    @Override
    public void onBackPressed() {

        Log.i(TAG, String.valueOf(mapsFragment.getChildFragmentManager().getBackStackEntryCount()));

        if (mapsFragment.isVisible()) {

            FragmentManager mapsFragmentManager = mapsFragment.getChildFragmentManager();
            Fragment mapModalFragment = mapsFragment.getModalFragment();

            int index = mapsFragmentManager.getBackStackEntryCount() - 1;

            if (index >= 0) {

                for (int i = 0; i <= index; i++) {
                    FragmentManager.BackStackEntry backEntry = mapsFragmentManager.getBackStackEntryAt(index);
                    String tag = backEntry.getName();

                    Log.i(TAG, tag);
                }
            }


            // if the modal fragment is visible
            if (mapsFragment.getModalFragment() != null && mapModalFragment.isVisible()) {

                // resets the modal state
                mapsFragment.resetSheetState();


                // hide modal view
                mapsFragment.hideModalFragment();
                mapsFragment.showOverlayBtns();

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
        } else if (composeFragment.isVisible()) {
            Log.i(TAG, "Compose");
        } else {
            Log.i(TAG, "Profile");
        }

//        super.onBackPressed();
    }
}