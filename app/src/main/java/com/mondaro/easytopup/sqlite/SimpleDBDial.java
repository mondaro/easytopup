package com.mondaro.easytopup.sqlite;

import android.provider.BaseColumns;

public final class SimpleDBDial {
    private SimpleDBDial(){}

    public static class ContactDial implements BaseColumns {
        public static final String TABLE_NAME = "quickdial";
        public static final String COLS_CODE = "code";
        public static final String COLS_PHONE = "phone";
        public static final String COLS_SCORE = "score";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLS_CODE + " TEXT, " +
                COLS_PHONE + " TEXT, " +
                COLS_SCORE + " INTEGER" + ")";

        public static final String SELECT_ALL = "SELECT * " + "FROM " + ContactDial.TABLE_NAME;
    }

}
