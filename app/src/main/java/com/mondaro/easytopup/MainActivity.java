package com.mondaro.easytopup;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mondaro.easytopup.adapter.NavDrawerAdapter;
import com.mondaro.easytopup.data.SecurePrefs;
import com.mondaro.easytopup.model.NavDrawerItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int VIEW_TOPUP = 0;
    public static final int VIEW_BALANCE = 1;
    public static final int VIEW_REFUND = 2;
    public static final int VIEW_ADDONS = 3;
    public static final int VIEW_SETTING = 4;
    public static final int VIEW_ABOUT = 5;
    public static final int VIEW_EXIT = 6;

    private DrawerLayout drawerLayout;
    private RecyclerView drawerList;
    private NavDrawerAdapter drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private String[] navMenuTitles;
    /** Text shared into the app, handed to the refund screen once it is created. */
    @Nullable
    private String pendingSharedText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.list_slidermenu);

        applySystemBarInsets();
        setUpDrawer();
        setUpBackHandling();

        if (savedInstanceState == null) {
            if (!SecurePrefs.get(this).isConfigured()) {
                Toast.makeText(this, R.string.not_configured_yet, Toast.LENGTH_LONG).show();
                displayView(VIEW_SETTING);
            } else if (!handleSharedText(getIntent())) {
                displayView(VIEW_TOPUP);
            }
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSharedText(intent);
    }

    /**
     * A carrier confirmation message shared from the messaging app opens the
     * refund screen with the text already in place. This is what lets the app do
     * refunds without the SMS permissions Google Play reserves for the default
     * messaging app.
     *
     * @return true when the intent carried text and the refund screen was opened
     */
    private boolean handleSharedText(@Nullable Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) {
            return false;
        }
        CharSequence shared = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        if (shared == null || shared.toString().trim().isEmpty()) {
            return false;
        }
        if (!SecurePrefs.get(this).isConfigured()) {
            Toast.makeText(this, R.string.not_configured_yet, Toast.LENGTH_LONG).show();
            displayView(VIEW_SETTING);
            return true;
        }
        // Consume it so a configuration change does not reopen the screen.
        intent.removeExtra(Intent.EXTRA_TEXT);
        pendingSharedText = shared.toString();
        displayView(VIEW_REFUND);
        return true;
    }

    /**
     * Android 15 draws apps edge to edge, so the content has to keep clear of the
     * status and navigation bars itself.
     */
    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            View content = findViewById(R.id.frame_container);
            content.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            drawerList.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
    }

    private void setUpDrawer() {
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        List<NavDrawerItem> navDrawerItems = new ArrayList<>(navMenuTitles.length);
        for (int i = 0; i < navMenuTitles.length; i++) {
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], navMenuIcons.getResourceId(i, -1)));
        }
        navMenuIcons.recycle();

        drawerAdapter = new NavDrawerAdapter(navDrawerItems, this::displayView);
        drawerList.setLayoutManager(new LinearLayoutManager(this));
        // The item count is fixed, so the list can skip re-measuring on every bind.
        drawerList.setHasFixedSize(true);
        DividerItemDecoration divider =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerDrawable =
                ContextCompat.getDrawable(this, R.drawable.nav_divider);
        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }
        drawerList.addItemDecoration(divider);
        drawerList.setAdapter(drawerAdapter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(R.string.app_name);
        }

        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
    }

    private void setUpBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(drawerList)) {
                    drawerLayout.closeDrawer(drawerList);
                } else {
                    displayView(VIEW_EXIT);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void displayView(int position) {
        Fragment fragment = createFragment(position);
        if (fragment == null) {
            return;
        }
        FragmentManager manager = getSupportFragmentManager();
        if (isFinishing() || manager.isStateSaved()) {
            return;
        }
        manager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
        drawerAdapter.setSelectedPosition(position);
        if (position < navMenuTitles.length) {
            setTitle(navMenuTitles[position]);
        }
        drawerLayout.closeDrawer(drawerList);
    }

    @Nullable
    private Fragment createFragment(int position) {
        switch (position) {
            case VIEW_TOPUP:
                return new TopupFragment();
            case VIEW_BALANCE:
                return new ChkBalanceFragment();
            case VIEW_REFUND: {
                String shared = pendingSharedText;
                pendingSharedText = null;
                return RefundFragment.newInstance(shared);
            }
            case VIEW_ADDONS:
                return new AddOnsFragment();
            case VIEW_SETTING:
                return new SettingFragment();
            case VIEW_ABOUT:
                return new AboutFragment();
            case VIEW_EXIT:
                return new ExitFragment();
            default:
                return null;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
