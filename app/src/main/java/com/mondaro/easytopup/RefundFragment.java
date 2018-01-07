package com.mondaro.easytopup;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RefundFragment extends Fragment {
    LinearLayout bg,panel;
    Button btnReAIS,btnReDTAC,btnReTRUE, btnRefund,btnCancel;
    ListView listViewRe;
    SimpleCursorAdapter adapter;
    String addr,cid,USERPIN1,USERPIN2,USERPIN3,txtTel, strPhone, strAmount, strRef,txtVender;
    String[] separated;
    int tag;
    SharedPreferences sharedPref;
    final private int REQUEST_CALL_PHONE_PERMISSIONS = 123;
    final private int REQUEST_READ_SMS_PERMISSIONS = 131;
    final private int REQUEST_SEND_SMS_PERMISSIONS = 132;

    public RefundFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_refund, container, false);
        bg = (LinearLayout) rootView.findViewById(R.id.list_refundBG);
        panel = (LinearLayout) rootView.findViewById(R.id.panelBtn);
        btnReAIS = (Button) rootView.findViewById(R.id.btn_smsAIS);
        btnReDTAC = (Button) rootView.findViewById(R.id.btn_smsDTAC);
        btnReTRUE = (Button) rootView.findViewById(R.id.btn_smsTRUE);
        btnRefund = (Button) rootView.findViewById(R.id.btnRefund);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
        listViewRe = (ListView) rootView.findViewById(R.id.listViewRefund);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        USERPIN1 = sharedPref.getString("UID1", "");
        USERPIN2 = sharedPref.getString("UID2", "");
        USERPIN3 = sharedPref.getString("UID3", "");
        String getCheck = sharedPref.getString("CHK","");

        if(getCheck.equals("")){
            Toast.makeText(getActivity(), "ผลการตรวจสอบ :\r\nเครื่องนี้ยังไม่ได้ทำรายการตั้งค่าคะ\r\n\n", Toast.LENGTH_LONG).show();
            ((MainActivity) getActivity()).displayView(4);
        }else{
            btnReAIS.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bg.setBackgroundResource(R.color.bg_topup_ais);
                if(!USERPIN1.equals("")){
                    loadSMS(1);
                }else{
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nคุณไม่ได้ลงทะเบียนรหัสประจำตัวของระบบ AIS \n" +
                            "กรุณาลงทะเบียนก่อนใช้งานด้วยคะ\r\n", Toast.LENGTH_LONG).show();
                    adapter = new SimpleCursorAdapter(getActivity(), R.layout.row_refund, null,
                            new String[] { "body" }, new int[] {
                            R.id.lblMsg});
                    adapter.notifyDataSetChanged();
                    listViewRe.setAdapter(adapter);
                }
            }});

            btnReDTAC.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bg.setBackgroundResource(R.color.bg_topup_dtac);
                if(!USERPIN3.equals("")){
                    loadSMS(2);
                }else{
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nคุณไม่ได้ลงทะเบียนรหัสประจำตัวของระบบ DTAC \n" +
                            "กรุณาลงทะเบียนก่อนใช้งานด้วยคะ\r\n", Toast.LENGTH_LONG).show();
                    adapter = new SimpleCursorAdapter(getActivity(), R.layout.row_refund, null,
                            new String[] { "body" }, new int[] {
                            R.id.lblMsg});
                    adapter.notifyDataSetChanged();
                    listViewRe.setAdapter(adapter);
                }
            }});

            btnReTRUE.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bg.setBackgroundResource(R.color.bg_topup_true);
                if(!USERPIN2.equals("")){
                    loadSMS(3);
                }else{
                    Toast.makeText(getActivity(), "แจ้งเตือน :\r\nคุณไม่ได้ลงทะเบียนรหัสประจำตัวของระบบ TRUE \n" +
                            "กรุณาลงทะเบียนก่อนใช้งานด้วยคะ\r\n", Toast.LENGTH_LONG).show();
                    adapter = new SimpleCursorAdapter(getActivity(), R.layout.row_refund, null,
                            new String[] { "body" }, new int[] {
                            R.id.lblMsg});
                    adapter.notifyDataSetChanged();
                    listViewRe.setAdapter(adapter);
                }
            }});

            if(USERPIN3.equals("")){
                btnReDTAC.setVisibility(View.GONE);
            }else{
                btnReDTAC.setVisibility(View.VISIBLE);
                btnReDTAC.performClick();
            }
            if(USERPIN2.equals("")){
                btnReTRUE.setVisibility(View.GONE);
            }else{
                btnReTRUE.setVisibility(View.VISIBLE);
                btnReTRUE.performClick();
            }
            if(USERPIN1.equals("")){
                btnReAIS.setVisibility(View.GONE);
            }else{
                btnReAIS.setVisibility(View.VISIBLE);
                btnReAIS.performClick();
            }

            btnRefund.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                switch (tag){
                    case 1:
                        try{
                            separated = cid.split(" ");
                            strPhone = separated[1].substring(separated[1].length()-10,separated[1].length());
                            strAmount = separated[1].substring(4,separated[1].length()-13);
                            strRef = separated[0].substring(4,separated[0].length());
                            final String last4phone = strPhone.substring(strPhone.length()-4,strPhone.length());
                            txtTel = "*321*" + USERPIN1 + "*" + last4phone + "*" + strRef + "%23";
                            txtVender = "AIS";
                            Log.d("dialing-example",txtTel);
                        }catch(Exception e){
                            //Null
                        }
                        break;
                    case 2:
                        try{
                            separated = cid.split(" ");
                            strPhone = "0" + separated[0].substring(separated[0].length()-9,separated[0].length());
                            strAmount = separated[0].substring(4,separated[0].length()-14);
                            strRef = separated[1].substring(separated[1].length()-16,separated[1].length()-7);
                            txtTel = "*211*8*" + strPhone + "*" + USERPIN3 + "*" + strAmount.substring(0,strAmount.length()-2) + "*1" + "%23";
                            txtVender = "DTAC";
                            Log.d("dialing-example",txtTel);
                        }catch(Exception e){
                            //Null
                        }
                        break;
                    case 3:
                        try{
                            separated = cid.split(" ");
                            strPhone = separated[5];
                            strAmount = separated[2] + " " + separated[3];
                            strRef = separated[0];
                            cid = cid.substring(0,cid.length()-18);
                            txtVender = "TRUE";
                            Log.d("dialing-example",cid);
                        }catch(Exception e){
                            //Null
                        }
                        break;
                }


                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_menu_revert)
                        .setTitle("ยืนยันการดึงเงินคืน")
                        .setMessage("ระบบ " + txtVender +"\nจากหมายเลข : " + strPhone + "\nจำนวน : " + strAmount + "\nREF : " + strRef)
                        .setNegativeButton("ดึงเงิน", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(tag != 3){
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
                                            callIntent.setData(Uri.parse("tel:" + txtTel));
                                            startActivity(callIntent);
                                        } catch (ActivityNotFoundException activityException) {
                                        }
                                    }
                                }else{
                                    int hasSendSMSPermission = ContextCompat.checkSelfPermission(getActivity(),
                                            Manifest.permission.SEND_SMS);
                                    if (hasSendSMSPermission != PackageManager.PERMISSION_GRANTED){
                                        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                                Manifest.permission.SEND_SMS)) {
                                            showMessageOK("ต้องการยืนยันการอนุญาตเข้าถึง\n\n[การส่ง SMS]\n\nเพื่อให้แอปพลิเคชันทำงานได้",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            ActivityCompat.requestPermissions(getActivity(),
                                                                    new String[] {Manifest.permission.SEND_SMS},
                                                                    REQUEST_SEND_SMS_PERMISSIONS);
                                                        }
                                                    });
                                            return;
                                        }
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[] {Manifest.permission.SEND_SMS},
                                                REQUEST_SEND_SMS_PERMISSIONS);
                                        return;
                                    }else{
                                        try {
                                            SmsManager sms = SmsManager.getDefault();
                                            sms.sendTextMessage("97322", null, cid, null, null);
                                        } catch (ActivityNotFoundException activityException) {
                                        }
                                    }
                                }
                            }
                        })
                        .setPositiveButton("ยกเลิก",null)
                        .show();
                panel.setVisibility(View.GONE);
            }});

            btnCancel.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                panel.setVisibility(View.GONE);
            }});
        }
        return rootView;
    }

    void loadSMS(int mode){
        int hasReadSMSPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_SMS);
        if (hasReadSMSPermission != PackageManager.PERMISSION_GRANTED){
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_SMS)) {
                showMessageOK("ต้องการยืนยันการอนุญาตเข้าถึง\n\n[การอ่าน SMS]\n\nเพื่อให้แอปพลิเคชันทำงานได้",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[] {Manifest.permission.READ_SMS},
                                        REQUEST_READ_SMS_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.READ_SMS},
                    REQUEST_READ_SMS_PERMISSIONS);
            return;
        }else{
            switch (mode){
                case 1: addr = "address='022719123'"; tag = 1; break;
                case 2: addr = "address='dtacOnline'"; tag = 2; break;
                case 3: addr = "address='MobileTopUp'"; tag = 3; break;
            }
            try{
                Uri inboxURI = Uri.parse("content://sms/inbox");
                String[] reqCols = new String[] { "_id", "address", "body", "date" };
                ContentResolver cr = getActivity().getContentResolver();

                Cursor c = cr.query(inboxURI, reqCols, addr, null, null);

                List<String> items = new ArrayList<String>();
                while(c.moveToNext()) {
                    Calendar calendar = Calendar.getInstance();
                    String date =  c.getString(c.getColumnIndex("date"));
                    Long timestamp = Long.parseLong(date);
                    calendar.setTimeInMillis(timestamp);
                    Date finaldate = calendar.getTime();
                    String smsDate = new SimpleDateFormat("dd/MM/yyyy",new Locale("th","TH")).format(finaldate);//finaldate.toString();
                    String smsBody = c.getString(c.getColumnIndex("body"));
                    items.add(smsBody + "\nวันที่ " +smsDate );
                }
                c.close();

                ListAdapter listAdapter= new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, items);
                listViewRe.setAdapter(listAdapter);

                listViewRe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View v,
                                            int position, long id) {
                        cid = ((TextView)(v.findViewById(android.R.id.text1))).getText().toString();
                        Log.d("TEST",cid);
                        panel.setVisibility(View.VISIBLE);
                    }
                });
            }catch (Exception e){

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
            case REQUEST_READ_SMS_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(getActivity(), "READ_SMS Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_SEND_SMS_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(getActivity(), "SEND_SMS Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOK(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

}
