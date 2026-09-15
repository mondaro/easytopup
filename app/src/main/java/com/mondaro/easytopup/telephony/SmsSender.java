package com.mondaro.easytopup.telephony;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * Sends the TRUE refund request to the carrier short code and reports whether it
 * actually went out.
 *
 * <p>The previous version called {@code sendTextMessage} with no result intent and
 * then asked the user to forward the message by hand as well, which produced two
 * refund requests for one mistake. It also used the single-part API for bodies
 * that are Thai text and therefore almost always multipart.
 */
public final class SmsSender {

    private static final String TAG = "SmsSender";
    private static final String ACTION_SENT = "com.mondaro.easytopup.SMS_SENT";

    public interface ResultCallback {
        void onResult(boolean sent);
    }

    private SmsSender() {
    }

    public static void send(@NonNull Context context,
                            @NonNull String destination,
                            @NonNull String body,
                            @NonNull ResultCallback callback) {
        SmsManager manager = smsManager(context);
        if (manager == null) {
            callback.onResult(false);
            return;
        }

        String action = ACTION_SENT + "." + System.currentTimeMillis();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            private boolean delivered;

            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (delivered) {
                    // Multipart messages report once per part; the first result decides.
                    return;
                }
                delivered = true;
                boolean ok = getResultCode() == android.app.Activity.RESULT_OK;
                try {
                    ctx.getApplicationContext().unregisterReceiver(this);
                } catch (IllegalArgumentException ignored) {
                    // Already unregistered.
                }
                callback.onResult(ok);
            }
        };
        ContextCompat.registerReceiver(context.getApplicationContext(), receiver,
                new IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED);

        try {
            ArrayList<String> parts = manager.divideMessage(body);
            ArrayList<PendingIntent> sentIntents = new ArrayList<>(parts.size());
            for (int i = 0; i < parts.size(); i++) {
                sentIntents.add(PendingIntent.getBroadcast(
                        context.getApplicationContext(),
                        i,
                        new Intent(action).setPackage(context.getPackageName()),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
            }
            manager.sendMultipartTextMessage(destination, null, parts, sentIntents, null);
        } catch (Exception e) {
            Log.w(TAG, "Unable to send the refund SMS", e);
            try {
                context.getApplicationContext().unregisterReceiver(receiver);
            } catch (IllegalArgumentException ignored) {
                // Already unregistered.
            }
            callback.onResult(false);
        }
    }

    private static SmsManager smsManager(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.getSystemService(SmsManager.class);
        }
        return SmsManager.getDefault();
    }
}
