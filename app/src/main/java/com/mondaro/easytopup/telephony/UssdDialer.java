package com.mondaro.easytopup.telephony;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

/**
 * Sends a USSD string to the network.
 *
 * <p>On API 26 and above the request goes through
 * {@link TelephonyManager#sendUssdRequest}, which keeps the PIN out of the call
 * log and - more usefully for an agent - hands back what the carrier actually
 * replied, so a failed top-up is no longer indistinguishable from a successful
 * one. Older devices, and users who opt out, fall back to the original
 * {@code ACTION_CALL} route.
 *
 * <p>A failed in-app request is never retried automatically: the network may have
 * processed the top-up before the session dropped, and a silent retry would risk
 * charging the agent twice.
 */
public final class UssdDialer {

    private static final String TAG = "UssdDialer";

    public interface Callback {
        /** The carrier answered. {@code response} is its text, shown as-is. */
        void onResponse(@NonNull CharSequence response);

        /** The radio refused the session. The caller must ask before retrying. */
        void onFailure(int failureCode);

        /** Handed to the system dialer; no result is available to the app. */
        void onHandedToDialer();
    }

    private UssdDialer() {
    }

    public static boolean canSendInApp(@NonNull Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && context.getSystemService(TelephonyManager.class) != null;
    }

    public static boolean hasCallPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @param ussd     the code in plain form, e.g. {@code *123*1234*0812345678*50#}
     * @param useLegacy force the system dialer even where the in-app session works
     */
    public static void send(@NonNull Context context,
                            @NonNull String ussd,
                            boolean useLegacy,
                            @NonNull Callback callback) {
        if (!hasCallPermission(context)) {
            callback.onFailure(TelephonyManager.USSD_ERROR_SERVICE_UNAVAIL);
            return;
        }
        if (!useLegacy && canSendInApp(context)) {
            if (sendInApp(context, ussd, callback)) {
                return;
            }
        }
        placeCall(context, ussd, callback);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static boolean sendInApp(@NonNull Context context,
                                     @NonNull String ussd,
                                     @NonNull Callback callback) {
        TelephonyManager tm = context.getSystemService(TelephonyManager.class);
        if (tm == null) {
            return false;
        }
        try {
            tm.sendUssdRequest(ussd, new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager,
                                                  String request,
                                                  CharSequence response) {
                    callback.onResponse(response == null ? "" : response);
                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager,
                                                        String request,
                                                        int failureCode) {
                    callback.onFailure(failureCode);
                }
            }, new Handler(Looper.getMainLooper()));
            return true;
        } catch (SecurityException | IllegalArgumentException | IllegalStateException e) {
            Log.w(TAG, "sendUssdRequest unavailable, falling back to the dialer", e);
            return false;
        }
    }

    private static void placeCall(@NonNull Context context,
                                  @NonNull String ussd,
                                  @NonNull Callback callback) {
        try {
            // fromParts percent-encodes the '#' for us; hand-rolled "%23" strings
            // break as soon as the code itself contains an encodable character.
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", ussd, null));
            if (!(context instanceof android.app.Activity)) {
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(callIntent);
            callback.onHandedToDialer();
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(TAG, "Call failed", e);
            callback.onFailure(TelephonyManager.USSD_ERROR_SERVICE_UNAVAIL);
        }
    }
}
