package com.mondaro.easytopup;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by MondAro on 11/4/2559.
 */
public class Helpmpay1Fragment extends Fragment {
    Button closePop;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_helpmpay1, container, false);
        closePop = (Button)rootView.findViewById(R.id.button_close_helpmpay);

        closePop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).displayView(3);
            }
        });

        return rootView;
    }
}