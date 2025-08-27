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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

public class MapPageFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MessagePageFragment";
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
    private Button btnNavigate;
    private Geocoder geocoder;

    // Current selected destination for navigation
    private LatLng selectedLocation;
    private String selectedAddress;
    private PlacesClient placesClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        inputSearch = view.findViewById(R.id.input_search);
        btnSearch = view.findViewById(R.id.btn_search);
        btnNavigate = view.findViewById(R.id.btn_navigate);

        // Initialize Geocoder
        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        Places.initialize(requireContext(), getString(R.string.my_api_key));
        placesClient = Places.createClient(requireContext());

        // Obtain the SupportMapFragment and get notified when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup search functionality
        setupSearchFunctionality();

        // Setup navigation button
        btnNavigate.setOnClickListener(v -> navigateToSelectedLocation());
    }


    /**
     * Sets up event listeners for the search functionality.
     * Handles both search button clicks and Enter key presses in the search field.
     */
    private void setupSearchFunctionality() {

        // Handle search button click event (search location method called)
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
     * Performs location search based on user input.
     * Uses Geocoder to convert address text to coordinates, places a marker on the map,
     * and shows the navigate button if location is found.
     */
    private void searchLocation() {
        String searchQuery = inputSearch.getText().toString().trim();
        if (searchQuery.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a location to search", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            /**
             *@param addressList used to handle duplicate locations
            * @param searchQuery The location name or address to search for
            * */
            List<Address> addressList = geocoder.getFromLocationName(searchQuery, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);

                // Get latitude and longitude from the address
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                selectedLocation = latLng;

                // Get the full address line
                selectedAddress = address.getAddressLine(0);

                // Clear existing markers and add a new marker for the searched location
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(0)));

                // Move and zoom the camera to the searched location
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                btnNavigate.setVisibility(View.VISIBLE);

                Toast.makeText(requireContext(), "Location found: " + address.getAddressLine(0), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
            Toast.makeText(requireContext(), "Error searching for location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Launches Google Maps navigation to the selected location.
     * Creates an intent to open Google Maps with turn-by-turn navigation.
     * Shows error message if Google Maps app is not installed.
     */

    private void navigateToSelectedLocation() {
        if (selectedLocation == null) {
            Toast.makeText(requireContext(), "No destination selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Uri from the selected location for navigation
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + selectedLocation.latitude + "," + selectedLocation.longitude + "&mode=d");
        // Create an Intent to launch Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if Google Maps app is installed and can handle the intent
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(requireContext(), "Google Maps app is not installed", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Callback method called when the Google Map is ready for use.
     * Sets up map listeners, location permissions, and initializes the current location display.
     * @param googleMap The GoogleMap instance ready for configuration
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        // Prompt for location permission
        getLocationPermission();
        //Update the map's UI settings based on permission
        updateLocationUI();
        //Get Device Location and set the map's camera
        getDeviceLocation();


        // Handle map clicks to select locations
        map.setOnMapClickListener(latLng -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = address.getAddressLine(0);

                    selectedLocation = latLng;
                    selectedAddress = addressText;

                    map.clear();
                    map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(addressText));

                    btnNavigate.setVisibility(View.VISIBLE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error getting address: " + e.getMessage());
            }
        });

        // Handle marker clicks to select existing markers
        map.setOnMarkerClickListener(marker -> {
            selectedLocation = marker.getPosition();
            selectedAddress = marker.getTitle();
            btnNavigate.setVisibility(View.VISIBLE);
            return false;
        });
    }

    /**
     * Checks and requests location permission from the user.
     * Check the device's current permission status for fine location access.
     * Updates the locationPermissionGranted flag based on current permission status.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    /**
     * Handles the result of location permission request.
     * Updates the locationPermissionGranted flag and refreshes the location UI.
     * @param requestCode The request code passed in requestPermissions
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }


    /**
     * Updates the map's location-related UI components based on permission status.
     * Enables or disables the "My Location" button and location display on the map.
     */
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

    /**
     * Retrieves the device's last known location and centers the map on it.
     * If location permission is granted, gets the current location and moves the map camera.
     * Falls back to default location if current location is unavailable.
     */
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {

                // Get the last known location from the FusedLocationProviderClient
                //Google asynchronously fetches the device's last known location
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();

                // Add a listener to handle the result of the location request
                locationResult.addOnCompleteListener(requireActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
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