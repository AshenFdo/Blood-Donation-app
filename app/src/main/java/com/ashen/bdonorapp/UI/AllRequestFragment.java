package com.ashen.bdonorapp.UI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class AllRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter<BloodRequest, BloodRequestAdapter.RequestViewHolder> adapter;

    private FirebaseAuth mAuth;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_request, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_requests);
      mAuth = FirebaseAuth.getInstance();
      String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        setupRecyclerView();

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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume(){
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged(); // Refresh the adapter to show any new data
            Log.d("RequestPageFragment", "Adapter notified of data set change");
        }
    }

    private void setupRecyclerView() {
        RequestDataManager requestManager = new RequestDataManager();
        Query query = requestManager.getActiveRequestsQuery();

        FirestoreRecyclerOptions<BloodRequest> options = new FirestoreRecyclerOptions.Builder<BloodRequest>()
                .setQuery(query, BloodRequest.class)
                .setLifecycleOwner(this)
                .build();

        adapter = new BloodRequestAdapter(options);
        recyclerView.setAdapter(adapter);
        Log.d("AllRequestFragment", "RecyclerView adapter set up with Firestore query");
    }




}




