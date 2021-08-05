package com.example.travelguide.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/*
 * https://gist.github.com/nesquena/318b6930aac3a56f96a4
 * */
public class DeviceDimenHelper {

    // DeviceDimensionsHelper.getDisplayWidth(context) => (display width in pixels)
    public static int getDisplayWidthPixels(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    // DeviceDimensionsHelper.getDisplayHeight(context) => (display height in pixels)
    public static int getDisplayHeightPixels(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    // DeviceDimensionsHelper.getDisplayWidth(context) => (display width in DP)
    public static float getDisplayWidthDP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return convertPixelsToDp(displayMetrics.widthPixels, context);
    }

    // DeviceDimensionsHelper.getDisplayHeight(context) => (display height in DP)
    public static float getDisplayHeightDP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return convertPixelsToDp(displayMetrics.heightPixels, context);
    }

    // DeviceDimensionsHelper.convertDpToPixel(25f, context) => (25dp converted to pixels)
    public static float convertDpToPixel(float dp, Context context) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    // DeviceDimensionsHelper.convertPixelsToDp(25f, context) => (25px converted to dp)
    public static float convertPixelsToDp(float px, Context context) {
        Resources r = context.getResources();
        DisplayMetrics metrics = r.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }
}
