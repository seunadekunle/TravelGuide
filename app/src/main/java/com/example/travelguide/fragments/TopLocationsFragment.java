package com.example.travelguide.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelguide.activities.MapsActivity;
import com.example.travelguide.adapters.TopLocationAdapter;
import com.example.travelguide.classes.Location;
import com.example.travelguide.databinding.FragmentTopLocationsBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     TopLocationsFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class TopLocationsFragment extends BottomSheetDialogFragment {

    private static final String ARG_LOCATIONS = "locations";
    private FragmentTopLocationsBinding binding;
    private Location[] topLocations;
    private RecyclerView recyclerView;

    public static TopLocationsFragment newInstance(Location[] topLocations) {

        final TopLocationsFragment fragment = new TopLocationsFragment();
        final Bundle args = new Bundle();

        args.putParcelableArray(ARG_LOCATIONS, topLocations);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        // there are arguments to be gotten
        if (getArguments() != null) {
            topLocations = (Location[]) getArguments().getParcelableArray(ARG_LOCATIONS);
        }

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = com.example.travelguide.databinding.FragmentTopLocationsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recyclerView = binding.rvTop;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TopLocationAdapter(requireContext(), Arrays.asList(topLocations.clone()), new TopLocationAdapter.OnItemClickListener() {

            // zooms to the location
            @Override
            public void onItemClick(Location location) {
                ((MapsActivity) requireActivity()).zoomToLocation(new LatLng(location.getCoord().latitude, location.getCoord().longitude));

                // shows bottom getFragmentsFrameId
                ((MapsActivity) requireActivity()).setModalLocationGuideFragment(
                        (LocationGuideFragment.newInstance(location, ((MapsActivity) requireActivity()).getFragmentsFrameId(), true)));
            }
        }));

        // sets button to dismiss the button
        binding.dismiss.setOnClickListener((v -> {
            ((MapsActivity) requireActivity()).hideModalFragment();
            dismiss();
        }));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}