package com.example.travelguide.fragments;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelguide.R;
import com.example.travelguide.adapters.SearchListAdapter;
import com.example.travelguide.helpers.DeviceDimenHelper;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsFragment extends Fragment {


    public static final String TAG = "MapsFragment";
    private int fragmentsFrameId;
    private int modalFrameId;

    // Ui elements
    private GoogleMap map;
    private FragmentManager fragmentManager;
    private ProgressBar pbMaps;
    private SearchView searchView;
    private RecyclerView rvSearchList;
    private View frameLayout;
    private androidx.fragment.app.FragmentContainerView mapContainer;
    private FloatingActionButton myLocationBtn;

    // search ui elements
    private SearchListAdapter adapter;
    private List<AutocompletePrediction> predictions;
    private BottomSheetBehavior<View> sheetBehavior;

    // different fragments
    private LocationGuideFragment locationGuideFragment;
    private LocationGuideFragment modalLocationGuideFragment;
    private TopLocationsFragment topLocationsFragment;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // top locations data structures
    private List<HashMap<Integer, String>> topLocations;
    private com.example.travelguide.classes.Location[] topLocationObjects;


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    private LatLng currentLocation;

    // variables for the window height and width
    private int height = 0;
    private int width = 0;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

            try {
                // Customise the styling of the base map using a JSON object
                boolean success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                requireContext(), R.raw.style_json));

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style. Error: ", e);
            }

            map.getUiSettings().setMapToolbarEnabled(false);
            map.getUiSettings().setScrollGesturesEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);

            // sets padding to change position of map controls
            map.setPadding(0, (int) (height / 1.25), 0, 0);

            // Prompt the user for permission.
            getLocationPermission();
            getLocationsandGuides();

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();
            // Get the current location of the device and set the position of the map.
            getDeviceLocation();

            map.setOnMarkerClickListener(marker -> {

                locationGuideFragment = LocationGuideFragment.newInstance(marker.getTag(), fragmentsFrameId, false);

                // Begin the transaction
                FragmentTransaction ft = fragmentManager.beginTransaction();
                // add fragment to container
                ft.replace(fragmentsFrameId, locationGuideFragment);

                // complete the transaction
                HelperClass.finishTransaction(ft, LocationGuideFragment.TAG, (Fragment) locationGuideFragment, true);
                hideOverlayBtns();

                return true;
            });
        }
    };

    // gets top location and guides
    public void getLocationsandGuides() {

        // passes in the parameters for the cloud function
        final HashMap<String, String> trendingParams = new HashMap<>();
        // Calling the cloud code function to get trending locations
        ParseCloud.callFunctionInBackground("getTrendingLocations", trendingParams, new FunctionCallback<Object>() {
            @Override
            public void done(Object response, ParseException e) {

                if (e != null) {
                    Log.i(TAG, e.getMessage());
                    return;
                }

                if (response != null) {
                    Log.i(TAG, String.valueOf(response));

                    topLocations = (List<HashMap<Integer, String>>) response;
                    topLocationObjects = new com.example.travelguide.classes.Location[topLocations.size()];

                    // get list of current guides
                    getGuides(true);
                }

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            // Retrieve location and camera position from saved instance state.
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);

            // moves camera to the location
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        if (map != null) {
            // saves map current location and camera position when fragment is paused
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);

            Log.i(TAG, "saved");
        }

        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // verify that we have permissions
        HelperClass.verifyPermissions(requireActivity());
        // init Places SDK
        HelperClass.initPlacesSDK(requireActivity());

        // Prompt the user for permission.
        getLocationPermission();

        // int variables for fragments
        fragmentsFrameId = R.id.fragmentsFrame;
        modalFrameId = R.id.modalLocationView;

        pbMaps = view.findViewById(R.id.pbMaps);
        searchView = view.findViewById(R.id.searchView);
        rvSearchList = view.findViewById(R.id.rvSearchList);
        frameLayout = view.findViewById(modalFrameId);
        mapContainer = view.findViewById(R.id.map);
        myLocationBtn = view.findViewById(R.id.myLocationBtn);
        sheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.modalLocationView));

        fragmentManager = getChildFragmentManager();

        height = DeviceDimenHelper.getDisplayHeightPixels(requireContext());
        width = DeviceDimenHelper.getDisplayWidthPixels(requireContext());

        // initiates the trending location arraylist
        topLocations = new ArrayList<>();

        initializeMap();

        rvSearchList.setVisibility(View.GONE);
        rvSearchList.setBackgroundResource(R.drawable.searchview_bg);

        setupSheetBehavior();

        setupSearchList();
        setupSearchView();

        hideOverlayBtns();

        myLocationBtn.setOnClickListener((v -> goToMyLocation()));
    }

    public void setupSearchList() {
        // elements needed for the search recyclerview
        SearchListAdapter.onItemClickListener onItemClickListener = prediction -> showPredictionInfo(prediction);
        predictions = new ArrayList<>();
        adapter = new SearchListAdapter(predictions, requireContext(), onItemClickListener);
    }

    private void initializeMap() {

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // if the fragment is available call onMapReady function
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    // show prediction info
    private void showPredictionInfo(AutocompletePrediction prediction) {

        String placeId = prediction.getPlaceId();
        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, HelperClass.placesFields);

        HelperClass.getPlacesClient().fetchPlace(request).addOnSuccessListener((response) -> {

            Place place = response.getPlace();
            closeSearchView();

            // zooms out and zooms to location
            map.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM), 3000, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                    GoogleMap.CancelableCallback callback = new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {

                            com.example.travelguide.classes.Location[] modalLocation = new com.example.travelguide.classes.Location[1];

                            GetCallback<com.example.travelguide.classes.Location> modalCallback = (result, e) -> {
                                if (e == null) {
                                    // get location from server
                                    modalLocation[0] = result;
                                } else {

                                    // if the location wasn't found add a new one
                                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                        modalLocation[0] = new com.example.travelguide.classes.Location();
                                        modalLocation[0].setPlaceId(placeId);
                                        modalLocation[0].setCoord(place.getLatLng().latitude, place.getLatLng().longitude);
                                    }
                                }

                                // shows modal view of location being selected
                                frameLayout.setVisibility(View.VISIBLE);
                                modalLocationGuideFragment = LocationGuideFragment.newInstance(modalLocation[0], fragmentsFrameId, true);

                                showModalFragment(modalLocationGuideFragment, true);
                            };

                            HelperClass.fetchLocation(place.getLatLng(), modalCallback);
                        }

                        @Override
                        public void onCancel() {

                        }
                    };
                    zoomToLocation(place.getLatLng(), callback);
                }

                @Override
                public void onCancel() {

                }
            });

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {

                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    }

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private void getDeviceLocation() {

        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(requireActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {

                            GoogleMap.CancelableCallback zoomCallback = new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    // show top locations after zoom
                                    showTopLocations();
                                    showMyLocationBtn();
                                }

                                @Override
                                public void onCancel() {
                                    // show top locations after zoom
                                    showTopLocations();
                                }
                            };

                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {

                                currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                zoomToLocation(currentLocation, zoomCallback);
                            }
                        } else {
                            Log.e(TAG, "Exception: %s", task.getException());

                            // moves camera to the location
                            zoomToLocation(defaultLocation);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    // gets list of locations from the ParseServer
    public void getGuides(boolean showModal) {

        // shows progress bar
        pbMaps.setVisibility(View.VISIBLE);

        // clears the map of markers
        map.clear();

        ParseQuery<com.example.travelguide.classes.Location> query = ParseQuery.getQuery(com.example.travelguide.classes.Location.class);
        query.findInBackground((locations, e) -> {

            // if there is no error
            if (e == null) {

                for (int i = 0; i < locations.size(); i++) {

                    // retrieves geo point from database and converts it to a LatLng Object
                    LatLng location = locations.get(i).getCoord();

                    // add top location to the object array
                    int pos = inTopLocations(locations.get(i));
                    if (pos != -1) {
                        topLocationObjects[pos] = locations.get(i);
                    }

                    // adds a new marker with the LatLng object
                    MarkerOptions markerOptions = new MarkerOptions().position(location)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon));
                    addMarker(markerOptions, locations.get(i));
                }

                // hides progress bar
                pbMaps.setVisibility(View.INVISIBLE);
                showOverlayBtns();

                // if you are supposed to show the modal fragment
                if (showModal) {
                    topLocationsFragment = TopLocationsFragment.newInstance(topLocationObjects);
                }
            } else {
                Log.e(TAG, "Not getting guides", e);
            }
        });
        ParseQuery.clearAllCachedResults();
    }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {

        // if the user granted permission to use the device location
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /*
     * updates the locationPermissionGranted variable based on the user permission dialog
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }


    // sets up modal sheetBehavior
    private void setupSheetBehavior() {

        sheetBehavior.setDraggable(modalLocationGuideFragment != null);

        // sets sheet behavior height
        sheetBehavior.setPeekHeight((int) (height / 2.25));
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull @NotNull View bottomSheet, int newState) {

                // toggle searchview if view is expanded and show expand indicator
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    searchView.setVisibility(View.INVISIBLE);

                    if (modalLocationGuideFragment != null) {
                        modalLocationGuideFragment.changeIndicatorState(View.INVISIBLE);
                    }
                }

                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    showOverlayBtns();
                }

                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    showOverlayBtns();
                    if (modalLocationGuideFragment != null) {
                        modalLocationGuideFragment.changeIndicatorState(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {

                if (slideOffset > 0.5) {
                    hideOverlayBtns();
                }

                // animates button based on if the modal view is visible
                if (bottomSheet.getVisibility() == View.INVISIBLE) {
                    myLocationBtn.animate().scaleX(1).scaleY(1).setDuration(0).start();
                } else {
                    // animate myLocationButton
                    myLocationBtn.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
                }
            }
        });
    }

    /*
     * sets location enabled to be true and updates maps ui
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                getDeviceLocation();
            } else {
                map.setMyLocationEnabled(false);
                lastKnownLocation = null;

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void hideMyLocationBtn() {
        myLocationBtn.hide();
    }

    private void showMyLocationBtn() {
        myLocationBtn.show();
    }


    // goes to the current location and shows the TopLocation Fragment
    private void goToMyLocation() {

        GoogleMap.CancelableCallback myLocation = new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                showTopLocations();
            }

            @Override
            public void onCancel() {
                showTopLocations();
            }
        };

        zoomToLocation(currentLocation, myLocation);
    }

    private void setupSearchView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        // creates divider for recyclerview
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvSearchList.getContext()
                , linearLayoutManager.getOrientation());

        // sets elements of the recycler view
        rvSearchList.setAdapter(adapter);
        rvSearchList.setLayoutManager(linearLayoutManager);
        // adds lines between the recyclerview elements
        rvSearchList.addItemDecoration(dividerItemDecoration);

        // Creates a new token for the autocomplete session
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // set searchview height
        searchView.getLayoutParams().height = (int) (DeviceDimenHelper.getDisplayHeightPixels(requireContext()) / 7.5);
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                Log.i(TAG, String.valueOf(hasFocus));
                // if the search view has focus
                if (hasFocus) {
                    hideModalFragment();
                }
            }
        });


        // Get the SearchView and set the searchable configuration
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                // clears focus and hides keyboard
                searchView.clearFocus();
                HelperClass.hideKeyboard(requireActivity());

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.isEmpty()) {
                    hideModalFragment();
                }

                if (lastKnownLocation != null) {
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setOrigin(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                            .setSessionToken(token).setQuery(newText).build();

                    HelperClass.getPlacesClient().findAutocompletePredictions(request).addOnSuccessListener((response) -> {

                        predictions = response.getAutocompletePredictions();

                        // updates the recyclerview
                        adapter.clear();
                        adapter.addAll(predictions);

                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    });
                }

                return false;
            }
        });
    }

    private void showTopLocations() {
        if (topLocationsFragment != null) {
            showModalFragment(topLocationsFragment, false);
        }
    }

    // check location object is trending
    private int inTopLocations(com.example.travelguide.classes.Location location) {

        if (topLocations != null) {

            for (int i = 0; i < topLocations.size(); i++) {
                // if the location is trending return true
                if (topLocations.get(i).containsValue(location.getObjectId())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void showModalFragment(Fragment modalFragment, boolean isDraggable) {

        // sets sheet behavior height
        sheetBehavior.setPeekHeight((int) (height / 2.25));

        sheetBehavior.setDraggable(isDraggable);
        // shows modal view of location being selected
        frameLayout.setVisibility(View.VISIBLE);

        // Begin the transaction
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // if the fragment is draggable
        if (isDraggable) {
            // add fragment to backstack
            ft.addToBackStack("ModalFragment");
        }

        // add fragment to container
        ft.replace(modalFrameId, modalFragment);
        // complete the transaction
        ft.show(modalFragment);
        // Complete the changes added above
        ft.commit();
    }

    // hides modal view making map draggable
    public void hideModalFragment() {
        if (frameLayout != null && fragmentManager != null) {
            setSheetState(BottomSheetBehavior.STATE_COLLAPSED);
            fragmentManager.popBackStack();
            sheetBehavior.setPeekHeight(0);
            frameLayout.setVisibility(View.INVISIBLE);
        }
    }

    // sets the view state for the ui elements
    public void hideOverlayBtns() {
        searchView.setVisibility(View.INVISIBLE);
        rvSearchList.setVisibility(View.INVISIBLE);
    }

    // sets the view state for the ui elements
    public void showOverlayBtns() {
        searchView.setVisibility(View.VISIBLE);
        rvSearchList.setVisibility(View.VISIBLE);
    }

    public void zoomToLocation(LatLng location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(returnAdjustedLocation(location), DEFAULT_ZOOM));
    }

    // overloaded function with callback
    public void zoomToLocation(LatLng location, GoogleMap.CancelableCallback callback) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(returnAdjustedLocation(location), DEFAULT_ZOOM), callback);
    }

    /*
     * ref: https://stackoverflow.com/questions/16764002/how-to-center-the-camera-so-that-marker-is-at-the-bottom-of-screen-google-map
     * functions to move camera up the screen using the location
     */
    @NotNull
    public LatLng returnAdjustedLocation(LatLng location) {

        double dpPerdegree = 256.0 * Math.pow(2, DEFAULT_ZOOM) / 170.0;
        double screen_height = mapContainer.getHeight();

        double adjusted_screen_height = -27.5 * screen_height / 100.0;
        double adjusted_degree = adjusted_screen_height / dpPerdegree;

        return new LatLng(location.latitude + adjusted_degree, location.longitude);
    }

    // add new marker to the map along with tag
    private void addMarker(MarkerOptions newMarker, com.example.travelguide.classes.Location location) {
        Marker marker = map.addMarker(newMarker);

        if (marker != null) {
            marker.setTag(location);
        }
    }

    /// close the searchview element
    public void closeSearchView() {
        searchView.clearFocus();
        searchView.setQuery("", false);
        searchView.setIconified(true);
        searchView.onActionViewCollapsed();
    }

    public void setModalLocationGuideFragment(LocationGuideFragment locationGuideFragment) {
        this.modalLocationGuideFragment = locationGuideFragment;
        showModalFragment(modalLocationGuideFragment, true);
    }

    public int getFragmentsFrameId() {
        return fragmentsFrameId;
    }

    public Fragment getModalFragment() {
        return modalLocationGuideFragment;
    }

    public void setSheetState(int newState) {
        sheetBehavior.setState(newState);
    }

    public void showModalIndicator() {

        if (modalLocationGuideFragment != null) {
            modalLocationGuideFragment.changeIndicatorState(View.VISIBLE);
        }
    }

    public BottomSheetBehavior getSheetBehavior() {
        return sheetBehavior;
    }
}