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
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelguide.R;
import com.example.travelguide.helpers.HelperClass;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangeAvatarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangeAvatarFragment extends Fragment {

    public static final String TAG = "ChangeAvatarFragment";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private String profilePhotoName = "profile_photo.jpg";
    private ActivityResultLauncher<Intent> photoActivityLauncher;

    private ImageButton ibTempAvatar;
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
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangeAvatarFragment.
     */
    public static ChangeAvatarFragment newInstance(String param1, String param2) {
        ChangeAvatarFragment fragment = new ChangeAvatarFragment();
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
        return inflater.inflate(R.layout.fragment_change_avatar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ibTempAvatar = view.findViewById(R.id.ibTempAvatar);
        useBtn = view.findViewById(R.id.useBtn);
        discardBtn = view.findViewById(R.id.discardBtn);
        photoBtn = view.findViewById(R.id.photoBtn);

        // loads the profile image
        HelperClass.loadProfileImage(getContext(), 1400, 1400, ibTempAvatar);

        avatarFile = HelperClass.getMediaFileUri(profilePhotoName, Environment.DIRECTORY_PICTURES, requireContext());
        Uri photoUri = HelperClass.getUriForFile(requireContext(), avatarFile);

        photoActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                Intent photoIntent = result.getData();

                if (photoIntent != null) {
                    // if the photo was taken with the phone camera
                    if (photoIntent.getData() == null) {
                        Log.i(TAG, "raw photo selected");
                    } else {
                        Log.i(TAG, "Gallery selected");

                    }
                }

            }
        });

        photoBtn.setOnClickListener(v -> {
            photoActivityLauncher.launch(HelperClass.getAvatarIntent(photoUri));

        });
    }
}