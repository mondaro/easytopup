package com.mondaro.easytopup.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

/**
 * One helper instance per process. {@link SQLiteOpenHelper} already keeps a
 * single shared connection, so creating a new helper per query - as the previous
 * version did - opened and leaked a connection on every keystroke.
 */
public final class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "easytopup";

    private static volatile DBHelper instance;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @NonNull
    public static DBHelper getInstance(@NonNull Context context) {
        DBHelper local = instance;
        if (local == null) {
            synchronized (DBHelper.class) {
                local = instance;
                if (local == null) {
                    local = new DBHelper(context.getApplicationContext());
                    instance = local;
                }
            }
        }
        return local;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DialContract.ContactDial.CREATE_TABLE);
        db.execSQL(DialContract.ContactDial.CREATE_PHONE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Never drop the table: it holds the user's own dialling history.
        if (oldVersion < 2) {
            db.execSQL(DialContract.ContactDial.CREATE_TABLE);
            db.execSQL(DialContract.ContactDial.CREATE_PHONE_INDEX);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Tolerate a downgrade rather than crashing on the default implementation.
    }
}
