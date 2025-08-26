package com.ashen.bdonorapp.UI;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ashen.bdonorapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private GoogleMap map;
    private boolean locationPermissionGranted;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;

    // UI components
    private EditText inputSearch;
    private ImageButton btnSearch;
    private Button btnFindHospitals;
    private Button btnNavigate;
    private Geocoder geocoder;

    // Current selected destination for navigation
    private LatLng selectedLocation;
    private String selectedAddress;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize UI components
        inputSearch = findViewById(R.id.input_search);
        btnSearch = findViewById(R.id.btn_search);
        btnNavigate = findViewById(R.id.btn_navigate);

        // Initialize Geocoder
        geocoder = new Geocoder(this, Locale.getDefault());

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Places.initialize(getApplicationContext(), getString(R.string.my_api_key));  // replace with your API key
        placesClient = Places.createClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Setup search functionality
        setupSearchFunctionality();



        // Setup navigation button
        btnNavigate.setOnClickListener(v -> navigateToSelectedLocation());
    }

    /**
     * Sets up the search functionality using Geocoder
     */
    private void setupSearchFunctionality() {
        // Handle search button click
        btnSearch.setOnClickListener(v -> searchLocation());

        // Handle Enter key press in search field
        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                searchLocation();
                return true;
            }
            return false;
        });
    }

    /**
     * Search for the location entered by the user
     */
    private void searchLocation() {
        String searchQuery = inputSearch.getText().toString().trim();
        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter a location to search", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<Address> addressList = geocoder.getFromLocationName(searchQuery, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Save selected location for navigation
                selectedLocation = latLng;
                selectedAddress = address.getAddressLine(0);

                // Clear previous markers
                map.clear();

                // Add marker for the searched location
                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(address.getAddressLine(0)));

                // Move camera to the searched location
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                // Show navigation button
                btnNavigate.setVisibility(View.VISIBLE);

                Toast.makeText(this, "Location found: " + address.getAddressLine(0), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
            Toast.makeText(this, "Error searching for location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Find nearby hospitals using Geocoder
     */

    /**
     * Navigate to the selected location using Google Maps
     */
    private void navigateToSelectedLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, "No destination selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a URI for the Google Maps navigation intent
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + selectedLocation.latitude + "," + selectedLocation.longitude + "&mode=d");

        // Create an Intent to launch Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps app is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        // Prompt the user for permission
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map
        updateLocationUI();

        // Get the current location of the device and set the position of the map
        getDeviceLocation();

        // Set map click listener
        map.setOnMapClickListener(latLng -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = address.getAddressLine(0);

                    // Save selected location for navigation
                    selectedLocation = latLng;
                    selectedAddress = addressText;

                    // Clear previous markers
                    map.clear();

                    // Add marker at clicked location
                    map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(addressText));

                    // Show navigation button
                    btnNavigate.setVisibility(View.VISIBLE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error getting address: " + e.getMessage());
            }
        });

        // Set marker click listener
        map.setOnMarkerClickListener(marker -> {
            selectedLocation = marker.getPosition();
            selectedAddress = marker.getTitle();
            btnNavigate.setVisibility(View.VISIBLE);
            return false; // Allow default behavior to continue
        });
    }


    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }


    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        currentLatLng, DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
}