package com.example.travelguide.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.travelguide.R;
import com.example.travelguide.classes.GlideApp;
import com.example.travelguide.classes.Location;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.parse.GetCallback;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// helper functions used multiple times in the project
public class HelperClass {

    private static final String TAG = "HelperClass";
    private static PlacesClient placesClient;
    private static ParseUser profileUser;

    public static int picRadius = 50;
    public static int resizedImgDimen = 650;
    public static int detailImgDimen = 475;

    public static final int AVATAR_IMG_DIMEN = 1000;

    public static String[] profileTabTitles = {"Guides", "Liked"};
    public static final String videoFileName = "video.mp4";
    public static final String defaultPlaceID = "0";

    // Set the fields to specify which types of place data to return
    // for Google places API
    public static List<Place.Field> placesFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);

    // gets location info from coordinates
    public static String getAddress(Context context, Double latitude, Double longitude) {

        List<Address> likelyNames = new ArrayList<>();
        Geocoder geocoder = new Geocoder(context);

        try {
            likelyNames = geocoder.getFromLocation(latitude, longitude, 1);
            Log.i(TAG, String.valueOf(likelyNames));
            return likelyNames.get(0).getAddressLine(0);

        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
        return "";
    }


    public static void fetchPlacesName(OnSuccessListener<FetchPlaceResponse> responseListener, String placeID) {
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, HelperClass.placesFields);
        HelperClass.getPlacesClient().fetchPlace(request)
                .addOnSuccessListener(responseListener);
    }


    public static void fetchCurrentPlace(Context context, OnCompleteListener<FindCurrentPlaceResponse> currentPlaceResponseOnCompleteListener) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            return;
        }

        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(HelperClass.placesFields);
        Task<FindCurrentPlaceResponse> findCurrentPlaceResponseTask = HelperClass.getPlacesClient().findCurrentPlace(request);
        findCurrentPlaceResponseTask.addOnCompleteListener(currentPlaceResponseOnCompleteListener);
    }


    /*
     * displays snackbar with margin
     * gotten from https://stackoverflow.com/questions/36588881/snackbar-behind-navigation-bar
     */
    public static void displaySnackBarWithBottomMargin(Snackbar snackbar, int marginBottom, Context context) {
        final View snackBarView = snackbar.getView();

        snackBarView.setTranslationY(-(DeviceDimenHelper.convertDpToPixel(marginBottom, context)));
        snackbar.show();
    }


    // Verifies that permissions has been granted
    public static void verifyPermissions(Activity activity) {

        // The request code used in ActivityCompat.requestPermissions()
        // and returned in the Activity's onRequestPermissionsResult()
        int PERMISSION_ALL = 1;

        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                Manifest.permission.VIBRATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.WAKE_LOCK
        };

        ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
    }

    // if the fragment manager stack is empty
    public static boolean emptyBackStack(FragmentManager fragmentManager) {
        return fragmentManager.getBackStackEntryCount() == 0;
    }

    // shows fragment in container
    public static void addFragment(FragmentManager fragmentManager, int fragmentsFrameId, Fragment fragment, String tag, Boolean addToBackStack) {

        // Begin the transaction
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // add fragment to container
        if (!fragment.isAdded())
            ft.add(fragmentsFrameId, fragment);

        finishTransaction(ft, tag, fragment, addToBackStack);
    }

    // shows fragment in container
    public static void replaceFragment(FragmentManager fragmentManager, int fragmentsFrameId, Fragment fragment, String tag, Boolean addToBackStack) {

        // Begin the transaction
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // replace fragment in container
        ft.replace(fragmentsFrameId, fragment);

        finishTransaction(ft, tag, fragment, addToBackStack);
    }


    // completes fragment transaction
    public static void finishTransaction(FragmentTransaction ft, String name, Fragment fragment,Boolean addToBackStack) {

        Log.i(TAG, " " + name);

        if (addToBackStack) {
            // add transaction to backstack
            ft.addToBackStack(name);
        }
        // show fragment
        ft.show(fragment);

        // Complete the changes added above
        ft.commit();
    }

    // changes the ui state of button
    public static void toggleButtonState(ImageButton button) {
        button.setSelected(!button.isSelected());
    }

    // Initialize places client sdk
    public static void initPlacesSDK(Context context) {
        // Initialize Places SDK
        Places.initialize(context.getApplicationContext(), context.getResources().getString(R.string.google_maps_key));
        // Create a new PlacesClient instance
        placesClient = Places.createClient(context);
    }

    public static PlacesClient getPlacesClient() {
        return placesClient;
    }

    // loads profile image
    public static void loadProfileImage(Context context, int width, int height, ImageView imageView) {

        // gets profile image and load it
        GlideApp.with(context)
                .load(getProfileUrl()).fitCenter().transform(new CircleCrop())
                .override(width, height).into(imageView);
    }

    // loads profile image for image button
    public static void loadProfileImage(String imgUrl, Context context, int width, int height, ImageButton imageButton) {
        GlideApp.with(context)
                .load(imgUrl).fitCenter().transform((new CircleCrop()))
                .override(width, height).into(imageButton);
    }

    // loads profile image for image button
    public static void loadCircularImage(String imgUrl, Context context, int width, int height, ImageView imageView) {
        GlideApp.with(context)
                .load(imgUrl).fitCenter().transform((new CircleCrop()))
                .override(width, height).into(imageView);
    }

    // loads profile image for image button
    public static void loadProfileImage(Bitmap bitmap, Context context, int width, int height, ImageView imageView) {

        // loads bitmap into image preview
        GlideApp.with(context).asBitmap().override(width, height).load(bitmap)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    // return url of profile img
    private static String getProfileUrl() {

        ParseFile profileImg = ParseUser.getCurrentUser().getParseFile("avatar");

        if (profileImg != null) {
            return profileImg.getUrl();
        }

        return "";
    }

    // Create intent for picking a photo from the gallery
    public static Intent getGalleryIntent() {
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    // returns Uri given a File
    public static Uri getUriForFile(Context context, File photoFile) {
        return FileProvider.getUriForFile(context, "com.travelguide.fileprovider", photoFile);
    }

    // Create intent for picking a photo from the gallery
    public static Intent getPhotoIntent(Uri photoUri) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        return takePictureIntent;
    }

    // creates an intent to choose the photo or camera intent
    @NotNull
    public static Intent getChooserIntent(Intent firstChoice, Intent secondChoice, String title) {
        Intent chooserIntent = Intent.createChooser(firstChoice, title);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{secondChoice});
        return chooserIntent;
    }

    // intent to change avatar using photo or gallery
    public static Intent getAvatarIntent(Uri photoUri) {
        return getChooserIntent(getPhotoIntent(photoUri), getGalleryIntent(), "Choose new image");
    }

    /*
     * returns a compressed, resized image
     * reference: https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media#accessing-stored-media
     */
    @NotNull
    public static File getResizedImg(Uri takenPhotoUri, Context context, String photoFileName, ImageView imageView, Boolean inProfile) {

        // get image from disk
        Bitmap rawTakenImage = loadFromUri(takenPhotoUri, context);
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, HelperClass.resizedImgDimen);

        // loads the image differently depending on if image is rendered in profile view
        if (!inProfile) {
            // loads bitmap into image preview
            GlideApp.with(context).asBitmap().override(HelperClass.resizedImgDimen, HelperClass.resizedImgDimen).load(resizedBitmap)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            super.setResource(resource);
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCornerRadius(HelperClass.picRadius);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            loadProfileImage(resizedBitmap, context, AVATAR_IMG_DIMEN, AVATAR_IMG_DIMEN, imageView);
        }

        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress the image further
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
        File resizedFile = getMediaFileUri("resized_" + photoFileName, Environment.DIRECTORY_PICTURES, context);
        try {
            resizedFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(resizedFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Write the bytes of the bitmap to file
        try {
            fos.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resizedFile;
    }


    /* returns a Bitmap object given a Uri
     *  reference: https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media#accessing-stored-media
     * */
    private static Bitmap loadFromUri(Uri photoUri, Context context) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /*
     * Returns the File for a photo stored on disk given the fileName
     * reference - https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media#accessing-stored-media
     */
    public static File getMediaFileUri(String fileName, String dir, Context context) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(context.getExternalFilesDir(dir), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename

        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    // returns user based on ID
    public static void fetchUser(String userID, GetCallback<ParseUser> callback) {
        ParseUser user = new ParseUser();
        user.setObjectId(userID);
        user.fetchInBackground(callback);
    }

    // returns location based on LatLng object
    public static void fetchLocation(LatLng location, GetCallback<Location> callback) {
        ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
        query.whereEqualTo(Location.getKeyCoord(), new ParseGeoPoint(location.latitude, location.longitude));
        query.getFirstInBackground(callback);
    }

    /*
     *  hides current keyboard
     *  ref: https://stackoverflow.com/questions/43061216/dismiss-keyboard-on-button-click-that-close-fragment
     */
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
