package com.mondaro.easytopup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mondaro.easytopup.data.Carrier;
import com.mondaro.easytopup.telephony.UssdCodes;
import com.mondaro.easytopup.ui.BaseFragment;

/** Dials each carrier's balance enquiry code. */
public class ChkBalanceFragment extends BaseFragment {

    private static final int[] ROW_IDS = {
            R.id.chkbalAIS, R.id.chkbalDTAC, R.id.chkbalTRUE, R.id.chkbalCAT};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chkbal, container, false);
        lockPortrait();

        if (!prefs().isConfigured()) {
            toast(R.string.not_configured_yet);
            ((MainActivity) requireActivity()).displayView(MainActivity.VIEW_SETTING);
            return root;
        }

        for (int id = Carrier.AIS; id <= Carrier.CAT; id++) {
            final int carrier = id;
            View row = root.findViewById(ROW_IDS[id - 1]);
            boolean enabled = !prefs().pinFor(carrier).isEmpty();
            row.setVisibility(enabled ? View.VISIBLE : View.GONE);
            row.setOnClickListener(v -> sendUssd(UssdCodes.balance(carrier), null));
        }

        return root;
    }
}
