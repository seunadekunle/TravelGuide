package com.example.travelguide.classes;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {


    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("BCrUQVkk80pCdeImSXoKXL5ZCtyyEZwbN7mAb11f")
                .clientKey("rWFPEbTs7UzkaVsIXnQ4qmmr9oWqwXfiiJehtIZu")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
