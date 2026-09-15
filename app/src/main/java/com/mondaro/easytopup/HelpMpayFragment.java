package com.mondaro.easytopup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * The four mPay help panels, which differed only in the layout they inflated.
 * Closing pops the panel instead of rebuilding the whole add-ons screen, so the
 * fields the user had already filled in survive.
 */
public class HelpMpayFragment extends Fragment {

    private static final String ARG_PAGE = "page";

    @LayoutRes
    private static final int[] LAYOUTS = {
            R.layout.fragment_helpmpay1,
            R.layout.fragment_helpmpay2,
            R.layout.fragment_helpmpay3,
            R.layout.fragment_helpmpay4};

    @NonNull
    public static HelpMpayFragment newInstance(int page) {
        HelpMpayFragment fragment = new HelpMpayFragment();
        Bundle args = new Bundle(1);
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        int page = requireArguments().getInt(ARG_PAGE, 0);
        if (page < 0 || page >= LAYOUTS.length) {
            page = 0;
        }
        View root = inflater.inflate(LAYOUTS[page], container, false);
        root.findViewById(R.id.button_close_helpmpay).setOnClickListener(v -> close());
        return root;
    }

    private void close() {
        FragmentManager manager = getParentFragmentManager();
        if (!manager.isStateSaved()) {
            manager.beginTransaction().remove(this).commit();
        }
    }
}
