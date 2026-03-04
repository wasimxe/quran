package com.tanxe.quran;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.reading.ReadingFragment;
import com.tanxe.quran.ui.search.SearchFragment;
import com.tanxe.quran.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private Fragment activeFragment;
    private ReadingFragment readingFragment;
    private SearchFragment searchFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyTheme();
        setupFragments();
        setupBottomNavigation();
    }

    private void applyTheme() {
        ThemeManager theme = ThemeManager.getInstance(this);
        View container = findViewById(R.id.main_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        getWindow().setStatusBarColor(theme.getBackgroundColor());
        getWindow().setNavigationBarColor(theme.getSurfaceColor());
    }

    private void setupFragments() {
        readingFragment = new ReadingFragment();
        searchFragment = new SearchFragment();
        settingsFragment = new SettingsFragment();

        activeFragment = readingFragment;

        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)
            .add(R.id.fragment_container, searchFragment, "search").hide(searchFragment)
            .add(R.id.fragment_container, readingFragment, "reading")
            .commit();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        ThemeManager theme = ThemeManager.getInstance(this);
        bottomNav.setBackgroundColor(theme.getSurfaceColor());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();
            if (id == R.id.nav_read) {
                selected = readingFragment;
            } else if (id == R.id.nav_search) {
                selected = searchFragment;
            } else if (id == R.id.nav_settings) {
                selected = settingsFragment;
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(selected)
                .commit();
            activeFragment = selected;
            return true;
        });
    }

    public void refreshTheme() {
        applyTheme();
        ThemeManager theme = ThemeManager.getInstance(this);
        bottomNav.setBackgroundColor(theme.getSurfaceColor());

        if (readingFragment != null) readingFragment.applyTheme();
        if (searchFragment != null) searchFragment.applyTheme();
        if (settingsFragment != null) settingsFragment.applyTheme();
    }

    public void navigateToAyah(int surah, int ayah) {
        bottomNav.setSelectedItemId(R.id.nav_read);
        readingFragment.navigateToAyah(surah, ayah);
    }
}
