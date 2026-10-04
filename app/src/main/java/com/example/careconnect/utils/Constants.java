package com.example.careconnect.utils;

public final class Constants {

    private Constants() {}

    // Firestore Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_DONORS = "donors";
    public static final String COLLECTION_HOSPITALS = "hospitals";

    // Intent Extra Keys
    public static final String EXTRA_DONOR_ID = "extra_donor_id";
    public static final String EXTRA_HOSPITAL_ID = "extra_hospital_id";
    public static final String EXTRA_BLOOD_GROUP = "extra_blood_group";
    public static final String EXTRA_CITY = "extra_city";

    // SharedPreferences Keys (Eco Mode & Settings)
    public static final String PREF_NAME = "careconnect_prefs";
    public static final String KEY_ECO_MODE = "key_eco_mode";
    public static final String KEY_DATA_SAVER = "key_data_saver";
    public static final String KEY_OLED_DARK = "key_oled_dark";
    public static final String KEY_BATTERY_THROTTLE = "key_battery_throttle";

    // Standard Blood Groups
    public static final String[] BLOOD_GROUPS = {
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    // Blood Groups for Search Filter
    public static final String[] BLOOD_GROUPS_FILTER = {
            "All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    // Common Cities
    public static final String[] SAMPLE_CITIES = {
            "All Cities", "Mumbai", "Delhi", "Bengaluru", "Hyderabad",
            "Chennai", "Kolkata", "Pune", "Ahmedabad", "Jaipur"
    };

    public static final String DISCLAIMER_HOSPITAL =
            "Bed availability depends on the latest available hospital update. " +
                    "Please contact the hospital directly to confirm availability.";
}