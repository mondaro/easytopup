package com.mondaro.easytopup.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Reads the carrier confirmation messages that back the refund screen. */
public final class SmsRepository {

    private static final String TAG = "SmsRepository";
    private static final Uri INBOX = Uri.parse("content://sms/inbox");
    private static final int MAX_ROWS = 200;

    /** Sender address each carrier uses for top-up confirmations. */
    private static final String[] SENDERS = {"020789123", "dtacOnline", "MobileTopUp", "iTop"};

    public static final class Message {
        public final String body;
        public final long timestamp;

        Message(@NonNull String body, long timestamp) {
            this.body = body;
            this.timestamp = timestamp;
        }
    }

    public interface Callback {
        void onLoaded(@NonNull List<Message> messages);
    }

    private static volatile SmsRepository instance;

    private final Context app;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "easytopup-sms"));

    private SmsRepository(@NonNull Context app) {
        this.app = app;
    }

    @NonNull
    public static SmsRepository get(@NonNull Context context) {
        SmsRepository local = instance;
        if (local == null) {
            synchronized (SmsRepository.class) {
                local = instance;
                if (local == null) {
                    local = new SmsRepository(context.getApplicationContext());
                    instance = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public static String senderFor(@Carrier.Id int carrier) {
        int index = carrier - 1;
        return (index >= 0 && index < SENDERS.length) ? SENDERS[index] : "";
    }

    /**
     * Loads the inbox messages for a carrier on a background thread. The inbox can
     * hold thousands of rows; doing this on the main thread - as the old code did -
     * froze the screen every time a carrier tab was tapped.
     */
    public void load(@Carrier.Id int carrier, @NonNull android.os.Handler main,
                     @NonNull Callback callback) {
        final String sender = senderFor(carrier);
        executor.execute(() -> {
            List<Message> messages;
            try {
                messages = query(sender);
            } catch (Exception e) {
                Log.w(TAG, "Unable to read the SMS inbox", e);
                messages = Collections.emptyList();
            }
            final List<Message> result = messages;
            main.post(() -> callback.onLoaded(result));
        });
    }

    @WorkerThread
    @NonNull
    private List<Message> query(@NonNull String sender) {
        List<Message> messages = new ArrayList<>();
        if (sender.isEmpty()) {
            return messages;
        }
        // Bound arguments rather than the old inlined "address='...'" string.
        try (Cursor cursor = app.getContentResolver().query(
                INBOX,
                new String[]{"body", "date"},
                "address = ?",
                new String[]{sender},
                "date DESC")) {
            if (cursor == null) {
                return messages;
            }
            while (cursor.moveToNext() && messages.size() < MAX_ROWS) {
                String body = cursor.isNull(0) ? "" : cursor.getString(0);
                if (!body.isEmpty()) {
                    messages.add(new Message(body, cursor.isNull(1) ? 0L : cursor.getLong(1)));
                }
            }
        }
        return messages;
    }
}
