package com.mondaro.easytopup.telephony;

import androidx.annotation.NonNull;

import com.mondaro.easytopup.data.Carrier;

/**
 * Every carrier short code the app dials, in one place.
 *
 * <p>Codes were previously built inline at each call site with string
 * concatenation, which is how the refund screen ended up able to dial a stale
 * code. Building them here keeps the format auditable.
 */
public final class UssdCodes {

    private UssdCodes() {
    }

    // ---- Top-up -----------------------------------------------------------

    @NonNull
    public static String topup(@Carrier.Id int carrier,
                               @NonNull String pin,
                               @NonNull String phone,
                               int amount) {
        switch (carrier) {
            case Carrier.AIS:
                return "*123*" + pin + "*" + phone + "*" + amount + "#";
            case Carrier.DTAC:
                return "*211*1*" + phone + "*" + pin + "*" + amount + "*1#";
            case Carrier.TRUE:
                return "*666*" + phone + "*" + amount + "*" + pin + "#";
            case Carrier.CAT:
                return "*215*" + phone + "*" + amount + "#";
            default:
                throw new IllegalArgumentException("unknown carrier " + carrier);
        }
    }

    // ---- Balance ----------------------------------------------------------

    @NonNull
    public static String balance(@Carrier.Id int carrier) {
        switch (carrier) {
            case Carrier.AIS:
                return "*533#";
            case Carrier.DTAC:
                return "*211*2#";
            case Carrier.TRUE:
                return "*123#";
            case Carrier.CAT:
                return "*902#";
            default:
                throw new IllegalArgumentException("unknown carrier " + carrier);
        }
    }

    // ---- Refund -----------------------------------------------------------

    @NonNull
    public static String refundAis(@NonNull String pin, @NonNull String lastFourDigits,
                                   @NonNull String reference) {
        return "*321*" + pin + "*" + lastFourDigits + "*" + reference + "#";
    }

    @NonNull
    public static String refundDtac(@NonNull String phone, @NonNull String pin,
                                    @NonNull String amountInBaht) {
        return "*211*8*" + phone + "*" + pin + "*" + amountInBaht + "*1#";
    }

    @NonNull
    public static String refundCat(@NonNull String phone, @NonNull String reference) {
        return "*215*1*" + phone + "*" + reference + "#";
    }

    /** Short code the TRUE refund request is forwarded to by SMS. */
    public static final String TRUE_REFUND_SHORTCODE = "97322";

    // ---- mPay / add-on services -------------------------------------------

    public static final String MPAY_BALANCE = "*556#";
    public static final String MPAY_COMMISSION = "*556*9#";

    @NonNull
    public static String mpayTopupRequest(int amount, @NonNull String mpayId) {
        return "*555*86*" + amount + "*" + mpayId + "#";
    }

    @NonNull
    public static String mpayTopupConfirm(@NonNull String type) {
        return "*555*" + type + "#";
    }

    @NonNull
    public static String buyCashCard(@NonNull String pin, @NonNull String phone, String amount) {
        return "*228*" + pin + "*" + phone + "*" + amount + "#";
    }

    @NonNull
    public static String checkBill(@NonNull String mpayId, @NonNull String phone) {
        return "*556*1*" + mpayId + "*" + phone + "#";
    }

    @NonNull
    public static String payBill(@NonNull String option) {
        return "*556*1*" + option + "#";
    }

    @NonNull
    public static String transferDays(@NonNull String phone, @NonNull String days) {
        return "*140*2*" + phone + "*" + days + "#";
    }
}
