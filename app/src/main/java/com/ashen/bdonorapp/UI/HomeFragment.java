package com.ashen.bdonorapp.UI;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ashen.bdonorapp.Models.BloodRequest;
import com.ashen.bdonorapp.Controller.OnUserDataLoadedListener;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.Controller.RequestDataManager;
import com.ashen.bdonorapp.Controller.UserDataManager;
import com.ashen.bdonorapp.RequestModule.RequestCardActivity;
import com.ashen.bdonorapp.RequestModule.RequestDetailsActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment {

    TextView textView_userName, textView_BloodType;
    String currentUserName, currentUserBloodType;
    private RecyclerView recyclerView;
    private TextView emptyRequestsTextView;
    private static FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    UserDataManager userDataManager;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setReenterTransition(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        userDataManager = new UserDataManager();
        fetchAndDisplayUserData();


    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        textView_userName = view.findViewById(R.id.userName);
        textView_BloodType = view.findViewById(R.id.profile_BloodType);
        recyclerView = view.findViewById(R.id.recycler_view_requests);
        emptyRequestsTextView = view.findViewById(R.id.text_view_empty_requests);
        db = FirebaseFirestore.getInstance();
//



        // Fetch and display user data
        fetchAndDisplayUserData();
        setupRecyclerView();


        return  view;

    }
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        fetchAndDisplayUserData();// Start listening for data changes
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening(); // Stop listening for data changes
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged(); // Refresh the adapter to show any new data
           fetchAndDisplayUserData();
        }
    }




    private void fetchAndDisplayUserData() {
        userDataManager.fetchUserData(new OnUserDataLoadedListener() {
            @Override
            public void onUserDataLoaded(String userName, String userEmail, String bloodType, String city) {
                // This method runs when the data is successfully fetched

                // 1. Store the values in the Activity's global variables
                currentUserName = userName;
                currentUserBloodType = bloodType;

                // Now you can use these variables throughout this Activity
                Log.d(TAG, "User data loaded!");
                Log.d(TAG, "Name: " + currentUserName);
                Log.d(TAG, "Blood Type: " + currentUserBloodType);

                // 2. Optionally, update UI elements with the data
                if (textView_userName != null) {
                    textView_userName.setText(currentUserName);
                }
                if (textView_BloodType != null) {
                    textView_BloodType.setText(currentUserBloodType);
                }
            }

            @Override
            public void onUserDataLoadFailed(String errorMessage) {
                // This method runs if there was an error or no user signed in
                Log.e(TAG, "Failed to load user data: " + errorMessage);

                Toast.makeText(getActivity(), "Failed to load user data: " + errorMessage, Toast.LENGTH_SHORT).show();
                // Handle the error, e.g., show a message to the user, redirect to login
                if (textView_userName != null) textView_userName.setText("User");
                if (textView_BloodType != null) textView_BloodType.setText("");
            }
        });
    }

    private void setupRecyclerView() {


        RequestDataManager requestManager = new RequestDataManager();
        Query query = requestManager.getActiveRequestsQuery();

        FirestoreRecyclerOptions<BloodRequest> options = new FirestoreRecyclerOptions.Builder<BloodRequest>()
                .setQuery(query, BloodRequest.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<BloodRequest, RequestCardActivity.BloodRequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(RequestCardActivity.BloodRequestViewHolder holder, int position, @NonNull BloodRequest model) {
                holder.bloodTypeTextView.setText(model.getBloodType());
                holder.userNameTextView.setText(model.getUserName());
                holder.userCityTextView.setText(model.getUserCity());

                // Handle card click
                holder.itemView.setOnClickListener(v -> {
                    // When a card is clicked, pass all details to a new activity
                    Intent intent = new Intent(requireContext(), RequestDetailsActivity.class);
                    intent.putExtra("bloodType", model.getBloodType());
                    intent.putExtra("userName", model.getUserName());
                    intent.putExtra("userCity", model.getUserCity());
                    intent.putExtra("description", model.getDescription());
                    intent.putExtra("urgentType", model.getUrgentType());
                    intent.putExtra("requestId", getSnapshots().getSnapshot(position).getId()); // Get Firestore document ID
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public RequestCardActivity.BloodRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request_card, parent, false);
                return new RequestCardActivity.BloodRequestViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                // Called when there is a new query snapshot (data changes)
                if (getItemCount() == 0) {
                    emptyRequestsTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyRequestsTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public int getItemCount() {
                // This method is primarily for internal use by the adapter.
                // The onDataChanged() method handles visibility based on actual data.
                return super.getItemCount();
            }


        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

    }
}