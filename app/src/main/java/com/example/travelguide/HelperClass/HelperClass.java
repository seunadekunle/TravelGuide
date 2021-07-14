package com.example.travelguide.HelperClass;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// helper functions used multiple times in the project
public class HelperClass {

    private static String TAG = "HelperClass";

    // gets location info from coordinates
    public static String getAddress(Context context, Double latitude, Double longitude) {

        List<Address> likelyNames = new ArrayList<>();
        Geocoder geocoder = new Geocoder(context);

        try {
            likelyNames = geocoder.getFromLocation(latitude, longitude, 1);
            Log.i(TAG, String.valueOf(likelyNames));
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
    public static void displaySnackBarWithBottomMargin(Snackbar snackbar, int sideMargin, int marginBottom) {
        final View snackBarView = snackbar.getView();
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackBarView.getLayoutParams();

        params.setMargins(params.leftMargin + sideMargin, params.topMargin, params.rightMargin + sideMargin, params.bottomMargin + marginBottom);

        snackBarView.setLayoutParams(params);
        snackbar.show();
    }
}
