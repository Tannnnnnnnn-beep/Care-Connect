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
import com.example.careconnect.adapters.HospitalAdapter;
import com.example.careconnect.firebase.FirestoreManager;
import com.example.careconnect.models.Hospital;
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
import java.util.Locale;

public class HospitalListActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private Spinner spinnerCity;
    private EditText etSearchQuery;
    private MaterialButton btnSearch, btnReset;
    private TextView tvHospitalsCount;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private RecyclerView rvHospitals;
    private BottomNavigationView bottomNav;

    private HospitalAdapter adapter;
    private List<Hospital> allHospitals;
    private List<Hospital> filteredHospitals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_list);

        initViews();
        setupSpinner();
        setupRecyclerView();
        setupListeners();

        loadHospitals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_hospitals);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_hospital_list);
        spinnerCity = findViewById(R.id.spinner_hospital_city);
        etSearchQuery = findViewById(R.id.et_hospital_query);
        btnSearch = findViewById(R.id.btn_hospital_search);
        btnReset = findViewById(R.id.btn_hospital_reset);
        tvHospitalsCount = findViewById(R.id.tv_hospitals_count);
        pbLoading = findViewById(R.id.pb_hospitals_loading);
        layoutEmpty = findViewById(R.id.layout_hospitals_empty);
        rvHospitals = findViewById(R.id.rv_hospitals_list);
        bottomNav = findViewById(R.id.bottom_navigation_hospitals);
    }

    private void setupSpinner() {
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Constants.SAMPLE_CITIES
        );
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);
    }

    private void setupRecyclerView() {
        allHospitals = new ArrayList<>();
        filteredHospitals = new ArrayList<>();
        adapter = new HospitalAdapter(this, filteredHospitals, new HospitalAdapter.OnHospitalClickListener() {
            @Override
            public void onHospitalClick(Hospital hospital) {
                Toast.makeText(HospitalListActivity.this, hospital.getName() + " - " + hospital.getCity(), Toast.LENGTH_SHORT).show();
            }
        });
        rvHospitals.setLayoutManager(new LinearLayoutManager(this));
        rvHospitals.setAdapter(adapter);
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
                applyFilters();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerCity.setSelection(0); // All Cities
                etSearchQuery.setText("");
                applyFilters();
            }
        });

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(HospitalListActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_donors) {
                    Intent intent = new Intent(HospitalListActivity.this, DonorSearchActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_hospitals) {
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(HospitalListActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadHospitals() {
        setLoading(true);

        FirestoreManager.getInstance().getHospitals("All Cities", new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                setLoading(false);
                if (task.isSuccessful() && task.getResult() != null) {
                    allHospitals.clear();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        Hospital h = doc.toObject(Hospital.class);
                        if (h != null) {
                            allHospitals.add(h);
                        }
                    }

                    // If collection was empty, seed sample hospitals and display
                    if (allHospitals.isEmpty()) {
                        seedSampleHospitals();
                        return;
                    }

                    applyFilters();
                } else {
                    Toast.makeText(HospitalListActivity.this, "Could not load hospital data", Toast.LENGTH_SHORT).show();
                    applyFilters();
                }
            }
        });
    }

    private void seedSampleHospitals() {
        List<Hospital> seedList = new ArrayList<>();
        seedList.add(new Hospital("hosp_01", "City Care Multi-Specialty Hospital", "Plot 14, MG Road, Central Area", "Mumbai", "+91 98200 11223", 24, 6, 4, true, System.currentTimeMillis()));
        seedList.add(new Hospital("hosp_02", "LifeLine Emergency & Trauma Center", "Sector 5, Outer Ring Road", "Bengaluru", "+91 98450 33445", 18, 8, 5, true, System.currentTimeMillis()));
        seedList.add(new Hospital("hosp_03", "Apex Heart & Critical Care Institute", "Lane 3, Civil Lines", "Delhi", "+91 98110 55667", 32, 12, 6, true, System.currentTimeMillis()));
        seedList.add(new Hospital("hosp_04", "Sunrise Community Hospital", "Main Bazaar, Station Road", "Pune", "+91 98220 77889", 12, 3, 2, true, System.currentTimeMillis()));
        seedList.add(new Hospital("hosp_05", "Grace Memorial Healthcare", "Park Street, Near Metro Station", "Kolkata", "+91 98300 99001", 15, 4, 3, true, System.currentTimeMillis()));
        seedList.add(new Hospital("hosp_06", "MedStar Super Specialty Hospital", "Banjara Hills, Road No 10", "Hyderabad", "+91 98490 22334", 28, 10, 8, true, System.currentTimeMillis()));

        for (Hospital h : seedList) {
            FirestoreManager.getInstance().getHospitalsCollection().document(h.getHospitalId()).set(h);
        }
        allHospitals.addAll(seedList);
        applyFilters();
    }

    private void applyFilters() {
        final String selectedCity = spinnerCity.getSelectedItem() != null ? spinnerCity.getSelectedItem().toString() : "All Cities";
        final String query = etSearchQuery.getText() != null ? etSearchQuery.getText().toString().trim().toLowerCase(Locale.ROOT) : "";

        filteredHospitals.clear();

        for (Hospital h : allHospitals) {
            boolean cityMatches = selectedCity.equalsIgnoreCase("All Cities") || (h.getCity() != null && h.getCity().equalsIgnoreCase(selectedCity));
            boolean nameMatches = query.isEmpty() ||
                    (h.getName() != null && h.getName().toLowerCase(Locale.ROOT).contains(query)) ||
                    (h.getAddress() != null && h.getAddress().toLowerCase(Locale.ROOT).contains(query));

            if (cityMatches && nameMatches) {
                filteredHospitals.add(h);
            }
        }

        adapter.updateList(filteredHospitals);

        if (filteredHospitals.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvHospitals.setVisibility(View.GONE);
            tvHospitalsCount.setText("0 Hospitals Found");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvHospitals.setVisibility(View.VISIBLE);
            tvHospitalsCount.setText("Available Hospitals (" + filteredHospitals.size() + ")");
        }
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!isLoading);
        btnReset.setEnabled(!isLoading);
    }
}
