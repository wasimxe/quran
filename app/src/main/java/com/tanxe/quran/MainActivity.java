package com.tanxe.quran;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.download.DownloadManager;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;
import com.tanxe.quran.ui.library.LibraryFragment;
import com.tanxe.quran.ui.reading.ReadingFragment;
import com.tanxe.quran.ui.search.SearchFragment;
import com.tanxe.quran.ui.settings.SettingsFragment;
import com.tanxe.quran.ui.surahindex.SurahIndexFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private Fragment activeFragment;
    private ReadingFragment readingFragment;
    private SurahIndexFragment surahIndexFragment;
    private SearchFragment searchFragment;
    private LibraryFragment libraryFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyTheme();
        setupFragments();
        setupBottomNavigation();
        ensureWbwDownloaded();
    }

    private void applyTheme() {
        ThemeManager theme = ThemeManager.getInstance(this);
        View container = findViewById(R.id.main_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        getWindow().setStatusBarColor(theme.getBackgroundColor());
        getWindow().setNavigationBarColor(theme.getSurfaceColor());

        // Light status bar icons for light theme
        if (!theme.isDarkTheme()) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    private void setupFragments() {
        readingFragment = new ReadingFragment();
        surahIndexFragment = new SurahIndexFragment();
        searchFragment = new SearchFragment();
        libraryFragment = new LibraryFragment();
        settingsFragment = new SettingsFragment();

        activeFragment = readingFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragment_container, libraryFragment, "library").hide(libraryFragment)
                .add(R.id.fragment_container, searchFragment, "search").hide(searchFragment)
                .add(R.id.fragment_container, surahIndexFragment, "surahs").hide(surahIndexFragment)
                .add(R.id.fragment_container, readingFragment, "reading")
                .commit();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        ThemeManager theme = ThemeManager.getInstance(this);
        bottomNav.setBackgroundColor(theme.getSurfaceColor());
        applyNavColors(theme);
        localizeNavLabels();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();
            if (id == R.id.nav_read) {
                selected = readingFragment;
            } else if (id == R.id.nav_surahs) {
                selected = surahIndexFragment;
            } else if (id == R.id.nav_search) {
                selected = searchFragment;
            } else if (id == R.id.nav_library) {
                selected = libraryFragment;
            } else if (id == R.id.nav_more) {
                selected = settingsFragment;
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .hide(activeFragment)
                    .show(selected)
                    .commit();
            activeFragment = selected;
            return true;
        });
    }

    private void applyNavColors(ThemeManager theme) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
                theme.getAccentColor(),
                theme.getSecondaryTextColor()
        };
        android.content.res.ColorStateList csl = new android.content.res.ColorStateList(states, colors);
        bottomNav.setItemIconTintList(csl);
        bottomNav.setItemTextColor(csl);
    }

    private void localizeNavLabels() {
        String lang = QuranRepository.getInstance(this).getLanguage();
        bottomNav.getMenu().findItem(R.id.nav_read).setTitle(Localization.get(lang, Localization.QURAN));
        bottomNav.getMenu().findItem(R.id.nav_surahs).setTitle(Localization.get(lang, Localization.SURAHS));
        bottomNav.getMenu().findItem(R.id.nav_search).setTitle(Localization.get(lang, Localization.SEARCH));
        bottomNav.getMenu().findItem(R.id.nav_library).setTitle(Localization.get(lang, Localization.LIBRARY));
        bottomNav.getMenu().findItem(R.id.nav_more).setTitle(Localization.get(lang, Localization.MORE));
    }

    public void refreshTheme() {
        applyTheme();
        ThemeManager theme = ThemeManager.getInstance(this);
        bottomNav.setBackgroundColor(theme.getSurfaceColor());
        applyNavColors(theme);
        localizeNavLabels();

        if (readingFragment != null) readingFragment.applyTheme();
        if (surahIndexFragment != null) surahIndexFragment.applyTheme();
        if (searchFragment != null) searchFragment.applyTheme();
        if (libraryFragment != null) libraryFragment.applyTheme();
        if (settingsFragment != null) settingsFragment.applyTheme();
    }

    public void navigateToAyah(int surah, int ayah) {
        bottomNav.setSelectedItemId(R.id.nav_read);
        readingFragment.navigateToAyah(surah, ayah);
    }

    public void navigateToBookmarks() {
        bottomNav.setSelectedItemId(R.id.nav_surahs);
        surahIndexFragment.showBookmarksTab();
    }

    public void navigateToSearch(String query) {
        bottomNav.setSelectedItemId(R.id.nav_search);
        searchFragment.searchFor(query);
    }

    /** Auto-download WBW Urdu if not already downloaded or data is corrupt */
    private void ensureWbwDownloaded() {
        QuranRepository repo = QuranRepository.getInstance(this);
        repo.saveSelectedWbwLanguage("ur");
        repo.getExecutor().execute(() -> {
            // Check if WBW data exists and has valid arabic words
            List<com.tanxe.quran.data.dao.WordByWordDao.WordWithTranslation> words =
                    repo.getWordsWithTranslations("ur");
            boolean needsDownload = (words == null || words.isEmpty());
            // Also check if arabic words are empty (corrupt data)
            if (!needsDownload && words.size() == 1 && (words.get(0).arabicWord == null || words.get(0).arabicWord.isEmpty())) {
                // Data is corrupt — all words have empty arabicWord
                repo.deleteWbw("ur");
                needsDownload = true;
            }
            if (needsDownload) {
                repo.setDownloadState("wbw.ur", "none");
                DownloadManager dm = new DownloadManager(MainActivity.this);
                dm.downloadWordByWord("ur", status -> {
                    // Silent background download
                });
            }
        });
    }
}
