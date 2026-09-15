package com.mondaro.easytopup;

import android.content.res.TypedArray;
import android.os.Bundle;
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
            if (SecurePrefs.get(this).isConfigured()) {
                displayView(VIEW_TOPUP);
            } else {
                Toast.makeText(this, R.string.not_configured_yet, Toast.LENGTH_LONG).show();
                displayView(VIEW_SETTING);
            }
        }
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
        android.graphics.drawable.Drawable dividerDrawable =
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
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
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
            case VIEW_REFUND:
                return new RefundFragment();
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
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
