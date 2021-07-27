package com.example.travelguide.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.travelguide.classes.Location;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
    protected int frameParam;


    private List<Guide> guideList;
    private TextView tvAddress;
    private ImageView ivExpandIndicator;
    private String locationName;
    private Button followBtn;

    private static final String ARG_LOC = "location";
    private static final String ARG_FRAME = "frame_ID";

    private Location parseLocation;

    // Mandatory empty constructor for the fragment manager
    public LocationGuideFragment() {
    }

    @SuppressWarnings("unused")
    public static LocationGuideFragment newInstance(Object location, int frameParam) {
        LocationGuideFragment fragment = new LocationGuideFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_LOC, (Parcelable) location);
        args.putInt(ARG_FRAME, frameParam);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // there are arguments to be gotten
        if (getArguments() != null) {
            // initializes local Parse variable
            parseLocation = (Location) getArguments().getParcelable(ARG_LOC);
            frameParam = getArguments().getInt(ARG_FRAME);
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
        followBtn = view.findViewById(R.id.followBtn);

        context = view.getContext();

        setTitleText();

        setupGuideList(view, context, view.findViewById(R.id.expandedImgView), view.findViewById(R.id.expandedImgViewBG), false);
        ivExpandIndicator.setVisibility(View.INVISIBLE);

        queryGuides();

        followBtn.setOnClickListener((v -> {
            // creates a like row and updates Guide text
            com.example.travelguide.classes.Activity followActivity = new com.example.travelguide.classes.Activity();

            followActivity.put(com.example.travelguide.classes.Activity.getKeyUserId(), ParseUser.getCurrentUser());
            followActivity.put(com.example.travelguide.classes.Activity.getKeyLocId(), parseLocation);
            followActivity.put(com.example.travelguide.classes.Activity.getKeyType(), "follow");
//            followActivity.saveInBackground(e -> {
//
//            });
        }));

    }

    public void setTitleText() {
        OnSuccessListener<FetchPlaceResponse> textSuccess = fetchPlaceResponse -> tvAddress.setText(fetchPlaceResponse.getPlace().getName());

        // sets the title name based on place id
        if (parseLocation.getPlaceID().equals(HelperClass.defaultPlaceID))
            tvAddress.setText(HelperClass.getAddress(context, parseLocation.getCoord().latitude, parseLocation.getCoord().longitude));
        else
            fetchPlacesName(textSuccess);
    }

    private void fetchPlacesName(OnSuccessListener<FetchPlaceResponse> responseListener) {
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(parseLocation.getPlaceID(), HelperClass.placesFields);
        HelperClass.getPlacesClient().fetchPlace(request)
                .addOnSuccessListener(responseListener);

    }

    protected void setupGuideList(@NotNull View view, Context context, ImageView expandedImgView, View expandedImgViewBG, boolean inProfile) {

        guideList = new ArrayList<>();
        rvGuides = view.findViewById(R.id.rvGuides);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        // Set the adapter of the recycler view
        adapter = new GuidesAdapter(guideList, context, expandedImgView, expandedImgViewBG, getActivity(), globalPlayer, inProfile
                , requireActivity().getSupportFragmentManager(), frameParam);

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
        query.whereEqualTo("locationID", parseLocation);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");

        // start an asynchronous call for posts
        query.findInBackground((guides, e) -> {
            // check for errors
            if (e != null) {

                Log.e(TAG, "Issue with getting guides", e);

                // show that the list is empty
                if (e.getCode() == ParseException.OTHER_CAUSE)
                    showEmptyListText();

                return;
            }

            // clears the adapter
            adapter.clear();
            // save received posts to list and notify adapter of new data
            adapter.addAll(guides);
            adapter.notifyDataSetChanged();

            showEmptyListText();
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