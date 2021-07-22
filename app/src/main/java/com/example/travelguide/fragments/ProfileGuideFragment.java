package com.example.travelguide.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelguide.R;
import com.example.travelguide.classes.Guide;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileGuideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileGuideFragment extends LocationGuideFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SimpleExoPlayer globalPlayer;


    public ProfileGuideFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileGuideFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileGuideFragment newInstance(String param1, String param2) {
        ProfileGuideFragment fragment = new ProfileGuideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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

        // set up the guide list
        setupGuideList(view, view.getContext(), globalPlayer);
        queryGuides();
    }

    @Override
    protected void queryGuides() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Guide> query = ParseQuery.getQuery(Guide.class);
        // include data referred by user key
        query.include(Guide.getKeyAuthor());
        // limit query to latest 20 items
        query.setLimit(20);
        //  where the author is the logged in user
        query.whereEqualTo("author", ParseUser.getCurrentUser());
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
}