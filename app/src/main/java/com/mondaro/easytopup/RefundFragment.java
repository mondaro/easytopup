package com.mondaro.easytopup;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.mondaro.easytopup.data.Carrier;
import com.mondaro.easytopup.data.RefundRequest;
import com.mondaro.easytopup.data.SecurePrefs;
import com.mondaro.easytopup.telephony.UssdCodes;
import com.mondaro.easytopup.ui.BaseFragment;
import com.mondaro.easytopup.util.PhoneNumbers;

/**
 * Pulls back a top-up that went to the wrong number.
 *
 * <p>The carrier's confirmation message arrives here by being shared from the
 * messaging app or pasted in - the app no longer reads the inbox, because Google
 * Play grants {@code READ_SMS} only to the device's default SMS handler. For the
 * same reason the app never sends anything itself: the TRUE flow explains what
 * the user has to forward, and to which short code.
 */
public class RefundFragment extends BaseFragment {

    private static final String ARG_SHARED_TEXT = "shared_text";

    private LinearLayout background;
    private EditText smsBody;
    private Button[] carrierTabs;

    @Carrier.Id
    private int carrier = Carrier.NONE;
    private int[] tabColors;

    @NonNull
    public static RefundFragment newInstance(@Nullable String sharedText) {
        RefundFragment fragment = new RefundFragment();
        if (sharedText != null && !sharedText.isEmpty()) {
            Bundle args = new Bundle(1);
            args.putString(ARG_SHARED_TEXT, sharedText);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_refund, container, false);
        lockPortrait();

        if (!prefs().isConfigured()) {
            toast(R.string.not_configured_yet);
            ((MainActivity) requireActivity()).displayView(MainActivity.VIEW_SETTING);
            return root;
        }

        background = root.findViewById(R.id.list_refundBG);
        smsBody = root.findViewById(R.id.editTextSmsBody);
        carrierTabs = new Button[]{
                root.findViewById(R.id.btn_smsAIS),
                root.findViewById(R.id.btn_smsDTAC),
                root.findViewById(R.id.btn_smsTRUE),
                root.findViewById(R.id.btn_smsCAT)};

        boolean defaultTheme = "A".equals(prefs().getString(SecurePrefs.THEME));
        tabColors = defaultTheme
                ? new int[]{R.color.bg_topup_ais_default, R.color.bg_topup_dtac_default,
                R.color.bg_topup_true_default, R.color.bg_topup_cat_default}
                : new int[]{R.color.bg_topup_ais_pastel, R.color.bg_topup_dtac_pastel,
                R.color.bg_topup_true_pastel, R.color.bg_topup_cat_pastel};

        int firstEnabled = Carrier.NONE;
        for (int id = Carrier.AIS; id <= Carrier.CAT; id++) {
            final int carrierId = id;
            int index = id - 1;
            boolean enabled = !prefs().pinFor(id).isEmpty();
            carrierTabs[index].setVisibility(enabled ? View.VISIBLE : View.GONE);
            if (enabled) {
                carrierTabs[index].setBackgroundResource(tabColors[index]);
                carrierTabs[index].setOnClickListener(v -> selectCarrier(carrierId));
                if (firstEnabled == Carrier.NONE) {
                    firstEnabled = id;
                }
            }
        }

        root.findViewById(R.id.btnPasteSms).setOnClickListener(v -> pasteFromClipboard());
        root.findViewById(R.id.btnClearSms).setOnClickListener(v -> smsBody.setText(""));
        root.findViewById(R.id.btnCancel).setOnClickListener(v -> smsBody.setText(""));
        root.findViewById(R.id.btnRefund).setOnClickListener(v -> confirmRefund());

        if (firstEnabled != Carrier.NONE) {
            selectCarrier(firstEnabled);
        }

        Bundle args = getArguments();
        if (args != null) {
            String shared = args.getString(ARG_SHARED_TEXT, "");
            if (!shared.isEmpty()) {
                smsBody.setText(shared);
                // Consume it, so rotating the screen does not silently refill the box.
                args.remove(ARG_SHARED_TEXT);
            }
        }

        return root;
    }

    private void selectCarrier(@Carrier.Id int id) {
        carrier = id;
        background.setBackgroundResource(tabColors[id - 1]);
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard =
                (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            toast(R.string.refund_clipboard_empty);
            return;
        }
        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            toast(R.string.refund_clipboard_empty);
            return;
        }
        CharSequence text = clip.getItemAt(0).coerceToText(requireContext());
        if (text == null || text.toString().trim().isEmpty()) {
            toast(R.string.refund_clipboard_empty);
            return;
        }
        smsBody.setText(text.toString());
    }

    private void confirmRefund() {
        String body = smsBody.getText().toString().trim();
        if (body.isEmpty()) {
            toast(R.string.refund_select_first);
            return;
        }
        if (carrier == Carrier.NONE) {
            toast(R.string.error_no_carrier);
            return;
        }

        final RefundRequest request = RefundRequest.parse(carrier, body);
        if (request == null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.refund_unreadable_title)
                    .setMessage(R.string.refund_unreadable_body)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        String summary = getString(R.string.refund_confirm_body,
                Carrier.displayName(request.carrier),
                request.phone,
                request.amountText,
                request.reference);

        // The action is the positive button: the old dialog put "pull the money
        // back" on the negative one, where people expect cancel.
        new AlertDialog.Builder(requireContext())
                .setIcon(android.R.drawable.ic_menu_revert)
                .setTitle(R.string.refund_confirm_title)
                .setMessage(summary)
                .setPositiveButton(R.string.refund_do_it, (d, w) -> execute(request))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void execute(@NonNull RefundRequest request) {
        if (request.carrier == Carrier.TRUE) {
            forwardTrueRefund(request);
            return;
        }
        String pin = prefs().pinFor(request.carrier);
        String ussd;
        switch (request.carrier) {
            case Carrier.AIS:
                ussd = UssdCodes.refundAis(pin, PhoneNumbers.lastFour(request.phone),
                        request.reference);
                break;
            case Carrier.DTAC:
                ussd = UssdCodes.refundDtac(request.phone, pin, request.amountForUssd);
                break;
            case Carrier.CAT:
                ussd = UssdCodes.refundCat(request.phone, request.reference);
                break;
            default:
                return;
        }
        sendUssd(ussd, () -> smsBody.setText(""));
    }

    /**
     * TRUE wants the original confirmation message forwarded to its short code.
     * The app does not send it: it explains what to send and where, and offers to
     * put the message on the clipboard so the user can paste it into their own
     * messaging app.
     */
    private void forwardTrueRefund(@NonNull RefundRequest request) {
        new AlertDialog.Builder(requireContext())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.refund_true_title)
                .setMessage(getString(R.string.refund_true_instructions,
                        UssdCodes.TRUE_REFUND_SHORTCODE, request.phone, request.amountText))
                .setPositiveButton(R.string.refund_copy_message,
                        (d, w) -> copyToClipboard(request.rawBody))
                .setNegativeButton(R.string.understood, null)
                .show();
    }

    private void copyToClipboard(@NonNull String text) {
        ClipboardManager clipboard =
                (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        clipboard.setPrimaryClip(
                ClipData.newPlainText(getString(R.string.refund_clip_label), text));
        toast(R.string.refund_copied);
    }
}
