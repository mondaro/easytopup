package com.mondaro.easytopup.data;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Carrier ids. The numeric values are persisted in the {@code quickdial} table
 * and in the preferences file, so they must never be renumbered.
 */
public final class Carrier {

    public static final int NONE = 0;
    public static final int AIS = 1;
    public static final int DTAC = 2;
    public static final int TRUE = 3;
    public static final int CAT = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, AIS, DTAC, TRUE, CAT})
    public @interface Id {
    }

    private Carrier() {
    }

    public static String displayName(@Id int carrier) {
        switch (carrier) {
            case AIS:
                return "AIS";
            case DTAC:
                return "DTAC";
            case TRUE:
                return "TRUE";
            case CAT:
                return "CAT";
            default:
                return "";
        }
    }
}
