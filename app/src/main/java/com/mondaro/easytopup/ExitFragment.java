package com.mondaro.easytopup;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mondaro.easytopup.R;

public class ExitFragment extends Fragment {

    Button btnExit,btnCancel;
    SharedPreferences sharedPref;

    public ExitFragment(){}

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_exit, container, false);

        sharedPref = this.getActivity().getPreferences(Context.MODE_PRIVATE);

        btnExit = (Button) rootView.findViewById(R.id.buttonExit);
        btnCancel = (Button) rootView.findViewById(R.id.buttonCancel);

        btnExit.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            getActivity().moveTaskToBack(true);
            getActivity().finish();
        }});
        btnCancel.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            if(sharedPref.getString("CHK","").equals("TRUE")){
                ((MainActivity) getActivity()).displayView(0);
            }else{
                ((MainActivity) getActivity()).displayView(2);
            }
        }});

        return rootView;
    }
}