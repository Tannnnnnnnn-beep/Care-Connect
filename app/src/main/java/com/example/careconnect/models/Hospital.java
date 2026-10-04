package com.example.careconnect.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class Hospital {
    private String hospitalId;
    private String name;
    private String address;
    private String city;
    private String phone;
    private int generalBeds;
    private int icuBeds;
    private int emergencyBeds;
    private boolean active = true;
    private long lastUpdated;
    private String adminEmail;

    // Required default constructor for Firestore deserialization
    public Hospital() {
    }

    public Hospital(String hospitalId, String name, String address, String city, String phone,
                    int generalBeds, int icuBeds, int emergencyBeds, boolean active, long lastUpdated) {
        this.hospitalId = hospitalId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.generalBeds = generalBeds;
        this.icuBeds = icuBeds;
        this.emergencyBeds = emergencyBeds;
        this.active = active;
        this.lastUpdated = lastUpdated;
    }

    public Hospital(String hospitalId, String name, String address, String city, String phone,
                    int generalBeds, int icuBeds, int emergencyBeds, boolean active, long lastUpdated, String adminEmail) {
        this(hospitalId, name, address, city, phone, generalBeds, icuBeds, emergencyBeds, active, lastUpdated);
        this.adminEmail = adminEmail;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Excluded from Firestore reflection to prevent duplicate property conflicts
    @Exclude
    public String getContactPhone() {
        return phone;
    }

    public int getGeneralBeds() {
        return generalBeds;
    }

    public void setGeneralBeds(int generalBeds) {
        this.generalBeds = generalBeds;
    }

    public int getIcuBeds() {
        return icuBeds;
    }

    public void setIcuBeds(int icuBeds) {
        this.icuBeds = icuBeds;
    }

    public int getEmergencyBeds() {
        return emergencyBeds;
    }

    public void setEmergencyBeds(int emergencyBeds) {
        this.emergencyBeds = emergencyBeds;
    }

    @PropertyName("active")
    public boolean isActive() {
        return active;
    }

    @PropertyName("active")
    public void setActive(boolean active) {
        this.active = active;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
}