package com.example.travelguide.activities;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.travelguide.databinding.ActivityMapsBinding;
import com.example.travelguide.databinding.SearchviewUiBinding;
import com.example.travelguide.fragments.ComposeFragment;
import com.example.travelguide.fragments.LocationGuideFragment;
import com.example.travelguide.fragments.ProfileFragment;
import com.example.travelguide.fragments.TopLocationsFragment;
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
import com.iammert.library.ui.multisearchviewlib.MultiSearchView;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
    private MultiSearchView searchView;
    private RecyclerView rvSearchList;
    private ImageButton ibProfile;
    private SearchviewUiBinding searchviewUiBinding;

    // search ui elements
    private SearchListAdapter adapter;
    private List<AutocompletePrediction> predictions;
    private BottomSheetBehavior<View> sheetBehavior;

    // different fragments
    private ComposeFragment composeFragment;
    private LocationGuideFragment locationGuideFragment;
    private LocationGuideFragment modalLocationGuideFragment;
    private ProfileFragment profileFragment;
    private SupportMapFragment mapFragment;
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

        // binds element to search view
        searchviewUiBinding = binding.searchViewUI;
        searchView = searchviewUiBinding.searchView;
        rvSearchList = searchviewUiBinding.rvSearchList;

        ibProfile = binding.ibProfile;
        sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.modalLocationView));

        fragmentsFrameId = R.id.fragmentsFrame;
        modalFrameId = R.id.modalLocationView;

        fragmentManager = getSupportFragmentManager();

        height = DeviceDimenHelper.getDisplayHeight(this);
        width = DeviceDimenHelper.getDisplayWidth(this);

        // initiates the trending location arraylist
        topLocations = new ArrayList<>();
        initializeMap();

        // makes search list gone and sets resource
        rvSearchList.setVisibility(View.GONE);
        rvSearchList.setBackgroundResource(R.drawable.searchview_bg);

        // creates new instance of the different fragments
        composeFragment = new ComposeFragment();
        profileFragment = ProfileFragment.newInstance(fragmentsFrameId, ParseUser.getCurrentUser().getObjectId());

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

    public void showModalFragment(Fragment modalFragment, boolean isDraggable) {

        sheetBehavior.setDraggable(isDraggable);
        // shows modal view of location being selected
        findViewById(modalFrameId).setVisibility(View.VISIBLE);

        // Begin the transaction
        FragmentTransaction ft = fragmentManager.beginTransaction();
        // add fragment to container
        ft.replace(modalFrameId, modalFragment);
        // complete the transaction
        ft.show(modalFragment);
        // Complete the changes added above
        ft.commitNow();
    }

    // hides modal view making map draggable
    public void hideModalFragment() {
        findViewById(modalFrameId).setVisibility(View.INVISIBLE);
    }


    // sets up modal sheetBehavior
    private void setupSheetBehavior() {

        sheetBehavior.setDraggable(modalLocationGuideFragment != null);

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

                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    searchView.setVisibility(View.VISIBLE);

                    if (modalLocationGuideFragment != null) {
                        modalLocationGuideFragment.changeIndicatorState(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {
            }
        });

        // sets sheet behavior height
        sheetBehavior.setPeekHeight((int) (DeviceDimenHelper.getDisplayHeight(getApplicationContext()) / 2.5));
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

        searchView.setSearchViewListener(new MultiSearchView.MultiSearchViewListener() {
            @Override
            public void onTextChanged(int i, @NotNull CharSequence charSequence) {
                Log.i(TAG, (String) charSequence);
            }

            @Override
            public void onSearchComplete(int i, @NotNull CharSequence charSequence) {

            }

            @Override
            public void onSearchItemRemoved(int i) {

            }

            @Override
            public void onItemSelected(int i, @NotNull CharSequence charSequence) {

            }
        });
//        // Creates a new token for the autocomplete session
//        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
//        // Get the SearchView and set the searchable configuration
//        searchView(new SearchView.OnQueryTextListener() {
//
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                // clears focus and hides keyboard
//                searchView.clearFocus();
//                HelperClass.hideKeyboard(MapsActivity.this);
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//
//                if (lastKnownLocation != null) {
//                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
//                            .setOrigin(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
//                            .setSessionToken(token).setQuery(newText).build();
//
//                    HelperClass.getPlacesClient().findAutocompletePredictions(request).addOnSuccessListener((response) -> {
//
//                        predictions = response.getAutocompletePredictions();
//
//                        // updates the recyclerview
//                        adapter.clear();
//                        adapter.addAll(predictions);
//
//                    }).addOnFailureListener((exception) -> {
//                        if (exception instanceof ApiException) {
//                            ApiException apiException = (ApiException) exception;
//                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
//                        }
//                    });
//                }
//
//                return false;
//            }
//        });
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
        // TODO: add map versions

        // sets padding to change position of map controls
        map.setPadding(0, (int) (height / 1.25), 0, 0);

        // Prompt the user for permission.
        getLocationPermission();

        getLocationsandGuides();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull @NotNull Marker marker) {


                locationGuideFragment = LocationGuideFragment.newInstance(marker.getTag(), fragmentsFrameId, false);

                // Begin the transaction
                FragmentTransaction ft = fragmentManager.beginTransaction();
                // add fragment to container
                ft.replace(fragmentsFrameId, locationGuideFragment);

                // complete the transaction
                HelperClass.finishTransaction(ft, LocationGuideFragment.TAG, (Fragment) locationGuideFragment, false);
                hideOverlayBtns();

                return true;
            }
        });
    }

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
                    getGuides();
                }

            }
        });
    }


    // gets list of locations from the ParseServer
    public void getGuides() {

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

                    // add top location to the object

                    int pos = inTopLocations(locations.get(i));
                    if (pos != -1) {
                        topLocationObjects[pos] = locations.get(i);
                    }

                    // adds a new marker with the LatLng object
                    MarkerOptions markerOptions = new MarkerOptions().position(location)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    addMarker(markerOptions, locations.get(i));
                }

                // hides progress bar
                pbMaps.setVisibility(View.INVISIBLE);
                showOverlayBtns();

                topLocationsFragment = TopLocationsFragment.newInstance(topLocationObjects);
                showTopLocations();
            } else {
                Log.e(TAG, "Not getting guides", e);
            }
        });
        ParseQuery.clearAllCachedResults();
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
    private void addMarker(MarkerOptions newMarker, com.example.travelguide.classes.Location location) {
        Marker marker = map.addMarker(newMarker);

        if (marker != null) {
            marker.setTag(location);
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull @org.jetbrains.annotations.NotNull Bundle outState) {

        if (map != null) {
            // saves map current location and camera position when activity is paused
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

            hideModalFragment();
            showOverlayBtns();

            return;
        }

            // if there are no stacks showing go to home screen
            if (HelperClass.emptyBackStack(fragmentManager)) {
                super.onBackPressed();

            } else {
                // if the stack isn't empty
                fragmentManager.popBackStack();

                // is back stack empty set addGuide button to be visible and refresh page
                if (fragmentManager.getBackStackEntryCount() == 1) {
                    getLocationsandGuides();
                    showOverlayBtns();
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
//        lastKnownLocation = (Location) location;
    }

    // overloaded function with callback
    public void zoomToLocation(LatLng location, GoogleMap.CancelableCallback callback) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM), callback);
    }

    /// close the searchview element
    public void closeSearchView() {
        searchView.clearFocus();
//        searchView.setQuery("", false);
//        searchView.setIconified(true);
//        searchView.onActionViewCollapsed();
    }

    public void setModalLocationGuideFragment(LocationGuideFragment locationGuideFragment) {
        this.modalLocationGuideFragment = locationGuideFragment;
        showModalFragment(modalLocationGuideFragment, true);
    }

    public int getFragmentsFrameId() {
        return fragmentsFrameId;
    }
}