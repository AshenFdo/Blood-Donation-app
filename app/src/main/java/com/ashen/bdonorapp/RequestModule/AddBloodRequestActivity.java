package com.ashen.bdonorapp.RequestModule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.ashen.bdonorapp.Controller.RequestCallback;
import com.ashen.bdonorapp.Models.BloodRequest;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.Controller.RequestDataManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddBloodRequestActivity extends AppCompatActivity {

    private static final String TAG = "AddBloodRequestActivity";

    private Spinner bloodTypeSpinner;
    private EditText descriptionEditText , titleEditText;



    private RadioButton urgent_radio_button;
    private Button submitButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_blood_request);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bloodTypeSpinner = findViewById(R.id.spinner_blood_type);
        descriptionEditText = findViewById(R.id.edit_text_description);
        urgent_radio_button = findViewById(R.id.urgent_radio_button);
        submitButton = findViewById(R.id.button_submit_request);
        titleEditText = findViewById(R.id.edit_text_title);

        // Populate Spinners
        ArrayAdapter<CharSequence> bloodTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_types, android.R.layout.simple_spinner_item);
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(bloodTypeAdapter);



        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBloodRequest();
            }
        });
    }

    private void submitBloodRequest() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to submit a request.", Toast.LENGTH_SHORT).show();
            return;
        }

        String bloodType = bloodTypeSpinner.getSelectedItem().toString();
        String description = descriptionEditText.getText().toString().trim();
        String urgentType = urgent_radio_button.isChecked() ? "Urgent" : "Normal";
        String postedByUserId = currentUser.getUid();
        String title = titleEditText.getText().toString().trim();


        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description.", Toast.LENGTH_SHORT).show();
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        // Get user data and create request
        db.collection("users").document(postedByUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String userName = task.getResult().getString("userName");
                        String userCity = task.getResult().getString("city");

                        if (userName != null && userCity != null) {
                            BloodRequest request = new BloodRequest(userName, userCity, bloodType,
                                    title, postedByUserId, description, urgentType);

                            RequestDataManager requestManager = new RequestDataManager();
                            requestManager.createBloodRequest(request, new RequestCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    Toast.makeText(AddBloodRequestActivity.this, message, Toast.LENGTH_SHORT).show();
                                    finish(); // Return to previous screen
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(AddBloodRequestActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    // Re-enable button if submission fails
                                    submitButton.setEnabled(true);
                                    submitButton.setText("Submit Blood Request");
                                }
                            });
                        } else {
                            Toast.makeText(AddBloodRequestActivity.this, "User profile incomplete", Toast.LENGTH_SHORT).show();
                            submitButton.setEnabled(true);
                            submitButton.setText("Submit Blood Request");
                        }
                    } else {
                        Toast.makeText(AddBloodRequestActivity.this, "Could not retrieve user data", Toast.LENGTH_SHORT).show();
                        submitButton.setEnabled(true);
                        submitButton.setText("Submit Blood Request");
                    }
                });
    }

}