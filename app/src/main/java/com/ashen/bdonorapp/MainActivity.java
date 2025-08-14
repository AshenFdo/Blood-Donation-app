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

    final Fragment homeFragment = new HomeFragment();
    final Fragment RequestPageFragment = new RequestPageFragment();
    final Fragment MessagePageFragment = new MessagePageFragment();
    final Fragment ProfilePageFragment = new ProfilePageFragment();

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
        fm.beginTransaction().add(R.id.fragment_container, MessagePageFragment, "3").hide(MessagePageFragment).commit();
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
            } else if (itemId == R.id.chat_page) {
                fm.beginTransaction().hide(active).show(MessagePageFragment).commit();
                active = MessagePageFragment;
                return true;
            } else if (itemId == R.id.profile_page) {
                fm.beginTransaction().hide(active).show(ProfilePageFragment).commit();
                active = ProfilePageFragment;
                return true;
            }
            return false;
        });
    }

    }
