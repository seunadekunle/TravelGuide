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

        shownFragment = mapsFragment;
        showFragment(mapsFragment);

        // sets up bottom bar
        smoothBottomBar = findViewById(R.id.bottomBar);
        smoothBottomBar.setSelected(true);

        // sets bottom bar state
        smoothBottomBar.setOnItemSelectedListener((OnItemSelectedListener) i -> {

            switch (i) {
                case 1:
                    fragmentManager.beginTransaction().hide(shownFragment);
                    shownFragment = composeFragment;
                    break;
                case 2:
                    fragmentManager.beginTransaction().hide(shownFragment);
                    shownFragment = profileFragment;
                    break;
                default:
                    fragmentManager.beginTransaction().hide(shownFragment);
                    shownFragment = mapsFragment;
                    break;
            }

            // replaces the fragment container
            showFragment(shownFragment);
            return true;
        });

//        smoothBottomBar.item
    }

    private void showFragment(Fragment mapsFragment) {
        // shows map fragment initially
        HelperClass.replaceFragment(fragmentManager, tabFrameID, mapsFragment, "TAG");
    }

    @Override
    public void onBackPressed() {

        if (mapsFragment.isVisible()) {
            Log.i(TAG, "Maps");
        } else if (composeFragment.isVisible()) {
            Log.i(TAG, "Compose");
        } else {
            Log.i(TAG, "Profile");
        }

//        super.onBackPressed();
    }
}