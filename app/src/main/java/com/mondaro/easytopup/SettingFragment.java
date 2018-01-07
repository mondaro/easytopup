package com.mondaro.easytopup;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SettingFragment extends Fragment {

    String uidAIS, uidTRUE, uidDTAC;
    EditText txtUID_AIS, txtUID_TRUE, txtUID_DTAC;
    CheckBox chkbAIS, chkbTRUE, chkbDTAC;
    Button btnSet;
    SharedPreferences.Editor edt;

    public SettingFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View settingView = inflater.inflate(R.layout.fragment_setting, container, false);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        final SharedPreferences sharedPrf = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        edt = sharedPrf.edit();

        uidAIS = sharedPrf.getString("UID1", "");
        uidTRUE = sharedPrf.getString("UID2","");
        uidDTAC = sharedPrf.getString("UID3","");

        txtUID_AIS = (EditText) settingView.findViewById(R.id.editTextAIS);
        txtUID_TRUE = (EditText) settingView.findViewById(R.id.editTextTRUE);
        txtUID_DTAC = (EditText) settingView.findViewById(R.id.editTextDTAC);

        chkbAIS = (CheckBox) settingView.findViewById(R.id.checkBoxAIS);
        chkbTRUE = (CheckBox) settingView.findViewById(R.id.checkBoxTRUE);
        chkbDTAC = (CheckBox) settingView.findViewById(R.id.checkBoxDTAC);

        if(!uidAIS.equals("")){
            chkbAIS.setChecked(true);
            txtUID_AIS.setText(uidAIS);
            txtUID_AIS.setBackgroundResource(R.drawable.border_active);
        }
        if(!uidTRUE.equals("")){
            chkbTRUE.setChecked(true);
            txtUID_TRUE.setText(uidTRUE);
            txtUID_TRUE.setBackgroundResource(R.drawable.border_active);
        }
        if(!uidDTAC.equals("")){
            chkbDTAC.setChecked(true);
            txtUID_DTAC.setText(uidDTAC);
            txtUID_DTAC.setBackgroundResource(R.drawable.border_active);
        }


        btnSet = (Button) settingView.findViewById(R.id.buttonOKSetting);

        chkbAIS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkbAIS.isChecked()){
                    txtUID_AIS.setEnabled(true);
                    chkbTRUE.performClick();
                    txtUID_AIS.requestFocus();
                    txtUID_AIS.setBackgroundResource(R.drawable.border_active);
                    chkbTRUE.performClick();
                    imm.showSoftInput(txtUID_AIS, InputMethodManager.SHOW_IMPLICIT);
                }else{
                    txtUID_AIS.setBackgroundResource(R.drawable.border_inactive);
                    txtUID_AIS.setText("");
                    txtUID_AIS.setEnabled(false);
                    edt.putString("UID1", "");
                    edt.putString("CHK", "");
                    edt.apply();
                }

        }});
        chkbTRUE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkbTRUE.isChecked()){
                    txtUID_TRUE.setEnabled(true);
                    txtUID_TRUE.requestFocus();
                    imm.showSoftInput(txtUID_TRUE, InputMethodManager.SHOW_IMPLICIT);
                    txtUID_TRUE.setBackgroundResource(R.drawable.border_active);
                }else{
                    txtUID_TRUE.setBackgroundResource(R.drawable.border_inactive);
                    txtUID_TRUE.setText("");
                    txtUID_TRUE.setEnabled(false);
                    edt.putString("UID2", "");
                    edt.putString("CHK", "");
                    edt.apply();
                }
            }});
        chkbDTAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkbDTAC.isChecked()){
                    txtUID_DTAC.setEnabled(true);
                    txtUID_DTAC.requestFocus();
                    imm.showSoftInput(txtUID_DTAC, InputMethodManager.SHOW_IMPLICIT);
                    txtUID_DTAC.setBackgroundResource(R.drawable.border_active);
                }else{
                    txtUID_DTAC.setBackgroundResource(R.drawable.border_inactive);
                    txtUID_DTAC.setText("");
                    txtUID_DTAC.setEnabled(false);
                    edt.putString("UID3", "");
                    edt.putString("CHK", "");
                    edt.apply();
                }
            }});

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkbAIS.isChecked()){
                    if(txtUID_AIS.getText().toString().trim().equals("") || txtUID_AIS.getText().toString().trim().length() < 4){
                        edt.putString("CHK", "");
                        edt.apply();
                        Toast.makeText(getActivity(), "ผลการตรวจสอบ :\r\nกรุณากรอกข้อมูลให้ถูกต้องก่อนคะ\r\n\n", Toast.LENGTH_SHORT).show();
                    }else{
                        edt.putString("UID1", txtUID_AIS.getText().toString().trim());
                        edt.putString("CHK", "PASS");
                        edt.apply();
                    }
                }
                if(chkbTRUE.isChecked()){
                    if(txtUID_TRUE.getText().toString().trim().equals("") || txtUID_TRUE.getText().toString().trim().length() < 4){
                        edt.putString("CHK", "");
                        edt.apply();
                        Toast.makeText(getActivity(), "ผลการตรวจสอบ :\r\nกรุณากรอกข้อมูลให้ถูกต้องก่อนคะ\r\n\n", Toast.LENGTH_SHORT).show();
                    }else{
                        edt.putString("UID2", txtUID_TRUE.getText().toString().trim());
                        edt.putString("CHK", "PASS");
                        edt.apply();
                    }
                }
                if(chkbDTAC.isChecked()){
                    if(txtUID_DTAC.getText().toString().trim().equals("") || txtUID_DTAC.getText().toString().trim().length() < 4){
                        edt.putString("CHK", "");
                        edt.apply();
                        Toast.makeText(getActivity(), "ผลการตรวจสอบ :\r\nกรุณากรอกข้อมูลให้ถูกต้องก่อนคะ\r\n\n", Toast.LENGTH_SHORT).show();
                    }else{
                        edt.putString("UID3", txtUID_DTAC.getText().toString().trim());
                        edt.putString("CHK", "PASS");
                        edt.apply();
                    }
                }
                if(sharedPrf.getString("CHK", "").equals("PASS")){
                    edt.putString("CHK", "TRUE");
                    edt.apply();
                    ((MainActivity) getActivity()).displayView(0);
                }

            }
        });

        return settingView;
    }
}