package com.tanxe.quran.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.tanxe.quran.MainActivity;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Bookmark;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.download.DownloadManager;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.BookmarkAdapter;
import com.tanxe.quran.ui.adapter.DownloadItemAdapter;
import com.tanxe.quran.ui.learn.LearnFragment;
import com.tanxe.quran.util.QuranDataParser;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {
    private QuranRepository repository;
    private ThemeManager theme;

    private RecyclerView rvBookmarks, rvTranslations, rvTafseers, rvWbw;
    private Slider sliderArabicSize, sliderTranslationSize;
    private DownloadItemAdapter translationAdapter, tafseerAdapter, wbwAdapter;
    private List<DownloadItemAdapter.DownloadItem> translationItems, tafseerItems, wbwItems;
    private DownloadManager dm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = QuranRepository.getInstance(requireContext());
        theme = ThemeManager.getInstance(requireContext());
        dm = new DownloadManager(requireContext());

        initViews(view);
        applyTheme();
        initDownloadLists();
        loadBookmarks();
    }

    private void initViews(View view) {
        rvBookmarks = view.findViewById(R.id.rv_bookmarks);
        rvBookmarks.setLayoutManager(new LinearLayoutManager(requireContext()));

        rvTranslations = view.findViewById(R.id.rv_translations);
        rvTranslations.setLayoutManager(new LinearLayoutManager(requireContext()));

        rvTafseers = view.findViewById(R.id.rv_tafseers);
        rvTafseers.setLayoutManager(new LinearLayoutManager(requireContext()));

        rvWbw = view.findViewById(R.id.rv_wbw);
        rvWbw.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Font sliders
        sliderArabicSize = view.findViewById(R.id.slider_arabic_size);
        sliderTranslationSize = view.findViewById(R.id.slider_translation_size);

        sliderArabicSize.setValue(repository.getArabicFontSize());
        sliderTranslationSize.setValue(repository.getTranslationFontSize());

        sliderArabicSize.addOnChangeListener((slider, value, fromUser) -> {
            repository.setArabicFontSize(value);
        });

        sliderTranslationSize.addOnChangeListener((slider, value, fromUser) -> {
            repository.setTranslationFontSize(value);
        });

        // Theme cards
        MaterialCardView cardEmerald = view.findViewById(R.id.card_emerald);
        MaterialCardView cardMidnight = view.findViewById(R.id.card_midnight);
        MaterialCardView cardPurple = view.findViewById(R.id.card_purple);
        MaterialCardView cardDesert = view.findViewById(R.id.card_desert);

        cardEmerald.setOnClickListener(v -> setTheme(ThemeManager.THEME_EMERALD));
        cardMidnight.setOnClickListener(v -> setTheme(ThemeManager.THEME_MIDNIGHT));
        cardPurple.setOnClickListener(v -> setTheme(ThemeManager.THEME_PURPLE));
        cardDesert.setOnClickListener(v -> setTheme(ThemeManager.THEME_DESERT));

        // Audio download
        Spinner spinnerAudio = view.findViewById(R.id.spinner_audio_surah);
        List<String> surahNames = new ArrayList<>();
        for (int i = 1; i <= 114; i++) {
            surahNames.add("Surah " + i);
        }
        spinnerAudio.setAdapter(new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, surahNames));

        MaterialButton btnDownloadAudio = view.findViewById(R.id.btn_download_audio);
        btnDownloadAudio.setOnClickListener(v -> {
            int surah = spinnerAudio.getSelectedItemPosition() + 1;
            dm.downloadAudioForSurah(surah, status -> {
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> btnDownloadAudio.setText(status));
                }
            });
        });

        // Vocabulary Builder button
        MaterialButton btnVocabulary = view.findViewById(R.id.btn_vocabulary);
        btnVocabulary.setOnClickListener(v -> {
            LearnFragment learnDialog = new LearnFragment();
            learnDialog.show(getChildFragmentManager(), "learn_dialog");
        });

        // Language spinner
        Spinner spinnerLang = view.findViewById(R.id.spinner_language);
        String[] langs = {"English", "اردو (Urdu)", "العربية (Arabic)", "Türkçe (Turkish)"};
        String[] langCodes = {"en", "ur", "ar", "tr"};
        spinnerLang.setAdapter(new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, langs));

        String currentLang = repository.getLanguage();
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                spinnerLang.setSelection(i);
                break;
            }
        }

        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                repository.saveLanguage(langCodes[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void initDownloadLists() {
        // Build translation items
        translationItems = new ArrayList<>();
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.jalandhry", "Jalandhry", "translation", "ur", true));
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.maududi", "Maududi", "translation", "ur", false));
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.kanzuliman", "Kanz-ul-Iman", "translation", "ur", false));
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.junagarhi", "Junagarhi", "translation", "ur", false));
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.qadri", "Tahir-ul-Qadri", "translation", "ur", false));
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.ahmedali", "Ahmed Ali", "translation", "ur", false));
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.jawadi", "Jawadi", "translation", "ur", false));
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.khan", "Ahmed Raza Khan", "translation", "ur", false));
        translationItems.add(new DownloadItemAdapter.DownloadItem("ur.najafi", "Najafi", "translation", "ur", false));

        // Build tafseer items
        tafseerItems = new ArrayList<>();
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.ibnkathir", "Ibn Kathir", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.bayanulquran", "Bayan-ul-Quran", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.tazkirulquran", "Tazkir-ul-Quran", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.zilal", "Fi Zilal-ul-Quran", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.jalalayn", "Jalalayn", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.tabari", "Tabari", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.qurtubi", "Qurtubi", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.baghawi", "Baghawi", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.saddi", "Saddi", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.muyassar", "Muyassar", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.wasit", "Al-Wasit", "tafseer", "ur", false));
        tafseerItems.add(new DownloadItemAdapter.DownloadItem("ur.tanwiralmiqbas", "Tanwir al-Miqbas", "tafseer", "ur", false));

        // Build WBW items
        wbwItems = new ArrayList<>();
        wbwItems.add(new DownloadItemAdapter.DownloadItem("wbw.ur", "Urdu WBW", "wbw", "ur", false));
        wbwItems.add(new DownloadItemAdapter.DownloadItem("wbw.en", "English WBW", "wbw", "en", false));

        // Check which ones are already downloaded
        checkDownloadedStatus();

        // Create adapters
        translationAdapter = new DownloadItemAdapter(translationItems, theme, new DownloadItemAdapter.OnDownloadAction() {
            @Override
            public void onDownload(DownloadItemAdapter.DownloadItem item, int position) {
                startTranslationDownload(item, position);
            }
            @Override
            public void onDelete(DownloadItemAdapter.DownloadItem item, int position) {
                deleteTranslation(item, position);
            }
        });

        tafseerAdapter = new DownloadItemAdapter(tafseerItems, theme, new DownloadItemAdapter.OnDownloadAction() {
            @Override
            public void onDownload(DownloadItemAdapter.DownloadItem item, int position) {
                startTafseerDownload(item, position);
            }
            @Override
            public void onDelete(DownloadItemAdapter.DownloadItem item, int position) {
                deleteTafseer(item, position);
            }
        });

        wbwAdapter = new DownloadItemAdapter(wbwItems, theme, new DownloadItemAdapter.OnDownloadAction() {
            @Override
            public void onDownload(DownloadItemAdapter.DownloadItem item, int position) {
                startWbwDownload(item, position);
            }
            @Override
            public void onDelete(DownloadItemAdapter.DownloadItem item, int position) {
                deleteWbw(item, position);
            }
        });

        rvTranslations.setAdapter(translationAdapter);
        rvTafseers.setAdapter(tafseerAdapter);
        rvWbw.setAdapter(wbwAdapter);
    }

    private void checkDownloadedStatus() {
        repository.getExecutor().execute(() -> {
            List<String> downloadedTrans = repository.getAvailableTranslations();
            List<String> downloadedTafseers = repository.getAvailableTafseers();
            List<String> downloadedWbw = repository.getAvailableWbwLanguages();

            if (downloadedTrans != null) {
                for (DownloadItemAdapter.DownloadItem item : translationItems) {
                    if (!item.isBuiltIn && downloadedTrans.contains(item.edition)) {
                        item.status = "downloaded";
                        item.progress = 100;
                    }
                }
            }

            if (downloadedTafseers != null) {
                for (DownloadItemAdapter.DownloadItem item : tafseerItems) {
                    if (downloadedTafseers.contains(item.edition)) {
                        item.status = "downloaded";
                        item.progress = 100;
                    }
                }
            }

            if (downloadedWbw != null) {
                for (DownloadItemAdapter.DownloadItem item : wbwItems) {
                    String lang = item.edition.replace("wbw.", "");
                    if (downloadedWbw.contains(lang)) {
                        item.status = "downloaded";
                        item.progress = 100;
                    }
                }
            }

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (translationAdapter != null) translationAdapter.notifyDataSetChanged();
                    if (tafseerAdapter != null) tafseerAdapter.notifyDataSetChanged();
                    if (wbwAdapter != null) wbwAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void startTranslationDownload(DownloadItemAdapter.DownloadItem item, int position) {
        item.status = "downloading";
        item.progress = 0;
        translationAdapter.notifyItemChanged(position);

        dm.downloadTranslation(item.edition, item.language, status -> {
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (status.startsWith("✓")) {
                        item.status = "downloaded";
                        item.progress = 100;
                    } else if (status.startsWith("Processing surah")) {
                        try {
                            String num = status.replace("Processing surah ", "").split("/")[0];
                            item.progress = (int) (Integer.parseInt(num) * 100.0 / 114);
                        } catch (Exception ignored) {}
                    } else if (status.startsWith("Failed")) {
                        item.status = "none";
                        item.progress = 0;
                        Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
                    }
                    translationAdapter.notifyItemChanged(position);
                });
            }
        });
    }

    private void startTafseerDownload(DownloadItemAdapter.DownloadItem item, int position) {
        item.status = "downloading";
        item.progress = 0;
        tafseerAdapter.notifyItemChanged(position);

        dm.downloadTafseer(item.edition, item.language, status -> {
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (status.startsWith("✓")) {
                        item.status = "downloaded";
                        item.progress = 100;
                    } else if (status.startsWith("Downloading surah")) {
                        try {
                            String num = status.replace("Downloading surah ", "").split("/")[0];
                            item.progress = (int) (Integer.parseInt(num) * 100.0 / 114);
                        } catch (Exception ignored) {}
                    } else if (status.startsWith("Failed")) {
                        item.status = "none";
                        item.progress = 0;
                        Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
                    }
                    tafseerAdapter.notifyItemChanged(position);
                });
            }
        });
    }

    private void startWbwDownload(DownloadItemAdapter.DownloadItem item, int position) {
        item.status = "downloading";
        item.progress = 0;
        wbwAdapter.notifyItemChanged(position);

        String lang = item.edition.replace("wbw.", "");
        dm.downloadWordByWord(lang, status -> {
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (status.startsWith("✓")) {
                        item.status = "downloaded";
                        item.progress = 100;
                    } else if (status.startsWith("WBW: Surah")) {
                        try {
                            String num = status.replace("WBW: Surah ", "").split("/")[0];
                            item.progress = (int) (Integer.parseInt(num) * 100.0 / 114);
                        } catch (Exception ignored) {}
                    } else if (status.startsWith("Failed")) {
                        item.status = "none";
                        item.progress = 0;
                        Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
                    }
                    wbwAdapter.notifyItemChanged(position);
                });
            }
        });
    }

    private void deleteTranslation(DownloadItemAdapter.DownloadItem item, int position) {
        repository.getExecutor().execute(() -> {
            repository.deleteTranslation(item.edition);
            item.status = "none";
            item.progress = 0;
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> translationAdapter.notifyItemChanged(position));
            }
        });
    }

    private void deleteTafseer(DownloadItemAdapter.DownloadItem item, int position) {
        repository.getExecutor().execute(() -> {
            repository.deleteTafseer(item.edition);
            item.status = "none";
            item.progress = 0;
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> tafseerAdapter.notifyItemChanged(position));
            }
        });
    }

    private void deleteWbw(DownloadItemAdapter.DownloadItem item, int position) {
        repository.getExecutor().execute(() -> {
            String lang = item.edition.replace("wbw.", "");
            repository.deleteWbw(lang);
            item.status = "none";
            item.progress = 0;
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> wbwAdapter.notifyItemChanged(position));
            }
        });
    }

    private void setTheme(String themeName) {
        theme.setTheme(themeName);
        applyTheme();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshTheme();
        }
    }

    private void loadBookmarks() {
        repository.getExecutor().execute(() -> {
            List<Bookmark> bookmarks = repository.getAllBookmarks();
            if (bookmarks == null) bookmarks = new ArrayList<>();
            List<Bookmark> finalBookmarks = bookmarks;

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    TextView tvNoBookmarks = getView().findViewById(R.id.tv_no_bookmarks);
                    if (finalBookmarks.isEmpty()) {
                        tvNoBookmarks.setVisibility(View.VISIBLE);
                        tvNoBookmarks.setTextColor(theme.getSecondaryTextColor());
                        rvBookmarks.setVisibility(View.GONE);
                    } else {
                        tvNoBookmarks.setVisibility(View.GONE);
                        rvBookmarks.setVisibility(View.VISIBLE);
                    }

                    BookmarkAdapter adapter = new BookmarkAdapter(finalBookmarks, theme,
                        bookmark -> {
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).navigateToAyah(bookmark.surahNumber, bookmark.ayahNumber);
                            }
                        },
                        bookmark -> {
                            repository.getExecutor().execute(() -> {
                                repository.removeBookmark(bookmark.surahNumber, bookmark.ayahNumber);
                                loadBookmarks();
                            });
                        }
                    );
                    rvBookmarks.setAdapter(adapter);
                });
            }
        });
    }

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());
        View container = getView().findViewById(R.id.settings_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        // Style all section headers
        int[] headerIds = {R.id.tv_theme_header, R.id.tv_translations_header,
            R.id.tv_tafseers_header, R.id.tv_wbw_header, R.id.tv_audio_header,
            R.id.tv_bookmarks_header, R.id.tv_language_header};
        for (int id : headerIds) {
            TextView tv = getView().findViewById(id);
            if (tv != null) tv.setTextColor(theme.getAccentColor());
        }

        int[] labelIds = {R.id.tv_arabic_size_label, R.id.tv_trans_size_label};
        for (int id : labelIds) {
            TextView tv = getView().findViewById(id);
            if (tv != null) tv.setTextColor(theme.getPrimaryTextColor());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks();
        checkDownloadedStatus();
    }
}
