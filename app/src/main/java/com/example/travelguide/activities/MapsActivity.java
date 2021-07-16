package com.example.travelguide.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.travelguide.R;
import com.example.travelguide.classes.Guide;
import com.example.travelguide.databinding.ActivityMapsBinding;
import com.example.travelguide.fragments.ComposeFragment;
import com.example.travelguide.fragments.LocationGuide;
import com.example.travelguide.helpers.DeviceDimenHelper;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private int fragmentsFrameId;


    // Ui elements
    private GoogleMap map;
    private ActivityMapsBinding binding;
    private FloatingActionButton addGuide;
    private FragmentManager fragmentManager;
    private ProgressBar pbMaps;

    // different fragments
    private ComposeFragment composeFragment;
    private LocationGuide locationGuide;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CameraPosition cameraPosition;

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
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // verify that we have permissions
        HelperClass.verifyPermissions(this);

        // Prompt the user for permission.
        getLocationPermission();

        // Initialize Places SDK
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);

        // bind ui element to variable
        addGuide = binding.addGuide;
        pbMaps = binding.pbMaps;

        fragmentsFrameId = R.id.fragmentsFrame;
        fragmentManager = getSupportFragmentManager();

        height = DeviceDimenHelper.getDisplayHeight(this);
        width = DeviceDimenHelper.getDisplayWidth(this);

        initializeMap();

        // creates new instance of the different fragments
        composeFragment = new ComposeFragment();

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
                hideAddBtn();
            }
        });

        hideAddBtn();
    }

    private void initializeMap() {
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);

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
        map.setPadding(0, (int) (height / 1.45), 0, 0);

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

                locationGuide = LocationGuide.newInstance(latitude, longitude);

                // Begin the transaction
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // add fragment to container
                ft.replace(fragmentsFrameId, locationGuide);

                // complete the transaction
                HelperClass.finishTransaction(ft, LocationGuide.TAG, (Fragment) locationGuide);
                hideAddBtn();

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
                    showAddBtn();
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

    // removes fragment from view if back button is pressed
    @Override
    public void onBackPressed() {

        // if there are no stacks showing go to home screen
        if (HelperClass.emptyBackStack(fragmentManager))
            super.onBackPressed();

        // if the stack isn't empty
        if (!HelperClass.emptyBackStack(fragmentManager)) {
            fragmentManager.popBackStack();

            // is back stack empty set addGuide button to be visible and refresh page
            if (fragmentManager.getBackStackEntryCount() == 1) {
                getGuides();
                showAddBtn();
            }
        }
    }

    // TODO: Add transition
    // sets the view state for the addGuide Button
    public void hideAddBtn() {
        addGuide.setVisibility(View.INVISIBLE);
    }

    // sets the view state for the addGuide Button
    public void showAddBtn() {
        addGuide.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {

            // if result code is ok set the location for the new guide
            if (resultCode == RESULT_OK) {

                Place place = Autocomplete.getPlaceFromIntent(data);
                composeFragment.setLocation(place);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

                if (composeFragment.getLocation() == null)
                    // tell the user they didn't select a location
                    Snackbar.make(addGuide, R.string.location_not_selected, Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void zoomToLocation(LatLng location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
    }
    // TODO: add zoom when navigating from adding new guide

}