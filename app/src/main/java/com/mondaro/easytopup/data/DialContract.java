package com.mondaro.easytopup.data;

import android.provider.BaseColumns;

/** Schema of the local quick-dial / history table. */
public final class DialContract {

    private DialContract() {
    }

    public static final class ContactDial implements BaseColumns {

        public static final String TABLE_NAME = "quickdial";
        public static final String _ID = "_ID";
        public static final String COLS_CARRIER = "carrier";
        public static final String COLS_PHONE = "phone";
        public static final String COLS_AMOUNT = "amount";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLS_CARRIER + " INTEGER, "
                + COLS_PHONE + " TEXT, "
                + COLS_AMOUNT + " INTEGER)";

        /**
         * Quick-dial lookups and the duplicate check both filter on phone; without
         * this index every top-up did a full table scan.
         */
        public static final String CREATE_PHONE_INDEX =
                "CREATE INDEX IF NOT EXISTS idx_quickdial_phone ON "
                        + TABLE_NAME + " (" + COLS_PHONE + ")";

        private ContactDial() {
        }
    }
}
