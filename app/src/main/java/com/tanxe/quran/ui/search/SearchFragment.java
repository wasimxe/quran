package com.tanxe.quran.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.tanxe.quran.MainActivity;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.entity.Translation;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.SearchAdapter;
import com.tanxe.quran.util.Localization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFragment extends Fragment {
    private QuranRepository repository;
    private ThemeManager theme;

    private TextInputEditText etSearch;
    private RecyclerView rvResults;
    private TextView tvResultCount, tvNoResults;
    private String searchFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = QuranRepository.getInstance(requireContext());
        theme = ThemeManager.getInstance(requireContext());

        initViews(view);
        applyTheme();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvResults = view.findViewById(R.id.rv_results);
        tvResultCount = view.findViewById(R.id.tv_result_count);
        tvNoResults = view.findViewById(R.id.tv_no_results);

        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Filter chips
        ChipGroup filterChips = view.findViewById(R.id.search_filter_chips);
        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_search_all) searchFilter = "all";
            else if (id == R.id.chip_search_arabic) searchFilter = "arabic";
            else if (id == R.id.chip_search_translation) searchFilter = "translation";
            else if (id == R.id.chip_search_tafseer) searchFilter = "tafseer";
            performSearch();
        });

        // Search on enter
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Live search as user types (debounced)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3) {
                    performSearch();
                }
            }
        });
    }

    private void performSearch() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        if (query.isEmpty()) return;

        repository.getExecutor().execute(() -> {
            List<Ayah> results;
            switch (searchFilter) {
                case "arabic":
                    results = repository.searchArabic(query);
                    break;
                case "translation":
                    // Search across built-in translation AND all downloaded translations
                    results = repository.searchTranslation(query);

                    // Also search across downloaded translations
                    List<Translation> extraResults = repository.searchAllTranslations(query);
                    if (extraResults != null && !extraResults.isEmpty()) {
                        // Merge results: add ayahs from extra results that aren't already in results
                        Set<String> existing = new HashSet<>();
                        if (results != null) {
                            for (Ayah a : results) {
                                existing.add(a.surahNumber + ":" + a.ayahNumber);
                            }
                        } else {
                            results = new ArrayList<>();
                        }

                        for (Translation t : extraResults) {
                            String key = t.surahNumber + ":" + t.ayahNumber;
                            if (!existing.contains(key)) {
                                existing.add(key);
                                // Fetch the full Ayah for display
                                Ayah ayah = repository.getAyah(t.surahNumber, t.ayahNumber);
                                if (ayah != null) results.add(ayah);
                            }
                        }
                    }
                    break;
                default:
                    results = repository.searchAll(query);
                    break;
            }

            if (results == null) results = new ArrayList<>();
            List<Ayah> finalResults = results;

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (finalResults.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        rvResults.setVisibility(View.GONE);
                        tvResultCount.setText("");
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        rvResults.setVisibility(View.VISIBLE);
                        String resLabel = Localization.get(repository.getLanguage(), Localization.RESULTS);
                        tvResultCount.setText(finalResults.size() + " " + resLabel);

                        SearchAdapter adapter = new SearchAdapter(finalResults, theme, ayah -> {
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).navigateToAyah(ayah.surahNumber, ayah.ayahNumber);
                            }
                        });
                        rvResults.setAdapter(adapter);
                    }
                });
            }
        });
    }

    /** Called externally to search a word and show results (e.g. from Learn mode) */
    public void searchFor(String query) {
        if (etSearch == null) return;
        // Set the filter to Arabic since learn mode searches Arabic words
        searchFilter = "arabic";
        // Update chip selection
        if (getView() != null) {
            ChipGroup filterChips = getView().findViewById(R.id.search_filter_chips);
            filterChips.check(R.id.chip_search_arabic);
        }
        etSearch.setText(query);
        etSearch.setSelection(query.length());
        performSearch();
    }

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());
        View container = getView().findViewById(R.id.search_container);
        container.setBackgroundColor(theme.getBackgroundColor());
        tvResultCount.setTextColor(theme.getSecondaryTextColor());
        tvNoResults.setTextColor(theme.getSecondaryTextColor());
        localizeLabels();
    }

    private void localizeLabels() {
        if (getView() == null) return;
        String lang = repository.getLanguage();

        etSearch.setHint(Localization.get(lang, Localization.SEARCH_HINT));

        Chip chipAll = getView().findViewById(R.id.chip_search_all);
        if (chipAll != null) chipAll.setText(Localization.get(lang, Localization.SEARCH_ALL));

        Chip chipArabic = getView().findViewById(R.id.chip_search_arabic);
        if (chipArabic != null) chipArabic.setText(Localization.get(lang, Localization.MODE_ARABIC));

        Chip chipTranslation = getView().findViewById(R.id.chip_search_translation);
        if (chipTranslation != null) chipTranslation.setText(Localization.get(lang, Localization.MODE_TRANSLATION));

        Chip chipTafseer = getView().findViewById(R.id.chip_search_tafseer);
        if (chipTafseer != null) chipTafseer.setText(Localization.get(lang, Localization.TAFSEER));

        tvNoResults.setText(Localization.get(lang, Localization.NO_RESULTS));
    }
}
