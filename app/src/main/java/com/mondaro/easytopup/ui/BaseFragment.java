package com.mondaro.easytopup.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mondaro.easytopup.R;
import com.mondaro.easytopup.data.SecurePrefs;
import com.mondaro.easytopup.telephony.UssdDialer;
import com.mondaro.easytopup.util.PhoneNumbers;

/**
 * Shared plumbing for every screen: permissions, the contact picker and USSD
 * dispatch.
 *
 * <p>Permissions are requested through the Activity Result APIs, so the action the
 * user was trying to perform is resumed once they grant - the old code requested
 * on behalf of the activity, which meant the fragment callback never fired and
 * the user had to press the button a second time.
 */
public abstract class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    public interface ContactPickedListener {
        void onContactPicked(@NonNull String normalisedNumber);
    }

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> contactLauncher;

    @Nullable
    private Runnable pendingAction;
    @Nullable
    private ContactPickedListener contactListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    Runnable action = pendingAction;
                    pendingAction = null;
                    if (granted && action != null) {
                        action.run();
                    } else if (!granted) {
                        toast(R.string.permission_denied);
                    }
                });
        contactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    ContactPickedListener listener = contactListener;
                    contactListener = null;
                    if (result.getResultCode() != Activity.RESULT_OK
                            || result.getData() == null
                            || listener == null) {
                        return;
                    }
                    String number = readPickedNumber(result.getData().getData());
                    if (number.isEmpty()) {
                        toast(R.string.contact_no_number);
                    } else {
                        listener.onContactPicked(number);
                    }
                });
    }

    // ---- orientation ------------------------------------------------------

    /** Locks this screen to portrait; layouts for the inner screens are portrait-only. */
    protected void lockPortrait() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    protected void allowAnyOrientation() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }

    // ---- helpers ----------------------------------------------------------

    @NonNull
    protected SecurePrefs prefs() {
        return SecurePrefs.get(requireContext());
    }

    protected void toast(@StringRes int message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    protected void toast(@NonNull CharSequence message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    protected void showMessage(@StringRes int title, @NonNull CharSequence message) {
        if (!isAdded()) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    // ---- permissions ------------------------------------------------------

    /**
     * Runs {@code action} once {@code permission} is held, asking for it - with a
     * rationale when the system says one is warranted - if it is not.
     */
    protected void withPermission(@NonNull String permission,
                                  @StringRes int rationale,
                                  @NonNull Runnable action) {
        if (!isAdded()) {
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            action.run();
            return;
        }
        pendingAction = action;
        if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(rationale)
                    .setPositiveButton(android.R.string.ok,
                            (d, w) -> permissionLauncher.launch(permission))
                    .setNegativeButton(android.R.string.cancel, (d, w) -> pendingAction = null)
                    .show();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    // ---- contact picker ---------------------------------------------------

    /**
     * Opens the phone-number picker. No {@code READ_CONTACTS} grant is needed: the
     * picker hands back a one-shot read permission for the row the user chose.
     */
    protected void pickContact(@NonNull ContactPickedListener listener) {
        contactListener = listener;
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        try {
            contactLauncher.launch(intent);
        } catch (Exception e) {
            contactListener = null;
            Log.w(TAG, "No contact picker available", e);
            toast(R.string.contact_picker_missing);
        }
    }

    @NonNull
    private String readPickedNumber(@Nullable Uri contactUri) {
        if (contactUri == null || !isAdded()) {
            return "";
        }
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
        try (Cursor cursor = requireContext().getContentResolver()
                .query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                return PhoneNumbers.normalise(cursor.getString(0));
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.w(TAG, "Unable to read the picked contact", e);
        }
        return "";
    }

    // ---- USSD -------------------------------------------------------------

    /**
     * Dials {@code ussd}, asking for the call permission first.
     *
     * @param onDispatched run on the main thread once the request has been handed
     *                     to the radio or to the dialer
     */
    protected void sendUssd(@NonNull String ussd, @Nullable Runnable onDispatched) {
        withPermission(Manifest.permission.CALL_PHONE, R.string.rationale_call_phone,
                () -> dispatchUssd(ussd, prefs().getBoolean(SecurePrefs.USE_LEGACY_DIALER, false),
                        onDispatched));
    }

    private void dispatchUssd(@NonNull String ussd, boolean legacy, @Nullable Runnable onDispatched) {
        // An in-app session answers seconds later, but the form should clear as
        // soon as the request is on its way so the next customer can be served.
        // It must not clear when send() rejected the request outright, which it
        // reports synchronously.
        final boolean[] rejected = {false};
        UssdDialer.send(requireContext(), ussd, legacy, new UssdDialer.Callback() {
            @Override
            public void onResponse(@NonNull CharSequence response) {
                if (isAdded()) {
                    showMessage(R.string.ussd_result_title, response);
                }
            }

            @Override
            public void onFailure(int failureCode) {
                rejected[0] = true;
                if (isAdded()) {
                    showUssdFailure(ussd, failureCode);
                }
            }

            @Override
            public void onHandedToDialer() {
                // The dialer owns the session from here; nothing to show.
            }
        });
        if (onDispatched != null && !rejected[0]) {
            onDispatched.run();
        }
    }

    /**
     * A failed in-app session is not retried automatically - the network may have
     * already acted on it - so the user is told what happened and decides.
     */
    private void showUssdFailure(@NonNull String ussd, int failureCode) {
        String reason = failureCode == TelephonyManager.USSD_RETURN_FAILURE
                ? getString(R.string.ussd_failed_network)
                : getString(R.string.ussd_failed_unsupported);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.ussd_failed_title)
                .setMessage(getString(R.string.ussd_failed_body, reason))
                .setPositiveButton(R.string.ussd_retry_dialer,
                        (d, w) -> dispatchUssd(ussd, true, null))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
