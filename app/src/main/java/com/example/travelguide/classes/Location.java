package com.example.travelguide.classes;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("Location")
public class Location extends ParseObject {

    private static final String TAG = Location.class.getSimpleName();

    private static final String KEY_CREATION_DATE = "createdAt";
    private static final String KEY_COORD = "coordinates";
    private static final String KEY_PLACE_ID = "placeID";
    private static final String KEY_FOLLOWERS = "followers";


    public void setCoord(double latitude, double longitude) {
        put(KEY_COORD, new ParseGeoPoint(latitude, longitude));
    }

    public LatLng getCoord() {

        ParseGeoPoint coord = getParseGeoPoint(KEY_COORD);

        double latitude = 0;
        if (coord != null) {
            latitude = coord.getLatitude();
        }
        double longitude = coord.getLongitude();

        return new LatLng(latitude, longitude);
    }

    public String getPlaceID (){
        return getString(KEY_PLACE_ID);
    }

    public void setPlaceId(String id) {
        put(KEY_PLACE_ID, id);
    }

    public void setFollowers(int followers) {
        put(KEY_FOLLOWERS, followers);
    }

    public static String getKeyCreationDate() {
        return KEY_CREATION_DATE;
    }

    public static String getKeyCoord() {
        return KEY_COORD;
    }

    public static String getKeyPlaceId() {
        return KEY_PLACE_ID;
    }

    public static String getKeyFollowers() {
        return KEY_FOLLOWERS;
    }
}
