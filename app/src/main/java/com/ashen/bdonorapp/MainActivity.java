package com.ashen.bdonorapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.ashen.bdonorapp.UI.HomeFragment;
import com.ashen.bdonorapp.UI.MapPageFragment;
import com.ashen.bdonorapp.UI.ProfilePageFragment;
import com.ashen.bdonorapp.UI.RequestPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    final Fragment homeFragment = new HomeFragment();
    final Fragment RequestPageFragment = new RequestPageFragment();
    final Fragment MapPageFragment = new MapPageFragment();
    final Fragment ProfilePageFragment = new ProfilePageFragment();

    //final Fragment testMessageFragment = new TestMessageFragment();

    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = homeFragment;

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

        // Add all fragments, but only show the home one
        fm.beginTransaction().add(R.id.fragment_container, RequestPageFragment, "4").hide(RequestPageFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, MapPageFragment, "3").hide(MapPageFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, ProfilePageFragment, "2").hide(ProfilePageFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, homeFragment, "1").commit();

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Get the ID once

            if (itemId == R.id.home_page) {
                fm.beginTransaction().hide(active).show(homeFragment).commit();
                active = homeFragment;
                return true;
            } else if (itemId == R.id.request_page) {
                fm.beginTransaction().hide(active).show(RequestPageFragment).commit();
                active = RequestPageFragment;
                return true;
            } else if (itemId == R.id.map_page) {
                fm.beginTransaction().hide(active).show(MapPageFragment).commit();
                active = MapPageFragment;
                return true;
            } else if (itemId == R.id.profile_page) {
                fm.beginTransaction().hide(active).show(ProfilePageFragment).commit();
                active = ProfilePageFragment;
                return true;
            }
            return false;
        });
    }

    private void showToast(String message) {
        // Implement your toast logic here
        // For example, you can use Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }






}
