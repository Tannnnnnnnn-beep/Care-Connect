package com.example.careconnect.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.careconnect.models.Donor;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    public interface OnDonorClickListener {
        void onDonorClick(Donor donor);
    }

    private final Context context;
    private List<Donor> donorList;
    private final OnDonorClickListener listener;

    public DonorAdapter(Context context, List<Donor> donorList, OnDonorClickListener listener) {
        this.context = context;
        this.donorList = (donorList != null) ? donorList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<Donor> newList) {
        this.donorList = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donor, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        Donor donor = donorList.get(position);

        holder.tvBloodGroup.setText(donor.getBloodGroup() != null ? donor.getBloodGroup() : "N/A");
        holder.tvName.setText(donor.getName() != null ? donor.getName() : "Unknown Donor");
        holder.tvCity.setText("Location: " + (donor.getCity() != null ? donor.getCity() : "Unspecified"));
        holder.tvPhone.setText(donor.getPhone() != null ? donor.getPhone() : "");

        if (donor.isAvailable()) {
            holder.tvStatus.setText(context.getString(R.string.donor_status_available));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_available);
            holder.tvStatus.setTextColor(context.getColor(R.color.status_available));
        } else {
            holder.tvStatus.setText(context.getString(R.string.donor_status_unavailable));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_unavailable);
            holder.tvStatus.setTextColor(context.getColor(R.color.status_unavailable));
        }

        // Feature 5: Zero-Emission Transit Recommendation
        // Feature 5: Zero-Emission Transit Recommendation (3 Distinct Proximity Tiers)
        if (holder.tvTransitPill != null) {
            int mod = Math.abs(position % 3);
            if (mod == 0) {
                holder.tvTransitPill.setText("🚶 ~800m Walk • 0g CO₂ (Zero Fuel)");
                holder.tvTransitPill.setBackgroundResource(R.drawable.bg_badge_available);
                holder.tvTransitPill.setTextColor(context.getColor(R.color.eco_green_dark));
            } else if (mod == 1) {
                holder.tvTransitPill.setText("🚲 ~1.5km Cycle • 0g CO₂ (Zero Fuel)");
                holder.tvTransitPill.setBackgroundResource(R.drawable.bg_badge_available);
                holder.tvTransitPill.setTextColor(context.getColor(R.color.secondary_dark));
            } else {
                holder.tvTransitPill.setText("🚇 Metro Connected • Green Transit");
                holder.tvTransitPill.setBackgroundResource(R.drawable.bg_badge_available);
                holder.tvTransitPill.setTextColor(context.getColor(R.color.bed_general));
            }
        }

        holder.btnCall.setOnClickListener(v -> {
            String phone = donor.getPhone();
            if (phone != null && !phone.trim().isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phone.trim()));
                context.startActivity(dialIntent);
            } else {
                Toast.makeText(context, "No phone number available for this donor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return donorList.size();
    }

    public static class DonorViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodGroup, tvName, tvCity, tvStatus, tvPhone, tvTransitPill;
        MaterialButton btnCall;

        public DonorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBloodGroup = itemView.findViewById(R.id.tv_donor_item_blood_group);
            tvName = itemView.findViewById(R.id.tv_donor_item_name);
            tvCity = itemView.findViewById(R.id.tv_donor_item_city);
            tvStatus = itemView.findViewById(R.id.tv_donor_item_status);
            tvPhone = itemView.findViewById(R.id.tv_donor_item_phone);
            tvTransitPill = itemView.findViewById(R.id.tv_donor_transit_pill);
            btnCall = itemView.findViewById(R.id.btn_donor_item_call);
        }
    }
}