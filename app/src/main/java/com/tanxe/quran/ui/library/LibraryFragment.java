package com.tanxe.quran.ui.library;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.tanxe.quran.R;
import com.tanxe.quran.util.Localization;
import com.tanxe.quran.data.entity.EditionInfo;
import com.tanxe.quran.data.entity.ReciterInfo;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.download.DownloadManager;
import com.tanxe.quran.theme.ThemeManager;

import java.util.List;
import java.util.Set;

public class LibraryFragment extends Fragment {
    private QuranRepository repository;
    private ThemeManager theme;
    private DownloadManager dm;

    private RecyclerView rvEditions;
    private LibraryAdapter adapter;
    private TabLayout tabCategory;
    private ChipGroup languageChips;
    private TextView tvEmpty;

    private String currentCategory = "translation"; // translation, tafseer, reciter
    private String currentLanguageFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = QuranRepository.getInstance(requireContext());
        theme = ThemeManager.getInstance(requireContext());
        dm = DownloadManager.getInstance(requireContext());

        rvEditions = view.findViewById(R.id.rv_editions);
        rvEditions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEditions.setHasFixedSize(true);
        rvEditions.setItemViewCacheSize(15);

        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new LibraryAdapter(theme, new LibraryAdapter.OnEditionAction() {
            @Override
            public void onDownload(EditionInfo edition, int position) {
                startDownload(edition, position);
            }

            @Override
            public void onDelete(EditionInfo edition, int position) {
                deleteEdition(edition, position);
            }

            @Override
            public void onCancelDownload(EditionInfo edition, int position) {
                confirmCancelDownload(edition, position);
            }

            @Override
            public void onSelect(EditionInfo edition, int position) {
                selectEdition(edition);
            }
        });
        rvEditions.setAdapter(adapter);

        // Category tabs
        tabCategory = view.findViewById(R.id.tab_category);
        String lang = repository.getLanguage();
        tabCategory.addTab(tabCategory.newTab().setText(Localization.get(lang, Localization.TRANSLATIONS)));
        tabCategory.addTab(tabCategory.newTab().setText(Localization.get(lang, Localization.TAFSEERS)));
        tabCategory.addTab(tabCategory.newTab().setText(Localization.get(lang, Localization.WORD_BY_WORD)));
        tabCategory.addTab(tabCategory.newTab().setText(Localization.get(lang, Localization.AUDIO_RECITERS)));

        tabCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentCategory = "translation"; break;
                    case 1: currentCategory = "tafseer"; break;
                    case 2: currentCategory = "wbw"; break;
                    case 3: currentCategory = "reciter"; break;
                }
                // Auto-select user's language or "All" when switching tabs
                if ("reciter".equals(currentCategory)) {
                    selectLanguageChip("all");
                } else {
                    String userLang = repository.getLanguage();
                    selectLanguageChip(userLang != null ? userLang : "all");
                }
                loadEditions();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Swipe left/right on edition list to switch category tabs
        GestureDetector tabSwipeDetector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) { return true; }
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        if (e1 == null || e2 == null) return false;
                        float diffX = e2.getX() - e1.getX();
                        float diffY = e2.getY() - e1.getY();
                        if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                            int current = tabCategory.getSelectedTabPosition();
                            int next = diffX < 0 ? current + 1 : current - 1;
                            if (next >= 0 && next < tabCategory.getTabCount()) {
                                tabCategory.selectTab(tabCategory.getTabAt(next));
                            }
                            return true;
                        }
                        return false;
                    }
                });
        rvEditions.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                tabSwipeDetector.onTouchEvent(e);
                return false;
            }
        });

        // Language chips
        languageChips = view.findViewById(R.id.language_chips);
        setupLanguageChips();

        // Search
        TextInputEditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                adapter.filterByQuery(s.toString());
            }
        });

        loadEditions();
        applyTheme();
    }

    /** Programmatically select a language chip by language code, falling back to "All" */
    private void selectLanguageChip(String langCode) {
        if (languageChips == null) return;
        // Try to find and check the matching chip
        for (int i = 0; i < languageChips.getChildCount(); i++) {
            View child = languageChips.getChildAt(i);
            if (child instanceof Chip) {
                String tag = (String) child.getTag();
                if (langCode.equalsIgnoreCase(tag) || (langCode.equalsIgnoreCase("all") && "All".equals(tag))) {
                    ((Chip) child).setChecked(true);
                    currentLanguageFilter = "All".equals(tag) ? "all" : tag;
                    adapter.filterByLanguage(currentLanguageFilter);
                    return;
                }
            }
        }
        // Language not found in chips — fall back to "All"
        for (int i = 0; i < languageChips.getChildCount(); i++) {
            View child = languageChips.getChildAt(i);
            if (child instanceof Chip && "All".equals(child.getTag())) {
                ((Chip) child).setChecked(true);
                currentLanguageFilter = "all";
                adapter.filterByLanguage("all");
                return;
            }
        }
    }

    private void setupLanguageChips() {
        String[] defaultLangs = {"All", "en", "ur", "ar", "tr", "fr", "id", "bn", "fa", "ms", "de", "es"};
        String allLabel = Localization.get(repository.getLanguage(), Localization.SEARCH_ALL);
        int accentColor = theme.getAccentColor();
        int surfaceColor = theme.getSurfaceColor();
        int textColor = theme.getSecondaryTextColor();
        for (String lang : defaultLangs) {
            Chip chip = new Chip(requireContext());
            chip.setText(lang.equals("All") ? allLabel : lang.toUpperCase());
            chip.setCheckable(true);
            chip.setChecked("All".equals(lang));
            chip.setTag(lang);
            // Style: accent bg when checked, surface bg when unchecked
            chip.setChipBackgroundColor(new android.content.res.ColorStateList(
                    new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                    },
                    new int[]{accentColor, surfaceColor}
            ));
            chip.setTextColor(new android.content.res.ColorStateList(
                    new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                    },
                    new int[]{0xFF1B3A1B, textColor}  // dark text on accent, secondary otherwise
            ));
            chip.setCheckedIconVisible(false);
            languageChips.addView(chip);
        }
        languageChips.setSelectionRequired(true);
        languageChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Should not happen with selectionRequired, but fallback to "all"
                currentLanguageFilter = "all";
            } else {
                View checked = group.findViewById(checkedIds.get(0));
                if (checked instanceof Chip) {
                    String lang = (String) checked.getTag();
                    currentLanguageFilter = "All".equals(lang) ? "all" : lang;
                }
            }
            // Reciters are language-independent — don't apply language filter
            if (!"reciter".equals(currentCategory)) {
                adapter.filterByLanguage(currentLanguageFilter);
            }
        });
    }

    private void loadEditions() {
        repository.getExecutor().execute(() -> {
            List<EditionInfo> editions;
            if ("reciter".equals(currentCategory) || "wbw".equals(currentCategory)) {
                editions = null; // handled differently
            } else if ("tafseer".equals(currentCategory)) {
                // Merge alquran.cloud tafseers + quran.com tafseers
                editions = repository.getEditionsByType("tafseer");
                if (editions == null) editions = new java.util.ArrayList<>();
                mergeQuranComTafseers(editions);
                // Single batch query for ALL tafseer stats instead of 3×N individual queries
                java.util.Map<String, com.tanxe.quran.data.entity.EditionStats> statsMap = new java.util.HashMap<>();
                for (com.tanxe.quran.data.entity.EditionStats s : repository.getAllTafseerStats()) {
                    statsMap.put(s.edition, s);
                }
                for (EditionInfo e : editions) {
                    com.tanxe.quran.data.entity.EditionStats s = statsMap.get(e.identifier);
                    if (s != null) {
                        // Consider complete if: ayah count >= 6236 OR all 114 surahs covered with >= 5000 ayahs
                        if (s.ayahCount >= 6236 || (s.surahCount >= 114 && s.ayahCount >= 5000)) {
                            e.isDownloaded = true;
                            e.sizeText = formatSize(s.textSize) + " · " + s.ayahCount + " ayahs";
                        } else if (s.ayahCount > 0) {
                            e.isDownloaded = false;
                            e.downloadProgress = 0;
                            e.sizeText = formatSize(s.textSize) + " · " + s.ayahCount + "/6236 ayahs (incomplete)";
                        }
                    }
                }
            } else {
                editions = repository.getEditionsByType(currentCategory);
                if (editions == null) editions = new java.util.ArrayList<>();
                mergeGitHubTranslations(editions);
                if (!editions.isEmpty()) {
                    // Single batch query for ALL translation stats
                    java.util.Map<String, com.tanxe.quran.data.entity.EditionStats> statsMap = new java.util.HashMap<>();
                    for (com.tanxe.quran.data.entity.EditionStats s : repository.getAllTranslationStats()) {
                        statsMap.put(s.edition, s);
                    }
                    for (EditionInfo e : editions) {
                        com.tanxe.quran.data.entity.EditionStats s = statsMap.get(e.identifier);
                        if (s != null) {
                            if (s.surahCount >= 114 && s.ayahCount >= 6236) {
                                e.isDownloaded = true;
                                e.sizeText = formatSize(s.textSize) + " · " + s.ayahCount + "/6236 ayahs";
                            } else if (s.ayahCount > 0) {
                                e.isDownloaded = false;
                                e.downloadProgress = 0;
                                e.sizeText = formatSize(s.textSize) + " · " + s.ayahCount + "/6236 ayahs (incomplete)";
                            }
                        }
                    }
                }
            }

            if (getActivity() != null) {
                List<EditionInfo> finalEditions = editions;
                requireActivity().runOnUiThread(() -> {
                    if ("reciter".equals(currentCategory)) {
                        loadReciters();
                    } else if ("wbw".equals(currentCategory)) {
                        loadWbwOptions();
                    } else if (finalEditions != null && !finalEditions.isEmpty()) {
                        adapter.setEditions(finalEditions);
                        updateActiveIdentifier();
                        tvEmpty.setVisibility(View.GONE);
                        rvEditions.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText(Localization.get(repository.getLanguage(), Localization.LOADING_CATALOG));
                        rvEditions.setVisibility(View.GONE);
                        // Try fetching from API
                        fetchCatalog();
                    }
                });
            }
        });
    }

    /** Merge quran.com tafseers into the edition list (avoids duplicates by identifier) */
    private void mergeQuranComTafseers(List<EditionInfo> editions) {
        java.util.Set<String> existing = new java.util.HashSet<>();
        for (EditionInfo e : editions) existing.add(e.identifier);

        // quran.com tafseers: {identifier, name, language, languageName, resourceId}
        String[][] qcTafseers = {
            {"qc.en.ibnkathir", "Ibn Kathir (Abridged)", "en", "English", "169"},
            {"qc.en.maarifulquran", "Ma'arif al-Qur'an", "en", "English", "168"},
            {"qc.en.tazkirulquran", "Tazkirul Quran", "en", "English", "817"},
            {"qc.ur.ibnkathir", "Ibn Kathir", "ur", "Urdu", "160"},
            {"qc.ur.bayanulquran", "Bayan ul Quran", "ur", "Urdu", "159"},
            {"qc.ur.fizilal", "Fi Zilal al-Quran", "ur", "Urdu", "157"},
            {"qc.ur.tazkirulquran", "Tazkir ul Quran", "ur", "Urdu", "818"},
            {"qc.ur.tafheem", "Tafheem ul Quran (Maududi)", "ur", "Urdu", "97"},
            {"qc.ur.usmani", "Tafsir-e-Usmani", "ur", "Urdu", "151"},
            {"qc.bn.ibnkathir", "Tafseer Ibn Kathir", "bn", "Bengali", "164"},
            {"qc.bn.ahsanulbayaan", "Tafsir Ahsanul Bayaan", "bn", "Bengali", "165"},
            {"qc.bn.abubakrzakaria", "Tafsir Abu Bakr Zakaria", "bn", "Bengali", "166"},
            {"qc.bn.fathulmajid", "Tafsir Fathul Majid", "bn", "Bengali", "381"},
            {"qc.ar.muyassar", "Tafsir Muyassar", "ar", "Arabic", "16"},
            {"qc.ar.ibnkathir", "Tafsir Ibn Kathir", "ar", "Arabic", "14"},
            {"qc.ar.tabari", "Tafsir al-Tabari", "ar", "Arabic", "15"},
            {"qc.ar.qurtubi", "Al-Qurtubi", "ar", "Arabic", "90"},
            {"qc.ar.saddi", "Al-Sa'di", "ar", "Arabic", "91"},
            {"qc.ar.wasit", "Al-Tafsir al-Wasit", "ar", "Arabic", "93"},
            {"qc.ar.baghawi", "Tafsir Al-Baghawi", "ar", "Arabic", "94"},
            {"qc.ru.saddi", "Al-Sa'di (Russian)", "ru", "Russian", "170"},
            {"qc.ku.rebar", "Rebar Kurdish Tafsir", "ku", "Kurdish", "804"},
            // alquran.cloud Arabic tafseers (no quran.com resource ID — use alquran.cloud API)
            {"ar.jalalayn", "Tafsir al-Jalalayn", "ar", "Arabic", "0"},
            {"ar.miqbas", "Tanwir al-Miqbas", "ar", "Arabic", "0"},
        };

        for (String[] t : qcTafseers) {
            if (!existing.contains(t[0])) {
                EditionInfo ei = new EditionInfo(t[0], t[1], t[2], t[3], "tafseer",
                        "ar".equals(t[2]) || "ur".equals(t[2]) || "fa".equals(t[2]) || "ku".equals(t[2]) ? "rtl" : "ltr");
                // isDownloaded will be set by the batch stats query in loadEditions()
                editions.add(ei);
            }
        }
    }

    /** Merge GitHub-only translations (fawazahmed0) that aren't in alquran.cloud */
    private void mergeGitHubTranslations(List<EditionInfo> editions) {
        java.util.Set<String> existing = new java.util.HashSet<>();
        for (EditionInfo e : editions) existing.add(e.identifier);

        // Translations only available via fawazahmed0/quran-api (not on alquran.cloud)
        String[][] ghTranslations = {
            {"gh.ur.taqiusmani", "Aasan Tarjuma - Mufti Taqi Usmani", "ur", "Urdu"},
            {"gh.ur.karamshah", "Zia ul Quran - Karam Shah", "ur", "Urdu"},
            {"gh.ur.mahmoodulhassan", "Fatehul Hameed - Mahmood ul Hassan", "ur", "Urdu"},
        };

        for (String[] t : ghTranslations) {
            if (!existing.contains(t[0])) {
                EditionInfo ei = new EditionInfo(t[0], t[1], t[2], t[3], "translation",
                        "ur".equals(t[2]) ? "rtl" : "ltr");
                editions.add(ei);
            }
        }
    }

    private void loadReciters() {
        repository.getExecutor().execute(() -> {
            List<ReciterInfo> reciters = repository.getAllReciters();
            if (getActivity() == null || reciters == null) return;

            // Convert reciters to EditionInfo and compute audio sizes
            java.util.ArrayList<EditionInfo> reciterEditions = new java.util.ArrayList<>();
            java.io.File audioRoot = new java.io.File(requireContext().getFilesDir(), "audio");
            for (ReciterInfo r : reciters) {
                EditionInfo ei = new EditionInfo(r.identifier, r.name, "ar",
                        r.style, "reciter", "rtl");
                // Check actual files on disk
                java.io.File reciterDir = new java.io.File(audioRoot, r.subfolder);
                if (reciterDir.exists()) {
                    java.io.File[] files = reciterDir.listFiles();
                    if (files != null && files.length > 0) {
                        long totalBytes = 0;
                        for (java.io.File f : files) totalBytes += f.length();
                        ei.isDownloaded = files.length >= 6236;
                        ei.sizeText = formatSize(totalBytes) + " (" + files.length + " files)";
                    } else {
                        ei.isDownloaded = false;
                    }
                } else {
                    ei.isDownloaded = r.isDownloaded;
                }
                reciterEditions.add(ei);
            }

            requireActivity().runOnUiThread(() -> {
                adapter.setEditions(reciterEditions);
                updateActiveIdentifier();
                tvEmpty.setVisibility(reciterEditions.isEmpty() ? View.VISIBLE : View.GONE);
                rvEditions.setVisibility(reciterEditions.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private void computeReciterSize(EditionInfo e) {
        repository.getExecutor().execute(() -> {
            java.io.File dir = new java.io.File(requireContext().getFilesDir(), "audio/" + e.identifier);
            if (dir.exists()) {
                java.io.File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    long totalBytes = 0;
                    for (java.io.File f : files) totalBytes += f.length();
                    String size = formatSize(totalBytes) + " (" + files.length + " files)";
                    if (getActivity() != null) {
                        final String finalSize = size;
                        requireActivity().runOnUiThread(() -> {
                            e.sizeText = finalSize;
                            adapter.updateByIdentifier(e.identifier);
                        });
                    }
                }
            }
        });
    }

    private void computeEditionSize(EditionInfo e) {
        repository.getExecutor().execute(() -> {
            String sizeText = null;
            if ("wbw".equals(e.type)) {
                String lang = e.identifier.replace("wbw.", "");
                int count = repository.getWbwWordCount(lang);
                long size = repository.getWbwTextSize(lang);
                int surahCount = repository.getWbwSurahCount(lang);
                sizeText = formatSize(size) + " · " + count + " words · " + surahCount + "/114 surahs";
            } else if ("tafseer".equals(e.type)) {
                int count = repository.getTafseerCount(e.identifier);
                long size = repository.getTafseerTextSize(e.identifier);
                sizeText = formatSize(size) + " · " + count + "/6236 ayahs";
            } else if ("translation".equals(e.type)) {
                int count = repository.getTranslationCount(e.identifier);
                long size = repository.getTranslationTextSize(e.identifier);
                sizeText = formatSize(size) + " · " + count + "/6236 ayahs";
            }
            if (sizeText != null && getActivity() != null) {
                final String finalSize = sizeText;
                requireActivity().runOnUiThread(() -> {
                    e.sizeText = finalSize;
                    adapter.updateByIdentifier(e.identifier);
                });
            }
        });
    }

    private void loadWbwOptions() {
        java.util.ArrayList<EditionInfo> wbwEditions = new java.util.ArrayList<>();

        // All languages supported by quran.com WBW API
        String[][] wbwLanguages = {
            {"en", "English", "ltr"},
            {"ur", "Urdu", "rtl"},
            {"bn", "Bengali", "ltr"},
            {"tr", "Turkish", "ltr"},
            {"es", "Spanish", "ltr"},
            {"fr", "French", "ltr"},
            {"id", "Indonesian", "ltr"},
            {"ru", "Russian", "ltr"},
            {"de", "German", "ltr"},
            {"ms", "Malay", "ltr"},
            {"hi", "Hindi", "ltr"},
            {"fa", "Persian", "rtl"},
            {"ta", "Tamil", "ltr"},
            {"ml", "Malayalam", "ltr"},
            {"nl", "Dutch", "ltr"},
            {"it", "Italian", "ltr"},
            {"pt", "Portuguese", "ltr"},
            {"ja", "Japanese", "ltr"},
            {"ko", "Korean", "ltr"},
            {"zh", "Chinese", "ltr"},
            {"th", "Thai", "ltr"},
            {"vi", "Vietnamese", "ltr"},
            {"az", "Azerbaijani", "ltr"},
            {"sq", "Albanian", "ltr"},
            {"bs", "Bosnian", "ltr"},
            {"uz", "Uzbek", "ltr"},
            {"sw", "Swahili", "ltr"},
            {"ha", "Hausa", "ltr"},
            {"so", "Somali", "ltr"},
            {"ku", "Kurdish", "rtl"},
            {"ps", "Pashto", "rtl"},
            {"sd", "Sindhi", "rtl"},
            {"gu", "Gujarati", "ltr"},
            {"kn", "Kannada", "ltr"},
            {"te", "Telugu", "ltr"},
            {"mr", "Marathi", "ltr"},
            {"ne", "Nepali", "ltr"},
            {"si", "Sinhala", "ltr"},
            {"tg", "Tajik", "ltr"},
            {"am", "Amharic", "ltr"},
        };

        for (String[] lang : wbwLanguages) {
            wbwEditions.add(new EditionInfo("wbw." + lang[0],
                    lang[1] + " Word by Word", lang[0], lang[1], "wbw", lang[2]));
        }

        // Check download status, detect incomplete, and compute sizes
        repository.getExecutor().execute(() -> {
            for (EditionInfo e : wbwEditions) {
                String lang = e.identifier.replace("wbw.", "");
                int surahCount = repository.getWbwSurahCount(lang);
                if (surahCount >= 114) {
                    e.isDownloaded = true;
                    int count = repository.getWbwWordCount(lang);
                    long size = repository.getWbwTextSize(lang);
                    e.sizeText = formatSize(size) + " · " + count + " words · " + surahCount + "/114 surahs";
                } else if (surahCount > 0) {
                    e.isDownloaded = false;
                    e.downloadProgress = 0;
                    int count = repository.getWbwWordCount(lang);
                    long size = repository.getWbwTextSize(lang);
                    e.sizeText = formatSize(size) + " · " + count + " words · " + surahCount + "/114 surahs (incomplete)";
                } else {
                    e.isDownloaded = false;
                }
            }
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    adapter.setEditions(wbwEditions);
                    updateActiveIdentifier();
                    tvEmpty.setVisibility(View.GONE);
                    rvEditions.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void fetchCatalog() {
        repository.getExecutor().execute(() -> {
            repository.fetchAndCacheEditions();
            repository.initReciters();
            repository.setEditionCatalogLoaded(true);
            if (getActivity() != null) {
                requireActivity().runOnUiThread(this::loadEditions);
            }
        });
    }

    /** Update download progress on the current adapter edition object (survives tab switches) */
    private void updateDownloadProgress(String identifier, String status) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(() -> {
            EditionInfo e = adapter.findByIdentifier(identifier);
            if (e == null) return;
            if (status.startsWith("\u2713")) {
                e.isDownloaded = true;
                e.downloadProgress = 100;
                // Compute final size
                if ("reciter".equals(e.type)) {
                    computeReciterSize(e);
                } else {
                    computeEditionSize(e);
                }
            } else if (status.startsWith("Incomplete")) {
                // Download finished but not all surahs — show as not downloaded so user can resume
                e.isDownloaded = false;
                e.downloadProgress = 0;
                // Extract size info from status message
                e.sizeText = status.replace("Incomplete: ", "").replace(" — tap download to resume", "").replace(" — tap download to retry", "");
            } else if (status.contains("(") && status.contains("%)")) {
                try {
                    java.util.regex.Matcher m = java.util.regex.Pattern
                            .compile("\\((\\d+)%\\)").matcher(status);
                    if (m.find()) {
                        e.downloadProgress = Math.max(1, Integer.parseInt(m.group(1)));
                    }
                } catch (Exception ignored) {}
                // Extract size info after the percentage (e.g. "123/6236 (5%) 45.2 MB")
                try {
                    java.util.regex.Matcher sm = java.util.regex.Pattern
                            .compile("\\d+%\\)\\s+(.+)$").matcher(status);
                    if (sm.find()) {
                        e.sizeText = sm.group(1);
                    }
                } catch (Exception ignored) {}
            } else if (status.contains("/114")) {
                try {
                    String num = status.replaceAll(".*?(\\d+)/114.*", "$1");
                    e.downloadProgress = Math.max(1, (int) (Integer.parseInt(num) * 100.0 / 114));
                } catch (Exception ignored) {}
            }
            adapter.updateByIdentifier(identifier);
        });
    }

    private void startDownload(EditionInfo edition, int position) {
        final String id = edition.identifier;
        edition.downloadProgress = 1;
        adapter.updateItem(position);

        if ("wbw".equals(currentCategory)) {
            String lang = id.replace("wbw.", "");
            dm.downloadWordByWord(lang, status -> updateDownloadProgress(id, status));
        } else if ("reciter".equals(currentCategory)) {
            repository.saveSelectedReciter(id);
            dm.downloadFullQuranAudio(id, status -> updateDownloadProgress(id, status));
        } else if ("translation".equals(edition.type)) {
            String ghEdition = getGitHubTranslationEdition(id);
            if (ghEdition != null) {
                dm.downloadTranslationFromGitHub(id, ghEdition, edition.language,
                        status -> updateDownloadProgress(id, status));
            } else {
                dm.downloadTranslation(id, edition.language, status -> updateDownloadProgress(id, status));
            }
        } else {
            int qcResourceId = getQuranComResourceId(id);
            if (qcResourceId > 0 && isQuranComTranslationAsTafseer(id)) {
                dm.downloadTafseerFromQuranComTranslation(id, qcResourceId, edition.language,
                        status -> updateDownloadProgress(id, status));
            } else if (qcResourceId > 0) {
                dm.downloadTafseerFromQuranCom(id, qcResourceId, edition.language,
                        status -> updateDownloadProgress(id, status));
            } else {
                dm.downloadTafseer(id, edition.language,
                        status -> updateDownloadProgress(id, status));
            }
        }
    }

    /** Get quran.com resource ID for a tafseer, or -1 if it's an alquran.cloud edition */
    private int getQuranComResourceId(String identifier) {
        switch (identifier) {
            case "qc.en.ibnkathir": return 169;
            case "qc.en.maarifulquran": return 168;
            case "qc.en.tazkirulquran": return 817;
            case "qc.ur.ibnkathir": return 160;
            case "qc.ur.bayanulquran": return 159;
            case "qc.ur.fizilal": return 157;
            case "qc.ur.tazkirulquran": return 818;
            case "qc.bn.ibnkathir": return 164;
            case "qc.bn.ahsanulbayaan": return 165;
            case "qc.bn.abubakrzakaria": return 166;
            case "qc.bn.fathulmajid": return 381;
            case "qc.ar.muyassar": return 16;
            case "qc.ar.ibnkathir": return 14;
            case "qc.ar.tabari": return 15;
            case "qc.ar.qurtubi": return 90;
            case "qc.ar.saddi": return 91;
            case "qc.ar.wasit": return 93;
            case "qc.ar.baghawi": return 94;
            case "qc.ru.saddi": return 170;
            case "qc.ku.rebar": return 804;
            case "qc.ur.tafheem": return 97;
            case "qc.ur.usmani": return 151;
            default: return -1;
        }
    }

    /** Check if this tafseer should be downloaded from quran.com translations API (not tafseers API) */
    private boolean isQuranComTranslationAsTafseer(String identifier) {
        switch (identifier) {
            case "qc.ur.tafheem":
            case "qc.ur.usmani":
                return true;
            default:
                return false;
        }
    }

    /** Get fawazahmed0/quran-api edition key for GitHub-only translations, or null */
    private String getGitHubTranslationEdition(String identifier) {
        switch (identifier) {
            case "gh.ur.taqiusmani": return "urd-muhammadtaqiusm";
            case "gh.ur.karamshah": return "urd-muhammadkaramsh";
            case "gh.ur.mahmoodulhassan": return "urd-mahmoodulhassan";
            default: return null;
        }
    }

    private void confirmCancelDownload(EditionInfo edition, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Download")
                .setMessage("Are you sure you want to cancel downloading " + edition.name + "?")
                .setPositiveButton("Cancel Download", (dialog, which) -> {
                    dm.cancelDownload(edition.identifier);
                    edition.downloadProgress = 0;
                    edition.isDownloaded = false;
                    adapter.updateItem(position);
                    Toast.makeText(requireContext(), "Download cancelled", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    private void selectEdition(EditionInfo edition) {
        String lang = repository.getLanguage();
        switch (currentCategory) {
            case "translation":
                // Multi-select: toggle this translation in/out of selected set
                repository.toggleSelectedTranslation(edition.identifier);
                boolean isNowSelected = repository.isTranslationSelected(edition.identifier);
                String transMsg = isNowSelected ?
                        Localization.get(lang, Localization.SELECTED) + ": " + edition.name :
                        Localization.get(lang, Localization.DESELECTED) + ": " + edition.name;
                Toast.makeText(requireContext(), transMsg, Toast.LENGTH_SHORT).show();
                break;
            case "tafseer":
                // Multi-select: toggle this tafseer in/out of selected set
                repository.toggleSelectedTafseer(edition.identifier);
                boolean isTafNowSelected = repository.isTafseerSelected(edition.identifier);
                String tafMsg = isTafNowSelected ?
                        Localization.get(lang, Localization.SELECTED) + ": " + edition.name :
                        Localization.get(lang, Localization.DESELECTED) + ": " + edition.name;
                Toast.makeText(requireContext(), tafMsg, Toast.LENGTH_SHORT).show();
                break;
            case "wbw":
                String wbwLang = edition.identifier.replace("wbw.", "");
                repository.saveSelectedWbwLanguage(wbwLang);
                String activeLabel = Localization.get(lang, Localization.ACTIVE);
                Toast.makeText(requireContext(), activeLabel + ": " + edition.name, Toast.LENGTH_SHORT).show();
                break;
            case "reciter":
                repository.saveSelectedReciter(edition.identifier);
                String activeRecLabel = Localization.get(lang, Localization.ACTIVE);
                Toast.makeText(requireContext(), activeRecLabel + ": " + edition.name, Toast.LENGTH_SHORT).show();
                break;
        }
        updateActiveIdentifier();
    }

    private void updateActiveIdentifier() {
        switch (currentCategory) {
            case "translation":
                // Multi-select: show ticks for all selected translations
                adapter.setActiveIdentifiers(repository.getSelectedTranslations());
                break;
            case "tafseer":
                // Multi-select: show ticks for all selected tafseers
                adapter.setActiveIdentifiers(repository.getSelectedTafseers());
                break;
            case "wbw":
                adapter.setActiveIdentifier("wbw." + repository.getSelectedWbwLanguage());
                break;
            case "reciter":
                adapter.setActiveIdentifier(repository.getSelectedReciter());
                break;
        }
    }

    private void deleteEdition(EditionInfo edition, int position) {
        repository.getExecutor().execute(() -> {
            if ("wbw".equals(currentCategory)) {
                String lang = edition.identifier.replace("wbw.", "");
                repository.deleteWbw(lang);
            } else if ("translation".equals(edition.type)) {
                repository.deleteTranslation(edition.identifier);
            } else if ("tafseer".equals(edition.type)) {
                repository.deleteTafseer(edition.identifier);
            }
            edition.isDownloaded = false;
            edition.downloadProgress = 0;
            repository.updateEditionDownloadState(edition.identifier, false, 0);

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> adapter.updateItem(position));
            }
        });
    }

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());
        View container = getView().findViewById(R.id.library_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        TextView tvTitle = getView().findViewById(R.id.tv_title);
        tvTitle.setTextColor(theme.getAccentColor());

        tabCategory.setBackgroundColor(theme.getSurfaceColor());
        tabCategory.setTabTextColors(theme.getSecondaryTextColor(), theme.getAccentColor());
        tabCategory.setSelectedTabIndicatorColor(theme.getAccentColor());

        tvEmpty.setTextColor(theme.getSecondaryTextColor());

        localizeLabels();

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void localizeLabels() {
        if (getView() == null) return;
        String lang = repository.getLanguage();

        TextView tvTitle = getView().findViewById(R.id.tv_title);
        if (tvTitle != null) tvTitle.setText(Localization.get(lang, Localization.LIBRARY));

        // Tab labels
        if (tabCategory.getTabCount() >= 4) {
            tabCategory.getTabAt(0).setText(Localization.get(lang, Localization.TRANSLATIONS));
            tabCategory.getTabAt(1).setText(Localization.get(lang, Localization.TAFSEERS));
            tabCategory.getTabAt(2).setText(Localization.get(lang, Localization.WORD_BY_WORD));
            tabCategory.getTabAt(3).setText(Localization.get(lang, Localization.AUDIO_RECITERS));
        }

        // Search hint
        TextInputEditText etSearch = getView().findViewById(R.id.et_search);
        if (etSearch != null) etSearch.setHint(Localization.get(lang, Localization.SEARCH_LIBRARY_HINT));
    }

    private boolean initialLoadDone = false;

    @Override
    public void onResume() {
        super.onResume();
        // Skip redundant reload on first show — onViewCreated already called loadEditions()
        if (initialLoadDone) {
            loadEditions();
        } else {
            initialLoadDone = true;
        }
    }
}
