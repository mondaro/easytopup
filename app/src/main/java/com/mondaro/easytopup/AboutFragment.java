package com.mondaro.easytopup;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.mondaro.easytopup.ui.BaseFragment;

public class AboutFragment extends BaseFragment {

    private static final String SUPPORT_EMAIL = "mondaro23@gmail.com";
    private static final String WEBSITE = "https://agoodmany.world";
    private static final String APP_SITE = "https://agoodmany.world/mondaro_devtech";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        lockPortrait();

        TextView version = root.findViewById(R.id.textViewAboutVersion);
        version.setText(getString(R.string.about_version) + "  " + appVersion());

        root.findViewById(R.id.buttonEmail).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .setTitle(R.string.about_email_warning_title)
                        .setMessage(R.string.about_email_warning_body)
                        .setPositiveButton(R.string.understood, (d, w) -> sendSupportEmail())
                        .show());
        root.findViewById(R.id.buttonWebsite).setOnClickListener(v -> openUrl(WEBSITE));
        root.findViewById(R.id.buttonAppSite).setOnClickListener(v -> openUrl(APP_SITE));

        return root;
    }

    private String appVersion() {
        try {
            PackageInfo info = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            return info.versionName == null ? "" : info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private void sendSupportEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", SUPPORT_EMAIL, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_email_subject));
        startActivitySafely(Intent.createChooser(intent, getString(R.string.about_email_chooser)));
    }

    private void openUrl(@NonNull String url) {
        startActivitySafely(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void startActivitySafely(@NonNull Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            toast(R.string.no_app_to_handle);
        }
    }
}
