package com.tanxe.quran.ui.library;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
        dm = new DownloadManager(requireContext());

        rvEditions = view.findViewById(R.id.rv_editions);
        rvEditions.setLayoutManager(new LinearLayoutManager(requireContext()));

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
                loadEditions();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
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

    private void setupLanguageChips() {
        String[] defaultLangs = {"All", "en", "ur", "ar", "tr", "fr", "id", "bn", "fa", "ms", "de", "es"};
        String allLabel = Localization.get(repository.getLanguage(), Localization.SEARCH_ALL);
        for (String lang : defaultLangs) {
            Chip chip = new Chip(requireContext());
            chip.setText(lang.equals("All") ? allLabel : lang.toUpperCase());
            chip.setCheckable(true);
            chip.setChecked("All".equals(lang));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentLanguageFilter = "All".equals(lang) ? "all" : lang;
                    adapter.filterByLanguage(currentLanguageFilter);
                }
            });
            languageChips.addView(chip);
        }
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
            } else {
                editions = repository.getEditionsByType(currentCategory);
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
        };

        List<String> downloadedTafseers = repository.getAvailableTafseers();
        java.util.Set<String> downloadedSet = new java.util.HashSet<>();
        if (downloadedTafseers != null) downloadedSet.addAll(downloadedTafseers);

        for (String[] t : qcTafseers) {
            if (!existing.contains(t[0])) {
                EditionInfo ei = new EditionInfo(t[0], t[1], t[2], t[3], "tafseer",
                        "ar".equals(t[2]) || "ur".equals(t[2]) || "fa".equals(t[2]) || "ku".equals(t[2]) ? "rtl" : "ltr");
                ei.isDownloaded = downloadedSet.contains(t[0]);
                editions.add(ei);
            }
        }
    }

    private void loadReciters() {
        repository.getExecutor().execute(() -> {
            List<ReciterInfo> reciters = repository.getAllReciters();
            if (getActivity() != null && reciters != null) {
                requireActivity().runOnUiThread(() -> {
                    // Convert reciters to EditionInfo for display
                    java.util.ArrayList<EditionInfo> reciterEditions = new java.util.ArrayList<>();
                    for (ReciterInfo r : reciters) {
                        EditionInfo ei = new EditionInfo(r.identifier, r.name, "ar",
                                r.style, "reciter", "rtl");
                        ei.isDownloaded = r.isDownloaded;
                        reciterEditions.add(ei);
                    }
                    adapter.setEditions(reciterEditions);
                    updateActiveIdentifier();
                    tvEmpty.setVisibility(reciterEditions.isEmpty() ? View.VISIBLE : View.GONE);
                    rvEditions.setVisibility(reciterEditions.isEmpty() ? View.GONE : View.VISIBLE);
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

        // Check download status
        repository.getExecutor().execute(() -> {
            List<String> downloaded = repository.getAvailableWbwLanguages();
            for (EditionInfo e : wbwEditions) {
                String lang = e.identifier.replace("wbw.", "");
                e.isDownloaded = downloaded != null && downloaded.contains(lang);
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

    private void startDownload(EditionInfo edition, int position) {
        if ("wbw".equals(currentCategory)) {
            String lang = edition.identifier.replace("wbw.", "");
            edition.downloadProgress = 1;
            adapter.updateItem(position);
            dm.downloadWordByWord(lang, status -> {
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (status.startsWith("\u2713")) {
                            edition.isDownloaded = true;
                            edition.downloadProgress = 100;
                        } else if (status.contains("/114")) {
                            try {
                                String num = status.replaceAll(".*?(\\d+)/114.*", "$1");
                                edition.downloadProgress = (int) (Integer.parseInt(num) * 100.0 / 114);
                            } catch (Exception ignored) {}
                        }
                        adapter.updateItem(position);
                    });
                }
            });
        } else if ("reciter".equals(currentCategory)) {
            Toast.makeText(requireContext(), "Select this reciter in Reading mode to stream audio", Toast.LENGTH_SHORT).show();
            repository.saveSelectedReciter(edition.identifier);
        } else {
            String language = edition.language;
            edition.downloadProgress = 1;
            adapter.updateItem(position);

            if ("translation".equals(edition.type)) {
                dm.downloadTranslation(edition.identifier, language, status -> {
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            if (status.startsWith("\u2713")) {
                                edition.isDownloaded = true;
                                edition.downloadProgress = 100;
                            } else if (status.contains("/114")) {
                                try {
                                    String num = status.replaceAll(".*?(\\d+)/114.*", "$1");
                                    edition.downloadProgress = (int) (Integer.parseInt(num) * 100.0 / 114);
                                } catch (Exception ignored) {}
                            }
                            adapter.updateItem(position);
                        });
                    }
                });
            } else {
                // Determine if this is a quran.com tafseer or alquran.cloud
                DownloadManager.StatusCallback tafseerCallback = status -> {
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            if (status.startsWith("\u2713")) {
                                edition.isDownloaded = true;
                                edition.downloadProgress = 100;
                            } else if (status.contains("/114")) {
                                try {
                                    String num = status.replaceAll(".*?(\\d+)/114.*", "$1");
                                    edition.downloadProgress = (int) (Integer.parseInt(num) * 100.0 / 114);
                                } catch (Exception ignored) {}
                            }
                            adapter.updateItem(position);
                        });
                    }
                };

                int qcResourceId = getQuranComResourceId(edition.identifier);
                if (qcResourceId > 0) {
                    dm.downloadTafseerFromQuranCom(edition.identifier, qcResourceId, language, tafseerCallback);
                } else {
                    dm.downloadTafseer(edition.identifier, language, tafseerCallback);
                }
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
            default: return -1;
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
        String activeLabel = Localization.get(repository.getLanguage(), Localization.ACTIVE);
        switch (currentCategory) {
            case "translation":
                repository.saveSelectedTranslation(edition.identifier);
                Toast.makeText(requireContext(), activeLabel + ": " + edition.name, Toast.LENGTH_SHORT).show();
                break;
            case "tafseer":
                repository.saveSelectedTafseer(edition.identifier);
                Toast.makeText(requireContext(), activeLabel + ": " + edition.name, Toast.LENGTH_SHORT).show();
                break;
            case "wbw":
                String lang = edition.identifier.replace("wbw.", "");
                repository.saveSelectedWbwLanguage(lang);
                Toast.makeText(requireContext(), activeLabel + ": " + edition.name, Toast.LENGTH_SHORT).show();
                break;
            case "reciter":
                repository.saveSelectedReciter(edition.identifier);
                Toast.makeText(requireContext(), activeLabel + ": " + edition.name, Toast.LENGTH_SHORT).show();
                break;
        }
        updateActiveIdentifier();
    }

    private void updateActiveIdentifier() {
        switch (currentCategory) {
            case "translation":
                adapter.setActiveIdentifier(repository.getSelectedTranslation());
                break;
            case "tafseer":
                adapter.setActiveIdentifier(repository.getSelectedTafseer());
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

    @Override
    public void onResume() {
        super.onResume();
        loadEditions();
    }
}
