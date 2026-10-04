package com.example.careconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.careconnect.adapters.DonorAdapter;
import com.example.careconnect.firebase.FirestoreManager;
import com.example.careconnect.models.Donor;
import com.example.careconnect.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DonorSearchActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private Spinner spinnerBloodGroup;
    private EditText etCity;
    private MaterialButton btnSearch, btnReset;
    private TextView tvDonorsCount;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private RecyclerView rvDonors;
    private BottomNavigationView bottomNav;

    private DonorAdapter adapter;
    private List<Donor> donorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_search);

        initViews();
        setupSpinner();
        setupRecyclerView();
        setupListeners();

        // Perform initial search to show available donors
        performSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_donors);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_donor_search);
        spinnerBloodGroup = findViewById(R.id.spinner_filter_blood_group);
        etCity = findViewById(R.id.et_filter_city);
        btnSearch = findViewById(R.id.btn_filter_search);
        btnReset = findViewById(R.id.btn_filter_reset);
        tvDonorsCount = findViewById(R.id.tv_donors_count);
        pbLoading = findViewById(R.id.pb_donors_loading);
        layoutEmpty = findViewById(R.id.layout_donors_empty);
        rvDonors = findViewById(R.id.rv_donors_list);
        bottomNav = findViewById(R.id.bottom_navigation_donors);
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Constants.BLOOD_GROUPS_FILTER
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(spinnerAdapter);
    }

    private void setupRecyclerView() {
        donorList = new ArrayList<>();
        adapter = new DonorAdapter(this, donorList, new DonorAdapter.OnDonorClickListener() {
            @Override
            public void onDonorClick(Donor donor) {
                Toast.makeText(DonorSearchActivity.this, "Donor: " + donor.getName() + " (" + donor.getBloodGroup() + ")", Toast.LENGTH_SHORT).show();
            }
        });
        rvDonors.setLayoutManager(new LinearLayoutManager(this));
        rvDonors.setAdapter(adapter);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerBloodGroup.setSelection(0); // "All"
                etCity.setText("");
                performSearch();
            }
        });

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(DonorSearchActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_donors) {
                    return true;
                } else if (id == R.id.nav_hospitals) {
                    Intent intent = new Intent(DonorSearchActivity.this, HospitalListActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(DonorSearchActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void performSearch() {
        final String selectedGroup = spinnerBloodGroup.getSelectedItem() != null ? spinnerBloodGroup.getSelectedItem().toString() : "All";
        final String cityQuery = etCity.getText() != null ? etCity.getText().toString().trim() : "";

        setLoading(true);

        FirestoreManager.getInstance().getDonors(selectedGroup, cityQuery, new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                setLoading(false);
                if (task.isSuccessful() && task.getResult() != null) {
                    donorList.clear();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        Donor donor = doc.toObject(Donor.class);
                        if (donor != null) {
                            donorList.add(donor);
                        }
                    }

                    // If collection had no records yet, seed sample donors for prototype demo
                    if (donorList.isEmpty() && selectedGroup.equalsIgnoreCase("All") && cityQuery.isEmpty()) {
                        seedSampleDonors();
                        return;
                    }

                    updateUiWithResults();
                } else {
                    Toast.makeText(DonorSearchActivity.this, "Could not fetch donors", Toast.LENGTH_SHORT).show();
                    updateUiWithResults();
                }
            }
        });
    }

    private void seedSampleDonors() {
        List<Donor> sampleList = new ArrayList<>();
        sampleList.add(new Donor("sample_01", "sample_01", "Dr. Rajesh Sharma", "O+", "+91 98201 12345", "Mumbai", true, System.currentTimeMillis()));
        sampleList.add(new Donor("sample_02", "sample_02", "Priya Nair", "A+", "+91 98450 67890", "Bengaluru", true, System.currentTimeMillis()));
        sampleList.add(new Donor("sample_03", "sample_03", "Amit Verma", "B+", "+91 98110 23456", "Delhi", true, System.currentTimeMillis()));
        sampleList.add(new Donor("sample_04", "sample_04", "Sneha Patil", "AB+", "+91 98220 34567", "Pune", true, System.currentTimeMillis()));
        sampleList.add(new Donor("sample_05", "sample_05", "Rahul Sen", "O-", "+91 98300 45678", "Kolkata", true, System.currentTimeMillis()));

        for (Donor d : sampleList) {
            FirestoreManager.getInstance().saveDonor(d, null);
        }
        donorList.addAll(sampleList);
        updateUiWithResults();
    }

    private void updateUiWithResults() {
        adapter.updateList(donorList);
        if (donorList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvDonors.setVisibility(View.GONE);
            tvDonorsCount.setText("0 Donors Available");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvDonors.setVisibility(View.VISIBLE);
            tvDonorsCount.setText("Available Donors (" + donorList.size() + ")");
        }
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!isLoading);
        btnReset.setEnabled(!isLoading);
    }
}
