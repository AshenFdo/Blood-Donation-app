package com.ashen.bdonorapp.authenticationModule;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ashen.bdonorapp.Models.User;
import com.ashen.bdonorapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editEmail,editUserName,editPassword,editConfirmPassword;
    private String email, userName, password, confirmPassword, bloodType;
    private MaterialButton registerButton;
    ProgressBar progressBar;

    private AutoCompleteTextView autoCompleteBloodType;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize Firebase

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        editEmail = findViewById(R.id.email);
        editUserName = findViewById(R.id.userName);
        editPassword = findViewById(R.id.password);
        editConfirmPassword = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.button_register);
        autoCompleteBloodType = findViewById(R.id.autoComplete_blood_type);
        progressBar = findViewById(R.id.progressBar);





        // --- Setup the dropdown for blood types ---
        String[] bloodTypes = getResources().getStringArray(R.array.blood_types); // You'll need to create this in strings.xml
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodTypes);
        autoCompleteBloodType.setAdapter(adapter);


        // --- Step 3: Set up the register button click listener ---
        registerButton.setOnClickListener(v -> {
            registerUser();
        });



    }
    private void registerUser() {

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.GONE);


        // --- Step 4: Get and validate user input ---
        userName = Objects.requireNonNull(editUserName.getText()).toString().trim();
        email = Objects.requireNonNull(editEmail.getText()).toString().trim();
        bloodType = autoCompleteBloodType.getText().toString().trim();
        password = Objects.requireNonNull(editPassword.getText()).toString().trim();
        confirmPassword = Objects.requireNonNull(editConfirmPassword.getText()).toString().trim();

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(bloodType) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            return;
        }
        if (password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            progressBar.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            progressBar.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(v -> {
                                            if (v.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this,
                                                        "Verification email sent to " + user.getEmail() + ". Please check your inbox.",
                                                        Toast.LENGTH_LONG).show();
                                                // Only save user data after email sent
                                                saveUserDataToFirestore(user, userName, email, bloodType);
                                            } else {
                                                Toast.makeText(RegisterActivity.this,
                                                        "Failed to send verification email.",
                                                        Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                                registerButton.setVisibility(View.VISIBLE);
                                            }
                                        });
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            registerButton.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }




    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String name, String email, String bloodType) {
        String userId = firebaseUser.getUid();

        //Add new User to the firestore database
        User user = new User(email, name, bloodType,userId);

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                });
    }


    public void onBackPressed(View view) {
        startActivity(new android.content.Intent(this, LoginActivity.class));


    }
}

