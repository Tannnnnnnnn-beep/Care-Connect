package com.example.careconnect.utils;

import android.text.TextUtils;
import android.util.Patterns;

public final class ValidationUtils {

    private ValidationUtils() {
        // Prevent instantiation
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.trim().length() >= 6;
    }

    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 13;
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
