package com.example.travelguide.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.travelguide.R;
import com.example.travelguide.adapters.GuidesAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.parse.Parse.getApplicationContext;

// helper functions used multiple times in the project
public class HelperClass {

    private static final String TAG = "HelperClass";
    private static PlacesClient placesClient;

    public static int picRadius = 25;
    public static int resizedImgDimen = 650;
    public static int detailImgDimen = 475;
    public static String[] profileTabTitles = {"Guides", "Liked"};

    // Set the fields to specify which types of place data to return
    // for Google places API
    public static List<Place.Field> placesFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);

    // gets location info from coordinates
    public static String getAddress(Context context, Double latitude, Double longitude) {

        List<Address> likelyNames = new ArrayList<>();
        Geocoder geocoder = new Geocoder(context);

        try {
            likelyNames = geocoder.getFromLocation(latitude, longitude, 1);
            return likelyNames.get(0).getAddressLine(0);

        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
        return "";
    }

    /*
     * displays snackbar with margin
     * gotten from https://stackoverflow.com/questions/36588881/snackbar-behind-navigation-bar
     */
    public static void displaySnackBarWithBottomMargin(Snackbar snackbar, int marginBottom, Context context) {
        final View snackBarView = snackbar.getView();

        snackBarView.setTranslationY(-(DeviceDimenHelper.convertDpToPixel(marginBottom, context)));
        snackbar.show();
    }


    // Verifies that permissions has been granted
    public static void verifyPermissions(Activity activity) {

        // The request code used in ActivityCompat.requestPermissions()
        // and returned in the Activity's onRequestPermissionsResult()
        int PERMISSION_ALL = 1;

        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
        };

        ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
    }

    // completes fragment transaction
    public static void finishTransaction(FragmentTransaction ft, String name, Fragment fragment) {

        // add transaction to backstack
        ft.addToBackStack(name);
        // show fragment
        ft.show(fragment);

        // Complete the changes added above
        ft.commit();
    }

    // if the fragment manager stack is empty
    public static boolean emptyBackStack(FragmentManager fragmentManager) {
        return fragmentManager.getBackStackEntryCount() == 0;
    }

    // changes the ui state of button
    public static void toggleButtonState(ImageButton button) {
        button.setSelected(!button.isSelected());
    }

    // Initialize places client sdk
    public static void initPlacesSDK(Context context) {
        // Initialize Places SDK
        Places.initialize(context.getApplicationContext(), context.getResources().getString(R.string.google_maps_key));
        // Create a new PlacesClient instance
        placesClient = Places.createClient(context);
    }

    public static PlacesClient getPlacesClient() {
        return placesClient;
    }

    // loads profile image
    public static void loadProfileImage(Context context, ParseFile profileImg, int width, int height, ImageView imageView) {
        Glide.with(context)
                .load(profileImg.getUrl()).fitCenter().transform(new CircleCrop())
                .override(width, height).into(imageView);
    }

    // loads profile image for image button
    public static void loadProfileImage(Context context, ParseFile profileImg, int width, int height, ImageButton imageButton) {
        Glide.with(context)
                .load(profileImg.getUrl()).fitCenter().transform((new CircleCrop()))
                .override(width, height).into(imageButton);
    }
}
