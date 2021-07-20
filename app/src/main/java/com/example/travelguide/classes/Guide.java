package com.example.travelguide.classes;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Guide")
public class Guide extends ParseObject {

    private static final String KEY_CREATION_DATE = "createdAt";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TEXT = "text";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_AUDIO = "audio";
    private static final String KEY_VIDEO = "video";
    private static final String KEY_LIKES = "likes";

    public void setAuthor(ParseUser user) {
        put(KEY_AUTHOR, user);
    }

    public void setText(String newText) {
        put(KEY_TEXT, newText);
    }

    public void setLocation(LatLng newLocation) {
        put(KEY_LOCATION, new ParseGeoPoint(newLocation.latitude, newLocation.longitude));
    }

    public void setPhoto(ParseFile newPhoto) {
        put(KEY_PHOTO, newPhoto);
    }

    public void setVideo(ParseFile newVideo) {
        put(KEY_VIDEO, newVideo);
    }

    public void setAudio(ParseFile newAudio) {
        put(KEY_AUDIO, newAudio);
    }

    public LatLng getLocation() {
        return (new LatLng(getParseGeoPoint(KEY_LOCATION).getLatitude(), getParseGeoPoint(KEY_LOCATION).getLongitude()));
    }

    public String getText() {
        return getString(KEY_TEXT);
    }

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }

    public ParseFile getPhoto() {
        return getParseFile(KEY_PHOTO);
    }

    public ParseFile getVideo() {
        return getParseFile(KEY_VIDEO);
    }

    public ParseFile getAudio() { return getParseFile(KEY_AUDIO); }

    public static String getKeyCreationDate() {
        return KEY_CREATION_DATE;
    }

    public static String getKeyAuthor() {
        return KEY_AUTHOR;
    }

    public static String getKeyText() {
        return KEY_TEXT;
    }

    public static String getKeyLocation() {
        return KEY_LOCATION;
    }

    public static String getKeyPhoto() {
        return KEY_PHOTO;
    }

    public static String getKeyAudio() {
        return KEY_AUDIO;
    }

    public static String getKeyVideo() {
        return KEY_VIDEO;
    }

    public static String getKeyLikes() {
        return KEY_LIKES;
    }

    // returns the timestamp as a string
    public String getTimeStamp() {
        return calculateTimeAgo(this.getCreatedAt());
    }

    // converts date object to String timestamp
    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m ago";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h ago";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d ago";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }
}
