package com.mondaro.easytopup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mondaro.easytopup.R;

public class ChkBalanceFragment extends Fragment {
    LinearLayout imgAIS,imgDTAC,imgTRUE,imgCAT;
    String USERPIN_AIS, USERPIN_DTAC, USERPIN_TRUE, USERPIN_CAT;
    final private int REQUEST_CALL_PHONE_PERMISSIONS = 123;

    public ChkBalanceFragment(){}

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chkbal, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SharedPreferences sharedPref = this.getActivity().getPreferences(Context.MODE_PRIVATE);

        USERPIN_AIS = sharedPref.getString("UID_AIS", "");
        USERPIN_DTAC = sharedPref.getString("UID_DTAC", "");
        USERPIN_TRUE = sharedPref.getString("UID_TRUE", "");
        USERPIN_CAT = sharedPref.getString("UID_CAT", "");
        String getCheck = sharedPref.getString("CHK_OK","");

        if(getCheck.equals("")){
            Toast.makeText(getActivity(), "ผลการตรวจสอบ :\r\nเครื่องนี้ยังไม่ได้ทำรายการตั้งค่าคะ\r\n\n", Toast.LENGTH_LONG).show();
            ((MainActivity) getActivity()).displayView(4);
        }else{
            imgAIS = (LinearLayout) rootView.findViewById(R.id.chkbalAIS);
            imgDTAC = (LinearLayout) rootView.findViewById(R.id.chkbalDTAC);
            imgTRUE = (LinearLayout) rootView.findViewById(R.id.chkbalTRUE);
            imgCAT = (LinearLayout) rootView.findViewById(R.id.chkbalCAT);

            if(USERPIN_CAT.equals("")){
                imgCAT.setVisibility(View.GONE);
            }else{
                imgCAT.setVisibility(View.VISIBLE);
            }
            if(USERPIN_TRUE.equals("")){
                imgTRUE.setVisibility(View.GONE);
            }else{
                imgTRUE.setVisibility(View.VISIBLE);
            }
            if(USERPIN_DTAC.equals("")){
                imgDTAC.setVisibility(View.GONE);
            }else{
                imgDTAC.setVisibility(View.VISIBLE);
            }
            if(USERPIN_AIS.equals("")){
                imgAIS.setVisibility(View.GONE);
            }else{
                imgAIS.setVisibility(View.VISIBLE);
            }

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
            imgCAT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chkBalance(4);
                }
            });
        }

        return rootView;
    }
    private void chkBalance(int i) {
        int hasCallPhonePermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CALL_PHONE);
        if (hasCallPhonePermission != PackageManager.PERMISSION_GRANTED){
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CALL_PHONE)) {
                showMessageOK("ต้องการยืนยันการอนุญาตเข้าถึง\n\n[ระบบการโทร]\n\nเพื่อให้แอปพลิเคชันทำงานได้",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[] {Manifest.permission.CALL_PHONE},
                                        REQUEST_CALL_PHONE_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PHONE_PERMISSIONS);
            return;
        }else{
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String txtTel="";
                switch (i){
                    case 1: txtTel = "*533" + "%23";break;
                    case 2: txtTel = "*211*2" + "%23";break;
                    case 3: txtTel = "*123" + "%23";break;
                    case 4:txtTel = "*902" + "%23";break;
                }
                ((MainActivity) getActivity()).displayView(0);
                callIntent.setData(Uri.parse("tel:" + txtTel));
                startActivity(callIntent);
            } catch (ActivityNotFoundException activityException) {
                Log.d("dialing-example", "Call failed", activityException);
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CALL_PHONE_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Do Nothing
            } else {
                Toast.makeText(getActivity(), "CALL_PHONE Denied", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            try {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(this).attach(this).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            try {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(this).attach(this).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}