package com.mondaro.easytopup;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AboutFragment extends Fragment {
    String version;

    public AboutFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        TextView tShow = (TextView) rootView.findViewById(R.id.txtLabel);
        Button btnEmail = (Button) rootView.findViewById(R.id.buttonEmail);
        Button btnWeb = (Button) rootView.findViewById(R.id.buttonWebsite);

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","mondaro23@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "ติดต่อจากแอปพลิเคชั่นเติมกะตังส์");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            }
        });

        btnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://mondaro.cc");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        try{
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(this.getActivity().getPackageName(), 0);
            version = pInfo.versionName ;
        }catch(Exception e){
            //No coding
        }
        String shText = getResources().getString(R.string.about_content1) + "เวอร์ชั่น  " + version + getResources().getString(R.string.about_content2);
        tShow.setText(shText);

        return rootView;
    }
}