package com.example.careconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.careconnect.firebase.FirebaseAuthManager;
import com.example.careconnect.firebase.FirestoreManager;
import com.example.careconnect.models.User;
import com.example.careconnect.utils.BatteryHelper;
import com.example.careconnect.utils.EcoModeManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting, tvUserSubtitle, tvEcoLabel, tvViewAllHospitals;
    // 1️⃣ Added cardBatteryBanner here:
    private MaterialCardView cardEcoStatus, cardFindBlood, cardFindHospital, cardBeDonor, cardGreenIT, cardFeaturedHospital, cardBatteryBanner;
    private BottomNavigationView bottomNav;
    private EcoModeManager ecoModeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ecoModeManager = new EcoModeManager(this);

        initViews();
        setupListeners();
        loadUserProfile();
        updateEcoStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        updateEcoStatus();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tv_main_greeting);
        tvUserSubtitle = findViewById(R.id.tv_main_user_subtitle);
        tvEcoLabel = findViewById(R.id.tv_main_eco_label);
        tvViewAllHospitals = findViewById(R.id.tv_main_view_all_hospitals);

        cardEcoStatus = findViewById(R.id.card_eco_status_badge);
        cardFindBlood = findViewById(R.id.card_action_find_blood);
        cardFindHospital = findViewById(R.id.card_action_find_hospital);
        cardBeDonor = findViewById(R.id.card_action_be_donor);
        cardGreenIT = findViewById(R.id.card_action_green_it);
        cardFeaturedHospital = findViewById(R.id.card_featured_hospital);

        // 2️⃣ Initialized cardBatteryBanner here:
        cardBatteryBanner = findViewById(R.id.card_battery_saver_banner);

        bottomNav = findViewById(R.id.bottom_navigation);
    }

    private void setupListeners() {
        // Quick Action 1: Find Blood
        cardFindBlood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DonorSearchActivity.class);
                startActivity(intent);
            }
        });

        // Quick Action 2: Find Hospital Beds
        cardFindHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HospitalListActivity.class);
                startActivity(intent);
            }
        });

        // Quick Action 3: Be a Donor
        cardBeDonor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DonorRegistrationActivity.class);
                startActivity(intent);
            }
        });

        // Quick Action 4: Green IT Mode
        cardGreenIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GreenITActivity.class);
                startActivity(intent);
            }
        });

        // Eco Badge Click
        cardEcoStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GreenITActivity.class);
                startActivity(intent);
            }
        });

        // View All Hospitals
        tvViewAllHospitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HospitalListActivity.class);
                startActivity(intent);
            }
        });

        // Featured Hospital
        cardFeaturedHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HospitalListActivity.class);
                startActivity(intent);
            }
        });

        // Bottom Navigation Bar
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_donors) {
                    Intent intent = new Intent(MainActivity.this, DonorSearchActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_hospitals) {
                    Intent intent = new Intent(MainActivity.this, HospitalListActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadUserProfile() {
        String uid = FirebaseAuthManager.getInstance().getCurrentUserId();
        if (uid != null) {
            FirestoreManager.getInstance().getUser(uid, new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                            tvGreeting.setText("Welcome, " + user.getName());
                            tvUserSubtitle.setText("City: " + user.getCity() + " • Blood: " + user.getBloodGroup());
                        }
                    }
                }
            });
        }
    }

    // 3️⃣ Replaced updateEcoStatus with the smart battery check:
    private void updateEcoStatus() {
        boolean isLowBattery = BatteryHelper.isLowBattery(this);
        boolean isEco = ecoModeManager.isEcoModeEnabled();

        if (cardBatteryBanner != null) {
            cardBatteryBanner.setVisibility(isLowBattery ? View.VISIBLE : View.GONE);
        }

        if (isLowBattery) {
            tvEcoLabel.setText("Battery Guard Active");
        } else if (isEco) {
            tvEcoLabel.setText("Eco Mode Active");
        } else {
            tvEcoLabel.setText("Eco Mode Off");
        }
    }
}