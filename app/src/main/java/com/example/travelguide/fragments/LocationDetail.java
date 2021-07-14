package com.example.travelguide.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelguide.HelperClass.HelperClass;
import com.example.travelguide.R;
import com.example.travelguide.adapters.GuidesAdapter;
import com.example.travelguide.classes.Guide;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class LocationDetail extends Fragment {

    public static final String TAG = "LocationDetail";

    private Context context;
    private RecyclerView rvGuides;
    private List<Guide> guideList;
    private GuidesAdapter adapter;
    private TextView tvAddress;
    private ProgressBar pbLoading;

    private static final String ARG_LAT = "lat";
    private static final String ARG_LONG = "long";

    private ParseGeoPoint parseLocation;

    // Mandatory empty constructor for the fragment manager
    public LocationDetail() {
    }

    @SuppressWarnings("unused")
    public static LocationDetail newInstance(Double latCoord, Double longCoord) {
        LocationDetail fragment = new LocationDetail();
        Bundle args = new Bundle();

        args.putDouble(ARG_LAT, latCoord);
        args.putDouble(ARG_LONG, longCoord);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // there are arguments to be gotten
        if (getArguments() != null) {
            // initializes local Parse variable
            parseLocation = new ParseGeoPoint(getArguments().getDouble(ARG_LAT), getArguments().getDouble(ARG_LONG));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_detail_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        guideList = new ArrayList<>();
        rvGuides = view.findViewById(R.id.rvGuides);
        tvAddress = view.findViewById(R.id.tvAddress);
        pbLoading = view.findViewById(R.id.pbLoading);

        // Set the adapter of the recycler view
        adapter = new GuidesAdapter(guideList, context);
        rvGuides.setAdapter(adapter);
        rvGuides.setLayoutManager(new LinearLayoutManager(context));

        // sets text of header
        tvAddress.setText(HelperClass.getAddress(context, parseLocation.getLatitude(), parseLocation.getLongitude()));

        pbLoading.setVisibility(View.VISIBLE);
        queryGuides();
    }


    // get list of guides from post server
    protected void queryGuides() {

        // specify what type of data we want to query - Post.class
        ParseQuery<Guide> query = ParseQuery.getQuery(Guide.class);
        // include data referred by user key
        query.include(Guide.getKeyAuthor());
        // limit query to latest 20 items
        query.setLimit(20);
        // get posts that were created by the user
        query.whereEqualTo(Guide.getKeyLocation(), parseLocation);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");

        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Guide>() {
            @Override
            public void done(List<Guide> guides, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting guides", e);
                    return;
                }

                // clears the adapter
                adapter.clear();
                // save received posts to list and notify adapter of new data
                guideList.addAll(guides);
                adapter.notifyDataSetChanged();

                pbLoading.setVisibility(View.INVISIBLE);
            }
        });

    }
}