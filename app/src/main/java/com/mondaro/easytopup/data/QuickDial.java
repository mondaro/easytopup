package com.mondaro.easytopup.data;

import androidx.annotation.NonNull;

/** One suggestion row shown above the keypad. */
public final class QuickDial {

    public final String phone;
    @Carrier.Id
    public final int carrier;
    public final int amount;

    public QuickDial(@NonNull String phone, @Carrier.Id int carrier, int amount) {
        this.phone = phone;
        this.carrier = carrier;
        this.amount = amount;
    }
}
