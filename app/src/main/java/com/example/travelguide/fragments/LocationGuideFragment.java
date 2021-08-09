package com.example.travelguide.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.TransitionInflater;

import com.example.travelguide.R;
import com.example.travelguide.adapters.GuidesAdapter;
import com.example.travelguide.adapters.TopLocationAdapter;
import com.example.travelguide.classes.Activity;
import com.example.travelguide.classes.Guide;
import com.example.travelguide.classes.Location;
import com.example.travelguide.classes.OnDoubleTapListener;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
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
    private SearchView svGuide;
    private LinearLayout locationLayout;
    protected int frameParam;

    private List<Guide> guideList;
    private TextView tvAddress;
    private ImageView ivExpandIndicator;
    private Boolean expandable;
    private Button followBtn;
    private RecyclerView rvRecommended;
    private View recommendedView;

    private static final String ARG_LOC = "location";
    private static final String ARG_FRAME = "frame_ID";
    private static final String ARG_MODAL = "in_modal";

    private Location parseLocation;

    // Mandatory empty constructor for the fragment manager
    public LocationGuideFragment() {
    }

    @SuppressWarnings("unused")
    public static LocationGuideFragment newInstance(Object location, int frameParam, boolean inModal) {
        LocationGuideFragment fragment = new LocationGuideFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_LOC, (Parcelable) location);
        args.putInt(ARG_FRAME, frameParam);
        args.putBoolean(ARG_MODAL, inModal);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // there are arguments to be gotten
        if (getArguments() != null) {
            // initializes local Parse variable
            parseLocation = (Location) getArguments().getParcelable(ARG_LOC);
            frameParam = getArguments().getInt(ARG_FRAME);

            // shows expand indicator
            expandable = getArguments().getBoolean(ARG_MODAL);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // sets entry and exit transition
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_up));
        setExitTransition(transitionInflater.inflateTransition(R.transition.slide_down));

        return inflater.inflate(R.layout.fragment_location_guide_list, container, false);
    }

    @SuppressLint({"ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        tvAddress = view.findViewById(R.id.tvAddress);
        ivExpandIndicator = view.findViewById(R.id.ivExpandIndicator);
        followBtn = view.findViewById(R.id.followBtn);
        svGuide = view.findViewById(R.id.svGuide);
        rvRecommended = view.findViewById(R.id.rvRecommended);
        recommendedView = view.findViewById(R.id.recommended);
        locationLayout = view.findViewById(R.id.locationFrame);

        context = view.getContext();

        setTitleText();

        guideList = new ArrayList<>();
        setupGuideList(view, context, view.findViewById(R.id.expandedImgView), view.findViewById(R.id.expandedImgViewBG), false);

        getRecommendedLocations();

        queryGuides();
        handleFollowBtn();

        setupSearchView();

        // show indicator if the fragment is expandable
        if (expandable) {
            changeIndicatorState(View.VISIBLE);
        } else {
            changeIndicatorState(View.INVISIBLE);
        }
    }

    private void getRecommendedLocations() {

        // passes in the parameters for the cloud function
        final HashMap<String, Double> trendingParams = new HashMap<>();

        trendingParams.put("locationLat", parseLocation.getCoord().latitude);
        trendingParams.put("locationLong", parseLocation.getCoord().longitude);

        // Calling the cloud code function to get trending locations
        ParseCloud.callFunctionInBackground("getTrendingLocations", trendingParams, new FunctionCallback<Object>() {
            @Override
            public void done(Object response, ParseException e) {

                if (e != null) {
                    Log.i(TAG, e.getMessage());
                    return;
                }

                if (response != null) {

                    // excludes the locations that is currently being seen
                    ArrayList<HashMap<Integer, String>> responseList = (ArrayList<HashMap<Integer, String>>) response;
                    ArrayList<String> locationIDs = new ArrayList<>();

                    for (int i = 0; i < responseList.size(); i++) {

                        if (!(responseList.get(i).containsValue(parseLocation.getObjectId()))) {
                            // add it to the arraylist of strings
                            locationIDs.add(responseList.get(i).get("id"));
                        }
                    }

                    // if there are surrounding locations
                    if (locationIDs.size() > 0) {

                        // shows recommended locations
                        recommendedView.setVisibility(View.VISIBLE);

                        // specify what type of data we want to query - Guide.class
                        ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
                        // include data referred by user key
                        query.include(Guide.getKeyAuthor());
                        // limit query to latest 20 items
                        query.setLimit(20);
                        // get posts that are specific to the location
                        query.whereContainsAll("objectId", locationIDs);
                        query.findInBackground((objects, e1) -> {

                            if (e1 == null) {
                                Log.i(TAG, String.valueOf(objects));

                                //set variables for recommended locations
                                rvRecommended.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                                rvRecommended.setAdapter(new TopLocationAdapter(requireContext(), (ArrayList<Location>) objects, 1, new TopLocationAdapter.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(Location location) {

                                        // zooms to location and dismisses fragment
                                        if (getParentFragment() != null) {

                                            ((MapsFragment) getParentFragment()).zoomToLocation(new LatLng(location.getCoord().latitude, location.getCoord().longitude));

                                            // hide bottom view
                                            ((MapsFragment) getParentFragment()).hideModalFragment();
                                            // shows modal view of location being selected
                                            ((MapsFragment) getParentFragment()).setModalLocationGuideFragment(LocationGuideFragment.newInstance(location, frameParam, true));
                                        }
                                    }
                                }));
                            }
                        });
                    }


                }

            }
        });
    }

    // setups the searchView
    public void setupSearchView() {

//        svGuide.onActionViewExpanded();
        svGuide.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // clears focus and hides keyboard
                svGuide.clearFocus();
                HelperClass.hideKeyboard(requireActivity());

                swipeContainer.setEnabled(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.isEmpty()) {
                    adapter.filter(newText);
                } else {
                    svGuide.clearFocus();
                    adapter.resetFilter();
                }

                swipeContainer.setEnabled(false);
                return false;
            }
        });

        svGuide.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {

                adapter.resetFilter();
                swipeContainer.setEnabled(true);
                return false;
            }
        });
    }

    public void handleFollowBtn() {
        // callback to follow a location
        FindCallback<ParseObject> isFollowedCallback = (objects, e) -> {
            if (e == null && objects.size() >= 1) {
                setFollowBtnState(true);
            }
        };

        // callback to unfollow a location
        FindCallback<ParseObject> unfollowCallback = (objects, e) -> {

            if (e == null && objects.size() >= 1) {
                for (ParseObject object : objects) {
                    try {
                        object.delete();
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                    object.saveInBackground();

                    // update the number of followers
                    parseLocation.setFollowers(parseLocation.getNumFollowers() - 1);
                    parseLocation.saveInBackground();
                }

                setFollowBtnState(false);
            }
        };

        // sets button state
        isLocationFollowed(parseLocation, isFollowedCallback);

        followBtn.setOnClickListener((v -> {

            // shows an alert dialog
            if (followBtn.isSelected()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

                // sets title and message
                builder.setTitle("Unfollow location");
                builder.setMessage("Unfollow " + tvAddress.getText());

                // Add the buttons
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked yes button
                        isLocationFollowed(parseLocation, unfollowCallback);
                    }
                });

                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                builder.create();
                builder.show();

            } else {
                // creates a follow row and updates button
                Activity followActivity = new Activity();

                followActivity.put(Activity.getKeyUserId(), ParseUser.getCurrentUser());
                followActivity.put(Activity.getKeyLocId(), parseLocation);
                followActivity.put(Activity.getKeyType(), "follow");
                followActivity.saveInBackground(e -> {
                    if (e == null) {

                        // update the number of followers
                        parseLocation.setFollowers(parseLocation.getNumFollowers() + 1);
                        parseLocation.saveInBackground();

                        setFollowBtnState(true);

                    } else {
                        Log.i(TAG, e.getMessage());
                    }
                });
            }
        }));
    }

    // changes button state based on boolean variable
    public void setFollowBtnState(boolean selected) {
        if (selected) {
            followBtn.setText(R.string.following);
            followBtn.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            followBtn.setText(R.string.follow);
            followBtn.setTextColor(Color.parseColor("#000000"));
        }

        followBtn.setSelected(selected);
    }


    public void isLocationFollowed(Location location, FindCallback<ParseObject> followCallback) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Activity");
        // only where the location exists
        query.whereExists(Activity.getKeyLocId());
        query.whereEqualTo(Activity.getKeyUserId(), ParseUser.getCurrentUser());
        query.whereEqualTo(Activity.getKeyLocId(), location);

        query.findInBackground(followCallback);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setTitleText() {

        // callback to set ui text
        OnSuccessListener<FetchPlaceResponse> textSuccess = fetchPlaceResponse -> tvAddress.setText(fetchPlaceResponse.getPlace().getName());

        // sets the title name based on place id
        if (parseLocation.getPlaceID().equals(HelperClass.defaultPlaceID)) {
            tvAddress.setText(HelperClass.getAddress(context, parseLocation.getCoord().latitude, parseLocation.getCoord().longitude));
        } else {
            HelperClass.fetchPlacesName(textSuccess, parseLocation.getPlaceID());
        }

        tvAddress.setOnTouchListener(new OnDoubleTapListener(context) {
            @Override
            public void onDoubleTap(MotionEvent e) {

                String queryString = String.format("google.navigation:q=%f,%f", parseLocation.getCoord().latitude, parseLocation.getCoord().longitude);
                Uri gmmIntentUri = Uri.parse(queryString);

                // creates google maps intent that will load directions for the location
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                startActivity(mapIntent);
                super.onDoubleTap(e);
            }
        });
    }


    protected void setupGuideList(@NotNull View view, Context context, ImageView expandedImgView, View expandedImgViewBG, boolean inProfile) {

        guideList = new ArrayList<>();
        rvGuides = view.findViewById(R.id.rvGuides);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        // Set the adapter of the recycler view
        adapter = new GuidesAdapter(guideList, context, expandedImgView, expandedImgViewBG, getActivity(), globalPlayer, inProfile
                , getParentFragmentManager(), frameParam);

        rvGuides.setAdapter(adapter);
        rvGuides.setLayoutManager(new LinearLayoutManager(context));

        // animation to ensure that like button state is preserved
        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        rvGuides.setItemAnimator(animator);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> fetchListAsync(0));

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
        query.setLimit(50);
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

    public void changeIndicatorState(int state) {
        
        // sets expand indicator visibility
        ivExpandIndicator.setVisibility(state);
    }
}