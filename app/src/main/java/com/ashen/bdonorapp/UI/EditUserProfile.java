package com.ashen.bdonorapp.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ashen.bdonorapp.Controller.OnUserDataLoadedListener;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.Controller.UserDataManager;
import com.google.android.material.button.MaterialButton;

public class EditUserProfile extends AppCompatActivity {
    TextView editUsername, editEmail, editBloodType,editCity;

    private AppCompatSpinner bloodTypeSpinner;
    String username, email, bloodType, city;
    ProgressBar progressBar;
    private AutoCompleteTextView autoCompleteBloodType;
    private UserDataManager userDataManager;

    MaterialButton btn_changeProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        editUsername = findViewById(R.id.userName);
        editEmail = findViewById(R.id.email);
        editCity = findViewById(R.id.city);
        editBloodType = findViewById(R.id.bloodType);
        btn_changeProfile = findViewById(R.id.button_changeProfile);
        progressBar = findViewById(R.id.progressBar);
        bloodTypeSpinner = findViewById(R.id.spinner_blood_type);

        userDataManager = new UserDataManager();

        // Populate Spinners
        ArrayAdapter<CharSequence> bloodTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_types, android.R.layout.simple_spinner_item);
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(bloodTypeAdapter);


        loadUserData();

        btn_changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        userDataManager.fetchUserData(new OnUserDataLoadedListener() {
            @Override
            public void onUserDataLoaded(String userName, String userEmail, String bloodType,String city) {
                progressBar.setVisibility(View.GONE);
                if (userName != null) editUsername.setText(userName);
                if (userEmail != null) editEmail.setText(userEmail);
                if (bloodType != null) {
                    ArrayAdapter adapter = (ArrayAdapter) bloodTypeSpinner.getAdapter();
                    int position = adapter.getPosition(bloodType);
                    if (position >= 0) bloodTypeSpinner.setSelection(position);
                }                if (city != null) editCity.setText(city);
            }

            @Override
            public void onUserDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditUserProfile.this, "Failed to load data: " + errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateProfile() {
        String name = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String bloodType = bloodTypeSpinner.getSelectedItem().toString().trim();
        String city = editCity.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btn_changeProfile.setVisibility(View.GONE);


        userDataManager.updateUserProfile(name, email, bloodType, city,
                new OnUserDataLoadedListener() {
                    @Override
                    public void onUserDataLoaded(String userName, String userEmail, String bloodType, String city) {
                        btn_changeProfile.setVisibility(View.VISIBLE); // Show button again
                        Toast.makeText(EditUserProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Return to previous screen
                    }

                    @Override
                    public void onUserDataLoadFailed(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        btn_changeProfile.setVisibility(View.VISIBLE); // Show button again
                        Toast.makeText(EditUserProfile.this, "Update failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onBackPressed(View view) {
       finish();
    }
}