package com.example.travelguide.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.travelguide.R;
import com.example.travelguide.adapters.GuidesAdapter;
import com.example.travelguide.classes.Guide;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class LocationGuideFragment extends Fragment {

    public static final String TAG = "LocationGuideFragment";

    protected Context context;
    protected RecyclerView rvGuides;
    protected GuidesAdapter adapter;
    protected ProgressBar pbLoading;
    protected SwipeRefreshLayout swipeContainer;
    protected SimpleExoPlayer globalPlayer;
    protected TextView tvEmptyList;

    private List<Guide> guideList;
    private TextView tvAddress;
    private ImageView ivExpandIndicator;

    private static final String ARG_LAT = "lat";
    private static final String ARG_LONG = "long";

    private ParseGeoPoint parseLocation;

    // Mandatory empty constructor for the fragment manager
    public LocationGuideFragment() {
    }

    @SuppressWarnings("unused")
    public static LocationGuideFragment newInstance(Double latCoord, Double longCoord) {
        LocationGuideFragment fragment = new LocationGuideFragment();
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
        return inflater.inflate(R.layout.fragment_location_guide_list, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        tvAddress = view.findViewById(R.id.tvAddress);
        ivExpandIndicator = view.findViewById(R.id.ivExpandIndicator);

        context = view.getContext();

        setupGuideList(view, context, view.findViewById(R.id.expandedImgView), view.findViewById(R.id.expandedImgViewBG));

        // sets text of header
        tvAddress.setText(HelperClass.getAddress(context, parseLocation.getLatitude(), parseLocation.getLongitude()));
        ivExpandIndicator.setVisibility(View.INVISIBLE);

        queryGuides();
    }

    protected void setupGuideList(@NotNull View view, Context context, ImageView expandedImgView, View expandedImgViewBG) {

        guideList = new ArrayList<>();
        rvGuides = view.findViewById(R.id.rvGuides);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        // Set the adapter of the recycler view
        adapter = new GuidesAdapter(guideList, context, expandedImgView, expandedImgViewBG, getActivity(), globalPlayer);
        rvGuides.setAdapter(adapter);
        rvGuides.setLayoutManager(new LinearLayoutManager(context));

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchListAsync(0);
            }
        });

        pbLoading.setVisibility(View.VISIBLE);
        tvEmptyList.setVisibility(View.INVISIBLE);
    }

    protected void fetchListAsync(int i) {
        queryGuides();
        // sets refreshing state to false
        swipeContainer.setRefreshing(false);
    }

    // get list of guides from post server
    protected void queryGuides() {

        // specify what type of data we want to query - Guide.class
        ParseQuery<Guide> query = ParseQuery.getQuery(Guide.class);
        // include data referred by user key
        query.include(Guide.getKeyAuthor());
        // limit query to latest 20 items
        query.setLimit(20);
        // get posts that are specific to the location
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
                adapter.addAll(guides);
                adapter.notifyDataSetChanged();

                showEmptyListText();
            }
        });
    }

    // shows empty guide text and removes progress bar
    protected void showEmptyListText() {

        if (adapter.getItemCount() == 0)
            tvEmptyList.setVisibility(View.VISIBLE);
        else
            tvEmptyList.setVisibility(View.INVISIBLE);

        pbLoading.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {

        // release the media player for the list
        if (globalPlayer != null)
            globalPlayer.release();

        super.onDestroy();
    }


    public void setIndicatorOpacity(int opacity) {
        ivExpandIndicator.setImageAlpha(opacity);
    }

    public void makeIndicatorVisible() {
        ivExpandIndicator.setVisibility(View.VISIBLE);
    }
}