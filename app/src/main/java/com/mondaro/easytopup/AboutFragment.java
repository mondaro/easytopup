package com.mondaro.easytopup;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mondaro.easytopup.R;

public class AboutFragment extends Fragment {
    String version;

    public AboutFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        TextView tShow = (TextView) rootView.findViewById(R.id.txtLabel);
        try{
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(this.getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        }catch(Exception e){

        }
        String shText = getResources().getString(R.string.about_content1) + "เวอร์ชั่น  " + version + getResources().getString(R.string.about_content2);
        tShow.setText(shText);

        return rootView;
    }
}