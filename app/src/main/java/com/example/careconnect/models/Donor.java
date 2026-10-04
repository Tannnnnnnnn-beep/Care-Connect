package com.example.careconnect.models;

public class Donor {
    private String donorId;
    private String userId;
    private String name;
    private String bloodGroup;
    private String phone;
    private String city;
    private boolean isAvailable;
    private long lastUpdated;

    // Required default constructor for Firestore deserialization
    public Donor() {
    }

    public Donor(String donorId, String userId, String name, String bloodGroup, String phone, String city, boolean isAvailable, long lastUpdated) {
        this.donorId = donorId;
        this.userId = userId;
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.city = city;
        this.isAvailable = isAvailable;
        this.lastUpdated = lastUpdated;
    }

    public Donor(String donorId, String name, String bloodGroup, String phone, String city, boolean isAvailable, long lastUpdated) {
        this(donorId, donorId, name, bloodGroup, phone, city, isAvailable, lastUpdated);
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
