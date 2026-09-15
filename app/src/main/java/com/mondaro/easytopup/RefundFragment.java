package com.mondaro.easytopup;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.mondaro.easytopup.data.Carrier;
import com.mondaro.easytopup.data.RefundRequest;
import com.mondaro.easytopup.data.SecurePrefs;
import com.mondaro.easytopup.data.SmsRepository;
import com.mondaro.easytopup.telephony.SmsSender;
import com.mondaro.easytopup.telephony.UssdCodes;
import com.mondaro.easytopup.ui.BaseFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * History of top-up confirmations, and the "pull the money back" flow for a
 * top-up sent to the wrong number.
 */
public class RefundFragment extends BaseFragment {

    private static final Locale THAI = new Locale("th", "TH");

    private LinearLayout background;
    private View actionPanel;
    private ListView messageList;
    private TextView countLabel;
    private Button[] carrierTabs;

    private final Handler main = new Handler(Looper.getMainLooper());
    private List<SmsRepository.Message> messages = Collections.emptyList();
    @Carrier.Id
    private int carrier = Carrier.NONE;
    private int selectedIndex = -1;

    private int[] tabColors;

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
        actionPanel = root.findViewById(R.id.panelBtn);
        messageList = root.findViewById(R.id.listViewRefund);
        countLabel = root.findViewById(R.id.textViewCountRowsRefund);
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

        actionPanel.setVisibility(View.GONE);
        messageList.setOnItemClickListener((parent, view, position, id) -> {
            selectedIndex = position;
            actionPanel.setVisibility(View.VISIBLE);
        });
        root.findViewById(R.id.btnCancel).setOnClickListener(v -> clearSelection());
        root.findViewById(R.id.btnRefund).setOnClickListener(v -> confirmRefund());

        if (firstEnabled != Carrier.NONE) {
            selectCarrier(firstEnabled);
        }
        return root;
    }

    private void selectCarrier(@Carrier.Id int id) {
        carrier = id;
        background.setBackgroundResource(tabColors[id - 1]);
        clearSelection();
        withPermission(Manifest.permission.READ_SMS, R.string.rationale_read_sms, this::loadMessages);
    }

    private void loadMessages() {
        SmsRepository.get(requireContext()).load(carrier, main, loaded -> {
            if (!isAdded()) {
                return;
            }
            messages = loaded;
            List<String> rows = new ArrayList<>(loaded.size());
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy เวลา H:mm", THAI);
            for (SmsRepository.Message message : loaded) {
                rows.add(message.body + "\n\n" + getString(R.string.refund_date_prefix) + " "
                        + format.format(new Date(message.timestamp)));
            }
            countLabel.setText(getString(R.string.refund_found_count, loaded.size()));
            messageList.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_2, android.R.id.text1, rows));
        });
    }

    private void clearSelection() {
        selectedIndex = -1;
        actionPanel.setVisibility(View.GONE);
    }

    private void confirmRefund() {
        if (selectedIndex < 0 || selectedIndex >= messages.size()) {
            toast(R.string.refund_select_first);
            return;
        }
        // Parse the raw SMS body, never the list row - the row also carries the
        // formatted date, which used to end up inside the forwarded request.
        final RefundRequest request =
                RefundRequest.parse(carrier, messages.get(selectedIndex).body);
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
        clearSelection();
        if (request.carrier == Carrier.TRUE) {
            forwardTrueRefund(request);
            return;
        }
        String pin = prefs().pinFor(request.carrier);
        String ussd;
        switch (request.carrier) {
            case Carrier.AIS:
                ussd = UssdCodes.refundAis(pin,
                        com.mondaro.easytopup.util.PhoneNumbers.lastFour(request.phone),
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
        sendUssd(ussd, null);
    }

    /** TRUE takes the original confirmation message forwarded to its short code. */
    private void forwardTrueRefund(@NonNull RefundRequest request) {
        withPermission(Manifest.permission.SEND_SMS, R.string.rationale_send_sms, () ->
                SmsSender.send(requireContext(), UssdCodes.TRUE_REFUND_SHORTCODE, request.rawBody,
                        sent -> {
                            if (!isAdded()) {
                                return;
                            }
                            new AlertDialog.Builder(requireContext())
                                    .setIcon(android.R.drawable.ic_menu_revert)
                                    .setTitle(R.string.refund_true_title)
                                    .setMessage(sent
                                            ? R.string.refund_true_sent
                                            : R.string.refund_true_failed)
                                    .setPositiveButton(R.string.understood, null)
                                    .show();
                        }));
    }
}
