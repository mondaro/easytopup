package com.mondaro.easytopup;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by MondAro on 11/4/2559.
 */
public class AddOnsFragment extends Fragment {
    String type,am_card,uid1;
    EditText t_am,t_id,t_phone;
    Spinner spn1,spn2;
    Button btnpass1,btnpass2, btnwh1,btnwh2,btnbuycard,btnaddmpayID;
    LinearLayout head1,head2,content1,content2;
    SharedPreferences sharedPref;
    SharedPreferences.Editor edt;

    public AddOnsFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_addons, container, false);
        t_am = (EditText)rootView.findViewById(R.id.editText_Amount);
        t_id  = (EditText)rootView.findViewById(R.id.editText_ID);
        t_phone = (EditText)rootView.findViewById(R.id.editText_phncard);
        spn1 = (Spinner)rootView.findViewById(R.id.spinner);
        spn2 = (Spinner)rootView.findViewById(R.id.spn2);
        btnpass1 = (Button)rootView.findViewById(R.id.button_pass1);
        btnpass2 = (Button)rootView.findViewById(R.id.button_pass2);
        btnwh1 = (Button)rootView.findViewById(R.id.button_what1);
        btnwh2 = (Button)rootView.findViewById(R.id.button_what2);
        btnbuycard = (Button)rootView.findViewById(R.id.button_card);
        btnaddmpayID = (Button)rootView.findViewById(R.id.button_addmPayID);
        head1 = (LinearLayout)rootView.findViewById(R.id.contentH1);
        head2 = (LinearLayout)rootView.findViewById(R.id.contentH2);
        content1 = (LinearLayout)rootView.findViewById(R.id.contentC1);
        content2 = (LinearLayout)rootView.findViewById(R.id.contentC2);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String getmPayID = sharedPref.getString("mpay","");
        uid1 = sharedPref.getString("UID1", "");
        edt = sharedPref.edit();

        if(getmPayID.equals("")){
            Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัส mPay และบันทึกลงระบบด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
        }else{
            t_id.setText(getmPayID.trim());
            t_id.setEnabled(false);
            btnaddmpayID.setVisibility(View.GONE);
        }

        type = "0";
        am_card = "50";

        btnpass1.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            if(t_am.getText().toString().equals("")){
                Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกจำนวนเงินที่ต้องการเติมด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
            }else if(t_id.getText().toString().trim().equals("")){
                Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัส mPay ด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
            }else{
                if(t_id.getText().toString().trim().equals("")){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัสประจำตัว mPay ด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        String txtTel = "*555*86*" + t_am.getText().toString().trim() + "*" + t_id.getText().toString().trim() + "%23";
                        callIntent.setData(Uri.parse("tel:" + txtTel));
                        startActivity(callIntent);
                        //Log.d("dialing-example",txtTel);
                    } catch (ActivityNotFoundException activityException) {
                        Log.d("dialing-example", "Call failed", activityException);
                    }
                    btnpass2.setEnabled(true);
                    btnpass1.setEnabled(false);
                    spn1.setClickable(true);
                }
            }
        }});
        btnpass2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String txtTel = "*555*" + type + "%23";
                callIntent.setData(Uri.parse("tel:" + txtTel));
                startActivity(callIntent);
                //Log.d("dialing-example",txtTel);
            } catch (ActivityNotFoundException activityException) {
                Log.d("dialing-example", "Call failed", activityException);
            }
            t_am.setText("");
            t_id.setText("");
            btnpass1.setEnabled(true);
            btnpass2.setEnabled(false);
            spn1.setClickable(false);
            ((MainActivity) getActivity()).displayView(0);
            }
        });
        btnbuycard.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            if(t_id.getText().toString().trim().equals("")){
                Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัส mPay ด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
            }else{
                if(t_phone.getText().toString().trim().equals("")){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกหมายเลขโทรศัพท์ที่ต้องการซื้อบัตรเงินสดด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
                }else if(t_phone.getText().length()<10){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกหมายเลขโทรศัพท์ให้ครบ 10 หลักด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        String txtTel = "*228*" + uid1 + "*" + t_phone.getText().toString().trim()
                                + "*" + am_card + "%23";
                        Log.d("",txtTel);
                        callIntent.setData(Uri.parse("tel:" + txtTel));
                        startActivity(callIntent);
                        //Log.d("dialing-example",txtTel);
                    } catch (ActivityNotFoundException activityException) {
                        Log.d("dialing-example", "Call failed", activityException);
                    }
                    t_phone.setText("");
                    ((MainActivity) getActivity()).displayView(0);
                }
            }
        }
        });
        spn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch (position) {
                    case 0:type = "0";break;
                    case 1:type = "1";break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        spn2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch (position) {
                    case 0:am_card = "50";break;
                    case 1:am_card = "100";break;
                    case 2:am_card = "300";break;
                    case 3:am_card = "500";break;
                    case 4:am_card = "1000";break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        btnaddmpayID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(t_id.getText().toString().trim().equals("")){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัสประจำตัว mPay ก่อนทำการบันทึกคะ\r\n", Toast.LENGTH_SHORT).show();
                }else if(t_id.getText().length()<4){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัสประจำตัว mPay ให้ครบ 4 หลักด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
                }else{
                    edt.putString("mpay",t_id.getText().toString().trim());
                    edt.apply();
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nบันทึกรหัสประจำตัว mPay เรียบร้อยแล้วคะ\r\n", Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).displayView(3);
                }
            }
        });
        btnwh1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpmpay1Fragment fragmentPop = new Helpmpay1Fragment();
                FragmentTransaction ft1 = getFragmentManager().beginTransaction();
                ft1.replace(R.id.contentL, fragmentPop);
                ft1.commit();
            }
        });
        btnwh2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpmpay2Fragment fragmentPop = new Helpmpay2Fragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.contentL, fragmentPop);
                ft2.commit();
            }
        });
        head1.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            if(content1.getVisibility() == View.VISIBLE){
                content1.setVisibility(View.GONE);
            }else{
                content1.setVisibility(View.VISIBLE);
                content2.setVisibility(View.GONE);
            }
        }
        });
        head2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            if(content2.getVisibility() == View.VISIBLE){
                content2.setVisibility(View.GONE);
            }else{
                content1.setVisibility(View.GONE);
                content2.setVisibility(View.VISIBLE);
            }
        }
        });

        return rootView;
    }
}