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

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// helper functions used multiple times in the project
public class HelperClass {

    private static String TAG = "HelperClass";

    public static int picRadius = 25;
    public static int resizedImgDimen = 650;
    public static int detailImgDimen = 475;

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

    // checks if the permissions passsd have been granted
    public static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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


}
