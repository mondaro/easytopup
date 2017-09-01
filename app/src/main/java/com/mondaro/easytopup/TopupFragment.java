package com.mondaro.easytopup;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.mondaro.easytopup.sqlite.DBHelper;
import com.mondaro.easytopup.sqlite.SimpleDBDial;

public class TopupFragment extends Fragment {
    String tmpDigit = "";
    char target ='1';
    int mode = 1;
    TextView txtphone,txtcost,txtQuick1,txtQuick2,txtQuick3;
    Button btn0,btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btnOK,btnDel,btnContact,btnHistory;
    LinearLayout sltAIS, sltDTAC, sltTRUE;
    FrameLayout bgcolor;
    String USERPIN1,USERPIN2,USERPIN3,LASTPHONE;
    SharedPreferences sharedPref;
    SharedPreferences.Editor edt;
    static final int PICK_CONTACT_REQUEST = 1;
    final private int REQUEST_READ_CONTACTS_PERMISSIONS = 112;
    final private int REQUEST_CALL_PHONE_PERMISSIONS = 123;

    public TopupFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_topup, container, false);

        sharedPref = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        USERPIN1 = sharedPref.getString("UID1", "");
        USERPIN2 = sharedPref.getString("UID2", "");
        USERPIN3 = sharedPref.getString("UID3", "");
        String getCheck = sharedPref.getString("CHK","");
        edt = sharedPref.edit();

        if(getCheck.equals("")){
            Toast.makeText(getActivity(), "ผลการตรวจสอบ :\r\nเครื่องนี้ยังไม่ได้ทำรายการตั้งค่าคะ\r\n\n", Toast.LENGTH_LONG).show();
            ((MainActivity) getActivity()).displayView(4);
        }else{
            bgcolor = (FrameLayout)rootView.findViewById(R.id.bgTheme);
            btnHistory = (Button) rootView.findViewById(R.id.btnHistory);
            btnContact = (Button) rootView.findViewById(R.id.btnFindContact);
            txtphone = (TextView) rootView.findViewById(R.id.textViewPhone);
            txtcost = (TextView) rootView.findViewById(R.id.textViewCost);
            txtQuick1 = (TextView) rootView.findViewById(R.id.textViewDial1);
            txtQuick2 = (TextView) rootView.findViewById(R.id.textViewDial2);
            txtQuick3 = (TextView) rootView.findViewById(R.id.textViewDial3);
            btn0 = (Button) rootView.findViewById(R.id.button0);
            btn1 = (Button) rootView.findViewById(R.id.button1);
            btn2 = (Button) rootView.findViewById(R.id.button2);
            btn3 = (Button) rootView.findViewById(R.id.button3);
            btn4 = (Button) rootView.findViewById(R.id.button4);
            btn5 = (Button) rootView.findViewById(R.id.button5);
            btn6 = (Button) rootView.findViewById(R.id.button6);
            btn7 = (Button) rootView.findViewById(R.id.button7);
            btn8 = (Button) rootView.findViewById(R.id.button8);
            btn9 = (Button) rootView.findViewById(R.id.button9);
            btnOK = (Button) rootView.findViewById(R.id.buttonTopup);
            btnDel = (Button) rootView.findViewById(R.id.buttonDel);
            sltAIS = (LinearLayout) rootView.findViewById(R.id.sltAIS);
            sltDTAC = (LinearLayout) rootView.findViewById(R.id.sltDTAC);
            sltTRUE = (LinearLayout) rootView.findViewById(R.id.sltTRUE);

            btnHistory.setOnClickListener(new OnClickListener() {@Override public void onClick(View v) {
                if(target=='1'){
                    tmpDigit = "";
                    LASTPHONE = sharedPref.getString("LAST","");
                    InsDigit(LASTPHONE);
                }else if(target=='2'){/*null*/}}});
            btnContact.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                pickContact(v);}});
            txtphone.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                target = '1';tmpDigit = txtphone.getText().toString().trim();
                txtphone.setBackgroundResource(R.drawable.border_active);
                txtcost.setBackgroundResource(R.drawable.border_inactive);}});
            txtcost.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                target = '2';tmpDigit = txtcost.getText().toString().trim();
                txtcost.setBackgroundResource(R.drawable.border_active);
                txtphone.setBackgroundResource(R.drawable.border_inactive);}});
            txtQuick1.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                if(!txtQuick1.getText().equals("")){
                    txtphone.setText(txtQuick1.getText());
                    target = '2';tmpDigit = "";
                    txtphone.setBackgroundResource(R.drawable.border_inactive);
                    txtcost.setBackgroundResource(R.drawable.border_active);
                }
            }});
            txtQuick2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                if(!txtQuick2.getText().equals("")){
                    txtphone.setText(txtQuick2.getText());
                    target = '2';tmpDigit = "";
                    txtphone.setBackgroundResource(R.drawable.border_inactive);
                    txtcost.setBackgroundResource(R.drawable.border_active);
                }
            }});
            txtQuick3.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                if(!txtQuick3.getText().equals("")){
                    txtphone.setText(txtQuick3.getText());
                    target = '2';tmpDigit = "";
                    txtphone.setBackgroundResource(R.drawable.border_inactive);
                    txtcost.setBackgroundResource(R.drawable.border_active);
                }
            }});
            sltAIS.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bgcolor.setBackgroundResource(R.color.bg_topup_ais);
                mode = 1;
            }});
            sltDTAC.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bgcolor.setBackgroundResource(R.color.bg_topup_dtac);
                mode = 2;
            }});
            sltTRUE.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bgcolor.setBackgroundResource(R.color.bg_topup_true);
                mode = 3;
            }});

            btn0.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("0");}});
            btn1.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("1");}});
            btn2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("2");}});
            btn3.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("3");}});
            btn4.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("4");}});
            btn5.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("5");}});
            btn6.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("6");}});
            btn7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InsDigit("7");
                }
            });
            btn8.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("8");}});
            btn9.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InsDigit("9");
                }
            });
            btnOK.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {InsDigit("OK");}});
            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InsDigit("DEL");
                }
            });
            txtphone.setBackgroundResource(R.drawable.border_active);

            btnDel.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(target=='1'){
                        txtphone.setText("");
                        txtphone.setBackgroundResource(R.drawable.border_active);
                        txtcost.setBackgroundResource(R.drawable.border_inactive);
                    }else{
                        txtcost.setText("");
                        txtphone.setBackgroundResource(R.drawable.border_inactive);
                        txtcost.setBackgroundResource(R.drawable.border_active);
                    }
                    return false;
                }
            });
        }

        return rootView;
    }

    void InsDigit(String tmp){
        if(tmp == "OK"){
            if(txtphone.getText().toString().equals("") || txtcost.getText().toString().equals("")){
                Toast.makeText(getActivity(), "ผิดพลาด :\r\nกรุณากรอกหมายเลขโทรศัพท์\nและจำนวนเงินที่ต้องการเติมเงินด้วยคะ", Toast.LENGTH_SHORT).show();
            }else if(txtphone.getText().length()>10 || txtphone.getText().length()<10) {
                Toast.makeText(getActivity(), "ผิดพลาด :\r\nกรุณากรอกหมายเลขโทรศัพท์\nให้ครบ 10 หลักด้วยคะ", Toast.LENGTH_SHORT).show();
            }else if(txtcost.getText().length()<1){
                Toast.makeText(getActivity(), "ผิดพลาด :\r\nกรุณากรอกจำนวนเงิน\nให้ถูกต้องด้วยคะ", Toast.LENGTH_SHORT).show();
            }else{
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
                    switch (mode){
                        case 1: TopupAIS();break;
                        case 2: TopupDTAC();break;
                        case 3: TopupTRUE();break;
                    }
                    saveToDB();
                    edt.putString("LAST", txtphone.getText().toString().trim() );
                    edt.commit();
                    tmpDigit = "";
                    txtphone.setText("");
                    txtcost.setText("");
                    txtphone.setBackgroundResource(R.drawable.border_active);
                    txtcost.setBackgroundResource(R.drawable.border_inactive);
                    target = '1';
                    txtQuick1.setText("");
                    txtQuick2.setText("");
                    txtQuick3.setText("");
                }

            }
        }else if(tmp == "DEL"){
            if(target=='1'){
                tmpDigit = txtphone.getText().toString();
                if (tmpDigit.length() > 0 ) {
                    tmpDigit = tmpDigit.substring(0, tmpDigit.length()-1);
                }
                txtphone.setText(tmpDigit);
            }else{
                tmpDigit = txtcost.getText().toString();
                if (tmpDigit.length() > 0 ) {
                    tmpDigit = tmpDigit.substring(0, tmpDigit.length()-1);
                }
                txtcost.setText(tmpDigit);
            }
        }else{
            tmpDigit += tmp;
            updateQuickDial();
            if(tmpDigit.length() == 11){
                tmpDigit = tmp;
                target = '2';
                txtphone.setBackgroundResource(R.drawable.border_inactive);
                txtcost.setBackgroundResource(R.drawable.border_active);
            }
        }
        if(tmpDigit.length() == 11){

        }
        if(target == '1'){
            txtphone.setBackgroundResource(R.drawable.border_active);
            txtphone.setText(tmpDigit);
        }else{
            txtcost.setText(tmpDigit);
        }
    }

    private void TopupAIS() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String txtTel = "*123*" + USERPIN1 + "*" + txtphone.getText().toString().trim() + "*" + txtcost.getText().toString().trim() + "%23";
            callIntent.setData(Uri.parse("tel:" + txtTel));
            startActivity(callIntent);
        } catch (ActivityNotFoundException activityException) {
            Log.d("dialing-example", "Call failed", activityException);
        }
    }

    private void TopupTRUE() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String txtTel = "*666*" + txtphone.getText().toString().trim() + "*" + txtcost.getText().toString().trim() + "*" + USERPIN2 + "%23";
            callIntent.setData(Uri.parse("tel:" + txtTel));
            startActivity(callIntent);
        } catch (ActivityNotFoundException activityException) {
            Log.d("dialing-example", "Call failed", activityException);
        }
    }

    private void TopupDTAC() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String txtTel = "*211*1*" + txtphone.getText().toString().trim() + "*" + USERPIN3 + "*" + txtcost.getText().toString().trim() + "*1*" + "%23";
            callIntent.setData(Uri.parse("tel:" + txtTel));
            startActivity(callIntent);
        } catch (ActivityNotFoundException activityException) {
            Log.d("dialing-example", "Call failed", activityException);
        }
    }

    private void pickContact(View v) {
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
            tmpDigit = "";
            Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
        }

    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
    super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == PICK_CONTACT_REQUEST ) {
            if ( resultCode == Activity.RESULT_OK ) {
                Uri contactData = data.getData();
                String[] projection = { ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
                Cursor cursor = getActivity().getContentResolver().query(contactData, projection,
                        null, null, null);
                cursor.moveToFirst();

                int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberColumnIndex).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                number.trim();
                    try{
                        txtphone.setText(number);
                        target = '2';
                        txtcost.setBackgroundResource(R.drawable.border_active);
                        txtphone.setBackgroundResource(R.drawable.border_inactive);
                    }catch (Exception e){
                        Toast.makeText(getActivity(), "\n** พบข้อผิดพลาด **\n\nกรุณาแจ้งผู้พัฒนาระบบ\n\nmondaro23@gmail.com\n\n\n", Toast.LENGTH_LONG).show();
                    }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case REQUEST_READ_CONTACTS_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(getActivity(), "READ_CONTACTS Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_CALL_PHONE_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(getActivity(), "CALL_PHONE Denied", Toast.LENGTH_SHORT)
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

    private void saveToDB() {
        SQLiteDatabase _writedatabase = new DBHelper(getActivity()).getWritableDatabase();
        SQLiteDatabase _readdatabase = new DBHelper(getActivity()).getReadableDatabase();
        ContentValues values = new ContentValues();
        int chk = chkDuplicateDB();
        if(chk != 0){
            int score = 0;
            Log.d("TAG","Duplicate at _ID : " + chk );
            String[] projection = {
                    SimpleDBDial.ContactDial.COLS_SCORE
            };

            String selection =
                    SimpleDBDial.ContactDial._ID + " = ?";

            String[] args = {String.valueOf(chk)};

            Cursor cursor = _readdatabase.query(
                    SimpleDBDial.ContactDial.TABLE_NAME,
                    projection,
                    selection,
                    args,
                    null, null, null
            );
            if(cursor.getCount() != 0){
                if (cursor.moveToFirst()){
                    do{
                        String data = cursor.getString(cursor.getColumnIndex("score"));
                        score = Integer.valueOf(data);
                    }while(cursor.moveToNext());
                }
                cursor.close();
            }
            score += 1;
            Log.d("TAG","At _ID : " + chk + " new score : " + score);
            values.put(SimpleDBDial.ContactDial.COLS_SCORE, score);
            _writedatabase.update(SimpleDBDial.ContactDial.TABLE_NAME,values,selection,args);
            Log.d("TAG","Update to DB completed");
        }else{
            values.put(SimpleDBDial.ContactDial.COLS_CARRIER, mode);
            values.put(SimpleDBDial.ContactDial.COLS_PHONE, txtphone.getText().toString());
            values.put(SimpleDBDial.ContactDial.COLS_SCORE, 1);
            _writedatabase.insert(SimpleDBDial.ContactDial.TABLE_NAME, null, values);
            Log.d("TAG","Insert to DB completed");
        }
    }
    private int chkDuplicateDB(){
        int chk = 0;
        SQLiteDatabase _findduplicatedatabase = new DBHelper(getActivity()).getReadableDatabase();
        String[] projection = {
                SimpleDBDial.ContactDial._ID
        };

        String selection = SimpleDBDial.ContactDial.COLS_PHONE + " like ?";

        String[] args = {"%" + txtphone.getText().toString() + "%"};

        Cursor cursor = _findduplicatedatabase.query(
                SimpleDBDial.ContactDial.TABLE_NAME,
                projection,
                selection,
                args,
                null, null, null
        );
        if(cursor.getCount() != 0){
            if (cursor.moveToFirst()){
                do{
                    String data = cursor.getString(cursor.getColumnIndex("_ID"));
                    chk = Integer.valueOf(data);
                }while(cursor.moveToNext());
            }
            cursor.close();
        }
        return chk;
    }
    private void chkCount(){
        SQLiteDatabase _countdatabase = new DBHelper(getActivity()).getReadableDatabase();
        Cursor cursor = _countdatabase.rawQuery(SimpleDBDial.ContactDial.SELECT_ALL,null);
        Toast.makeText(getActivity(), "Count : " + cursor.getCount(), Toast.LENGTH_LONG).show();
    }
    private void updateQuickDial() {

    }
}

