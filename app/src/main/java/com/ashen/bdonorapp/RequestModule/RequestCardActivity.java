package com.ashen.bdonorapp.RequestModule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ashen.bdonorapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ashen.bdonorapp.Models.BloodRequest; // Import your BloodRequest model

public class RequestCardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyRequestsTextView;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_card);

        recyclerView = findViewById(R.id.recycler_view_requests);
        emptyRequestsTextView = findViewById(R.id.text_view_empty_requests);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Query to get all blood requests, ordered by creation time (you might add a timestamp field in BloodRequest)
        Query query = db.collection("bloodRequests").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<BloodRequest> options = new FirestoreRecyclerOptions.Builder<BloodRequest>()
                .setQuery(query, BloodRequest.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<BloodRequest, BloodRequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(BloodRequestViewHolder holder, int position, @NonNull BloodRequest model) {
                holder.bloodTypeTextView.setText(model.getBloodType());
                holder.userNameTextView.setText(model.getUserName());
                holder.userCityTextView.setText(model.getUserCity());
                holder.userUrgentTextView.setText(model.getUrgentType());

                // Handle card click
                holder.itemView.setOnClickListener(v -> {
                    // When a card is clicked, pass all details to a new activity
                    Intent intent = new Intent(RequestCardActivity.this, RequestDetailsActivity.class);
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
            public BloodRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request_card, parent, false);
                return new BloodRequestViewHolder(view);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // ViewHolder for the RecyclerView
    public static class BloodRequestViewHolder extends RecyclerView.ViewHolder {
        public TextView bloodTypeTextView;
        public TextView userNameTextView;
        public TextView userCityTextView;
        public TextView userUrgentTextView;

        public BloodRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodTypeTextView = itemView.findViewById(R.id.text_view_card_blood_type);
            userNameTextView = itemView.findViewById(R.id.text_view_card_user_name);
            userCityTextView = itemView.findViewById(R.id.text_view_card_user_city);
            userUrgentTextView = itemView.findViewById(R.id.text_view_urgentType);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening(); // Start listening for data changes
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening(); // Stop listening when the activity is not in foreground
    }
}