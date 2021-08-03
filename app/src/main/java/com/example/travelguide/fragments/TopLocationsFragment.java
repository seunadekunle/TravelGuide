package com.example.travelguide.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.example.travelguide.R;
import com.example.travelguide.adapters.TopLocationAdapter;
import com.example.travelguide.classes.Location;
import com.example.travelguide.databinding.FragmentTopLocationsBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.parse.ParseUser;

import java.util.Arrays;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     TopLocationsFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class TopLocationsFragment extends BottomSheetDialogFragment {

    private static final String ARG_LOCATIONS = "locations";
    private static final String TAG = "TopLocationsFragment";


    private FragmentTopLocationsBinding binding;
    private Location[] topLocations;
    private RecyclerView recyclerView;
    private TextView tvMessage;

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

        // get top location array
        if (getArguments() != null) {
            topLocations = (Location[]) getArguments().getParcelableArray(ARG_LOCATIONS);
        }

        super.onCreate(savedInstanceState);

        // sets entry transition
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_up));
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
        tvMessage = binding.tvMessage;

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TopLocationAdapter(requireContext(), Arrays.asList(topLocations.clone()), 0, new TopLocationAdapter.OnItemClickListener() {

            // zooms to the location
            @Override
            public void onItemClick(Location location) {

                if (getParentFragment() != null) {

                    dismiss();
                    // zoom to location
                    ((MapsFragment) getParentFragment()).zoomToLocation(new LatLng(location.getCoord().latitude, location.getCoord().longitude));
                    // shows bottom getFragmentsFrameId
                    ((MapsFragment) getParentFragment()).setModalLocationGuideFragment(
                            (LocationGuideFragment.newInstance(location, ((MapsFragment) getParentFragment()).getFragmentsFrameId(), true)));
                }
            }

        }));

        // sets button to dismiss the button
        binding.dismiss.setOnClickListener((v -> {
            ((MapsFragment) getParentFragment()).hideModalFragment();
            dismiss();
        }));

        // sets message text
        tvMessage.setText(String.format("Hey %s", ParseUser.getCurrentUser().getUsername()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}