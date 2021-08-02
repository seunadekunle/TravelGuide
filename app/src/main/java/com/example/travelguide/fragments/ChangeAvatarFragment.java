package com.example.travelguide.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelguide.R;
import com.example.travelguide.activities.EntryActivity;
import com.example.travelguide.activities.MainActivity;
import com.example.travelguide.activities.MapsActivity;
import com.example.travelguide.helpers.HelperClass;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangeAvatarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangeAvatarFragment extends Fragment {

    public static final String TAG = "ChangeAvatarFragment";

    private static final String IN_PROFILE = "inProfile";


    private Boolean inProfile;

    private String avatarPhotoName = "profile_photo.jpg";
    private ActivityResultLauncher<Intent> changAvatarLauncher;

    private ImageView ivTempAvatar;
    private Button photoBtn;
    private Button useBtn;
    private Button discardBtn;

    private File avatarFile;

    public ChangeAvatarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param inProfile Parameter 1.
     * @return A new instance of fragment ChangeAvatarFragment.
     */
    public static ChangeAvatarFragment newInstance(Boolean inProfile) {
        ChangeAvatarFragment fragment = new ChangeAvatarFragment();
        Bundle args = new Bundle();

        args.putBoolean(IN_PROFILE, inProfile);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            inProfile = getArguments().getBoolean(IN_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_avatar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivTempAvatar = view.findViewById(R.id.ivTempAvatar);
        useBtn = view.findViewById(R.id.useBtn);
        discardBtn = view.findViewById(R.id.discardBtn);
        photoBtn = view.findViewById(R.id.photoBtn);

        // loads the profile image
        HelperClass.loadProfileImage(getContext(), HelperClass.AVATAR_IMG_DIMEN, HelperClass.AVATAR_IMG_DIMEN, ivTempAvatar);

        avatarFile = HelperClass.getMediaFileUri(avatarPhotoName, Environment.DIRECTORY_PICTURES, requireContext());
        Uri photoUri = HelperClass.getUriForFile(requireContext(), avatarFile);

        changAvatarLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                Intent photoIntent = result.getData();

                if (photoIntent != null) {
                    Uri photoUri;

                    // if the photo was taken with the phone camera
                    if (photoIntent.getData() == null) {

                        photoUri = Uri.fromFile(HelperClass.getMediaFileUri(avatarPhotoName, Environment.DIRECTORY_PICTURES, requireContext()));

                        avatarFile = HelperClass.getResizedImg(photoUri, getContext(), avatarPhotoName, ivTempAvatar, true);
                        Log.i(TAG, avatarFile.toString());
                    }

                    // gets image data from gallery and sets the file variable
                    else {
                        photoUri = photoIntent.getData();
                        avatarFile = HelperClass.getResizedImg(photoUri, getContext(), avatarPhotoName, ivTempAvatar, true);
                    }

                }

            }
        });


        photoBtn.setOnClickListener(v -> {
            // launch the chooser activity
            changAvatarLauncher.launch(HelperClass.getAvatarIntent(photoUri));

        });

        discardBtn.setOnClickListener(v -> {
            if (inProfile)
                ((MainActivity) requireActivity()).onBackPressed();
            else {
                // goes to map activity
                ((EntryActivity) requireActivity()).navigateToMapView();

            }
        });

        useBtn.setOnClickListener(v -> {

            // change the profile photo of the current user
            ParseUser currentUser = ParseUser.getCurrentUser();
            currentUser.put("avatar", new ParseFile(avatarFile));

            currentUser.saveInBackground(e -> {
                if (e != null) {
                    Log.i(TAG, "there was an error saving the profile photo");
                } else {
                    // navigates to different sections depending on where it is shown
                    if (inProfile)
                        ((MainActivity) requireActivity()).onBackPressed();
                    else
                        ((EntryActivity) requireActivity()).navigateToMapView();
                }
            });
        });
    }
}

