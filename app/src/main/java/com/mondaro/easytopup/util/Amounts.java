package com.mondaro.easytopup.util;

import androidx.annotation.Nullable;

/** Parsing and bounds for top-up amounts. */
public final class Amounts {

    public static final int MIN = 1;
    /**
     * Guards against overflow and typos, not against carrier limits - the network
     * enforces those itself, and the mPay wallet accepts larger sums than a
     * handset top-up does.
     */
    public static final int MAX = 100000;
    public static final int INVALID = -1;

    private Amounts() {
    }

    /**
     * Returns the amount in baht, or {@link #INVALID} when the text is empty, not
     * a number, or outside the accepted range. The old code called
     * {@code Integer.valueOf} straight on the field, which threw - after the USSD
     * had already been dialled.
     */
    public static int parse(@Nullable String text) {
        if (text == null) {
            return INVALID;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.length() > 6) {
            return INVALID;
        }
        int value;
        try {
            value = Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return INVALID;
        }
        if (value < MIN || value > MAX) {
            return INVALID;
        }
        return value;
    }
}
