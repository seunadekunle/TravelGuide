package com.example.travelguide.classes;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Activity")
public class Activity extends ParseObject {

    private static final String TAG = Activity.class.getSimpleName();

    private static final String KEY_CREATION_DATE = "createdAt";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_GUIDE_ID = "guideID";
    private static final String KEY_LOC_ID = "locationID";
    private static final String KEY_TYPE = "type";


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

    public static String getKeyLocId() {
        return KEY_LOC_ID;
    }

    public static String getKeyType() {
        return KEY_TYPE;
    }
}
