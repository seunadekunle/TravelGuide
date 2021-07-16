package com.example.travelguide.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.travelguide.R;
import com.example.travelguide.activities.MapsActivity;
import com.example.travelguide.classes.Guide;
import com.example.travelguide.helpers.BitmapScaler;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.example.travelguide.R.string.empty_text;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ComposeFragment} factory method to
 * create an instance of this fragment.
 */
public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";

    private static final String ARG_LONG = "longitude";
    private static final String ARG_LAT = "latitude";

    private static final int CAPTURE_MEDIA_RESULT_CODE = 34;
    public static final int PICK_PHOTO_GALLERY_CODE = 1046;

    private final String photoFileName = "photo.jpg";
    private final String videoFileName = "video.mp4";
    private String audioFileName = "";
    private File photoFile;
    private File videoFile;

    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;


    ActivityResultLauncher<Intent> galleryActivityLauncher;

    // parameters for passing data
    private Double longParam;
    private Double latParam;

    // default location
    private LatLng location = new LatLng(0, 0);

    private EditText etText;
    private Button locationBtn;
    private Button addBtn;
    private ImageButton photoBtn;
    private ImageButton galleryBtn;
    private ImageButton audioBtn;
    private ImageView ivPreview;
    private VideoView vvPreview;
    private MediaController controller;

    // ui elements for the audio recorder
    private LinearLayout recordingLayout;
    private LinearLayout playLayout;
    private ImageButton recordBtn;
    private ImageButton playBtn;


    public ComposeFragment() {
        // Required empty public constructor
    }

//    /**
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment ComposeFragment.
//     */
    // TODO: Rename and change types and number of parameters
//    public static ComposeFragment newInstance(Double param1, Double param2) {
//        ComposeFragment fragment = new ComposeFragment();
//        Bundle args = new Bundle();
//
//        args.putDouble(ARG_LONG, param1);
//        args.putDouble(ARG_LAT, param2);
//
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//
//            longParam = getArguments().getDouble(ARG_LONG);
//            latParam = getArguments().getDouble(ARG_LAT);
//
//            // sets default location for new Travel Guide
//            location = new LatLng(longParam, latParam);
//
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationBtn = view.findViewById(R.id.locationBtn);
        addBtn = view.findViewById(R.id.addBtn);
        etText = view.findViewById(R.id.etText);
        photoBtn = view.findViewById(R.id.photoBtn);
        galleryBtn = view.findViewById(R.id.galleryBtn);
        audioBtn = view.findViewById(R.id.audioBtn);
        ivPreview = view.findViewById(R.id.ivPreview);
        vvPreview = view.findViewById(R.id.vvPreview);

        recordingLayout = view.findViewById(R.id.recordingLayout);
        playLayout = view.findViewById(R.id.playLayout);

        recordBtn = view.findViewById(R.id.recordBtn);
        playBtn = view.findViewById(R.id.playBtn);

        // Create media recorder and player
        mediaRecorder = new MediaRecorder();
        mediaPlayer = new MediaPlayer();

        if (latParam != null && longParam != null)
            // gets location info from coordinates and sets button text
            setButtonText(HelperClass.getAddress(getContext(), latParam, longParam));

        setClickListeners();
    }

    private void setClickListeners() {
        // button click listener to add new guide
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the fields to specify which types of place data to return
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(getContext());
                // ensure that the request code is the one defined in MapsActivity
                getActivity().startActivityForResult(intent, MapsActivity.AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        // add photo button on click
        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a File reference for future access
                photoFile = getPhotoFileUri(photoFileName);
                videoFile = getVideoFileUri(videoFileName);

                // wrap File object into a content provider
                // required for API >= 24
                // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
                Uri photoUri = FileProvider.getUriForFile(getContext(), "com.travelguide.fileprovider", photoFile);
                Uri videoUri = Uri.fromFile(videoFile);

                onPickMedia(photoUri, videoUri);
            }
        });

        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        galleryActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getData() != null) {

                            // gets image data from gallery
                            Uri photoUri = result.getData().getData();

                            Log.i(TAG, String.valueOf(photoUri));
                            photoFile = getResizedImg(photoUri);
                            loadImgIntoPreview();

                            showImgView();
                        }
                    }
                });
        // gallery button on click
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });

        // audio button on click
        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAudioView();

                if (audioBtn.isSelected()) {

                    // Verify that the device has a mic first
                    PackageManager pmanager = getContext().getPackageManager();
                    if (pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {

                        // Set the file location for the audio
                        audioFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                        audioFileName += "/audiorecordtest.3gp";

                        // Set the audio format and encoder
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                        // Setup the output location
                        mediaRecorder.setOutputFile(audioFileName);

                    } else {
                        // no mic on device
                        Snackbar emptyText = Snackbar.make(etText, R.string.no_recorder, Snackbar.LENGTH_SHORT);
                        HelperClass.displaySnackBarWithBottomMargin(emptyText, 80, getActivity());
                        toggleAudioView();
                    }
                }

            }
        });

        // audio button on click
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelperClass.toggleButtonState(recordBtn);

                if (recordBtn.isSelected()) {

                    // Start recording the audio
                    try {
                        Log.i(TAG, "recording started");
                        mediaRecorder.prepare();
                        mediaRecorder.start();

                    } catch (IOException e) {
                        Log.e(TAG, "prepare() failed", e);
                    }
                } else {

                    Log.i(TAG, "recording stopped");

                    // Stop recording the audio
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();

                    recordBtn.setClickable(false);
                    playLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HelperClass.toggleButtonState(playBtn);

                if (playBtn.isSelected()) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(audioFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // start to play the audio
                    mediaPlayer.start();
                    mediaPlayer.getCurrentPosition();
                }
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etText.getText().toString();

                // if the text field is empty
                if (text.isEmpty()) {
                    Snackbar emptyText = Snackbar.make(etText, empty_text, Snackbar.LENGTH_SHORT);
                    HelperClass.displaySnackBarWithBottomMargin(emptyText, 80, getActivity());
                    return;
                }

                // if no location is selected
                if (location == null) {
                    Snackbar emptyLocation = Snackbar.make(etText, R.string.no_locattion, Snackbar.LENGTH_SHORT);
                    HelperClass.displaySnackBarWithBottomMargin(emptyLocation, 80, getActivity());
                    return;
                }

                ParseUser user = ParseUser.getCurrentUser();
                saveGuide(text, user, photoFile, videoFile);
            }
        });
    }

    private void toggleAudioView() {
        // shows recording view based on state of audio button
        if (!audioBtn.isSelected()) {
            audioBtn.setSelected(true);
            recordingLayout.setVisibility(View.VISIBLE);
        } else {
            audioBtn.setSelected(false);
            recordingLayout.setVisibility(View.GONE);
        }
    }

    // creates intent to create a new photo or video
    private void onPickMedia(Uri photoUri, Uri videoUri) {

        // intent to take a photo
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        // intent to take a video
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
//                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);

        // creates an intent to choose the photo or camera intent
        Intent chooserIntent = Intent.createChooser(takePictureIntent, "Capture Image or Video");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});

        // checks if the intent is valid
        if (chooserIntent.resolveActivity(getContext().getPackageManager()) != null)
            startActivityForResult(chooserIntent, CAPTURE_MEDIA_RESULT_CODE);
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // as long as intent isn't nullt
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            galleryActivityLauncher.launch(intent);
        }
    }

    // creates new Travel guide and updates it to the database
    private void saveGuide(String text, ParseUser user, File photo, File video) {

        Guide guide = new Guide();
        guide.setAuthor(user);
        guide.setText(text);
        guide.setLocation(location);

        // sets the photo and video fields if they exist
        if (photo != null)
            guide.setPhoto(new ParseFile(photo));
        else if (video != null)
            guide.setPhoto(new ParseFile(video));

        // uploads new guide in the background
        guide.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Error while saving tag", e);
                    return;
                }

                // clears guide and goes back to main fragment
                guide.setText("");
                ivPreview.setImageResource(0);
                vvPreview.setVideoPath("");

                getActivity().onBackPressed();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_MEDIA_RESULT_CODE) {

            if (resultCode == getActivity().RESULT_OK) {

                if (data.getData() == null) {

                    // resize bitmap
                    Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(photoFileName));
                    File resizedFile = getResizedImg(takenPhotoUri);

                    // updates value of photoFile
                    photoFile = resizedFile;
                    loadImgIntoPreview();

                    // Bitmap takenImage = BitmapFactory.decodeFile(resizedFile.getAbsolutePath());
                    // sets other buttons to be not clickable
                    showImgView();

                } else {
                    // adjust view states to be visible
                    vvPreview.setVisibility(View.VISIBLE);
                    ivPreview.setVisibility(View.GONE);

                    // sets other file to be null
                    photoFile = null;

                    // play recorded video
                    playbackRecordedVideo(data.getData());
                }


            }
        } else { // Result was a failure
            Toast.makeText(getContext(), "Media wasn't taken!", Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * returns a compressed, resized image
     * reference: https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media#accessing-stored-media
     */
    @NotNull
    private File getResizedImg(Uri takenPhotoUri) {

        // get image from disk
        Bitmap rawTakenImage = loadFromUri(takenPhotoUri);
        Log.i(TAG, String.valueOf(rawTakenImage));
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, HelperClass.resizedImgDimen);

        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress the image further
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
        File resizedFile = getPhotoFileUri(photoFileName + "_resized");
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

    private void showImgView() {
        // adjust view states to be visible
        ivPreview.setVisibility(View.VISIBLE);
        vvPreview.setVisibility(View.GONE);

        // sets other file to be null
        videoFile = null;
    }

    // load image into preview ui element
    private void loadImgIntoPreview() {
        // Load the taken image into a preview
        Glide.with(getContext())
                .load(photoFile)
                .override(HelperClass.resizedImgDimen, HelperClass.resizedImgDimen)
                .transform(new RoundedCornersTransformation(HelperClass.picRadius, 0))
                .into(ivPreview);
    }

    // plays video that has been recorded
    public void playbackRecordedVideo(Uri videoUri) {

        vvPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                        // sets the controller to be anchored to the video view
                        controller = new MediaController(getContext());
                        vvPreview.setMediaController(controller);
                        controller.setAnchorView(vvPreview);
                    }
                });
            }
        });
        vvPreview.setVideoURI(videoUri);
        vvPreview.requestFocus();
        vvPreview.start();
    }

    // sets location from Google place object and changes button text
    public void setLocation(Place newLocation) {
        location = newLocation.getLatLng();
        setButtonText(newLocation.getName());
    }

    // sets location from LatLNg object
    public void setLocation(LatLng newLocation) {
        longParam = newLocation.longitude;
        latParam = newLocation.latitude;
        location = newLocation;
    }

    // returns location of new guide
    public LatLng getLocation() {
        return location;
    }

    // sets the button text given String
    private void setButtonText(String newText) {
        locationBtn.setText(newText);
    }

    /*
     * Returns the File for a photo stored on disk given the fileName
     * reference - https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media#accessing-stored-media
     */
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    /*
     * Returns the File for a video stored on disk given the fileName
     * https://guides.codepath.com/android/Video-Playback-and-Recording
     */
    public File getVideoFileUri(String fileName) {
        return new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName);
    }

    /* returns a Bitmap object given a Uri
     *  reference: https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media#accessing-stored-media
     * */
    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    //    TODO: add delete button for media
}