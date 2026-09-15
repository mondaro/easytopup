package com.mondaro.easytopup.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Turns whatever the contact book hands us into a dialable Thai mobile number. */
public final class PhoneNumbers {

    public static final int LENGTH = 10;

    private PhoneNumbers() {
    }

    /**
     * Strips formatting and rewrites the international forms of a Thai number
     * ({@code +66 81 234 5678}, {@code 0066...}, {@code 66...}) to the national
     * {@code 0...} form the carrier USSD menus expect.
     */
    @NonNull
    public static String normalise(@Nullable String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder digits = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') {
                digits.append(c);
            }
        }
        String value = digits.toString();
        if (value.startsWith("0066")) {
            value = value.substring(4);
        } else if (value.startsWith("66") && value.length() == LENGTH + 1) {
            value = value.substring(2);
        }
        if (!value.startsWith("0") && value.length() == LENGTH - 1) {
            value = "0" + value;
        }
        return value;
    }

    /** True for a well-formed 10 digit national number. */
    public static boolean isValid(@Nullable String normalised) {
        if (normalised == null || normalised.length() != LENGTH) {
            return false;
        }
        if (normalised.charAt(0) != '0' || normalised.charAt(1) == '0') {
            return false;
        }
        for (int i = 0; i < LENGTH; i++) {
            char c = normalised.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /** Last four digits, used by the AIS refund menu. Empty when unavailable. */
    @NonNull
    public static String lastFour(@Nullable String normalised) {
        if (normalised == null || normalised.length() < 4) {
            return "";
        }
        return normalised.substring(normalised.length() - 4);
    }
}
