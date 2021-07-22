package com.example.travelguide.classes;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Activity")
public class Activity extends ParseObject {

    private static final String TAG = "Activity";

    private static final String KEY_CREATION_DATE = "createdAt";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_GUIDE_ID = "guideID";

    public Guide getGuide() {
        return (Guide) getParseObject(KEY_GUIDE_ID);
    }

    public static String getTAG() {
        return TAG;
    }

    public static String getKeyCreationDate() {
        return KEY_CREATION_DATE;
    }

    public static String getKeyUserId() {
        return KEY_USER_ID;
    }

    public static String getKeyGuideId() {
        return KEY_GUIDE_ID;
    }
}
