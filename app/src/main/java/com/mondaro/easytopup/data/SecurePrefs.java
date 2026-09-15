package com.mondaro.easytopup.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.util.Map;

/**
 * Single place where every persisted setting lives.
 *
 * <p>The agent PIN stored here is the equivalent of cash, so the backing file is
 * an {@link EncryptedSharedPreferences} whenever the device keystore can provide
 * one. A handful of older or vendor-modified devices ship a broken keystore; on
 * those we fall back to a plain private file rather than refusing to start, which
 * is still protected by the app sandbox and by {@code allowBackup="false"}.
 *
 * <p>Settings written by version 6.5 and earlier lived in the activity-scoped
 * {@code MainActivity.xml} file. They are migrated on first access and the old
 * file is cleared, so upgrading users keep their PINs without re-entering them.
 */
public final class SecurePrefs {

    private static final String TAG = "SecurePrefs";
    private static final String FILE_ENCRYPTED = "easytopup_secure";
    private static final String FILE_PLAIN = "easytopup_prefs";
    /** {@code Activity.getPreferences()} file used up to version 6.5. */
    private static final String FILE_LEGACY = "MainActivity";
    private static final String KEY_MIGRATED = "_migrated_from_v65";

    public static final String UID_AIS = "UID_AIS";
    public static final String UID_DTAC = "UID_DTAC";
    public static final String UID_TRUE = "UID_TRUE";
    public static final String UID_CAT = "UID_CAT";
    public static final String MPAY = "MPAY";
    public static final String THEME = "THEME";
    public static final String CONFIGURED = "CHK_OK";
    public static final String LAST_PHONE = "LAST";
    public static final String LAST_TRANSFER_DAYS_PHONE = "tdrecent";
    public static final String ROLL_YEAR = "YEAR";
    /** Opt out of the in-app USSD session and use the system dialer instead. */
    public static final String USE_LEGACY_DIALER = "USE_LEGACY_DIALER";

    private static volatile SecurePrefs instance;

    private final SharedPreferences prefs;

    private SecurePrefs(@NonNull Context app) {
        this.prefs = open(app);
        migrateLegacy(app);
    }

    @NonNull
    public static SecurePrefs get(@NonNull Context context) {
        SecurePrefs local = instance;
        if (local == null) {
            synchronized (SecurePrefs.class) {
                local = instance;
                if (local == null) {
                    local = new SecurePrefs(context.getApplicationContext());
                    instance = local;
                }
            }
        }
        return local;
    }

    private static SharedPreferences open(@NonNull Context app) {
        try {
            MasterKey key = new MasterKey.Builder(app)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    app,
                    FILE_ENCRYPTED,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Throwable t) {
            // Keystore unusable on this device - degrade instead of crashing.
            Log.w(TAG, "Encrypted preferences unavailable, using private file", t);
            return app.getSharedPreferences(FILE_PLAIN, Context.MODE_PRIVATE);
        }
    }

    private void migrateLegacy(@NonNull Context app) {
        if (prefs.getBoolean(KEY_MIGRATED, false)) {
            return;
        }
        SharedPreferences legacy = app.getSharedPreferences(FILE_LEGACY, Context.MODE_PRIVATE);
        Map<String, ?> all = legacy.getAll();
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(entry.getKey(), (Boolean) value);
            } else if (value instanceof Long) {
                editor.putLong(entry.getKey(), (Long) value);
            }
        }
        editor.putBoolean(KEY_MIGRATED, true).apply();
        if (!all.isEmpty()) {
            // The old file was plain text; do not leave PINs lying around in it.
            legacy.edit().clear().apply();
        }
    }

    @NonNull
    public String getString(@NonNull String key) {
        return prefs.getString(key, "");
    }

    public boolean has(@NonNull String key) {
        return !getString(key).isEmpty();
    }

    public int getInt(@NonNull String key, int fallback) {
        return prefs.getInt(key, fallback);
    }

    public boolean getBoolean(@NonNull String key, boolean fallback) {
        return prefs.getBoolean(key, fallback);
    }

    public void putString(@NonNull String key, @Nullable String value) {
        prefs.edit().putString(key, value == null ? "" : value).apply();
    }

    public void putInt(@NonNull String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public void putBoolean(@NonNull String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    /** True once at least one carrier has been configured in Settings. */
    public boolean isConfigured() {
        return "TRUE".equals(getString(CONFIGURED));
    }

    /** PIN for the given carrier id, or an empty string when it is not set up. */
    @NonNull
    public String pinFor(int carrier) {
        switch (carrier) {
            case Carrier.AIS:
                return getString(UID_AIS);
            case Carrier.DTAC:
                return getString(UID_DTAC);
            case Carrier.TRUE:
                return getString(UID_TRUE);
            case Carrier.CAT:
                return getString(UID_CAT);
            default:
                return "";
        }
    }
}
