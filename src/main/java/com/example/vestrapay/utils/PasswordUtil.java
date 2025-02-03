package com.example.vestrapay.utils;

public class PasswordUtil {
    public static boolean isValidPassword(String password) {
        // Check if the password is at least 8 characters long
        if (password.length() < 8) {
            return false;
        }

        // Check if the password contains at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check if the password contains at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Check if the password contains at least one number
        if (!password.matches(".*[0-9].*")) {
            return false;
        }

        // If all checks pass, the password is valid
        return true;
    }
}
