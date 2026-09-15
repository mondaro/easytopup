package com.mondaro.easytopup.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A refund parsed out of a carrier confirmation SMS.
 *
 * <p>Parsing is positional and therefore brittle - the carriers' wording is not a
 * contract. The important part is that a parse either produces a complete request
 * or returns {@code null}: the previous version swallowed the exception and left
 * the previously parsed values in place, so the confirmation dialog could show one
 * transaction while the USSD it dialled belonged to another.
 */
public final class RefundRequest {

    @Carrier.Id
    public final int carrier;
    @NonNull
    public final String phone;
    /** Amount exactly as the carrier wrote it, for display. */
    @NonNull
    public final String amountText;
    /**
     * Amount in the exact form the carrier's refund menu expects. DTAC states the
     * amount in satang in its SMS and wants baht in the USSD, so the last two
     * digits are dropped; the other carriers identify the transaction by reference
     * and do not take an amount at all.
     */
    @NonNull
    public final String amountForUssd;
    @NonNull
    public final String reference;
    /** The untouched SMS body, needed by the TRUE forward-to-shortcode flow. */
    @NonNull
    public final String rawBody;

    private RefundRequest(@Carrier.Id int carrier, @NonNull String phone,
                          @NonNull String amountText, @NonNull String amountForUssd,
                          @NonNull String reference, @NonNull String rawBody) {
        this.carrier = carrier;
        this.phone = phone;
        this.amountText = amountText;
        this.amountForUssd = amountForUssd;
        this.reference = reference;
        this.rawBody = rawBody;
    }

    /** @return the parsed request, or {@code null} when the SMS does not match. */
    @Nullable
    public static RefundRequest parse(@Carrier.Id int carrier, @NonNull String body) {
        try {
            String[] parts = body.trim().split("\\s+");
            switch (carrier) {
                case Carrier.AIS:
                    return parseAis(parts, body);
                case Carrier.DTAC:
                    return parseDtac(parts, body);
                case Carrier.TRUE:
                    return parseTrue(parts, body);
                case Carrier.CAT:
                    return parseCat(parts, body);
                default:
                    return null;
            }
        } catch (RuntimeException e) {
            // Any index or range problem means the format has moved on.
            return null;
        }
    }

    @Nullable
    private static RefundRequest parseAis(@NonNull String[] parts, @NonNull String body) {
        if (parts.length < 2 || parts[1].length() < 24 || parts[0].length() < 5) {
            return null;
        }
        String phone = parts[1].substring(parts[1].length() - 10);
        String amountText = parts[1].substring(4, parts[1].length() - 13);
        String reference = parts[0].substring(4);
        return build(Carrier.AIS, phone, amountText, "", reference, body);
    }

    @Nullable
    private static RefundRequest parseDtac(@NonNull String[] parts, @NonNull String body) {
        if (parts.length < 5 || parts[2].length() < 3 || parts[1].length() < 6
                || parts[4].length() < 10) {
            return null;
        }
        String phone = "0" + parts[2].substring(2);
        String amountText = parts[1].substring(0, parts[1].length() - 3);
        String reference = parts[4].substring(3, parts[4].length() - 6);
        String baht = satangToBaht(amountText);
        if (baht == null) {
            return null;
        }
        return build(Carrier.DTAC, phone, amountText, baht, reference, body);
    }

    @Nullable
    private static RefundRequest parseTrue(@NonNull String[] parts, @NonNull String body) {
        if (parts.length < 6) {
            return null;
        }
        String phone = parts[5];
        String amountText = parts[2] + " " + parts[3];
        String reference = parts[0];
        return build(Carrier.TRUE, phone, amountText, "", reference, body);
    }

    @Nullable
    private static RefundRequest parseCat(@NonNull String[] parts, @NonNull String body) {
        if (parts.length < 10 || parts[9].length() < 5) {
            return null;
        }
        String phone = parts[4];
        String amountText = parts[1];
        String reference = parts[9].substring(0, 5);
        return build(Carrier.CAT, phone, amountText, "", reference, body);
    }

    @Nullable
    private static RefundRequest build(@Carrier.Id int carrier, @NonNull String phone,
                                       @NonNull String amountText,
                                       @NonNull String amountForUssd,
                                       @NonNull String reference, @NonNull String body) {
        if (phone.isEmpty() || amountText.isEmpty() || reference.isEmpty()) {
            return null;
        }
        return new RefundRequest(carrier, phone, amountText, amountForUssd, reference, body);
    }

    /** DTAC writes "5000" for fifty baht. Returns {@code null} if it is not digits. */
    @Nullable
    private static String satangToBaht(@NonNull String amountText) {
        if (amountText.length() < 3) {
            return null;
        }
        for (int i = 0; i < amountText.length(); i++) {
            char c = amountText.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
        }
        return amountText.substring(0, amountText.length() - 2);
    }
}
