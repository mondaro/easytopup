package com.mondaro.easytopup.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.mondaro.easytopup.util.PhoneNumbers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * All SQLite access for the dialling history.
 *
 * <p>Every query used to run on the main thread - on each keypad press - which is
 * what made typing a number feel sticky. Work happens on a single background
 * thread here and results are posted back to the main looper.
 */
public final class HistoryRepository {

    private static final String TAG = "HistoryRepository";
    private static final int QUICK_DIAL_LIMIT = 3;

    /** Delivered on the main thread. */
    public interface QuickDialCallback {
        void onResult(@NonNull List<QuickDial> items);
    }

    private static volatile HistoryRepository instance;

    private final DBHelper helper;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "easytopup-db");
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            });
    private final Handler main = new Handler(Looper.getMainLooper());
    /** Guards against a slow query overwriting the result of a newer one. */
    private final AtomicLong quickDialGeneration = new AtomicLong();

    private HistoryRepository(@NonNull Context context) {
        this.helper = DBHelper.getInstance(context);
    }

    @NonNull
    public static HistoryRepository get(@NonNull Context context) {
        HistoryRepository local = instance;
        if (local == null) {
            synchronized (HistoryRepository.class) {
                local = instance;
                if (local == null) {
                    local = new HistoryRepository(context.getApplicationContext());
                    instance = local;
                }
            }
        }
        return local;
    }

    /**
     * Records a completed top-up: adds to the running total for that number, or
     * inserts it when seen for the first time.
     */
    public void recordTopup(@NonNull String phone, @Carrier.Id int carrier, int amount) {
        final String normalised = PhoneNumbers.normalise(phone);
        executor.execute(() -> {
            try {
                upsert(normalised, carrier, amount);
            } catch (Exception e) {
                Log.w(TAG, "recordTopup failed", e);
            }
        });
    }

    @WorkerThread
    private void upsert(@NonNull String phone, @Carrier.Id int carrier, int amount) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            long rowId = -1L;
            int existingAmount = 0;
            // Exact match, not the LIKE prefix the old code used: "0812345678"
            // must not merge into "08123456789".
            try (Cursor cursor = db.query(
                    DialContract.ContactDial.TABLE_NAME,
                    new String[]{DialContract.ContactDial._ID, DialContract.ContactDial.COLS_AMOUNT},
                    DialContract.ContactDial.COLS_PHONE + " = ?",
                    new String[]{phone},
                    null, null, DialContract.ContactDial._ID + " DESC", "1")) {
                if (cursor.moveToFirst()) {
                    rowId = cursor.getLong(0);
                    existingAmount = cursor.isNull(1) ? 0 : cursor.getInt(1);
                }
            }

            ContentValues values = new ContentValues(3);
            values.put(DialContract.ContactDial.COLS_CARRIER, carrier);
            if (rowId >= 0) {
                values.put(DialContract.ContactDial.COLS_AMOUNT, existingAmount + amount);
                db.update(DialContract.ContactDial.TABLE_NAME, values,
                        DialContract.ContactDial._ID + " = ?",
                        new String[]{String.valueOf(rowId)});
            } else {
                values.put(DialContract.ContactDial.COLS_PHONE, phone);
                values.put(DialContract.ContactDial.COLS_AMOUNT, amount);
                db.insert(DialContract.ContactDial.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Looks up the most-used numbers starting with {@code prefix}. An empty prefix
     * returns nothing, matching the behaviour of the keypad.
     */
    public void quickDial(@NonNull String prefix, @NonNull QuickDialCallback callback) {
        final long generation = quickDialGeneration.incrementAndGet();
        if (prefix.isEmpty()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        executor.execute(() -> {
            List<QuickDial> items;
            try {
                items = queryQuickDial(prefix);
            } catch (Exception e) {
                Log.w(TAG, "quickDial failed", e);
                items = Collections.emptyList();
            }
            final List<QuickDial> result = items;
            main.post(() -> {
                if (generation == quickDialGeneration.get()) {
                    callback.onResult(result);
                }
            });
        });
    }

    @WorkerThread
    @NonNull
    private List<QuickDial> queryQuickDial(@NonNull String prefix) {
        List<QuickDial> items = new ArrayList<>(QUICK_DIAL_LIMIT);
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(
                DialContract.ContactDial.TABLE_NAME,
                new String[]{
                        DialContract.ContactDial.COLS_PHONE,
                        DialContract.ContactDial.COLS_CARRIER,
                        DialContract.ContactDial.COLS_AMOUNT},
                DialContract.ContactDial.COLS_PHONE + " LIKE ? ESCAPE '\\'",
                new String[]{escapeLike(prefix) + "%"},
                null, null,
                DialContract.ContactDial.COLS_AMOUNT + " DESC",
                String.valueOf(QUICK_DIAL_LIMIT))) {
            while (cursor.moveToNext()) {
                String phone = cursor.isNull(0) ? "" : cursor.getString(0);
                if (phone.isEmpty()) {
                    continue;
                }
                items.add(new QuickDial(
                        phone,
                        cursor.isNull(1) ? Carrier.NONE : cursor.getInt(1),
                        cursor.isNull(2) ? 0 : cursor.getInt(2)));
            }
        }
        return items;
    }

    /** Zeroes every running total - used when the calendar year rolls over. */
    public void resetAmounts() {
        executor.execute(() -> {
            try {
                ContentValues values = new ContentValues(1);
                values.put(DialContract.ContactDial.COLS_AMOUNT, 0);
                helper.getWritableDatabase()
                        .update(DialContract.ContactDial.TABLE_NAME, values, null, null);
            } catch (Exception e) {
                Log.w(TAG, "resetAmounts failed", e);
            }
        });
    }

    private static String escapeLike(@NonNull String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
