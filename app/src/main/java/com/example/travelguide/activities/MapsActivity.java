package com.example.travelguide.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelguide.R;
import com.example.travelguide.adapters.SearchListAdapter;
import com.example.travelguide.classes.Guide;
import com.example.travelguide.databinding.ActivityMapsBinding;
import com.example.travelguide.fragments.ComposeFragment;
import com.example.travelguide.fragments.LocationGuideFragment;
import com.example.travelguide.fragments.ProfileFragment;
import com.example.travelguide.helpers.DeviceDimenHelper;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private int fragmentsFrameId;
    private int modalFrameId;

    // Ui elements
    private GoogleMap map;
    private ActivityMapsBinding binding;
    private FloatingActionButton addGuide;
    private FragmentManager fragmentManager;
    private ProgressBar pbMaps;
    private SearchView searchView;
    private RecyclerView rvSearchList;
    private ImageButton ibProfile;

    // search ui elements
    private SearchListAdapter adapter;
    private List<AutocompletePrediction> predictions;
    private BottomSheetBehavior sheetBehavior;

    // different fragments
    private ComposeFragment composeFragment;
    private LocationGuideFragment locationGuideFragment;
    private LocationGuideFragment modalLocationGuideFragment;
    private ProfileFragment profileFragment;
    private SupportMapFragment mapFragment;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 20;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // variables for the window height and width
    private int height = 0;
    private int width = 0;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // verify that we have permissions
        HelperClass.verifyPermissions(this);
        // init Places SDK
        HelperClass.initPlacesSDK(this);

        // Prompt the user for permission.
        getLocationPermission();

        // bind ui element to variable
        addGuide = binding.addGuide;
        pbMaps = binding.pbMaps;
        searchView = binding.searchView;
        rvSearchList = binding.rvSearchList;
        ibProfile = binding.ibProfile;
        sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.modalLocationView));

        fragmentsFrameId = R.id.fragmentsFrame;
        modalFrameId = R.id.modalLocationView;

        fragmentManager = getSupportFragmentManager();

        height = DeviceDimenHelper.getDisplayHeight(this);
        width = DeviceDimenHelper.getDisplayWidth(this);

        initializeMap();

        // creates new instance of the different fragments
        composeFragment = new ComposeFragment();
        profileFragment = new ProfileFragment();

        setupSheetBehavior();

        // elements needed for the search recyclerview
        SearchListAdapter.onItemClickListener onItemClickListener = new SearchListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(AutocompletePrediction prediction) {
                showPredictionInfo(prediction);
            }
        };
        predictions = new ArrayList<>();
        adapter = new SearchListAdapter(predictions, this, onItemClickListener);
        setupSearchView();

        ibProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "clicked");

                // Begin the transaction
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // add fragment to container
                if (!profileFragment.isAdded())
                    ft.add(fragmentsFrameId, profileFragment);

                HelperClass.finishTransaction(ft, ProfileFragment.TAG, (Fragment) profileFragment);
                hideOverlayBtns();
            }
        });

        // add button on click listener
        addGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Begin the transaction
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // add fragment to container
                if (!composeFragment.isAdded())
                    ft.add(fragmentsFrameId, composeFragment);

                HelperClass.finishTransaction(ft, ComposeFragment.TAG, (Fragment) composeFragment);
                hideOverlayBtns();
            }
        });

        hideOverlayBtns();
    }

    // show prediction info
    private void showPredictionInfo(AutocompletePrediction prediction) {

        String placeId = prediction.getPlaceId();
        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, HelperClass.placesFields);

        HelperClass.getPlacesClient().fetchPlace(request).addOnSuccessListener((response) -> {

            Place place = response.getPlace();
            closeSearchView();

            addGuide.setVisibility(View.INVISIBLE);

            // zooms out and zooms to location
            map.animateCamera(CameraUpdateFactory.zoomTo(20), 3000, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                    GoogleMap.CancelableCallback callback = new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {

                            // shows modal view of location being selected
                            findViewById(modalFrameId).setVisibility(View.VISIBLE);
                            modalLocationGuideFragment = LocationGuideFragment.newInstance(Objects.requireNonNull(place.getLatLng()).latitude, place.getLatLng().longitude);

                            // Begin the transaction
                            FragmentTransaction ft = fragmentManager.beginTransaction();
                            // add fragment to container
                            ft.replace(modalFrameId, modalLocationGuideFragment);
                            // complete the transaction
                            ft.show(modalLocationGuideFragment);
                            // Complete the changes added above
                            ft.commit();
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

    // sets up modal sheetBehavior
    private void setupSheetBehavior() {
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull @NotNull View bottomSheet, int newState) {

                // toggle searchview if view is expanded
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    searchView.setVisibility(View.INVISIBLE);
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    searchView.setVisibility(View.VISIBLE);
                    modalLocationGuideFragment.makeIndicatorVisible();
                }
            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {
                // shows expansion indicator
                modalLocationGuideFragment.setIndicatorOpacity(255 - ((int) slideOffset * 255));
            }
        });
    }

    private void setupSearchView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
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
        // Get the SearchView and set the searchable configuration
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                // clears focus and hides keyboard
                searchView.clearFocus();
                hideKeyboard();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

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

    private void initializeMap() {
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);

        // if the fragment is available call onMapReady function
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setScrollGesturesEnabled(true);

        // sets padding to change position of map controls
        map.setPadding(0, (int) (height / 1.25), 0, 0);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // get list of currrent guides
        getGuides();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull @NotNull Marker marker) {

                // gets the location of the marker
                Double latitude = ((ParseGeoPoint) marker.getTag()).getLatitude();
                Double longitude = ((ParseGeoPoint) marker.getTag()).getLongitude();

                locationGuideFragment = LocationGuideFragment.newInstance(latitude, longitude);

                // Begin the transaction
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // add fragment to container
                ft.replace(fragmentsFrameId, locationGuideFragment);

                // complete the transaction
                HelperClass.finishTransaction(ft, LocationGuideFragment.TAG, (Fragment) locationGuideFragment);
                hideOverlayBtns();

                return true;
            }
        });
    }


    // gets list of locations from the ParseServer
    public void getGuides() {

        // shows progress bar
        pbMaps.setVisibility(View.VISIBLE);

        // clears the map of markers
        map.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Guide");
        query.whereExists(Guide.getKeyLocation());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> locations, ParseException e) {

                if (e == null) {
                    for (int i = 0; i < locations.size(); i++) {

                        // retrieves geo point from database and converts it to a LatLng Object
                        ParseGeoPoint locationData = locations.get(i).getParseGeoPoint(Guide.getKeyLocation());
                        LatLng location = new LatLng(locationData.getLatitude(), locationData.getLongitude());

                        // adds a new marker with the LatLng object
                        addMarker(new MarkerOptions().position(location), locations.get(i).getParseGeoPoint(Guide.getKeyLocation()));
                    }

                    // hides progress bar
                    pbMaps.setVisibility(View.INVISIBLE);
                    showOverlayBtns();
                } else {
                    Log.e(TAG, "Not getting guides", e);
                }
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
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
                map.getUiSettings().setMyLocationButtonEnabled(true);
                getDeviceLocation();
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;

                // call location permissions dialog again
//                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private void getDeviceLocation() {

        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                LatLng currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                zoomToLocation(currentLocation);

                                // sends current location data to compose fragment
                                composeFragment.setLocation(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                            }
                        } else {
                            Log.e(TAG, "Exception: %s", task.getException());
                            zoomToLocation(defaultLocation);
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    // add new marker to the map along with tag
    // TODO: replace with place id
    private void addMarker(MarkerOptions newMarker, ParseGeoPoint objectId) {
        Marker marker = map.addMarker(newMarker);
        marker.setTag(objectId);
    }

    // saves map current location and camera position when activity is paused
    @Override
    protected void onSaveInstanceState(@NonNull @org.jetbrains.annotations.NotNull Bundle outState) {

        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }

        super.onSaveInstanceState(outState);
    }

    // handles the back press based what is showing
    @Override
    public void onBackPressed() {

        // if the modal fragment is visible
        if (modalLocationGuideFragment != null && modalLocationGuideFragment.isVisible()) {

            // resets the modal state
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // adjust the ui accordingly
            getSupportFragmentManager().beginTransaction().remove(modalLocationGuideFragment).commit();
            findViewById(modalFrameId).setVisibility(View.INVISIBLE);
            showOverlayBtns();

            return;
        }

        if (!(searchView.isIconified())) {
            closeSearchView();
        } else {

            // if there are no stacks showing go to home screen
            if (HelperClass.emptyBackStack(fragmentManager)) {
                super.onBackPressed();

            } else {
                // if the stack isn't empty
                fragmentManager.popBackStack();

                // is back stack empty set addGuide button to be visible and refresh page
                if (fragmentManager.getBackStackEntryCount() == 1) {
                    getGuides();
                    showOverlayBtns();
                }
            }
        }
    }

    // TODO: Add transition
    // sets the view state for the addGuide Button
    public void hideOverlayBtns() {
        addGuide.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.INVISIBLE);
        rvSearchList.setVisibility(View.INVISIBLE);
        ibProfile.setVisibility(View.INVISIBLE);
    }

    // sets the view state for the addGuide Button
    public void showOverlayBtns() {
        addGuide.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.VISIBLE);
        rvSearchList.setVisibility(View.VISIBLE);
        ibProfile.setVisibility(View.VISIBLE);
    }

    // TODO: add zoom when navigating from adding new guide
    public void zoomToLocation(LatLng location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
    }

    // overloaded function with callback
    public void zoomToLocation(LatLng location, GoogleMap.CancelableCallback callback) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM), callback);
    }

    /// close the searchview element
    public void closeSearchView() {
        searchView.clearFocus();
        searchView.setQuery("", false);
        searchView.setIconified(true);
        searchView.onActionViewCollapsed();
    }

    /*
     *  hides current keyboard
     *  ref: https://stackoverflow.com/questions/43061216/dismiss-keyboard-on-button-click-that-close-fragment
     */
    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}