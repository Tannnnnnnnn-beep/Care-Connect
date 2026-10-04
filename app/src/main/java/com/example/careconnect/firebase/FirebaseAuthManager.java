package com.example.careconnect.firebase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {

    private static FirebaseAuthManager instance;
    private final FirebaseAuth auth;

    private FirebaseAuthManager() {
        this.auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthManager getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    public void registerWithEmail(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public void loginWithEmail(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public void sendPasswordResetEmail(String email, OnCompleteListener<Void> listener) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(listener);
    }

    public void logout() {
        auth.signOut();
    }
}
