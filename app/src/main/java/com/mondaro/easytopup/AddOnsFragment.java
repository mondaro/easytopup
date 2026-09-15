package com.mondaro.easytopup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.mondaro.easytopup.data.SecurePrefs;
import com.mondaro.easytopup.telephony.UssdCodes;
import com.mondaro.easytopup.ui.BaseFragment;
import com.mondaro.easytopup.util.Amounts;
import com.mondaro.easytopup.util.PhoneNumbers;

/** mPay wallet top-up, cash cards, bill payment and day transfers. */
public class AddOnsFragment extends BaseFragment {

    private static final int MIN_MPAY_ID_LENGTH = 4;

    private static final int[] HEADER_IDS = {
            R.id.contentH0, R.id.contentH1, R.id.contentH2, R.id.contentH3, R.id.contentH4};
    private static final int[] CONTENT_IDS = {
            R.id.contentC0, R.id.contentC1, R.id.contentC2, R.id.contentC3, R.id.contentC4};

    private static final String[] MPAY_TOPUP_TYPES = {"0", "1"};
    private static final String[] CASH_CARD_AMOUNTS = {"50", "100", "300", "500", "1000"};
    private static final String[] BILL_OPTIONS = {"0", "1", "2"};
    private static final String[] TRANSFER_DAYS = {"5", "10", "20"};

    private EditText amountField;
    private EditText mpayIdField;
    private EditText cashCardPhone;
    private EditText billPhone;
    private EditText transferDaysPhone;

    private Button saveMpayId;
    private Button checkBill;
    private Button payBill;
    private Button requestTopup;
    private Button confirmTopup;

    private final View[] contentPanels = new View[CONTENT_IDS.length];

    private String topupType = MPAY_TOPUP_TYPES[0];
    private String cashCardAmount = CASH_CARD_AMOUNTS[0];
    private String billOption = BILL_OPTIONS[0];
    private String transferDays = TRANSFER_DAYS[0];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_addons, container, false);
        lockPortrait();

        amountField = root.findViewById(R.id.editText_Amount);
        mpayIdField = root.findViewById(R.id.editText_ID);
        cashCardPhone = root.findViewById(R.id.editText_phonecard);
        billPhone = root.findViewById(R.id.editText_phonebill);
        transferDaysPhone = root.findViewById(R.id.editText_phonedays);

        saveMpayId = root.findViewById(R.id.button_addmPayID);
        checkBill = root.findViewById(R.id.button_chkbill);
        payBill = root.findViewById(R.id.button_paybill);
        requestTopup = root.findViewById(R.id.button_pass1);
        confirmTopup = root.findViewById(R.id.button_pass2);

        setUpAccordion(root);
        setUpMpayId();
        setUpSpinners(root);
        setUpActions(root);
        setUpHelpButtons(root);

        return root;
    }

    /**
     * One expandable section at a time. The five headers previously repeated the
     * same forty lines - including a call-permission check that guarded opening a
     * panel rather than dialling; the permission is now requested by the action
     * itself, where it is actually needed.
     */
    private void setUpAccordion(@NonNull View root) {
        for (int i = 0; i < HEADER_IDS.length; i++) {
            final int index = i;
            contentPanels[i] = root.findViewById(CONTENT_IDS[i]);
            root.findViewById(HEADER_IDS[i]).setOnClickListener(v -> togglePanel(index));
        }
    }

    private void togglePanel(int index) {
        boolean open = contentPanels[index].getVisibility() == View.VISIBLE;
        for (int i = 0; i < contentPanels.length; i++) {
            contentPanels[i].setVisibility(!open && i == index ? View.VISIBLE : View.GONE);
        }
    }

    private void setUpMpayId() {
        String saved = prefs().getString(SecurePrefs.MPAY);
        applyMpayIdState(saved);
        saveMpayId.setOnClickListener(v -> {
            if (!prefs().getString(SecurePrefs.MPAY).isEmpty()) {
                prefs().putString(SecurePrefs.MPAY, "");
                applyMpayIdState("");
                return;
            }
            String id = mpayId();
            if (id.length() < MIN_MPAY_ID_LENGTH) {
                toast(R.string.mpay_id_too_short);
                return;
            }
            prefs().putString(SecurePrefs.MPAY, id);
            applyMpayIdState(id);
            toast(R.string.mpay_id_saved);
        });
    }

    private void applyMpayIdState(@NonNull String saved) {
        boolean stored = !saved.isEmpty();
        mpayIdField.setText(saved);
        mpayIdField.setEnabled(!stored);
        saveMpayId.setText(stored ? R.string.mpay_id_clear : R.string.mpay_id_save);
    }

    @NonNull
    private String mpayId() {
        return mpayIdField.getText().toString().trim();
    }

    private void setUpSpinners(@NonNull View root) {
        bindSpinner(root.findViewById(R.id.spn1), MPAY_TOPUP_TYPES, value -> topupType = value);
        bindSpinner(root.findViewById(R.id.spn2), CASH_CARD_AMOUNTS, value -> cashCardAmount = value);
        bindSpinner(root.findViewById(R.id.spn3), BILL_OPTIONS, value -> billOption = value);
        bindSpinner(root.findViewById(R.id.spn4), TRANSFER_DAYS, value -> transferDays = value);
    }

    private interface SpinnerListener {
        void onSelected(@NonNull String value);
    }

    private static void bindSpinner(@NonNull Spinner spinner, @NonNull String[] values,
                                    @NonNull SpinnerListener listener) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < values.length) {
                    listener.onSelected(values[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpActions(@NonNull View root) {
        root.findViewById(R.id.button_chkBalance)
                .setOnClickListener(v -> sendUssd(UssdCodes.MPAY_BALANCE, null));
        root.findViewById(R.id.button_chkCommission)
                .setOnClickListener(v -> sendUssd(UssdCodes.MPAY_COMMISSION, null));

        root.findViewById(R.id.button_contactBuyCard)
                .setOnClickListener(v -> pickContact(cashCardPhone::setText));
        root.findViewById(R.id.button_contactBill)
                .setOnClickListener(v -> pickContact(billPhone::setText));
        root.findViewById(R.id.button_contactTransDay)
                .setOnClickListener(v -> pickContact(transferDaysPhone::setText));

        requestTopup.setOnClickListener(v -> requestMpayTopup());
        confirmTopup.setOnClickListener(v -> {
            sendUssd(UssdCodes.mpayTopupConfirm(topupType), null);
            confirmTopup.setEnabled(false);
            requestTopup.setEnabled(true);
        });

        root.findViewById(R.id.button_card).setOnClickListener(v -> buyCashCard());
        checkBill.setOnClickListener(v -> checkBill());
        payBill.setOnClickListener(v -> {
            sendUssd(UssdCodes.payBill(billOption), null);
            resetBillPanel();
        });
        root.findViewById(R.id.button_transDays).setOnClickListener(v -> transferDays());

        root.findViewById(R.id.imageButton_RepeatPhone).setOnClickListener(v ->
                transferDaysPhone.setText(prefs().getString(SecurePrefs.LAST_TRANSFER_DAYS_PHONE)));
        root.findViewById(R.id.imageButton_ResetPhoneBilll).setOnClickListener(v -> resetBillPanel());

        resetBillPanel();
        confirmTopup.setEnabled(false);
    }

    private void requestMpayTopup() {
        int amount = Amounts.parse(amountField.getText().toString());
        if (amount == Amounts.INVALID) {
            toast(R.string.error_amount);
            return;
        }
        String id = mpayId();
        if (id.length() < MIN_MPAY_ID_LENGTH) {
            toast(R.string.mpay_id_too_short);
            return;
        }
        sendUssd(UssdCodes.mpayTopupRequest(amount, id), () -> {
            requestTopup.setEnabled(false);
            confirmTopup.setEnabled(true);
        });
    }

    private void buyCashCard() {
        String pin = prefs().getString(SecurePrefs.UID_AIS);
        if (pin.isEmpty()) {
            toast(R.string.carrier_not_configured);
            return;
        }
        String phone = PhoneNumbers.normalise(cashCardPhone.getText().toString());
        if (!PhoneNumbers.isValid(phone)) {
            toast(R.string.error_phone_length);
            return;
        }
        sendUssd(UssdCodes.buyCashCard(pin, phone, cashCardAmount), null);
    }

    private void checkBill() {
        String id = mpayId();
        if (id.length() < MIN_MPAY_ID_LENGTH) {
            toast(R.string.mpay_id_too_short);
            return;
        }
        String phone = PhoneNumbers.normalise(billPhone.getText().toString());
        if (!PhoneNumbers.isValid(phone)) {
            toast(R.string.error_phone_length);
            return;
        }
        sendUssd(UssdCodes.checkBill(id, phone), () -> {
            checkBill.setEnabled(false);
            payBill.setEnabled(true);
        });
    }

    private void transferDays() {
        String phone = PhoneNumbers.normalise(transferDaysPhone.getText().toString());
        if (!PhoneNumbers.isValid(phone)) {
            toast(R.string.error_phone_length);
            return;
        }
        sendUssd(UssdCodes.transferDays(phone, transferDays), () ->
                prefs().putString(SecurePrefs.LAST_TRANSFER_DAYS_PHONE, phone));
    }

    private void resetBillPanel() {
        billPhone.setText("");
        checkBill.setEnabled(true);
        payBill.setEnabled(false);
    }

    private void setUpHelpButtons(@NonNull View root) {
        int[] helpButtons = {R.id.button_what1, R.id.button_what2,
                R.id.button_what3, R.id.button_what4};
        for (int i = 0; i < helpButtons.length; i++) {
            final int page = i;
            root.findViewById(helpButtons[i]).setOnClickListener(v -> showHelp(page));
        }
    }

    private void showHelp(int page) {
        FragmentManager manager = getChildFragmentManager();
        if (manager.isStateSaved()) {
            return;
        }
        manager.beginTransaction()
                .replace(R.id.contentL, HelpMpayFragment.newInstance(page))
                .commit();
    }
}
