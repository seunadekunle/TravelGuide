package com.example.travelguide.classes;

import android.app.Application;
import android.util.Log;

import com.example.travelguide.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

public class ParseApplication extends Application {

    public static final String TAG = ParseApplication.class.getSimpleName();

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register parse models
        ParseObject.registerSubclass(Guide.class);
        ParseObject.registerSubclass(Activity.class);
        ParseObject.registerSubclass(Location.class);

        // creates Parse Client
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("LehUdwXhP2IpTN6Tnu7gXIayECJALrtOKyEao0N5")
                .clientKey("qjnlfP8YLU5Ck78BS5juNBr4hWa5YjdbYAyxSwML")
                .server("https://parseapi.back4app.com")
                .build()
        );

        // creates installation object
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", getResources().getString(R.string.firebase_id));
        // associates a user to this device
        installation.put("userID", ParseUser.getCurrentUser());
        // Save the updated installation object
        installation.saveInBackground(e -> Log.i(TAG, "Installation object saved " + ((e != null) ? "failed" : "successfully")));
    }
}
