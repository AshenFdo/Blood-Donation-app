package com.ashen.bdonorapp.UI;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ashen.bdonorapp.Adapters.BloodRequestAdapter;
import com.ashen.bdonorapp.Models.BloodRequest;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.Controller.RequestDataManager;
import com.ashen.bdonorapp.RequestModule.AddBloodRequestActivity;
import com.ashen.bdonorapp.RequestModule.RequestCardActivity;
import com.ashen.bdonorapp.RequestModule.RequestDetailsActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class RequestPageFragment extends Fragment {

    private RecyclerView recyclerView;
    private BloodRequestAdapter adapter;
    private TextView emptyRequestsTextView;
    private FirebaseFirestore db;

    private LinearLayout addNewRequest;
    private LinearLayout mapFeature;




    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_page, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_requests);
        emptyRequestsTextView = view.findViewById(R.id.text_view_empty_requests);
        addNewRequest = view.findViewById(R.id.addNewRequest);
        mapFeature = view.findViewById(R.id.mapFeature);
        db = FirebaseFirestore.getInstance();


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        setupRecyclerView();

        // Set up the click listener for the map feature
        mapFeature.setOnClickListener(v -> {
            // Start the MapActivity when the map feature is clicked
            Intent intent = new Intent(requireContext(), MapsActivity.class);
            startActivity(intent);
        });

        // Set up the click listener for the "Add New Request" button
        addNewRequest.setOnClickListener(v -> {
            // Start the AddBloodRequestActivity when the button is clicked
            Intent intent = new Intent(requireContext(), AddBloodRequestActivity.class);
            startActivity(intent);
        });

        return  view;
    }
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening(); // Start listening for data changes
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
        }
    }

    private void setupRecyclerView() {
        RequestDataManager requestManager = new RequestDataManager();
        Query query = requestManager.getActiveRequestsQuery();

        FirestoreRecyclerOptions<BloodRequest> options = new FirestoreRecyclerOptions.Builder<BloodRequest>()
                .setQuery(query, BloodRequest.class)
                .build();

        adapter = new BloodRequestAdapter(options);
        recyclerView.setAdapter(adapter);
    }




}




