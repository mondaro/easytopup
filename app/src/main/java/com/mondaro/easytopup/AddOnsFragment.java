package com.mondaro.easytopup;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class AddOnsFragment extends Fragment {
    String getmPayID,type,am_card, uidAIS, opBill,opDays,txtphoneContact,phoneTDRecent;
    EditText t_am,t_id, t_phoneCard,t_phoneBill,t_phoneDays;
    Spinner spn1,spn2,spn3,spn4;
    Button btnchkbal,btnchkcom,btnpass1,btnpass2, btnwh1,btnwh2,btnwh3,btnwh4,
            btnbuycard,btnaddmpayID,btnchkbill,btnpaybill,btntransdays,
            btnCntCard,btnCntBill,btnCntDays;
    ImageButton imgbtnRecentTD,imgbtnResetPB;
    LinearLayout head0,head1,head2,head3,head4,content0,content1,content2,content3,content4;
    SharedPreferences sharedPref;
    SharedPreferences.Editor edt;
    final private int REQUEST_READ_CONTACTS_PERMISSIONS = 112;
    final private int REQUEST_CALL_PHONE_PERMISSIONS = 123;

    public AddOnsFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_addons, container, false);
        t_am = (EditText)rootView.findViewById(R.id.editText_Amount);
        t_id  = (EditText)rootView.findViewById(R.id.editText_ID);
        t_phoneCard = (EditText)rootView.findViewById(R.id.editText_phonecard);
        t_phoneBill = (EditText)rootView.findViewById(R.id.editText_phonebill);
        t_phoneDays = (EditText)rootView.findViewById(R.id.editText_phonedays);
        spn1 = (Spinner)rootView.findViewById(R.id.spn1);
        spn2 = (Spinner)rootView.findViewById(R.id.spn2);
        spn3 = (Spinner)rootView.findViewById(R.id.spn3);
        spn4 = (Spinner)rootView.findViewById(R.id.spn4);
        btnchkbal = (Button)rootView.findViewById(R.id.button_chkBalance);
        btnchkcom = (Button)rootView.findViewById(R.id.button_chkCommission);
        btnpass1 = (Button)rootView.findViewById(R.id.button_pass1);
        btnpass2 = (Button)rootView.findViewById(R.id.button_pass2);
        btnwh1 = (Button)rootView.findViewById(R.id.button_what1);
        btnwh2 = (Button)rootView.findViewById(R.id.button_what2);
        btnwh3 = (Button)rootView.findViewById(R.id.button_what3);
        btnwh4 = (Button)rootView.findViewById(R.id.button_what4);
        btnbuycard = (Button)rootView.findViewById(R.id.button_card);
        btnaddmpayID = (Button)rootView.findViewById(R.id.button_addmPayID);
        btnchkbill = (Button)rootView.findViewById(R.id.button_chkbill);
        btnpaybill = (Button)rootView.findViewById(R.id.button_paybill);
        btntransdays = (Button)rootView.findViewById(R.id.button_transDays);
        btnCntCard = (Button)rootView.findViewById(R.id.button_contactBuyCard);
        btnCntBill = (Button)rootView.findViewById(R.id.button_contactBill);
        btnCntDays = (Button)rootView.findViewById(R.id.button_contactTransDay);
        imgbtnRecentTD = (ImageButton)rootView.findViewById(R.id.imageButton_RepeatPhone);
        imgbtnResetPB = (ImageButton)rootView.findViewById(R.id.imageButton_ResetPhoneBilll);
        head0 = (LinearLayout)rootView.findViewById(R.id.contentH0);
        head1 = (LinearLayout)rootView.findViewById(R.id.contentH1);
        head2 = (LinearLayout)rootView.findViewById(R.id.contentH2);
        head3 = (LinearLayout)rootView.findViewById(R.id.contentH3);
        head4 = (LinearLayout)rootView.findViewById(R.id.contentH4);
        content0 = (LinearLayout)rootView.findViewById(R.id.contentC0);
        content1 = (LinearLayout)rootView.findViewById(R.id.contentC1);
        content2 = (LinearLayout)rootView.findViewById(R.id.contentC2);
        content3 = (LinearLayout)rootView.findViewById(R.id.contentC3);
        content4 = (LinearLayout)rootView.findViewById(R.id.contentC4);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        getmPayID = sharedPref.getString("MPAY","");
        uidAIS = sharedPref.getString("UID_AIS", "");
        edt = sharedPref.edit();

        if(getmPayID.equals("")){
            Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัส mPay และบันทึกลงระบบด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
        }else{
            t_id.setText(getmPayID.trim());
            t_id.setEnabled(false);
            btnaddmpayID.setText("-");
        }

        type = "0";
        am_card = "50";
        opBill = "0";
        opDays = "5";

        btnchkbal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    String txtTel = "*556" + "%23";
                    callIntent.setData(Uri.parse("tel:" + txtTel));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Log.d("dialing-example", "Call failed", activityException);
                }
            }
        });

        btnchkcom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    String txtTel = "*556*9" + "%23";
                    callIntent.setData(Uri.parse("tel:" + txtTel));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Log.d("dialing-example", "Call failed", activityException);
                }
            }
        });

        btnCntCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact(v,1);
            }
        });

        btnCntBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact(v,2);
            }
        });

        btnCntDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact(v,3);
            }
        });

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
            } catch (ActivityNotFoundException activityException) {
                Log.d("dialing-example", "Call failed", activityException);
            }
            ((MainActivity) getActivity()).displayView(0);
        }});

        btnbuycard.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            if(t_id.getText().toString().trim().equals("")){
                Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัส mPay ด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
            }else{
                if(t_phoneCard.getText().toString().trim().equals("")){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกหมายเลขโทรศัพท์ที่ต้องการซื้อบัตรเงินสดด้วยคะ\r\n",
                            Toast.LENGTH_SHORT).show();
                }else if(t_phoneCard.getText().length()<10){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกหมายเลขโทรศัพท์ให้ครบ 10 หลักด้วยคะ\r\n",
                            Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        String txtTel = "*228*" + uidAIS + "*" + t_phoneCard.getText().toString().trim()
                                + "*" + am_card + "%23";
                        callIntent.setData(Uri.parse("tel:" + txtTel));
                        startActivity(callIntent);
                    } catch (ActivityNotFoundException activityException) {
                        Log.d("dialing-example", "Call failed", activityException);
                    }
                }
            }
        }});

        btnchkbill.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            if(t_id.getText().toString().trim().equals("")){
                Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัส mPay ด้วยคะ\r\n", Toast.LENGTH_SHORT).show();
            }else{
                if(t_phoneBill.getText().toString().trim().equals("")){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกหมายเลขโทรศัพท์ที่ต้องการชำระด้วยคะ\r\n",
                            Toast.LENGTH_SHORT).show();
                }else if(t_phoneBill.getText().length()<10){
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกหมายเลขโทรศัพท์ให้ครบ 10 หลักด้วยคะ\r\n",
                            Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        String txtTel = "*556*1*" + getmPayID + "*" + t_phoneBill.getText().toString().trim() + "%23";
                        callIntent.setData(Uri.parse("tel:" + txtTel));
                        startActivity(callIntent);
                    } catch (ActivityNotFoundException activityException) {
                        Log.d("dialing-example", "Call failed", activityException);
                    }
                    btnchkbill.setEnabled(false);
                    btnpaybill.setEnabled(true);
                    spn3.setClickable(true);
                }
            }
        }});

        btnpaybill.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String txtTel = "*556*1*" + opBill + "%23";
                callIntent.setData(Uri.parse("tel:" + txtTel));
                startActivity(callIntent);
            } catch (ActivityNotFoundException activityException) {
                Log.d("dialing-example", "Call failed", activityException);
            }
            imgbtnResetPB.performClick();
        }});

        btntransdays.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            if(t_phoneDays.getText().toString().trim().equals("")){
                Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกหมายเลขโทรศัพท์ที่ต้องการโอนวันให้ด้วยคะ\r\n",
                        Toast.LENGTH_SHORT).show();
            }else if(t_phoneDays.getText().length()<10){
                Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกหมายเลขโทรศัพท์ให้ครบ 10 หลักด้วยคะ\r\n",
                        Toast.LENGTH_SHORT).show();
            }else{
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    String txtTel = "*140*2*" + t_phoneDays.getText().toString().trim()
                            + "*" + opDays + "%23";
                    callIntent.setData(Uri.parse("tel:" + txtTel));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Log.d("dialing-example", "Call failed", activityException);
                }
            }
            edt.putString("tdrecent",t_phoneDays.getText().toString().trim());
            edt.apply();
        }});

        imgbtnRecentTD.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            phoneTDRecent = sharedPref.getString("tdrecent", "");
            t_phoneDays.setText(phoneTDRecent);
        }});

        imgbtnResetPB.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            t_phoneBill.setText("");
            t_phoneBill.setFocusableInTouchMode(true);
            btnchkbill.setEnabled(true);
            btnpaybill.setEnabled(false);
            spn3.setClickable(false);
        }});

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

        spn3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch (position) {
                    case 0:
                        opBill = "0";break;
                    case 1:
                        opBill = "1";break;
                    case 2:
                        opBill = "2";break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        spn4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch (position) {
                    case 0:
                        opDays = "5";break;
                    case 1:
                        opDays = "10";break;
                    case 2:
                        opDays = "20";break;
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
                if(btnaddmpayID.getText().equals("-")){
                    t_id.setText("");
                    getmPayID = "";
                    edt.putString("MPAY","");
                    edt.apply();
                    ((MainActivity) getActivity()).displayView(3);
                }else{
                    if(t_id.getText().toString().trim().equals("")){
                        Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัสประจำตัว mPay ก่อนทำการบันทึกคะ\r\n",
                                Toast.LENGTH_SHORT).show();
                    }else if(t_id.getText().length()<4){
                        Toast.makeText(getActivity(), "แจ้งเตือน :\r\nกรุณากรอกรหัสประจำตัว mPay ให้ครบ 4 หลักด้วยคะ\r\n",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        edt.putString("MPAY",t_id.getText().toString().trim());
                        edt.apply();
                        Toast.makeText(getActivity(), "แจ้งเตือน :\r\nบันทึกรหัสประจำตัว mPay เรียบร้อยแล้วคะ\r\n",
                                Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).displayView(3);
                    }
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

        btnwh3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpmpay3Fragment fragmentPop = new Helpmpay3Fragment();
                FragmentTransaction ft3 = getFragmentManager().beginTransaction();
                ft3.replace(R.id.contentL, fragmentPop);
                ft3.commit();
            }
        });

        btnwh4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpmpay4Fragment fragmentPop = new Helpmpay4Fragment();
                FragmentTransaction ft4 = getFragmentManager().beginTransaction();
                ft4.replace(R.id.contentL, fragmentPop);
                ft4.commit();
            }
        });

        head0.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
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
                if(content0.getVisibility() == View.VISIBLE){
                    content0.setVisibility(View.GONE);
                }else{
                    content0.setVisibility(View.VISIBLE);
                    content1.setVisibility(View.GONE);
                    content2.setVisibility(View.GONE);
                    content3.setVisibility(View.GONE);
                    content4.setVisibility(View.GONE);
                }
            }
        }});

        head1.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
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
                if(content1.getVisibility() == View.VISIBLE){
                    content1.setVisibility(View.GONE);
                }else{
                    content0.setVisibility(View.GONE);
                    content1.setVisibility(View.VISIBLE);
                    content2.setVisibility(View.GONE);
                    content3.setVisibility(View.GONE);
                    content4.setVisibility(View.GONE);
                }
            }
        }});

        head2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
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
                if(content2.getVisibility() == View.VISIBLE){
                    content2.setVisibility(View.GONE);
                }else{
                    content0.setVisibility(View.GONE);
                    content1.setVisibility(View.GONE);
                    content2.setVisibility(View.VISIBLE);
                    content3.setVisibility(View.GONE);
                    content4.setVisibility(View.GONE);
                }
            }
        }});

        head3.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
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
                if(content3.getVisibility() == View.VISIBLE){
                    content3.setVisibility(View.GONE);
                }else{
                    content0.setVisibility(View.GONE);
                    content1.setVisibility(View.GONE);
                    content2.setVisibility(View.GONE);
                    content3.setVisibility(View.VISIBLE);
                    content4.setVisibility(View.GONE);
                }
            }
        }});

        head4.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
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
                if(content4.getVisibility() == View.VISIBLE){
                    content4.setVisibility(View.GONE);
                }else{
                    content0.setVisibility(View.GONE);
                    content1.setVisibility(View.GONE);
                    content2.setVisibility(View.GONE);
                    content3.setVisibility(View.GONE);
                    content4.setVisibility(View.VISIBLE);
                }
            }
        }});

        return rootView;
    }

    private void pickContact(View v,int target) {
        int hasReadContactsPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS);
        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED){
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {
                showMessageOK("ต้องการยืนยันการอนุญาตเข้าถึง\n\n[สมุดรายชื่อ]\n\nเพื่อให้แอปพลิเคชันทำงานได้",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[] {Manifest.permission.READ_CONTACTS},
                                        REQUEST_READ_CONTACTS_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS_PERMISSIONS);
            return;
        }else{
            txtphoneContact = "";
            Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(pickContactIntent, target);
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        String number = "";
        if ( resultCode == Activity.RESULT_OK ) {
            Uri contactData = data.getData();
            String[] projection = { ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
            Cursor cursor = getActivity().getContentResolver().query(contactData, projection,
                    null, null, null);
            cursor.moveToFirst();

            int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            number = cursor.getString(numberColumnIndex).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
            number.trim();
        }
        if(requestCode == 1){
            try{
                t_phoneCard.setText(number);
            }catch (Exception e){
                Toast.makeText(getActivity(), R.string.hintReport,
                        Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == 2){
            try{
                t_phoneBill.setText(number);
            }catch (Exception e){
                Toast.makeText(getActivity(), R.string.hintReport,
                        Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == 3){
            try{
                t_phoneDays.setText(number);
            }catch (Exception e){
                Toast.makeText(getActivity(), R.string.hintReport,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case REQUEST_CALL_PHONE_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(getActivity(), "CALL_PHONE Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_READ_CONTACTS_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(getActivity(), "READ_CONTACTS Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
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
}
