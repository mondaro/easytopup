package com.mondaro.easytopup;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.mondaro.easytopup.data.Carrier;
import com.mondaro.easytopup.data.HistoryRepository;
import com.mondaro.easytopup.data.QuickDial;
import com.mondaro.easytopup.data.SecurePrefs;
import com.mondaro.easytopup.telephony.UssdCodes;
import com.mondaro.easytopup.ui.BaseFragment;
import com.mondaro.easytopup.util.Amounts;
import com.mondaro.easytopup.util.PhoneNumbers;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/** The keypad: pick a carrier, type a number and an amount, dial the top-up. */
public class TopupFragment extends BaseFragment {

    private static final int FIELD_PHONE = 1;
    private static final int FIELD_AMOUNT = 2;

    private static final int[] KEYPAD_IDS = {
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9};

    private TextView txtPhone;
    private TextView txtCost;
    private TextView[] quickViews;
    private LinearLayout bgColor;
    private LinearLayout[] carrierTabs;
    private LinearLayout[] carrierSlots;

    private int field = FIELD_PHONE;
    @Carrier.Id
    private int carrier = Carrier.NONE;

    private List<QuickDial> quickDials = Collections.emptyList();
    private HistoryRepository history;
    private SecurePrefs prefs;

    private int[] tabBackgrounds;
    private int[] tabColors;

    // Survive the view being re-inflated when the device is rotated: the activity
    // handles the configuration change itself, so only the view is rebuilt.
    private String pendingPhone = "";
    private String pendingAmount = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_topup, container, false);
        allowAnyOrientation();

        prefs = prefs();
        history = HistoryRepository.get(requireContext());

        if (!prefs.isConfigured()) {
            toast(R.string.not_configured_yet);
            ((MainActivity) requireActivity()).displayView(MainActivity.VIEW_SETTING);
            return root;
        }

        bindViews(root);
        applyTheme();
        wireKeypad(root);
        wireCarrierTabs();
        setUpEnabledCarriers();
        resetAmountsOnNewYear();
        restorePendingInput();

        return root;
    }

    private void bindViews(@NonNull View root) {
        bgColor = root.findViewById(R.id.bgTheme);
        txtPhone = root.findViewById(R.id.textViewPhone);
        txtCost = root.findViewById(R.id.textViewCost);
        quickViews = new TextView[]{
                root.findViewById(R.id.textViewDial1),
                root.findViewById(R.id.textViewDial2),
                root.findViewById(R.id.textViewDial3)};
        carrierTabs = new LinearLayout[]{
                root.findViewById(R.id.sltAIS),
                root.findViewById(R.id.sltDTAC),
                root.findViewById(R.id.sltTRUE),
                root.findViewById(R.id.sltCAT)};
        carrierSlots = new LinearLayout[]{
                root.findViewById(R.id.slt1),
                root.findViewById(R.id.slt2),
                root.findViewById(R.id.slt3),
                root.findViewById(R.id.slt4)};

        txtPhone.setOnClickListener(v -> focusField(FIELD_PHONE));
        txtCost.setOnClickListener(v -> focusField(FIELD_AMOUNT));
        focusField(FIELD_PHONE);

        root.findViewById(R.id.btnHistory).setOnClickListener(v -> {
            String last = prefs.getString(SecurePrefs.LAST_PHONE);
            if (!last.isEmpty()) {
                focusField(FIELD_PHONE);
                setText(txtPhone, last);
                refreshQuickDial(last);
            }
        });
        root.findViewById(R.id.btnFindContact).setOnClickListener(v -> pickContact(number -> {
            setText(txtPhone, number);
            if (!PhoneNumbers.isValid(number)) {
                toast(R.string.error_phone_length);
            }
            focusField(FIELD_AMOUNT);
            clearQuickDial();
        }));

        for (int i = 0; i < quickViews.length; i++) {
            final int index = i;
            quickViews[i].setOnClickListener(v -> onQuickDialClicked(index));
        }
    }

    /** Re-inflates the view so the layout for the new orientation is picked up. */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (txtPhone != null) {
            pendingPhone = txtPhone.getText().toString();
            pendingAmount = txtCost.getText().toString();
        }
        FragmentManager manager = getParentFragmentManager();
        if (!manager.isStateSaved()) {
            manager.beginTransaction().detach(this).attach(this).commit();
        }
    }

    private void restorePendingInput() {
        if (!pendingPhone.isEmpty()) {
            setText(txtPhone, pendingPhone);
        }
        if (!pendingAmount.isEmpty()) {
            setText(txtCost, pendingAmount);
        }
        focusField(pendingPhone.length() >= PhoneNumbers.LENGTH ? FIELD_AMOUNT : FIELD_PHONE);
    }

    private void applyTheme() {
        boolean defaultTheme = "A".equals(prefs.getString(SecurePrefs.THEME));
        tabColors = defaultTheme
                ? new int[]{R.color.bg_topup_ais_default, R.color.bg_topup_dtac_default,
                R.color.bg_topup_true_default, R.color.bg_topup_cat_default}
                : new int[]{R.color.bg_topup_ais_pastel, R.color.bg_topup_dtac_pastel,
                R.color.bg_topup_true_pastel, R.color.bg_topup_cat_pastel};
        tabBackgrounds = defaultTheme
                ? new int[]{R.drawable.button_select_ais_default, R.drawable.button_select_dtac_default,
                R.drawable.button_select_true_default, R.drawable.button_select_cat_default}
                : new int[]{R.drawable.button_select_ais_pastel, R.drawable.button_select_dtac_pastel,
                R.drawable.button_select_true_pastel, R.drawable.button_select_cat_pastel};
    }

    private void wireKeypad(@NonNull View root) {
        for (int digit = 0; digit < KEYPAD_IDS.length; digit++) {
            final String value = String.valueOf(digit);
            root.findViewById(KEYPAD_IDS[digit]).setOnClickListener(v -> appendDigit(value));
        }
        root.findViewById(R.id.buttonTopup).setOnClickListener(v -> submit());

        View delete = root.findViewById(R.id.buttonDel);
        delete.setOnClickListener(v -> deleteDigit());
        delete.setOnLongClickListener(v -> {
            setText(activeField(), "");
            if (field == FIELD_PHONE) {
                clearQuickDial();
            }
            return true;
        });
    }

    private void wireCarrierTabs() {
        for (int i = 0; i < carrierTabs.length; i++) {
            final int id = i + 1;
            carrierTabs[i].setOnClickListener(v -> selectCarrier(id));
        }
    }

    /** Hides the tabs for carriers that have no PIN and selects the first that does. */
    private void setUpEnabledCarriers() {
        int firstEnabled = Carrier.NONE;
        for (int id = Carrier.AIS; id <= Carrier.CAT; id++) {
            int index = id - 1;
            boolean enabled = !prefs.pinFor(id).isEmpty();
            carrierSlots[index].setVisibility(enabled ? View.VISIBLE : View.GONE);
            if (enabled && firstEnabled == Carrier.NONE) {
                firstEnabled = id;
            }
        }
        // Keep the tab the user had chosen when the view is rebuilt on rotation.
        int wanted = (carrier != Carrier.NONE && !prefs.pinFor(carrier).isEmpty())
                ? carrier : firstEnabled;
        if (wanted != Carrier.NONE) {
            selectCarrier(wanted);
        }
    }

    private void selectCarrier(@Carrier.Id int id) {
        carrier = id;
        int index = id - 1;
        bgColor.setBackgroundResource(tabColors[index]);
        for (int i = 0; i < carrierSlots.length; i++) {
            // The selected tab carries its carrier colour and a brass edge; the
            // rest are a scrim over whichever ground is behind them. Width alone
            // used to be the only thing marking the selection.
            carrierTabs[i].setBackgroundResource(
                    i == index ? tabBackgrounds[i] : R.drawable.tab_inactive);
            // Mutating the weight keeps the margins the XML set; rebuilding the
            // LayoutParams - as the old code did - threw them away on every tap.
            ViewGroup.LayoutParams params = carrierSlots[i].getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) params).weight = (i == index) ? 1f : 0f;
            }
            carrierSlots[i].requestLayout();
        }
    }

    // ---- keypad -----------------------------------------------------------

    private TextView activeField() {
        return field == FIELD_PHONE ? txtPhone : txtCost;
    }

    private void focusField(int which) {
        field = which;
        txtPhone.setBackgroundResource(which == FIELD_PHONE
                ? R.drawable.border_active : R.drawable.border_inactive);
        txtCost.setBackgroundResource(which == FIELD_AMOUNT
                ? R.drawable.border_active : R.drawable.border_inactive);
    }

    private void appendDigit(@NonNull String digit) {
        TextView target = activeField();
        String current = target.getText().toString();
        if (field == FIELD_PHONE) {
            if (current.length() >= PhoneNumbers.LENGTH) {
                // Number complete - roll over to the amount and start it with this key.
                focusField(FIELD_AMOUNT);
                setText(txtCost, digit);
                return;
            }
            String updated = current + digit;
            setText(txtPhone, updated);
            refreshQuickDial(updated);
        } else {
            if (current.length() >= 6) {
                return;
            }
            setText(txtCost, current + digit);
        }
    }

    private void deleteDigit() {
        TextView target = activeField();
        String current = target.getText().toString();
        if (current.isEmpty()) {
            if (field == FIELD_AMOUNT) {
                focusField(FIELD_PHONE);
            }
            return;
        }
        String updated = current.substring(0, current.length() - 1);
        setText(target, updated);
        if (field == FIELD_PHONE) {
            refreshQuickDial(updated);
        }
    }

    private void setText(@NonNull TextView view, @NonNull String value) {
        view.setText(value);
    }

    // ---- quick dial -------------------------------------------------------

    private void refreshQuickDial(@NonNull String prefix) {
        history.quickDial(prefix, items -> {
            if (!isAdded()) {
                return;
            }
            quickDials = items;
            for (int i = 0; i < quickViews.length; i++) {
                if (i < items.size()) {
                    QuickDial item = items.get(i);
                    quickViews[i].setText(item.phone);
                    quickViews[i].setBackgroundResource(quickBackground(item.carrier));
                    quickViews[i].setVisibility(View.VISIBLE);
                } else {
                    quickViews[i].setText("");
                    quickViews[i].setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void clearQuickDial() {
        quickDials = Collections.emptyList();
        for (TextView view : quickViews) {
            view.setText("");
            view.setVisibility(View.INVISIBLE);
        }
    }

    private static int quickBackground(@Carrier.Id int carrier) {
        switch (carrier) {
            case Carrier.DTAC:
                return R.drawable.button_quick_dtac;
            case Carrier.TRUE:
                return R.drawable.button_quick_true;
            case Carrier.CAT:
                return R.drawable.button_quick_cat;
            case Carrier.AIS:
            default:
                return R.drawable.button_quick_ais;
        }
    }

    private void onQuickDialClicked(int index) {
        if (index >= quickDials.size()) {
            return;
        }
        QuickDial item = quickDials.get(index);
        if (prefs.pinFor(item.carrier).isEmpty()) {
            toast(R.string.carrier_not_configured);
            return;
        }
        setText(txtPhone, item.phone);
        selectCarrier(item.carrier);
        focusField(FIELD_AMOUNT);
        setText(txtCost, "");
        clearQuickDial();
    }

    // ---- submit -----------------------------------------------------------

    private void submit() {
        String phone = PhoneNumbers.normalise(txtPhone.getText().toString());
        int amount = Amounts.parse(txtCost.getText().toString());

        if (phone.isEmpty() || amount == Amounts.INVALID) {
            toast(R.string.error_phone_and_amount);
            return;
        }
        if (!PhoneNumbers.isValid(phone)) {
            toast(R.string.error_phone_length);
            return;
        }
        if (carrier == Carrier.NONE) {
            toast(R.string.error_no_carrier);
            return;
        }
        String pin = prefs.pinFor(carrier);
        if (pin.isEmpty()) {
            toast(R.string.carrier_not_configured);
            return;
        }

        final int dialledCarrier = carrier;
        sendUssd(UssdCodes.topup(dialledCarrier, pin, phone, amount), () -> {
            history.recordTopup(phone, dialledCarrier, amount);
            prefs.putString(SecurePrefs.LAST_PHONE, phone);
            pendingPhone = "";
            pendingAmount = "";
            setText(txtPhone, "");
            setText(txtCost, "");
            focusField(FIELD_PHONE);
            clearQuickDial();
        });
    }

    /** Running totals drive the quick-dial ranking and start fresh each year. */
    private void resetAmountsOnNewYear() {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        int storedYear = prefs.getInt(SecurePrefs.ROLL_YEAR, 0);
        if (storedYear == 0) {
            prefs.putInt(SecurePrefs.ROLL_YEAR, thisYear);
            return;
        }
        if (storedYear != thisYear) {
            prefs.putInt(SecurePrefs.ROLL_YEAR, thisYear);
            history.resetAmounts();
        }
    }
}
