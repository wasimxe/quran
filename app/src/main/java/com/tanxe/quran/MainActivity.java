package com.tanxe.quran;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.activity.OnBackPressedCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.download.DownloadManager;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;
import com.tanxe.quran.ui.learn.LearnFragment;
import com.tanxe.quran.ui.library.LibraryFragment;
import com.tanxe.quran.ui.reading.ReadingFragment;
import com.tanxe.quran.ui.search.SearchFragment;
import com.tanxe.quran.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private Fragment activeFragment;
    private ReadingFragment readingFragment;
    private SearchFragment searchFragment;
    private LibraryFragment libraryFragment;
    private SettingsFragment settingsFragment;
    private int previousNavId = R.id.nav_read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyTheme();
        setupFragments();
        setupBottomNavigation();
        setupBackPress();
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
        searchFragment = new SearchFragment();
        libraryFragment = new LibraryFragment();
        settingsFragment = new SettingsFragment();

        activeFragment = readingFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragment_container, libraryFragment, "library").hide(libraryFragment)
                .add(R.id.fragment_container, searchFragment, "search").hide(searchFragment)
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
            int id = item.getItemId();

            // Learn tab opens as dialog, doesn't switch fragments
            if (id == R.id.nav_learn) {
                LearnFragment learnDialog = new LearnFragment();
                learnDialog.show(getSupportFragmentManager(), "learn_mode");
                // Re-select the previous tab after showing dialog
                bottomNav.post(() -> bottomNav.setSelectedItemId(previousNavId));
                return false;
            }

            Fragment selected;
            if (id == R.id.nav_read) {
                selected = readingFragment;
            } else if (id == R.id.nav_search) {
                selected = searchFragment;
            } else if (id == R.id.nav_library) {
                selected = libraryFragment;
            } else if (id == R.id.nav_more) {
                selected = settingsFragment;
            } else {
                return false;
            }

            previousNavId = id;
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .hide(activeFragment)
                    .show(selected)
                    .commit();
            activeFragment = selected;
            return true;
        });
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If not on Quran (first) tab, go back to it
                if (activeFragment != readingFragment) {
                    bottomNav.setSelectedItemId(R.id.nav_read);
                } else {
                    // Already on Quran tab — exit app
                    finish();
                }
            }
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
        bottomNav.getMenu().findItem(R.id.nav_learn).setTitle(Localization.get(lang, Localization.MODE_LEARN));
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
        if (searchFragment != null) searchFragment.applyTheme();
        if (libraryFragment != null) libraryFragment.applyTheme();
        if (settingsFragment != null) settingsFragment.applyTheme();
    }

    public void navigateToAyah(int surah, int ayah) {
        bottomNav.setSelectedItemId(R.id.nav_read);
        readingFragment.navigateToAyah(surah, ayah);
    }

    public void navigateToBookmarks() {
        // Show bookmarks directly from reading fragment
        bottomNav.setSelectedItemId(R.id.nav_read);
        readingFragment.showBookmarks();
    }

    public void navigateToSearch(String query) {
        bottomNav.setSelectedItemId(R.id.nav_search);
        searchFragment.searchFor(query);
    }

    /** Auto-download defaults in background based on user-selected language */
    private void ensureWbwDownloaded() {
        QuranRepository repo = QuranRepository.getInstance(this);
        repo.saveSelectedWbwLanguage("ur");
        DownloadManager dm = DownloadManager.getInstance(this);

        repo.getExecutor().execute(() -> {
            // Fetch edition catalog if not cached yet
            if (!repo.isEditionCatalogLoaded()) {
                try {
                    repo.fetchAndCacheEditions();
                    repo.setEditionCatalogLoaded(true);
                } catch (Exception ignored) {}
            }

            // Download WBW (Urdu) if needed
            List<com.tanxe.quran.data.dao.WordByWordDao.WordWithTranslation> words =
                    repo.getWordsWithTranslations("ur");
            boolean needsWbw = (words == null || words.isEmpty());
            if (!needsWbw && words.size() == 1 && (words.get(0).arabicWord == null || words.get(0).arabicWord.isEmpty())) {
                repo.deleteWbw("ur");
                needsWbw = true;
            }
            if (needsWbw) {
                repo.setDownloadState("wbw.ur", "none");
                dm.downloadWordByWord("ur", status -> { /* silent */ });
            }

            // Download default translation based on language
            String lang = repo.getLanguage();
            String defaultTranslation = getDefaultTranslation(lang);
            if (defaultTranslation != null && !"ur.jalandhry".equals(defaultTranslation)) {
                List<String> existing = repo.getAvailableTranslations();
                if (existing == null || !existing.contains(defaultTranslation)) {
                    repo.saveSelectedTranslation(defaultTranslation);
                    dm.downloadTranslation(defaultTranslation, lang, status -> { /* silent */ });
                }
            }

            // Download default tafseer based on language
            String defaultTafseer = getDefaultTafseer(lang);
            if (defaultTafseer != null) {
                List<String> existing = repo.getAvailableTafseers();
                if (existing == null || !existing.contains(defaultTafseer)) {
                    repo.saveSelectedTafseer(defaultTafseer);
                    dm.downloadTafseer(defaultTafseer, lang, status -> { /* silent */ });
                }
            }

            // Download audio for first few surahs in background
            String reciter = repo.getSelectedReciter();
            if (reciter == null || reciter.isEmpty()) reciter = "Alafasy_128kbps";
            for (int surah = 1; surah <= 114; surah++) {
                dm.downloadAudioForSurah(surah, reciter, status -> { /* silent */ });
            }
        });
    }

    private static String getDefaultTranslation(String lang) {
        switch (lang) {
            case "ur": return "ur.jalandhry"; // built-in
            case "en": return "en.sahih";
            case "ar": return "ar.muyassar";
            case "tr": return "tr.ates";
            case "bn": return "bn.bengali";
            case "fa": return "fa.makarem";
            case "id": return "id.indonesian";
            case "fr": return "fr.hamidullah";
            default: return "en.sahih";
        }
    }

    private static String getDefaultTafseer(String lang) {
        switch (lang) {
            case "ur": return "ur.maududi";
            case "en": return "en.jalalayn";
            case "ar": return "ar.jalalayn";
            case "tr": return null;
            case "bn": return null;
            default: return "en.jalalayn";
        }
    }
}
