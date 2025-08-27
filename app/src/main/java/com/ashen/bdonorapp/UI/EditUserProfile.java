package com.ashen.bdonorapp.UI;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.ashen.bdonorapp.Controller.OnUserDataLoadedListener;
import com.ashen.bdonorapp.Controller.OnUserDataUpdateListener;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.Controller.UserDataManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditUserProfile extends AppCompatActivity {
   private TextView editUsername, editEmail,editCity;
    private ShapeableImageView profileImageView;

    private AppCompatSpinner bloodTypeSpinner, spinner_gender;
     String  encodedImage;
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
        btn_changeProfile = findViewById(R.id.button_changeProfile);
        progressBar = findViewById(R.id.progressBar);
        bloodTypeSpinner = findViewById(R.id.spinner_blood_type);
        profileImageView = findViewById(R.id.profile_image);
        spinner_gender = findViewById(R.id.spinner_gender);




        userDataManager = new UserDataManager();

        // Populate Spinner with blood types
        ArrayAdapter<CharSequence> bloodTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_types, android.R.layout.simple_spinner_item);
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(bloodTypeAdapter);

        // Populate Spinner with genders
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,R.array.gender, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_gender.setAdapter(genderAdapter);


        // Load existing user data
        loadUserData();


        // Set up profile image click listener to select a new image
        profileImageView.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            picImage.launch(intent);
        });

        // Set up button click listener to update profile
        btn_changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

    }

    // Load user data from UserDataManager and populate UI fields
    private void loadUserData() {
        userDataManager.fetchUserData(new OnUserDataLoadedListener() {

            // This method is called when user data is successfully loaded
            @Override
            public void onUserDataLoaded(String userName, String userEmail, String bloodType,String city, String profileImageUrl,String gender) {
                if (userName != null) editUsername.setText(userName);
                if (userEmail != null) editEmail.setText(userEmail);
                if (bloodType != null) {
                    ArrayAdapter adapter = (ArrayAdapter) bloodTypeSpinner.getAdapter();
                    int position = adapter.getPosition(bloodType);
                    if (position >= 0) bloodTypeSpinner.setSelection(position);
                }
                if(gender != null) {
                    ArrayAdapter adapter = (ArrayAdapter) spinner_gender.getAdapter();
                    int position = adapter.getPosition(gender);
                    if (position >= 0) spinner_gender.setSelection(position);
                }

                if (city != null) editCity.setText(city);

                if(profileImageUrl != null){
                    byte[] bytes= Base64.decode(profileImageUrl, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    profileImageView.setImageBitmap(bitmap);
                }

            }

            @Override
            public void onUserDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditUserProfile.this, "Failed to load data: " + errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Update user profile with new data

    private void updateProfile() {
        String name = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String bloodType = bloodTypeSpinner.getSelectedItem().toString().trim();
        String city = editCity.getText().toString().trim();
        String gender = spinner_gender.getSelectedItem().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btn_changeProfile.setVisibility(View.GONE);

        //Update user data via UserDataManager
        userDataManager.updateUserProfile(name, email, bloodType, city,encodedImage, gender,new OnUserDataUpdateListener () {
                    @Override
                    public void onSuccess(String message) {
                        progressBar.setVisibility(View.GONE);
                        btn_changeProfile.setVisibility(View.VISIBLE);// Show button again
                        Log.d("EditUserProfile", "Profile updated successfully");
                        Toast.makeText(EditUserProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Return to previous screen
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        btn_changeProfile.setVisibility(View.VISIBLE); // Show button again
                        Log.e("EditUserProfile", "Update failed: " + errorMessage);
                        Toast.makeText(EditUserProfile.this, "Update failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });;
    }

    public void onBackPressed(View view) {
       finish();
    }

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
                            profileImageView.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);

                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                            Toast.makeText(EditUserProfile.this, "Image not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );
}