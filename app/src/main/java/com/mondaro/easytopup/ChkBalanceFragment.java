package com.mondaro.easytopup;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mondaro.easytopup.R;

public class ChkBalanceFragment extends Fragment {

    ImageView imgAIS,imgDTAC,imgTRUE;
    public ChkBalanceFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sharedPref = this.getActivity().getPreferences(Context.MODE_PRIVATE);

        View rootView = inflater.inflate(R.layout.fragment_chkbal, container, false);


        String getCheck = sharedPref.getString("CHK","");
        if(getCheck.equals("")){
            Toast.makeText(getActivity(), "ผลการตรวจสอบ :\r\nเครื่องนี้ยังไม่ได้ทำรายการตั้งค่าคะ\r\n\n", Toast.LENGTH_LONG).show();
            ((MainActivity) getActivity()).displayView(4);
        }else{
            imgAIS = (ImageView) rootView.findViewById(R.id.imageViewAIS);
            imgDTAC = (ImageView) rootView.findViewById(R.id.imageViewDTAC);
            imgTRUE = (ImageView) rootView.findViewById(R.id.imageViewTRUE);

            imgAIS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chkBalance(1);}});
            imgDTAC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chkBalance(2);}});
            imgTRUE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chkBalance(3);
                }
            });
        }

        return rootView;
    }
    private void chkBalance(int i) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String txtTel="";
            switch (i){
                case 1: txtTel = "*533" + "%23";break;
                case 2: txtTel = "*211*2" + "%23";break;
                case 3: txtTel = "*933" + "%23";break;
            }
            ((MainActivity) getActivity()).displayView(0);
            callIntent.setData(Uri.parse("tel:" + txtTel));
            startActivity(callIntent);
        } catch (ActivityNotFoundException activityException) {
            Log.d("dialing-example", "Call failed", activityException);
        }
    }
}