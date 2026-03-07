package com.tanxe.quran.ui.surahindex;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.tanxe.quran.MainActivity;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Bookmark;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.BookmarkAdapter;
import com.tanxe.quran.ui.adapter.JuzAdapter;
import com.tanxe.quran.util.Localization;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SurahIndexFragment extends Fragment {
    private QuranRepository repository;
    private ThemeManager theme;
    private RecyclerView rvSurahs;
    private TextView tvEmpty;
    private SurahIndexAdapter surahAdapter;
    private JuzAdapter juzAdapter;
    private TabLayout tabLayout;
    private Typeface arabicFont;
    private List<JSONObject> surahList = new ArrayList<>();
    private int currentTab = 0; // 0=Surahs, 1=Juz, 2=Bookmarks

    // Juz boundary data: [surah, ayah] that starts each juz (30 entries)
    private static final int[][] JUZ_STARTS = {
        {1,1}, {2,142}, {2,253}, {3,93}, {4,24}, {4,148}, {5,82}, {6,111},
        {7,88}, {8,41}, {9,93}, {11,6}, {12,53}, {15,1}, {17,1}, {18,75},
        {21,1}, {23,1}, {25,21}, {27,56}, {29,46}, {33,31}, {36,28}, {39,32},
        {41,47}, {46,1}, {51,31}, {58,1}, {67,1}, {78,1}
    };

    // Arabic names for each Juz (traditional names)
    private static final String[] JUZ_ARABIC_NAMES = {
        "آلم", "سَيَقُولُ", "تِلْكَ الرُّسُلُ", "لَنْ تَنَالُوا", "وَالْمُحْصَنَاتُ",
        "لَا يُحِبُّ اللَّهُ", "وَإِذَا سَمِعُوا", "وَلَوْ أَنَّنَا", "قَالَ الْمَلَأُ", "وَاعْلَمُوا",
        "يَعْتَذِرُونَ", "وَمَا مِنْ دَابَّةٍ", "وَمَا أُبَرِّئُ", "رُبَمَا", "سُبْحَانَ الَّذِي",
        "قَالَ أَلَمْ", "اقْتَرَبَ", "قَدْ أَفْلَحَ", "وَقَالَ الَّذِينَ", "أَمَّنْ خَلَقَ",
        "اتْلُ مَا أُوحِيَ", "وَمَنْ يَقْنُتْ", "وَمَا لِيَ", "فَمَنْ أَظْلَمُ", "إِلَيْهِ يُرَدُّ",
        "حم", "قَالَ فَمَا خَطْبُكُمْ", "قَدْ سَمِعَ اللَّهُ", "تَبَارَكَ الَّذِي", "عَمَّ"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_surah_index, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = QuranRepository.getInstance(requireContext());
        theme = ThemeManager.getInstance(requireContext());

        try {
            arabicFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/al_mushaf.ttf");
        } catch (Exception e) {
            arabicFont = Typeface.DEFAULT;
        }

        rvSurahs = view.findViewById(R.id.rv_surahs);
        rvSurahs.setLayoutManager(new LinearLayoutManager(requireContext()));
        tvEmpty = view.findViewById(R.id.tv_empty);

        tabLayout = view.findViewById(R.id.tab_layout);
        String lang = repository.getLanguage();
        tabLayout.addTab(tabLayout.newTab().setText(Localization.get(lang, Localization.SURAHS)));
        tabLayout.addTab(tabLayout.newTab().setText(Localization.get(lang, Localization.JUZ)));
        tabLayout.addTab(tabLayout.newTab().setText(Localization.get(lang, Localization.BOOKMARKS)));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadTabContent();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        TextInputEditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (currentTab == 0 && surahAdapter != null) {
                    surahAdapter.filter(s.toString());
                }
            }
        });

        surahAdapter = new SurahIndexAdapter(theme, arabicFont, surahNumber -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAyah(surahNumber, 1);
            }
        });

        juzAdapter = new JuzAdapter(theme, arabicFont, (surah, ayah) -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAyah(surah, ayah);
            }
        });

        rvSurahs.setAdapter(surahAdapter);

        loadSurahMetadata();
        applyTheme();
    }

    private void loadSurahMetadata() {
        repository.getExecutor().execute(() -> {
            try {
                InputStream is = requireContext().getAssets().open("surah_metadata.json");
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();

                String json = new String(buffer, StandardCharsets.UTF_8);
                JSONArray array = new JSONArray(json);

                surahList.clear();
                for (int i = 0; i < array.length(); i++) {
                    surahList.add(array.getJSONObject(i));
                }

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> surahAdapter.setSurahs(surahList));
                }
            } catch (Exception e) {
                loadSurahsFromDb();
            }
        });
    }

    private void loadSurahsFromDb() {
        repository.getExecutor().execute(() -> {
            var surahs = repository.getAllSurahs();
            surahList.clear();
            if (surahs != null) {
                for (var s : surahs) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("number", s.surahNumber);
                        obj.put("nameEn", s.surahNameEn);
                        obj.put("nameAr", s.surahNameAr);
                        obj.put("meaningEn", "");
                        obj.put("revelationType", "");
                        obj.put("ayahCount", com.tanxe.quran.util.QuranDataParser.SURAH_AYAH_COUNT[s.surahNumber - 1]);
                        surahList.add(obj);
                    } catch (Exception ignored) {}
                }
            }
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> surahAdapter.setSurahs(surahList));
            }
        });
    }

    private void loadTabContent() {
        tvEmpty.setVisibility(View.GONE);
        switch (currentTab) {
            case 0: // Surahs
                rvSurahs.setAdapter(surahAdapter);
                surahAdapter.setSurahs(surahList);
                rvSurahs.setVisibility(View.VISIBLE);
                break;
            case 1: // Juz
                loadJuzView();
                break;
            case 2: // Bookmarks
                loadBookmarksView();
                break;
        }
    }

    private void loadJuzView() {
        rvSurahs.setAdapter(juzAdapter);
        rvSurahs.setVisibility(View.VISIBLE);

        // Build surah name lookup from surahList
        String[] surahNames = new String[115]; // 1-indexed
        for (JSONObject obj : surahList) {
            int num = obj.optInt("number", 0);
            if (num > 0 && num <= 114) {
                surahNames[num] = obj.optString("nameEn", "Surah " + num);
            }
        }

        List<JuzAdapter.JuzItem> juzItems = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            int juzNum = i + 1;
            int startSurah = JUZ_STARTS[i][0];
            int startAyah = JUZ_STARTS[i][1];
            String startName = surahNames[startSurah] != null ? surahNames[startSurah] : "Surah " + startSurah;

            // Determine surah range for this juz
            int endSurah;
            if (i < 29) {
                // End surah is the surah before next juz starts (or same if ayah > 1)
                int nextStartSurah = JUZ_STARTS[i + 1][0];
                int nextStartAyah = JUZ_STARTS[i + 1][1];
                endSurah = nextStartAyah > 1 ? nextStartSurah : nextStartSurah - 1;
            } else {
                endSurah = 114;
            }

            String endName = surahNames[endSurah] != null ? surahNames[endSurah] : "Surah " + endSurah;
            String surahRange;
            if (startSurah == endSurah) {
                surahRange = startName;
            } else {
                surahRange = startName + " — " + endName;
            }

            String arabicName = JUZ_ARABIC_NAMES[i];
            juzItems.add(new JuzAdapter.JuzItem(juzNum, startSurah, startAyah, startName, surahRange, arabicName));
        }

        juzAdapter.setItems(juzItems);
    }

    private void loadBookmarksView() {
        repository.getExecutor().execute(() -> {
            List<Bookmark> bookmarks = repository.getAllBookmarks();
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (bookmarks != null && !bookmarks.isEmpty()) {
                        tvEmpty.setVisibility(View.GONE);
                        rvSurahs.setVisibility(View.VISIBLE);
                        BookmarkAdapter adapter = new BookmarkAdapter(bookmarks, theme,
                                bookmark -> {
                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).navigateToAyah(bookmark.surahNumber, bookmark.ayahNumber);
                                    }
                                },
                                bookmark -> {
                                    repository.getExecutor().execute(() -> {
                                        repository.removeBookmark(bookmark.surahNumber, bookmark.ayahNumber);
                                        loadBookmarksView();
                                    });
                                });
                        rvSurahs.setAdapter(adapter);
                    } else {
                        rvSurahs.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText(Localization.get(repository.getLanguage(), Localization.NO_BOOKMARKS));
                        tvEmpty.setTextColor(theme.getSecondaryTextColor());
                    }
                });
            }
        });
    }

    public void showBookmarksTab() {
        if (tabLayout != null && tabLayout.getTabCount() > 2) {
            tabLayout.selectTab(tabLayout.getTabAt(2));
        }
    }

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());
        View container = getView().findViewById(R.id.surah_index_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        TextView tvTitle = getView().findViewById(R.id.tv_title);
        tvTitle.setTextColor(theme.getAccentColor());

        tabLayout.setBackgroundColor(theme.getSurfaceColor());
        tabLayout.setTabTextColors(theme.getSecondaryTextColor(), theme.getAccentColor());
        tabLayout.setSelectedTabIndicatorColor(theme.getAccentColor());

        localizeLabels();

        if (surahAdapter != null) surahAdapter.notifyDataSetChanged();
        if (juzAdapter != null) juzAdapter.notifyDataSetChanged();
        // Refresh bookmarks tab if active
        if (currentTab == 2) loadBookmarksView();
    }

    private void localizeLabels() {
        if (getView() == null) return;
        String lang = repository.getLanguage();

        TextView tvTitle = getView().findViewById(R.id.tv_title);
        if (tvTitle != null) tvTitle.setText(Localization.get(lang, Localization.SURAHS));

        TextInputEditText etSearch = getView().findViewById(R.id.et_search);
        if (etSearch != null) etSearch.setHint(Localization.get(lang, Localization.SEARCH_SURAH_HINT));

        if (tabLayout.getTabCount() >= 3) {
            tabLayout.getTabAt(0).setText(Localization.get(lang, Localization.SURAHS));
            tabLayout.getTabAt(1).setText(Localization.get(lang, Localization.JUZ));
            tabLayout.getTabAt(2).setText(Localization.get(lang, Localization.BOOKMARKS));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentTab == 2) loadBookmarksView();
    }
}
