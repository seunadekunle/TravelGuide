package com.example.travelguide.classes;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {


    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("LehUdwXhP2IpTN6Tnu7gXIayECJALrtOKyEao0N5")
                .clientKey("qjnlfP8YLU5Ck78BS5juNBr4hWa5YjdbYAyxSwML")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
