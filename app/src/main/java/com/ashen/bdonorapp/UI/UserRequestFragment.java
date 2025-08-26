package com.ashen.bdonorapp.UI;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashen.bdonorapp.Adapters.BloodRequestAdapter;
import com.ashen.bdonorapp.Controller.RequestDataManager;
import com.ashen.bdonorapp.Models.BloodRequest;
import com.ashen.bdonorapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;


public class UserRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter<BloodRequest, BloodRequestAdapter.RequestViewHolder> adapter;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_request, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_requests);
        mAuth = FirebaseAuth.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupRecyclerView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private void setupRecyclerView() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId != null) {
            RequestDataManager requestManager = new RequestDataManager();
            Query query = requestManager.getUserRequestsQuery(currentUserId);

            FirestoreRecyclerOptions<BloodRequest> options = new FirestoreRecyclerOptions.Builder<BloodRequest>()
                    .setQuery(query, BloodRequest.class)
                    .setLifecycleOwner(this)
                    .build();

            adapter = new BloodRequestAdapter(options);
            recyclerView.setAdapter(adapter);
        }
    }
}