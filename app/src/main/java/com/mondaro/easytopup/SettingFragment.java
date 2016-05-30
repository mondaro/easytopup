package com.mondaro.easytopup;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.mondaro.easytopup.R;

public class SettingFragment extends Fragment {

    String uid1,uid2,uid3;
    EditText txtUID1,txtUID2,txtUID3;
    CheckBox chkb1,chkb2,chkb3;
    Button btnSet;
    SharedPreferences.Editor edt;

    public SettingFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Toast.makeText(getActivity(), "คำเตือน :\r\nกรุณาเลือกรายการระบบเติมเงินที่เครื่องเติมเงินรองรับ\nและทำการตั้งต่าหมายเลขประจำตัวคนเติมเงินให้ตรงตามระบบด้วยคะ",
                Toast.LENGTH_LONG).show();

        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        final SharedPreferences sharedPrf = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        edt = sharedPrf.edit();

        uid1 = sharedPrf.getString("UID1", "");
        uid2 = sharedPrf.getString("UID2","");
        uid3 = sharedPrf.getString("UID3","");

        txtUID1 = (EditText) rootView.findViewById(R.id.editTextUID1);
        txtUID2 = (EditText) rootView.findViewById(R.id.editTextUID2);
        txtUID3 = (EditText) rootView.findViewById(R.id.editTextUID3);

        chkb1 = (CheckBox) rootView.findViewById(R.id.checkBox1);
        chkb2 = (CheckBox) rootView.findViewById(R.id.checkBox2);
        chkb3 = (CheckBox) rootView.findViewById(R.id.checkBox3);

        if(!uid1.equals("")){
            chkb1.setChecked(true);
            txtUID1.setText(uid1);
            txtUID1.setBackgroundResource(R.drawable.border_active);
        }
        if(!uid2.equals("")){
            chkb2.setChecked(true);
            txtUID2.setText(uid2);
            txtUID2.setBackgroundResource(R.drawable.border_active);
        }
        if(!uid3.equals("")){
            chkb3.setChecked(true);
            txtUID3.setText(uid3);
            txtUID3.setBackgroundResource(R.drawable.border_active);
        }


        btnSet = (Button) rootView.findViewById(R.id.buttonOKSetting);

        chkb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkb1.isChecked()){
                    txtUID1.setEnabled(true);
                    chkb2.performClick();
                    txtUID1.setBackgroundResource(R.drawable.border_active);
                    chkb2.performClick();
                    txtUID1.requestFocus();
                    imm.showSoftInput(txtUID1, InputMethodManager.SHOW_IMPLICIT);
                }else{
                    txtUID1.setBackgroundResource(R.drawable.border_inactive);
                    txtUID1.setText("");
                    txtUID1.setEnabled(false);
                }

        }});
        chkb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkb2.isChecked()){
                    txtUID2.setEnabled(true);
                    txtUID2.requestFocus();
                    imm.showSoftInput(txtUID2, InputMethodManager.SHOW_IMPLICIT);
                    txtUID2.setBackgroundResource(R.drawable.border_active);
                }else{
                    txtUID2.setBackgroundResource(R.drawable.border_inactive);
                    txtUID2.setText("");
                    txtUID2.setEnabled(false);
                }
            }});
        chkb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkb3.isChecked()){
                    txtUID3.setEnabled(true);
                    txtUID3.requestFocus();
                    imm.showSoftInput(txtUID3, InputMethodManager.SHOW_IMPLICIT);
                    txtUID3.setBackgroundResource(R.drawable.border_active);
                }else{
                    txtUID3.setBackgroundResource(R.drawable.border_inactive);
                    txtUID3.setText("");
                    txtUID3.setEnabled(false);
                }
            }});

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edt.putString("UID1",txtUID1.getText().toString().trim());
                edt.putString("UID2",txtUID2.getText().toString().trim());
                edt.putString("UID3", txtUID3.getText().toString().trim());
                if(!chkb1.isChecked() && !chkb2.isChecked() && !chkb3.isChecked()){
                    edt.putString("CHK", "");
                }else{
                    edt.putString("CHK", "TRUE");
                    edt.apply();
                    ((MainActivity) getActivity()).displayView(0);
                }

            }
        });

        return rootView;
    }
}