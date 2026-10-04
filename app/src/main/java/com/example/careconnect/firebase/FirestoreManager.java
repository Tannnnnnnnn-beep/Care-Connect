package com.example.careconnect.firebase;

import android.util.Log;

import com.example.careconnect.models.Donor;
import com.example.careconnect.models.Hospital;
import com.example.careconnect.models.User;
import com.example.careconnect.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {

    private static final String TAG = "FirestoreManager";
    private static FirestoreManager instance;
    private final FirebaseFirestore db;

    private FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
        try {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
            Log.d(TAG, "Firestore offline persistence enabled for Green IT.");
        } catch (Exception e) {
            Log.w(TAG, "Persistence settings may have already been configured: " + e.getMessage());
        }
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public CollectionReference getUsersCollection() {
        return db.collection(Constants.COLLECTION_USERS);
    }

    public CollectionReference getDonorsCollection() {
        return db.collection(Constants.COLLECTION_DONORS);
    }

    public CollectionReference getHospitalsCollection() {
        return db.collection(Constants.COLLECTION_HOSPITALS);
    }

    // ---------------- User Operations ----------------

    public void saveUser(User user, OnCompleteListener<Void> listener) {
        if (user == null || user.getUid() == null) return;
        getUsersCollection().document(user.getUid()).set(user).addOnCompleteListener(listener);
    }

    public void getUser(String uid, OnCompleteListener<DocumentSnapshot> listener) {
        if (uid == null) return;
        getUsersCollection().document(uid).get().addOnCompleteListener(listener);
    }

    // ---------------- Donor Operations ----------------

    public void saveDonor(Donor donor, OnCompleteListener<Void> listener) {
        if (donor == null || donor.getDonorId() == null) return;
        getDonorsCollection().document(donor.getDonorId()).set(donor).addOnCompleteListener(listener);
    }

    public void updateDonorAvailability(String donorId, boolean isAvailable, OnCompleteListener<Void> listener) {
        if (donorId == null) return;
        getDonorsCollection().document(donorId)
                .update("available", isAvailable, "lastUpdated", System.currentTimeMillis())
                .addOnCompleteListener(listener);
    }

    public void getDonors(String bloodGroup, String city, OnCompleteListener<QuerySnapshot> listener) {
        Query query = getDonorsCollection().whereEqualTo("available", true);

        if (bloodGroup != null && !bloodGroup.isEmpty() && !bloodGroup.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("bloodGroup", bloodGroup);
        }
        if (city != null && !city.isEmpty() && !city.equalsIgnoreCase("All Cities")) {
            query = query.whereEqualTo("city", city);
        }

        query.get().addOnCompleteListener(listener);
    }

    // ---------------- Hospital Operations ----------------

    public void getHospitals(String city, OnCompleteListener<QuerySnapshot> listener) {
        // Query ALL hospitals so public patients see live status (OPEN vs PAUSED)
        Query query = getHospitalsCollection();

        if (city != null && !city.isEmpty() && !city.equalsIgnoreCase("All Cities")) {
            query = query.whereEqualTo("city", city);
        }

        query.get().addOnCompleteListener(listener);
    }

    public void getHospitalById(String hospitalId, OnCompleteListener<DocumentSnapshot> listener) {
        if (hospitalId == null) return;
        getHospitalsCollection().document(hospitalId).get().addOnCompleteListener(listener);
    }

    public void saveHospital(Hospital hospital, OnCompleteListener<Void> listener) {
        if (hospital == null || hospital.getHospitalId() == null) return;
        DocumentReference docRef = getHospitalsCollection().document(hospital.getHospitalId());
        if (listener != null) {
            docRef.set(hospital, SetOptions.merge()).addOnCompleteListener(listener);
        } else {
            docRef.set(hospital, SetOptions.merge());
        }
    }

    public void updateHospitalBeds(String hospitalId, int generalBeds, int icuBeds, int emergencyBeds, boolean active, OnCompleteListener<Void> listener) {
        if (hospitalId == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("generalBeds", Math.max(0, generalBeds));
        updates.put("icuBeds", Math.max(0, icuBeds));
        updates.put("emergencyBeds", Math.max(0, emergencyBeds));
        updates.put("active", active);
        updates.put("isActive", active);
        updates.put("lastUpdated", System.currentTimeMillis());

        DocumentReference docRef = getHospitalsCollection().document(hospitalId);
        if (listener != null) {
            docRef.set(updates, SetOptions.merge()).addOnCompleteListener(listener);
        } else {
            docRef.set(updates, SetOptions.merge());
        }
    }

    public void checkIfHospital(String uid, OnCompleteListener<DocumentSnapshot> listener) {
        if (uid == null) return;
        getHospitalsCollection().document(uid).get().addOnCompleteListener(listener);
    }

    public ListenerRegistration attachHospitalListener(String hospitalId, EventListener<DocumentSnapshot> listener) {
        if (hospitalId == null) return null;
        return getHospitalsCollection().document(hospitalId).addSnapshotListener(listener);
    }

    // Preload sample hospitals for college evaluation / prototype demonstration
    public void seedInitialHospitalsIfEmpty() {
        getHospitalsCollection().limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().isEmpty()) {
                Log.d(TAG, "Hospitals collection is empty. Seeding initial data...");
                List<Hospital> seedList = new ArrayList<>();
                seedList.add(new Hospital("hosp_01", "City Care Multi-Specialty Hospital", "Plot 14, MG Road, Central Area", "Mumbai", "+91 98200 11223", 24, 6, 4, true, System.currentTimeMillis()));
                seedList.add(new Hospital("hosp_02", "LifeLine Emergency & Trauma Center", "Sector 5, Outer Ring Road", "Bengaluru", "+91 98450 33445", 18, 8, 5, true, System.currentTimeMillis()));
                seedList.add(new Hospital("hosp_03", "Apex Heart & Critical Care Institute", "Lane 3, Civil Lines", "Delhi", "+91 98110 55667", 32, 12, 6, true, System.currentTimeMillis()));
                seedList.add(new Hospital("hosp_04", "Sunrise Community Hospital", "Main Bazaar, Station Road", "Pune", "+91 98220 77889", 12, 3, 2, true, System.currentTimeMillis()));
                seedList.add(new Hospital("hosp_05", "Grace Memorial Healthcare", "Park Street, Near Metro Station", "Kolkata", "+91 98300 99001", 15, 4, 3, true, System.currentTimeMillis()));

                for (Hospital h : seedList) {
                    getHospitalsCollection().document(h.getHospitalId()).set(h);
                }
            }
        });
    }
}