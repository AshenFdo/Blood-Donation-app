package com.ashen.bdonorapp.Adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashen.bdonorapp.Models.BloodRequest;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.RequestModule.OwnerReqDetailsActivity;
import com.ashen.bdonorapp.RequestModule.RequestDetailsActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class BloodRequestAdapter extends FirestoreRecyclerAdapter<BloodRequest, BloodRequestAdapter.RequestViewHolder> {

    public BloodRequestAdapter(@NonNull FirestoreRecyclerOptions<BloodRequest> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull BloodRequest model) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        try{
            // Check if the current user is the owner of the request
            if(model.getPostedByUserId().equals(currentUserId)){
                holder.bloodTypeTextView.setText(model.getBloodType());
                holder.userNameTextView.setText(model.getUserName());
                holder.userCityTextView.setText(model.getUserCity());
                holder.urgentTypeTextView.setText(model.getUrgentType());
                holder.checkOwner.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener(v -> {
                    // Open OwnerReqDetailsActivity
                    Intent intent = new Intent(holder.itemView.getContext(), OwnerReqDetailsActivity.class);
                    intent.putExtra("requestId", getSnapshots().getSnapshot(position).getId());
                    holder.itemView.getContext().startActivity(intent);
                });
            }else {
                // For other users, open RequestDetailsActivity
                holder.bloodTypeTextView.setText(model.getBloodType());
                holder.userNameTextView.setText(model.getUserName());
                holder.userCityTextView.setText(model.getUserCity());
                holder.urgentTypeTextView.setText(model.getUrgentType());
                holder.checkOwner.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(holder.itemView.getContext(), RequestDetailsActivity.class);
                    intent.putExtra("requestId", getSnapshots().getSnapshot(position).getId());
                    holder.itemView.getContext().startActivity(intent);
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if(getItemCount() == 0){
            // Handle empty state if needed
            Log.d("BloodRequest", "No requests available.");
        }else {
            Log.d("BloodRequest", "Requests available: " + getItemCount());
        }
    }




    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request_card, parent, false);
        return new RequestViewHolder(view);
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView bloodTypeTextView, userNameTextView, userCityTextView, urgentTypeTextView, checkOwner;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodTypeTextView = itemView.findViewById(R.id.text_view_card_blood_type);
            userNameTextView = itemView.findViewById(R.id.text_view_card_user_name);
            userCityTextView = itemView.findViewById(R.id.text_view_card_user_city);
            urgentTypeTextView = itemView.findViewById(R.id.text_view_urgentType);
            checkOwner = itemView.findViewById(R.id.text_view_card_owner_label);
        }
    }
}