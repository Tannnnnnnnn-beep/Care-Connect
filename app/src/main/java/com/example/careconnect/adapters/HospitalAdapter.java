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
import com.example.careconnect.models.Hospital;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    public interface OnHospitalClickListener {
        void onHospitalClick(Hospital hospital);
    }

    private final Context context;
    private List<Hospital> hospitalList;
    private final OnHospitalClickListener listener;

    public HospitalAdapter(Context context, List<Hospital> hospitalList, OnHospitalClickListener listener) {
        this.context = context;
        this.hospitalList = (hospitalList != null) ? hospitalList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<Hospital> newList) {
        this.hospitalList = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hospital, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);

        holder.tvName.setText(hospital.getName() != null ? hospital.getName() : "Hospital");
        holder.tvAddress.setText(hospital.getAddress() != null ? hospital.getAddress() : "");
        holder.tvCity.setText(hospital.getCity() != null ? hospital.getCity() : "");
        holder.tvGeneralBeds.setText(String.valueOf(hospital.getGeneralBeds()));
        holder.tvIcuBeds.setText(String.valueOf(hospital.getIcuBeds()));
        holder.tvEmergencyBeds.setText(String.valueOf(hospital.getEmergencyBeds()));
        holder.tvPhone.setText(hospital.getContactPhone() != null ? hospital.getContactPhone() : "");

        // -------------------------------------------------------------------------
        // 1. Live Emergency Admissions Status (Admin Controlled) goes HERE:
        // -------------------------------------------------------------------------
        if (holder.tvEmergencyStatus != null) {
            if (hospital.isActive()) {
                holder.tvEmergencyStatus.setText("● OPEN • Accepting Emergencies");
                holder.tvEmergencyStatus.setBackgroundResource(R.drawable.bg_badge_available);
                holder.tvEmergencyStatus.setTextColor(context.getColor(R.color.status_available));
            } else {
                holder.tvEmergencyStatus.setText("■ PAUSED • Emergency Ward Full");
                holder.tvEmergencyStatus.setBackgroundResource(R.drawable.bg_badge_unavailable);
                holder.tvEmergencyStatus.setTextColor(context.getColor(R.color.status_unavailable));
            }
        }

        // Call / Contact Hospital Action
        holder.btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = hospital.getContactPhone();
                if (phone != null && !phone.trim().isEmpty()) {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + phone.trim()));
                    context.startActivity(dialIntent);
                } else {
                    Toast.makeText(context, "No contact number listed for this hospital", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Item Card Click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onHospitalClick(hospital);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    // -------------------------------------------------------------------------
    // 2. HospitalViewHolder with tvEmergencyStatus goes at the bottom HERE:
    // -------------------------------------------------------------------------
    public static class HospitalViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvCity, tvGeneralBeds, tvIcuBeds, tvEmergencyBeds, tvPhone, tvEmergencyStatus;
        MaterialButton btnContact;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_hospital_item_name);
            tvAddress = itemView.findViewById(R.id.tv_hospital_item_address);
            tvCity = itemView.findViewById(R.id.tv_hospital_item_city);
            tvGeneralBeds = itemView.findViewById(R.id.tv_hospital_item_general_beds);
            tvIcuBeds = itemView.findViewById(R.id.tv_hospital_item_icu_beds);
            tvEmergencyBeds = itemView.findViewById(R.id.tv_hospital_item_emergency_beds);
            tvPhone = itemView.findViewById(R.id.tv_hospital_item_phone);
            tvEmergencyStatus = itemView.findViewById(R.id.tv_hospital_item_emergency_status);
            btnContact = itemView.findViewById(R.id.btn_hospital_item_contact);
        }
    }
}