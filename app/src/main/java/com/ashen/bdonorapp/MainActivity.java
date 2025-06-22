package com.ashen.bdonorapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.ashen.bdonorapp.UI.HomeFragment;
import com.ashen.bdonorapp.UI.MessagePageFragment;
import com.ashen.bdonorapp.UI.ProfilePageFragment;
import com.ashen.bdonorapp.UI.RequestPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Set the listener for item selection
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home_page) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.request_page) {
                selectedFragment = new RequestPageFragment();
            } else if (itemId == R.id.chat_page) {
                selectedFragment = new MessagePageFragment();
            } else if (itemId == R.id.profile_page) {
                selectedFragment = new ProfilePageFragment();
            }

            // If a fragment is selected, replace the container with it
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true; // Return true to display the item as the selected item
        });

        // --- Load the default fragment ---
        // This ensures that the Home page is displayed when the app starts
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.home_page);
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the contents of the container with the new fragment
        fragmentTransaction.replace(R.id.fragment_container, fragment);

        // Commit the transaction
        fragmentTransaction.commit();
    }
}