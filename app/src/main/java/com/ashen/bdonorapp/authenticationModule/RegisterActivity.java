package com.ashen.bdonorapp.authenticationModule;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editEmail,editUserName,editPassword,editConfirmPassword;
    private String email, userName, password, confirmPassword, bloodType ,encodedImage , gender;
    private MaterialButton registerButton;
    ProgressBar progressBar;

    private ShapeableImageView profile_image;

    private AutoCompleteTextView autoCompleteBloodType , autoCompleteGender;

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

        // Initialize UI elements
        editEmail = findViewById(R.id.email);
        editUserName = findViewById(R.id.userName);
        editPassword = findViewById(R.id.password);
        editConfirmPassword = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.button_register);
        autoCompleteBloodType = findViewById(R.id.autoComplete_blood_type);
        autoCompleteGender = findViewById(R.id.autoComplete_gender);
        progressBar = findViewById(R.id.progressBar);
        profile_image = findViewById(R.id.profile_image);


        // --- Handle profile image selection for select a pic from device---
        profile_image.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            picImage.launch(intent);
        });





        // --- Setup the dropdown for blood types ---
        String[] bloodTypes = getResources().getStringArray(R.array.blood_types); // You'll need to create this in strings.xml
        ArrayAdapter<String> bTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodTypes);
        autoCompleteBloodType.setAdapter(bTypeAdapter);

        // --- Setup the dropdown for gender ---
        String[] genders = getResources().getStringArray(R.array.gender);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        autoCompleteGender.setAdapter(genderAdapter);


        // register button click listener ---
        registerButton.setOnClickListener(v -> {
            registerUser();
        });



    }
    private void registerUser() {

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.GONE);


        // --- Get and validate user input ---
        userName = Objects.requireNonNull(editUserName.getText()).toString().trim();
        email = Objects.requireNonNull(editEmail.getText()).toString().trim();
        bloodType = autoCompleteBloodType.getText().toString().trim();
        gender = autoCompleteGender.getText().toString().trim();
        password = Objects.requireNonNull(editPassword.getText()).toString().trim();
        confirmPassword = Objects.requireNonNull(editConfirmPassword.getText()).toString().trim();

        // Input Validations
        //Check null values
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(bloodType)|| TextUtils.isEmpty(gender) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            return;
        }

        //Validate password length and match
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

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Invalid email format");
            progressBar.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            return;
        }
        if (encodedImage == null || encodedImage.isEmpty()) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            return;
        }


        // --- Create user with Firebase Authentication ---
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
                                                /*
                                                * Save the user data to the firestore
                                                */
                                                saveUserDataToFirestore(user, userName, email, bloodType,encodedImage);

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


    // Save user data to Firestore
    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String name, String email, String bloodType, String encodedImage) {
        String userId = firebaseUser.getUid();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    // Add new User to the firestore database (including FCM token)
                    User user = new User(email, name, bloodType, userId, encodedImage,gender);

                    db.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "Registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "User data saved successfully");
                                mAuth.signOut();

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.w(TAG, "Error saving user data", e);
                                progressBar.setVisibility(View.GONE);
                                registerButton.setEnabled(true);
                            });
                });
    }

    public void onBackPressed(View view) {
        startActivity(new android.content.Intent(this, LoginActivity.class));

    }

    //Method for encode image to base64 string
    public String encodeImage(Bitmap bitmap){
       int preViewWidth  = 150;
       int preViewHeight = bitmap.getHeight() * preViewWidth / bitmap.getWidth();
       Bitmap preViewBitmap = Bitmap.createScaledBitmap(bitmap, preViewWidth, preViewHeight, false);
       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
       preViewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
       byte[] byteArray = byteArrayOutputStream.toByteArray();
       return Base64.encodeToString(byteArray , Base64.DEFAULT );
    }

    public final ActivityResultLauncher<Intent> picImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            profile_image.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);

                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "Image not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );
}

