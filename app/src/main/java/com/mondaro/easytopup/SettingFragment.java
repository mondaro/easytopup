package com.mondaro.easytopup;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mondaro.easytopup.data.Carrier;
import com.mondaro.easytopup.data.SecurePrefs;
import com.mondaro.easytopup.telephony.UssdDialer;
import com.mondaro.easytopup.ui.BaseFragment;

/** Where the agent PINs, the colour theme and the dial mode are configured. */
public class SettingFragment extends BaseFragment {

    private static final int MIN_PIN_LENGTH = 4;

    private EditText pinAis;
    private EditText pinDtac;
    private EditText pinTrue;
    private CheckBox useAis;
    private CheckBox useDtac;
    private CheckBox useTrue;
    private CheckBox useCat;
    private CheckBox themeDefault;
    private CheckBox themePastel;
    private CheckBox legacyDialer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, container, false);
        lockPortrait();

        pinAis = root.findViewById(R.id.editTextAIS);
        pinDtac = root.findViewById(R.id.editTextDTAC);
        pinTrue = root.findViewById(R.id.editTextTRUE);
        useAis = root.findViewById(R.id.checkBoxAIS);
        useDtac = root.findViewById(R.id.checkBoxDTAC);
        useTrue = root.findViewById(R.id.checkBoxTRUE);
        useCat = root.findViewById(R.id.checkBoxCAT);
        themeDefault = root.findViewById(R.id.checkBoxThemeDefault);
        themePastel = root.findViewById(R.id.checkBoxThemePastel);
        legacyDialer = root.findViewById(R.id.checkBoxLegacyDialer);

        SecurePrefs prefs = prefs();
        bindCarrier(useAis, pinAis, prefs.getString(SecurePrefs.UID_AIS));
        bindCarrier(useDtac, pinDtac, prefs.getString(SecurePrefs.UID_DTAC));
        bindCarrier(useTrue, pinTrue, prefs.getString(SecurePrefs.UID_TRUE));
        useCat.setChecked(prefs.has(SecurePrefs.UID_CAT));

        String theme = prefs.getString(SecurePrefs.THEME);
        boolean useDefaultTheme = "A".equals(theme);
        themeDefault.setChecked(useDefaultTheme);
        themePastel.setChecked(!useDefaultTheme);
        themeDefault.setOnClickListener(v -> setTheme(true));
        themePastel.setOnClickListener(v -> setTheme(false));

        // The in-app USSD session only exists from API 26 onwards; below that the
        // system dialer is the only route, so there is nothing to choose.
        View dialModeGroup = root.findViewById(R.id.groupDialMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialModeGroup.setVisibility(View.VISIBLE);
            legacyDialer.setChecked(prefs.getBoolean(SecurePrefs.USE_LEGACY_DIALER, false));
        } else {
            dialModeGroup.setVisibility(View.GONE);
        }

        root.findViewById(R.id.buttonOKSetting).setOnClickListener(v -> save());
        return root;
    }

    private void bindCarrier(@NonNull CheckBox toggle, @NonNull EditText pin,
                             @NonNull String storedPin) {
        boolean enabled = !storedPin.isEmpty();
        toggle.setChecked(enabled);
        pin.setEnabled(enabled);
        pin.setText(storedPin);
        pin.setBackgroundResource(enabled ? R.drawable.border_active : R.drawable.border_inactive);
        pin.addTextChangedListener(new SimpleWatcher(() ->
                pin.setBackgroundResource(pin.getText().toString().trim().length() >= MIN_PIN_LENGTH
                        ? R.drawable.border_active : R.drawable.border_inactive)));
        toggle.setOnClickListener(v -> {
            boolean on = toggle.isChecked();
            pin.setEnabled(on);
            if (on) {
                pin.requestFocus();
            } else {
                pin.setText("");
                pin.setBackgroundResource(R.drawable.border_inactive);
            }
        });
    }

    private void setTheme(boolean useDefault) {
        themeDefault.setChecked(useDefault);
        themePastel.setChecked(!useDefault);
    }

    /**
     * Validates everything first and only then writes. The previous version wrote
     * each carrier as it went and left the "configured" flag in whatever state the
     * last carrier happened to produce.
     */
    private void save() {
        String ais = useAis.isChecked() ? pinAis.getText().toString().trim() : "";
        String dtac = useDtac.isChecked() ? pinDtac.getText().toString().trim() : "";
        String truemove = useTrue.isChecked() ? pinTrue.getText().toString().trim() : "";
        boolean cat = useCat.isChecked();

        if (invalidPin(useAis, ais) || invalidPin(useDtac, dtac) || invalidPin(useTrue, truemove)) {
            toast(R.string.setting_invalid_pin);
            return;
        }
        if (ais.isEmpty() && dtac.isEmpty() && truemove.isEmpty() && !cat) {
            toast(R.string.setting_pick_one);
            return;
        }

        SecurePrefs prefs = prefs();
        prefs.putString(SecurePrefs.UID_AIS, ais);
        prefs.putString(SecurePrefs.UID_DTAC, dtac);
        prefs.putString(SecurePrefs.UID_TRUE, truemove);
        prefs.putString(SecurePrefs.UID_CAT, cat ? Carrier.displayName(Carrier.CAT) : "");
        prefs.putString(SecurePrefs.THEME, themeDefault.isChecked() ? "A" : "B");
        if (UssdDialer.canSendInApp(requireContext())) {
            prefs.putBoolean(SecurePrefs.USE_LEGACY_DIALER, legacyDialer.isChecked());
        }
        prefs.putString(SecurePrefs.CONFIGURED, "TRUE");

        toast(R.string.setting_saved);
        ((MainActivity) requireActivity()).displayView(MainActivity.VIEW_TOPUP);
    }

    private static boolean invalidPin(@NonNull CheckBox toggle, @NonNull String pin) {
        return toggle.isChecked() && pin.length() < MIN_PIN_LENGTH;
    }

    /** Minimal {@link TextWatcher} so the callers only supply the part they need. */
    private static final class SimpleWatcher implements TextWatcher {

        private final Runnable onChanged;

        SimpleWatcher(@NonNull Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onChanged.run();
        }
    }
}
