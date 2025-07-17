package com.ashen.bdonorapp.UI;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ashen.bdonorapp.Controller.OnUserDataLoadedListener;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.Controller.UserDataManager;
import com.ashen.bdonorapp.authenticationModule.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;


public class ProfilePageFragment extends Fragment {

    UserDataManager userDataManager;

    private TextView textView_userName;
    private TextView textView_city;
    private TextView texView_bloodType;

    MaterialButton btn_logOut;



    String currentUserName,currentUserCity,currentUserBloodType;





        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";

        // TODO: Rename and change types of parameters
        private String mParam1;
        private String mParam2;

        private ImageView editProfileButton;

        public ProfilePageFragment() {}
            // Required empty public constructo}

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfilePageFragment.
         */
        // TODO: Rename and change types and number of parameters
        public static com.ashen.bdonorapp.UI.ProfilePageFragment newInstance(String param1, String param2) {
            com.ashen.bdonorapp.UI.ProfilePageFragment fragment = new com.ashen.bdonorapp.UI.ProfilePageFragment();
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

            userDataManager = new UserDataManager();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_profile_page, container, false);

            // Initialize views
            textView_city = view.findViewById(R.id.profile_City);
            textView_userName = view.findViewById(R.id.profile_UserName);
            texView_bloodType = view.findViewById(R.id.profile_BloodType);
            editProfileButton = view.findViewById(R.id.editProfileButton);
            btn_logOut = view.findViewById(R.id.btn_logOut);

            editProfileButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditUserProfile.class);
                startActivity(intent);
            });

            // Fetch and display user data
            fetchAndDisplayUserData();

            btn_logOut.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut(); // Log out the user
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent); // Navigate to LoginActivity
                Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            });

            return view;


        }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchAndDisplayUserData();
    }
    @Override
    public void onPause() {
        super.onPause();
    }




    private void fetchAndDisplayUserData() {
        userDataManager.fetchUserData(new OnUserDataLoadedListener() {
            @Override
            public void onUserDataLoaded(String userName, String userEmail, String bloodType, String city) {
                // This method runs when the data is successfully fetched

                // 1. Store the values in the Activity's global variables
                currentUserName = userName;
                currentUserCity = city;
                currentUserBloodType = bloodType;

                // Now you can use these variables throughout this Activity
                Log.d(TAG, "User data loaded!");
                Log.d(TAG, "Name: " + currentUserName);
                Log.d(TAG, "Email: " + currentUserCity);
                Log.d(TAG, "Blood Type: " + currentUserBloodType);

                // 2. Optionally, update UI elements with the data
                if (textView_userName != null) {
                  textView_userName.setText(currentUserName);
                }
                if (textView_city != null) {
                    textView_city.setText(currentUserCity);
                }
                if (texView_bloodType != null) {
                    texView_bloodType.setText(currentUserBloodType);
                }
            }

            @Override
            public void onUserDataLoadFailed(String errorMessage) {
                // This method runs if there was an error or no user signed in
                Log.e(TAG, "Failed to load user data: " + errorMessage);

                Toast.makeText(getActivity(), "Failed to load user data: " + errorMessage, Toast.LENGTH_SHORT).show();
                // Handle the error, e.g., show a message to the user, redirect to login
                if (textView_userName != null) textView_userName.setText("User");
                if (textView_city != null) textView_city.setText("");
                if (texView_bloodType != null) texView_bloodType.setText("");
            }
        });
    }


}
