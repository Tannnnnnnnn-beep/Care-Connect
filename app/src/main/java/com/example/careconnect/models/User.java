package com.example.careconnect.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String bloodGroup;
    private String city;
    private boolean isDonor;
    private long createdAt;

    // Required default constructor for Firestore deserialization
    public User() {
    }

    public User(String uid, String name, String email, String phone, String bloodGroup, String city, boolean isDonor, long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.isDonor = isDonor;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isDonor() {
        return isDonor;
    }

    public void setDonor(boolean donor) {
        isDonor = donor;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
