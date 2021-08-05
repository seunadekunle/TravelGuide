package com.example.travelguide.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.travelguide.R;
import com.example.travelguide.activities.EntryActivity;
import com.example.travelguide.databinding.FragmentEntryBinding;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.Objects;

public class EntryFormFragment extends Fragment {

    public static final String TAG = "EntryFormFragment";

    private static final String ARG_TYPE = "type";
    private static final String ARG_ID = "id";

    private static final String INVALID_ENTRY_DATA_ERROR = "Invalid username/password.";

    private FragmentEntryBinding binding;
    private String entryState;
    private ChangeAvatarFragment newUserAvatar;
    FragmentManager fragmentManager;

    private ProgressBar loadingProgressBar;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton;

    public EntryFormFragment() {
        // Required empty public constructor
    }

    /**
     * @param type Parameter 1.
     * @return A new instance of fragment ComposeFragment.
     */
    public static EntryFormFragment newInstance(int frameID, String type) {
        EntryFormFragment fragment = new EntryFormFragment();
        Bundle args = new Bundle();

        args.putString(ARG_TYPE, type);
        args.putInt(ARG_ID, frameID);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // sets entry state depending on instance variable
            entryState = getArguments().getString(ARG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentEntryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newUserAvatar = ChangeAvatarFragment.newInstance(false);

        usernameEditText = binding.username;
        passwordEditText = binding.password;
        loginButton = binding.login;
        signUpButton = binding.signup;
        loadingProgressBar = binding.loading;


        Log.i(TAG, entryState);
        // changes the action button based on entry state
        if (entryState.equals("Signup")) {
            signUpButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
        } else if (entryState.equals("Login")) {
            signUpButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }

        signUpButton.setOnClickListener(v ->
                signUpUser(usernameEditText.getText().toString(), passwordEditText.getText().toString()));

        loginButton.setOnClickListener(v ->
                loginUser(usernameEditText.getText().toString(), passwordEditText.getText().toString()));
    }

    // logs in the user using Parse
    private void loginUser(String username, String password) {

        if (isInputValid(username, password)) {

            loadingProgressBar.setVisibility(View.VISIBLE);
            ParseUser.logInInBackground(username, password, (user, e) -> {

                if (e != null) {
                    // if the username or password is wrong
                    if (Objects.equals(e.getMessage(), INVALID_ENTRY_DATA_ERROR)) {
                        showSignUpState(R.string.error_invalid_credentials);
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                    Log.e(TAG, "Issue with login " + e.getMessage());
                    return;
                }

                assignUser();
                ((EntryActivity) requireActivity()).navigateToMapView();
            });

        }
    }


    private void signUpUser(String username, String password) {

        // if username or password is empty show error meessage
        if (isInputValid(username, password)) {
            // create a new parse user
            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setPassword(password);

            loadingProgressBar.setVisibility(View.VISIBLE);

            // signs the user up in background
            user.signUpInBackground(e -> {

                if (e == null) {

                    assignUser();
                    // goes to change avatar
                    if (getArguments() != null) {
                        HelperClass.replaceFragment(requireActivity().getSupportFragmentManager(),
                                getArguments().getInt(ARG_ID), newUserAvatar, ChangeAvatarFragment.TAG, true);
                    }
                    
                } else {

                    // Sign up didn't succeed. stay on page and show message
                    showSignUpState(R.string.sign_up_failed);
                    Log.e(TAG, "Issue with signup", e);
                }
            });
        }
    }

    private void assignUser() {
        ParseInstallation.getCurrentInstallation().put("userID", ParseUser.getCurrentUser());
    }

    // returns if the entered values are valid
    private boolean isInputValid(String username, String password) {

        if (username.isEmpty() && password.isEmpty()) {
            showSignUpState(R.string.empty_fields);
            return false;
        } else if (username.isEmpty()) {
            showSignUpState(R.string.invalid_username);
            return false;
        } else if (password.isEmpty()) {
            showSignUpState(R.string.invalid_password);
            return false;
        }

        return true;
    }

    // shows the sign up state
    private void showSignUpState(@StringRes Integer stateString) {
        Snackbar stateText = Snackbar.make(requireView(), stateString, Snackbar.LENGTH_SHORT);
        HelperClass.displaySnackBarWithBottomMargin(stateText, 400, getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}