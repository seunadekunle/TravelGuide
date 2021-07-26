package com.example.travelguide.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelguide.R;
import com.example.travelguide.classes.Activity;
import com.example.travelguide.classes.Guide;
import com.example.travelguide.helpers.HelperClass;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileGuideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileGuideFragment extends LocationGuideFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PAGE_TYPE = "type";
    private static final String ARG_USER_ID = "userID";

    private String type = "";
    private String userID = "";

    private List<Guide> guideList;
    private ImageView imageView;
    private View expandedView;
    private ParseUser parseUser;


    public ProfileGuideFragment() {
        // Required empty public constructor
    }

    /**
     * factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ProfileGuideFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileGuideFragment newInstance(String param1, String param2) {
        ProfileGuideFragment fragment = new ProfileGuideFragment();
        Bundle args = new Bundle();

        args.putString(ARG_PAGE_TYPE, param1);
        args.putString(ARG_USER_ID, param2);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PAGE_TYPE);
            userID = getArguments().getString(ARG_USER_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_guide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        guideList = new ArrayList<>();
        setParseUser(userID, view);
    }

    // returns user based on ID
    public void setParseUser(String userID, View view) {

        if (userID != null) {
            if (ParseUser.getCurrentUser().getObjectId().equals(userID)) {
                parseUser = ParseUser.getCurrentUser();


            } else {
                HelperClass.fetchUser(userID, new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser object, ParseException e) {
                        parseUser = object;
                    }
                });
            }

            // set up guide list
            setupGuideList(view, view.getContext(), imageView, expandedView, true);
            queryGuides();
        }
    }

    @Override
    protected void queryGuides() {

        if (type.equals(HelperClass.profileTabTitles[0])) {
            queryCreatedGuides();
        } else if (type.equals(HelperClass.profileTabTitles[1])) {
            queryLikedGuides();
        }
    }

    private void queryCreatedGuides() {
        // specify what type of data we want to query - Guide.class
        ParseQuery<Guide> query = ParseQuery.getQuery(Guide.class);
        // include data referred by user key
        query.include(Guide.getKeyAuthor());
        // limit query to latest 20 items
        query.setLimit(20);
        //  where the author is the logged in user
        query.whereEqualTo("author", parseUser);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");

        // start an asynchronous call for posts
        query.findInBackground((guides, e) -> {
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
        });
    }


    // gets guides that user liked
    private void queryLikedGuides() {

        // specify what type of data we want to query - Guide.class
        ParseQuery<Activity> query = ParseQuery.getQuery(Activity.class);

        //  where the author is the logged in user
        query.whereEqualTo(Activity.getKeyUserId(), parseUser);
        query.include(Activity.getKeyGuideId());

        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");

        // start an asynchronous call for posts
        query.findInBackground((activities, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting guides", e);
                return;
            }

            guideList = new ArrayList<>();

            // get guides that have been liked
            for (Activity activity : activities) {
                guideList.add(activity.getGuide());
            }

            // clears the adapter
            adapter.clear();
            // save received posts to list and notify adapter of new data
            adapter.addAll(guideList);

            adapter.notifyDataSetChanged();

            Log.i(TAG, String.valueOf(adapter.getItemCount()));

            showEmptyListText();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        queryGuides();
    }

    public void setExpandedElements(ImageView expandedImgViewID, View expandedImgViewBgID) {
        imageView = expandedImgViewID;
        expandedView = expandedImgViewBgID;
    }
}