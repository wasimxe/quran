package com.tanxe.quran.ui.search;

import android.os.Bundle;
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

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.tanxe.quran.MainActivity;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.SearchAdapter;

import java.util.ArrayList;
import java.util.List;

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
                    results = repository.searchTranslation(query);
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
                        tvResultCount.setText(finalResults.size() + " results");

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

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());
        View container = getView().findViewById(R.id.search_container);
        container.setBackgroundColor(theme.getBackgroundColor());
        tvResultCount.setTextColor(theme.getSecondaryTextColor());
        tvNoResults.setTextColor(theme.getSecondaryTextColor());
    }
}
