package com.ashen.bdonorapp.UI;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestPageFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyRequestsTextView;
    private FirebaseFirestore db;
    private static FirestoreRecyclerAdapter adapter;

    private LinearLayout addNewRequest;
    private LinearLayout mapFeature;




    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestPageFragment newInstance(String param1, String param2) {
        RequestPageFragment fragment = new RequestPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_page, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_requests);
        emptyRequestsTextView = view.findViewById(R.id.text_view_empty_requests);
        addNewRequest = view.findViewById(R.id.addNewRequest);
        mapFeature = view.findViewById(R.id.mapFeature);
        db = FirebaseFirestore.getInstance();

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


        adapter = new FirestoreRecyclerAdapter<BloodRequest, RequestCardActivity.BloodRequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(RequestCardActivity.BloodRequestViewHolder holder, int position, @NonNull BloodRequest model) {
                holder.bloodTypeTextView.setText(model.getBloodType());
                holder.userNameTextView.setText(model.getUserName());
                holder.userCityTextView.setText(model.getUserCity());
                holder.userUrgentTextView.setText(model.getUrgentType());

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




