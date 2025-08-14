package com.ashen.bdonorapp.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashen.bdonorapp.Models.BloodRequest;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.RequestModule.RequestDetailsActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class BloodRequestAdapter extends FirestoreRecyclerAdapter<BloodRequest, BloodRequestAdapter.RequestViewHolder> {

    public BloodRequestAdapter(@NonNull FirestoreRecyclerOptions<BloodRequest> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull BloodRequest model) {
        holder.bloodTypeTextView.setText(model.getBloodType());
        holder.userNameTextView.setText(model.getUserName());
        holder.userCityTextView.setText(model.getUserCity());
        holder.urgentTypeTextView.setText(model.getUrgentType());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), RequestDetailsActivity.class);
            intent.putExtra("requestId", getSnapshots().getSnapshot(position).getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request_card, parent, false);
        return new RequestViewHolder(view);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView bloodTypeTextView, userNameTextView, userCityTextView, urgentTypeTextView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodTypeTextView = itemView.findViewById(R.id.text_view_card_blood_type);
            userNameTextView = itemView.findViewById(R.id.text_view_card_user_name);
            userCityTextView = itemView.findViewById(R.id.text_view_card_user_city);
            urgentTypeTextView = itemView.findViewById(R.id.text_view_urgentType);
        }
    }
}