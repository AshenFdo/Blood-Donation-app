package com.ashen.bdonorapp.UI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class RequestPageFragment extends Fragment {

    private RecyclerView recyclerView;
    // private BloodRequestAdapter adapter;

    private FirestoreRecyclerAdapter<BloodRequest, BloodRequestAdapter.RequestViewHolder> adapter;
    private TextView emptyRequestsTextView;
    private FirebaseFirestore db;

    private LinearLayout addNewRequest;
    private LinearLayout mapFeature;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_page, container, false);


        addNewRequest = view.findViewById(R.id.addNewRequest);
        db = FirebaseFirestore.getInstance();


        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        // Set up the adapter that will provide the fragments for the ViewPager2
        viewPager.setAdapter(new ViewPagerAdapter(this));

        // Link the TabLayout with the ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("All");
                    break;
                case 1:
                    tab.setText("My requests");
                    break;
            }
        }).attach();

        // Set up the click listener for the "Add New Request" button
        addNewRequest.setOnClickListener(v -> {
            // Start the AddBloodRequestActivity when the button is clicked
            Intent intent = new Intent(requireContext(), AddBloodRequestActivity.class);
            startActivity(intent);
        });

        return  view;
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
    }

    // A private inner class to manage the fragments for each tab
    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private static final String[] TAB_TITLES = new String[]{"Friends", "Find People"};

        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Return the correct fragment based on the position
            switch (position) {
                case 0:
                    return new AllRequestFragment();
                case 1:
                    return new UserRequestFragment();
                default:
                    return new AllRequestFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


}




