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
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.mondaro.easytopup.sqlite.DBHelper;
import com.mondaro.easytopup.sqlite.SimpleDBDial;

public class TopupFragment extends Fragment {
    String tmpDigit = "",tmpLabel = "";
    char target ='1';
    int mode = 0;
    TextView txtphone,txtcost,txtQuick1,txtQuick2,txtQuick3,labelAIS,labelDTAC,labelTRUE,labelCAT;
    Button btn0,btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btnOK,btnDel,btnContact,btnHistory;
    LinearLayout bgcolor,sltAIS, sltDTAC, sltTRUE, sltCAT, slt1, slt2, slt3, slt4;
    String USERPIN_AIS, USERPIN_DTAC, USERPIN_TRUE, USERPIN_CAT,LASTPHONE,CHK_THEME;
    int bgAIS,bgDTAC,bgTRUE,bgCAT,tabAIS,tabDTAC,tabTRUE,tabCAT;
    List<String> getPhone;
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
        USERPIN_AIS = sharedPref.getString("UID_AIS", "");
        USERPIN_DTAC = sharedPref.getString("UID_DTAC", "");
        USERPIN_TRUE = sharedPref.getString("UID_TRUE", "");
        USERPIN_CAT = sharedPref.getString("UID_CAT", "");
        CHK_THEME = sharedPref.getString("THEME", "");
        String getCheck = sharedPref.getString("CHK_OK","");
        edt = sharedPref.edit();
        edt.putString("UID1", "");edt.putString("UID2", "");edt.putString("UID3", "");edt.putString("CHK", "");
        edt.apply();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        chkNewRoll(year);

        if(getCheck.equals("")){
            Toast.makeText(getActivity(), "ผลการตรวจสอบ :\r\nเครื่องนี้ยังไม่ได้ทำรายการตั้งค่าคะ\r\n\n", Toast.LENGTH_LONG).show();
            ((MainActivity) getActivity()).displayView(4);
        }else{
            bgcolor = (LinearLayout)rootView.findViewById(R.id.bgTheme);
            btnHistory = (Button) rootView.findViewById(R.id.btnHistory);
            btnContact = (Button) rootView.findViewById(R.id.btnFindContact);
            txtphone = (TextView) rootView.findViewById(R.id.textViewPhone);
            txtcost = (TextView) rootView.findViewById(R.id.textViewCost);
            txtQuick1 = (TextView) rootView.findViewById(R.id.textViewDial1);
            txtQuick2 = (TextView) rootView.findViewById(R.id.textViewDial2);
            txtQuick3 = (TextView) rootView.findViewById(R.id.textViewDial3);
            labelAIS = (TextView) rootView.findViewById(R.id.textViewAIS);
            labelDTAC = (TextView) rootView.findViewById(R.id.textViewDTAC);
            labelTRUE = (TextView) rootView.findViewById(R.id.textViewTRUE);
            labelCAT = (TextView) rootView.findViewById(R.id.textViewCAT);
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
            sltCAT = (LinearLayout) rootView.findViewById(R.id.sltCAT);
            slt1 = (LinearLayout) rootView.findViewById(R.id.slt1);
            slt2 = (LinearLayout) rootView.findViewById(R.id.slt2);
            slt3 = (LinearLayout) rootView.findViewById(R.id.slt3);
            slt4 = (LinearLayout) rootView.findViewById(R.id.slt4);

            if(CHK_THEME.equals("A")){
                bgAIS = R.color.bg_topup_ais_default;
                bgDTAC = R.color.bg_topup_dtac_default;
                bgTRUE = R.color.bg_topup_true_default;
                bgCAT = R.color.bg_topup_cat_default;
                tabAIS = R.drawable.button_select_ais_default;
                tabDTAC = R.drawable.button_select_dtac_default;
                tabTRUE = R.drawable.button_select_true_default;
                tabCAT = R.drawable.button_select_cat_default;
            }else{
                bgAIS = R.color.bg_topup_ais_pastel;
                bgDTAC = R.color.bg_topup_dtac_pastel;
                bgTRUE = R.color.bg_topup_true_pastel;
                bgCAT = R.color.bg_topup_cat_pastel;
                tabAIS = R.drawable.button_select_ais_pastel;
                tabDTAC = R.drawable.button_select_dtac_pastel;
                tabTRUE = R.drawable.button_select_true_pastel;
                tabCAT = R.drawable.button_select_cat_pastel;
            }

            btnHistory.setOnClickListener(new OnClickListener() {@Override public void onClick(View v) {
                if(target=='1'){
                    tmpDigit = "";
                    LASTPHONE = sharedPref.getString("LAST","");
                    InsDigit(LASTPHONE);
                }}});
            btnContact.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                pickContact();}});
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
                    chkCarrier(getPhone.get(0).substring(10,11));
                }
            }});
            txtQuick2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                if(!txtQuick2.getText().equals("")){
                    txtphone.setText(txtQuick2.getText());
                    chkCarrier(getPhone.get(1).substring(10,11));
                }
            }});
            txtQuick3.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                if(!txtQuick3.getText().equals("")){
                    txtphone.setText(txtQuick3.getText());
                    chkCarrier(getPhone.get(2).substring(10,11));
                }
            }});
            sltAIS.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bgcolor.setBackgroundResource(bgAIS);
                mode = 1;
                showLabel("1000");
            }});
            sltDTAC.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bgcolor.setBackgroundResource(bgDTAC);
                mode = 2;
                showLabel("0100");
            }});
            sltTRUE.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bgcolor.setBackgroundResource(bgTRUE);
                mode = 3;
                showLabel("0010");
            }});
            sltCAT.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
                bgcolor.setBackgroundResource(bgCAT);
                mode = 4;
                showLabel("0001");
            }});

            if(USERPIN_CAT.equals("")){
                tmpLabel = "0";
                slt4.setVisibility(View.GONE);
            }else{
                tmpLabel = "1";
                slt4.setVisibility(View.VISIBLE);
                sltCAT.setBackgroundResource(tabCAT);
                bgcolor.setBackgroundResource(bgCAT);
                sltCAT.performClick();
            }
            if(USERPIN_TRUE.equals("")){
                tmpLabel = "0" + tmpLabel;
                slt3.setVisibility(View.GONE);
            }else{
                tmpLabel = "1" + tmpLabel;
                slt3.setVisibility(View.VISIBLE);
                sltTRUE.setBackgroundResource(tabTRUE);
                bgcolor.setBackgroundResource(bgTRUE);
                sltTRUE.performClick();
            }
            if(USERPIN_DTAC.equals("")){
                tmpLabel = "0" + tmpLabel;
                slt2.setVisibility(View.GONE);
            }else{
                tmpLabel = "1" + tmpLabel;
                slt2.setVisibility(View.VISIBLE);
                sltDTAC.setBackgroundResource(tabDTAC);
                bgcolor.setBackgroundResource(bgDTAC);
                sltDTAC.performClick();
            }
            if(USERPIN_AIS.equals("")){
                tmpLabel = "0" + tmpLabel;
                slt1.setVisibility(View.GONE);
            }else{
                tmpLabel = "1" + tmpLabel;
                slt1.setVisibility(View.VISIBLE);
                sltAIS.setBackgroundResource(tabAIS);
                bgcolor.setBackgroundResource(bgAIS);
                sltAIS.performClick();
            }

            showLabel(tmpLabel);

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
            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {InsDigit("OK");promptQuickDial();}});
            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    InsDigit("DEL");
                    if(txtphone.getText().toString().equals("")){
                        promptQuickDial();
                    }
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
        switch (tmp) {
            case "OK":
                if (txtphone.getText().toString().equals("") || txtcost.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "ผิดพลาด :\r\nกรุณากรอกหมายเลขโทรศัพท์\nและจำนวนเงินที่ต้องการเติมเงินด้วยคะ", Toast.LENGTH_SHORT).show();
                } else if (txtphone.getText().length() > 10 || txtphone.getText().length() < 10) {
                    Toast.makeText(getActivity(), "ผิดพลาด :\r\nกรุณากรอกหมายเลขโทรศัพท์\nให้ครบ 10 หลักด้วยคะ", Toast.LENGTH_SHORT).show();
                } else if (txtcost.getText().length() < 1) {
                    Toast.makeText(getActivity(), "ผิดพลาด :\r\nกรุณากรอกจำนวนเงิน\nให้ถูกต้องด้วยคะ", Toast.LENGTH_SHORT).show();
                } else {
                    int hasCallPhonePermission = ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CALL_PHONE);
                    if (hasCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                Manifest.permission.CALL_PHONE)) {
                            tmpDigit = "";
                            showMessageOK("ต้องการยืนยันการอนุญาตเข้าถึง\n\n[ระบบการโทร]\n\nเพื่อให้แอปพลิเคชันทำงานได้",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ActivityCompat.requestPermissions(getActivity(),
                                                    new String[]{Manifest.permission.CALL_PHONE},
                                                    REQUEST_CALL_PHONE_PERMISSIONS);
                                        }
                                    });
                            return;
                        }
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CALL_PHONE},
                                REQUEST_CALL_PHONE_PERMISSIONS);
                        return;
                    } else {
                        switch (mode) {
                            case 1:
                                TopupAIS();
                                break;
                            case 2:
                                TopupDTAC();
                                break;
                            case 3:
                                TopupTRUE();
                                break;
                            case 4:
                                TopupCAT();
                                break;
                        }
                        saveToDB();
                        edt.putString("LAST", txtphone.getText().toString().trim());
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
                break;
            case "DEL":
                if (target == '1') {
                    tmpDigit = txtphone.getText().toString();
                    if (tmpDigit.length() > 0) {
                        tmpDigit = tmpDigit.substring(0, tmpDigit.length() - 1);
                    }
                    txtphone.setText(tmpDigit);
                    updateQuickDial(tmpDigit);
                } else {
                    tmpDigit = txtcost.getText().toString();
                    if (tmpDigit.length() > 0) {
                        tmpDigit = tmpDigit.substring(0, tmpDigit.length() - 1);
                    }
                    txtcost.setText(tmpDigit);
                }
                break;
            default:
                tmpDigit += tmp;
                if (tmpDigit.length() == 11) {
                    tmpDigit = tmp;
                    target = '2';
                    txtphone.setBackgroundResource(R.drawable.border_inactive);
                    txtcost.setBackgroundResource(R.drawable.border_active);
                }
                break;
        }
        if(target == '1'){
            txtphone.setBackgroundResource(R.drawable.border_active);
            txtphone.setText(tmpDigit);
            updateQuickDial(tmpDigit);
        }else{
            txtcost.setText(tmpDigit);
        }
    }

    private void TopupAIS() {
        Log.d("Test", "To this");
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String txtTel = "*123*" + USERPIN_AIS + "*" + txtphone.getText().toString().trim() + "*" + txtcost.getText().toString().trim() + "%23";
            callIntent.setData(Uri.parse("tel:" + txtTel));
            startActivity(callIntent);
        } catch (ActivityNotFoundException activityException) {
            Log.d("dialing-example", "Call failed", activityException);
        }
    }

    private void TopupDTAC() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String txtTel = "*211*1*" + txtphone.getText().toString().trim() + "*" + USERPIN_DTAC + "*" + txtcost.getText().toString().trim() + "*1" + "%23";
            callIntent.setData(Uri.parse("tel:" + txtTel));
            startActivity(callIntent);
        } catch (ActivityNotFoundException activityException) {
            Log.d("dialing-example", "Call failed", activityException);
        }
    }

    private void TopupTRUE() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String txtTel = "*666*" + txtphone.getText().toString().trim() + "*" + txtcost.getText().toString().trim() + "*" + USERPIN_TRUE + "%23";
            callIntent.setData(Uri.parse("tel:" + txtTel));
            startActivity(callIntent);
        } catch (ActivityNotFoundException activityException) {
            Log.d("dialing-example", "Call failed", activityException);
        }
    }

    private void TopupCAT() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String txtTel = "*215*" + txtphone.getText().toString().trim() + "*" + txtcost.getText().toString().trim() + "%23";
            callIntent.setData(Uri.parse("tel:" + txtTel));
            startActivity(callIntent);
        } catch (ActivityNotFoundException activityException) {
            Log.d("dialing-example", "Call failed", activityException);
        }
    }

    private void pickContact() {
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
            //return;
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
        String number = "";
        if ( requestCode == PICK_CONTACT_REQUEST ) {
            if ( resultCode == Activity.RESULT_OK ) {
                Uri contactData = data.getData();
                String[] projection = { ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
                Cursor cursor = getActivity().getContentResolver().query(contactData, projection,
                        null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    number = cursor.getString(numberColumnIndex).replace("(", "").replace(")", "").replace("-", "").replace(" ", "").trim();
                    cursor.close();
                }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_READ_CONTACTS_PERMISSIONS:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getActivity(), "READ_CONTACTS Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_CALL_PHONE_PERMISSIONS:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
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
            int amount = 0;
            String[] projection = {
                    SimpleDBDial.ContactDial.COLS_AMOUNT
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
                        String data = cursor.getString(cursor.getColumnIndexOrThrow(SimpleDBDial.ContactDial.COLS_AMOUNT));
                        amount = Integer.valueOf(data);
                    }while(cursor.moveToNext());
                }
                cursor.close();
            }
            amount += Integer.valueOf(txtcost.getText().toString());
            values.put(SimpleDBDial.ContactDial.COLS_AMOUNT, amount);
            values.put(SimpleDBDial.ContactDial.COLS_CARRIER, mode);
            _writedatabase.update(SimpleDBDial.ContactDial.TABLE_NAME,values,selection,args);
        }else{
            values.put(SimpleDBDial.ContactDial.COLS_CARRIER, mode);
            values.put(SimpleDBDial.ContactDial.COLS_PHONE, txtphone.getText().toString());
            values.put(SimpleDBDial.ContactDial.COLS_AMOUNT, Integer.valueOf(txtcost.getText().toString()));
            _writedatabase.insert(SimpleDBDial.ContactDial.TABLE_NAME, null, values);
        }
    }

    private int chkDuplicateDB(){
        int chk = 0;
        SQLiteDatabase _findduplicatedatabase = new DBHelper(getActivity()).getReadableDatabase();
        String[] projection = {
                SimpleDBDial.ContactDial._ID
        };

        String selection = SimpleDBDial.ContactDial.COLS_PHONE + " LIKE ?";

        String[] args = {txtphone.getText().toString() + "%"};

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
                    String data = cursor.getString(cursor.getColumnIndexOrThrow(SimpleDBDial.ContactDial._ID));
                    chk = Integer.valueOf(data);
                }while(cursor.moveToNext());
            }
            cursor.close();
        }
        return chk;
    }

    private void updateQuickDial(String t) {
        promptQuickDial();
        SQLiteDatabase _getquickphone = new DBHelper(getActivity()).getReadableDatabase();
        String[] projection = {
                SimpleDBDial.ContactDial.COLS_CARRIER,
                SimpleDBDial.ContactDial.COLS_PHONE
        };

        String selection = SimpleDBDial.ContactDial.COLS_PHONE + " LIKE ?";

        String[] args = {t + "%"};

        Cursor cur = _getquickphone.query(
                SimpleDBDial.ContactDial.TABLE_NAME,
                projection,
                selection,
                args,
                null, null,
                SimpleDBDial.ContactDial.COLS_AMOUNT + " DESC",
                "3"
        );
        getPhone = new ArrayList<>();
        String carrier;
        String phone;
        if(cur.getCount() != 0){
            if (cur.moveToFirst()){
                do{
                    carrier = cur.getString(cur.getColumnIndexOrThrow(SimpleDBDial.ContactDial.COLS_CARRIER));
                    phone = cur.getString(cur.getColumnIndexOrThrow(SimpleDBDial.ContactDial.COLS_PHONE));
                    getPhone.add(phone+carrier);
                }while(cur.moveToNext());
            }
            cur.close();
        }
        try{
            txtQuick1.setVisibility(View.VISIBLE);
            txtQuick2.setVisibility(View.VISIBLE);
            txtQuick3.setVisibility(View.VISIBLE);
            txtQuick1.setText(getPhone.get(0).substring(0,10));
            txtQuick2.setText(getPhone.get(1).substring(0,10));
            txtQuick3.setText(getPhone.get(2).substring(0,10));
        }catch (Exception e){
            //No code
        }
        try{
            switch (getPhone.get(0).substring(10,11)){
                case "1" :txtQuick1.setBackgroundResource(R.drawable.button_quick_ais);break;
                case "2" :txtQuick1.setBackgroundResource(R.drawable.button_quick_dtac);break;
                case "3" :txtQuick1.setBackgroundResource(R.drawable.button_quick_true);break;
                case "4" :txtQuick1.setBackgroundResource(R.drawable.button_quick_cat);break;
            }
            switch (getPhone.get(1).substring(10,11)){
                case "1" :txtQuick2.setBackgroundResource(R.drawable.button_quick_ais);break;
                case "2" :txtQuick2.setBackgroundResource(R.drawable.button_quick_dtac);break;
                case "3" :txtQuick2.setBackgroundResource(R.drawable.button_quick_true);break;
                case "4" :txtQuick2.setBackgroundResource(R.drawable.button_quick_cat);break;
            }
            switch (getPhone.get(2).substring(10,11)){
                case "1" :txtQuick3.setBackgroundResource(R.drawable.button_quick_ais);break;
                case "2" :txtQuick3.setBackgroundResource(R.drawable.button_quick_dtac);break;
                case "3" :txtQuick3.setBackgroundResource(R.drawable.button_quick_true);break;
                case "4" :txtQuick3.setBackgroundResource(R.drawable.button_quick_cat);break;
            }
        }catch (Exception e){
            //No code
        }
        if(txtQuick1.getText() == ""){txtQuick1.setVisibility(View.INVISIBLE);}
        if(txtQuick2.getText() == ""){txtQuick2.setVisibility(View.INVISIBLE);}
        if(txtQuick3.getText() == ""){txtQuick3.setVisibility(View.INVISIBLE);}
    }

    private void promptQuickDial(){
        txtQuick1.setText("");
        txtQuick1.setVisibility(View.INVISIBLE);
        txtQuick2.setText("");
        txtQuick2.setVisibility(View.INVISIBLE);
        txtQuick3.setText("");
        txtQuick3.setVisibility(View.INVISIBLE);
        txtcost.setText("");
    }

    private void chkCarrier(String c){
        switch (c){
            case "1":
                if(USERPIN_AIS.equals("")){
                    Toast.makeText(getActivity(), "หมายเลขนี้อยู่ในระบบที่คุณไม่ได้ตั้งค่าไว้ค่ะ", Toast.LENGTH_LONG).show();
                    txtphone.setText("");
                }else{
                    sltAIS.performClick();
                }
                break;
            case "2":
                if(USERPIN_DTAC.equals("")){
                    Toast.makeText(getActivity(), "หมายเลขนี้อยู่ในระบบที่คุณไม่ได้ตั้งค่าไว้ค่ะ", Toast.LENGTH_LONG).show();
                    txtphone.setText("");
                }else{
                    sltDTAC.performClick();
                }
                break;
            case "3":
                if(USERPIN_TRUE.equals("")){
                    Toast.makeText(getActivity(), "หมายเลขนี้อยู่ในระบบที่คุณไม่ได้ตั้งค่าไว้ค่ะ", Toast.LENGTH_LONG).show();
                    txtphone.setText("");
                }else{
                    sltTRUE.performClick();
                }
                break;
            case "4":
                if(USERPIN_CAT.equals("")){
                    Toast.makeText(getActivity(), "หมายเลขนี้อยู่ในระบบที่คุณไม่ได้ตั้งค่าไว้ค่ะ", Toast.LENGTH_LONG).show();
                    txtphone.setText("");
                }else{
                    sltCAT.performClick();
                }
                break;
        }
        target = '2';tmpDigit = "";
        txtphone.setBackgroundResource(R.drawable.border_inactive);
        txtcost.setBackgroundResource(R.drawable.border_active);
        promptQuickDial();
    }

    private void chkNewRoll(int yearNow){
        int yearOn = sharedPref.getInt("YEAR",0);
        if(yearOn == 0){
            edt.putInt("YEAR", Calendar.getInstance().get(Calendar.YEAR));
            edt.commit();
            yearOn = sharedPref.getInt("YEAR",0);
        }
        if(yearOn != yearNow){
            edt.putInt("YEAR", Calendar.getInstance().get(Calendar.YEAR));
            edt.commit();
            try {
                cleanAmount();
            }catch (Exception e){
                //Null
            }
        }
    }

    private void cleanAmount(){
        SQLiteDatabase _writedatabase = new DBHelper(getActivity()).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SimpleDBDial.ContactDial.COLS_AMOUNT, 0);
        _writedatabase.update(SimpleDBDial.ContactDial.TABLE_NAME,cv,null,null);

    }

    private void showLabel(String lb){
        switch (lb){
            case "1000" :case "1100" :case "1010" :case "1001" :
                labelAIS.setVisibility(View.VISIBLE);
                labelDTAC.setVisibility(View.GONE);
                labelTRUE.setVisibility(View.GONE);
                labelCAT.setVisibility(View.GONE);
                slt1.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f));
                slt2.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt3.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt4.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                break;
            case "0100" :case "0110" :case "0101" :
                labelAIS.setVisibility(View.GONE);
                labelDTAC.setVisibility(View.VISIBLE);
                labelTRUE.setVisibility(View.GONE);
                labelCAT.setVisibility(View.GONE);
                slt1.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt2.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f));
                slt3.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt4.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                break;
            case "0010" :case "0011" :
                labelAIS.setVisibility(View.GONE);
                labelDTAC.setVisibility(View.GONE);
                labelTRUE.setVisibility(View.VISIBLE);
                labelCAT.setVisibility(View.GONE);
                slt1.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt2.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt3.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f));
                slt4.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                break;
            case "0001" :
                labelAIS.setVisibility(View.GONE);
                labelDTAC.setVisibility(View.GONE);
                labelTRUE.setVisibility(View.GONE);
                labelCAT.setVisibility(View.VISIBLE);
                slt1.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt2.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt3.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.0f));
                slt4.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f));
                break;
        }
    }
    public static void setBackgroundResource(@NonNull View view, @DrawableRes int resId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int paddingTop = view.getPaddingTop();
            int paddingLeft = view.getPaddingLeft();
            int paddingRight = view.getPaddingRight();
            int paddingBottom = view.getPaddingBottom();
            view.setBackgroundResource(resId);
            view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        } else {
            view.setBackgroundResource(resId);
        }
    }
}

