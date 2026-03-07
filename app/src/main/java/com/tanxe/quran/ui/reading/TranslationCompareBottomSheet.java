package com.tanxe.quran.ui.reading;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Translation;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;

import java.util.ArrayList;
import java.util.List;

public class TranslationCompareBottomSheet extends BottomSheetDialogFragment {

    private int surah, ayah;
    private String surahName;
    private String arabicText;
    private String defaultTranslation;
    private Typeface arabicFont;
    private Typeface urduFont;

    // Loaded entries with their type tag
    private final List<EntryData> allEntries = new ArrayList<>();
    private LinearLayout container;
    private ThemeManager theme;
    private boolean isRtl;
    private String currentFilter = "all"; // "all", "translation", "tafseer"

    private static class EntryData {
        final String label;
        final String text;
        final String type; // "translation" or "tafseer"
        EntryData(String label, String text, String type) {
            this.label = label;
            this.text = text;
            this.type = type;
        }
    }

    public void setData(int surah, int ayah, String surahName, String arabicText,
                        String defaultTranslation, Typeface arabicFont, Typeface urduFont) {
        this.surah = surah;
        this.ayah = ayah;
        this.surahName = surahName;
        this.arabicText = arabicText;
        this.defaultTranslation = defaultTranslation;
        this.arabicFont = arabicFont;
        this.urduFont = urduFont;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_compare_translations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Guard: if fragment was recreated without setData(), dismiss
        if (arabicText == null && surah == 0) {
            dismissAllowingStateLoss();
            return;
        }

        theme = ThemeManager.getInstance(requireContext());
        QuranRepository repository = QuranRepository.getInstance(requireContext());

        View sheetContainer = view.findViewById(R.id.sheet_container);
        sheetContainer.setBackgroundColor(theme.getSurfaceColor());

        // Header
        TextView tvRef = view.findViewById(R.id.tv_ayah_ref);
        tvRef.setText(surahName + " " + surah + ":" + ayah);
        tvRef.setTextColor(theme.getAccentColor());

        // Arabic text
        TextView tvArabic = view.findViewById(R.id.tv_arabic);
        tvArabic.setText(arabicText != null ? arabicText : "");
        tvArabic.setTextColor(theme.getArabicTextColor());
        if (arabicFont != null) tvArabic.setTypeface(arabicFont);

        container = view.findViewById(R.id.translations_container);

        String appLang = repository.getLanguage();
        isRtl = "ur".equals(appLang) || "ar".equals(appLang) || "fa".equals(appLang);

        // Localize filter chips
        Chip chipAll = view.findViewById(R.id.chip_all);
        Chip chipTrans = view.findViewById(R.id.chip_translations);
        Chip chipTafs = view.findViewById(R.id.chip_tafseers);
        chipAll.setText(Localization.get(appLang, Localization.SEARCH_ALL));
        chipTrans.setText(Localization.get(appLang, Localization.TRANSLATIONS));
        chipTafs.setText(Localization.get(appLang, Localization.TAFSEERS));

        // Theme chips
        int chipBg = theme.getModePillColor();
        int chipTextColor = theme.getPrimaryTextColor();
        for (Chip chip : new Chip[]{chipAll, chipTrans, chipTafs}) {
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setTextColor(chipTextColor);
            chip.setChipStrokeWidth(1f);
        }

        ChipGroup filterChips = view.findViewById(R.id.filter_chips);
        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_all) currentFilter = "all";
            else if (id == R.id.chip_translations) currentFilter = "translation";
            else if (id == R.id.chip_tafseers) currentFilter = "tafseer";
            renderEntries();
        });

        // Load all data on background
        repository.getExecutor().execute(() -> {
            String builtInText = "ur".equals(appLang) ? defaultTranslation : null;
            List<String> editions = repository.getTranslationsByLanguage(appLang);
            List<String> tafseers = repository.getTafseersByLanguage(appLang);

            // Collect all entries
            List<EntryData> entries = new ArrayList<>();

            // Built-in translation (Urdu only)
            if (builtInText != null) {
                entries.add(new EntryData("ur.jalandhry (Built-in)", builtInText, "translation"));
            }

            // Downloaded translations
            if (editions != null) {
                for (String edition : editions) {
                    Translation trans = repository.getTranslation(surah, ayah, edition);
                    if (trans != null) {
                        entries.add(new EntryData(edition, trans.text, "translation"));
                    }
                }
            }

            // Downloaded tafseers
            if (tafseers != null) {
                for (String edition : tafseers) {
                    com.tanxe.quran.data.entity.Tafseer tafseer = repository.getTafseer(surah, ayah, edition);
                    if (tafseer != null) {
                        entries.add(new EntryData(edition, tafseer.text, "tafseer"));
                    }
                }
            }

            if (getActivity() == null || !isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                allEntries.clear();
                allEntries.addAll(entries);
                renderEntries();
            });
        });
    }

    private void renderEntries() {
        container.removeAllViews();
        for (EntryData entry : allEntries) {
            if (!"all".equals(currentFilter) && !currentFilter.equals(entry.type)) continue;

            String displayLabel = entry.label;
            if ("tafseer".equals(entry.type)) {
                displayLabel = entry.label + " (Tafseer)";
            }
            addTranslationEntry(container, theme, displayLabel, entry.text, isRtl);
        }

        if (container.getChildCount() == 0) {
            TextView tvEmpty = new TextView(requireContext());
            tvEmpty.setText("—");
            tvEmpty.setTextColor(theme.getSecondaryTextColor());
            tvEmpty.setTextSize(14);
            tvEmpty.setPadding(0, 24, 0, 24);
            container.addView(tvEmpty);
        }
    }

    private void addTranslationEntry(LinearLayout container, ThemeManager theme, String editionName, String text, boolean isRtl) {
        // Edition name label
        TextView tvEdition = new TextView(requireContext());
        tvEdition.setText(editionName);
        tvEdition.setTextSize(12);
        tvEdition.setTextColor(theme.getAccentColor());
        tvEdition.setPadding(0, 16, 0, 4);
        tvEdition.setTypeface(null, Typeface.BOLD);
        container.addView(tvEdition);

        // Translation text
        TextView tvText = new TextView(requireContext());
        tvText.setText(text);
        tvText.setTextSize(15);
        tvText.setTextColor(theme.getPrimaryTextColor());
        tvText.setLineSpacing(0, 1.3f);
        tvText.setTextIsSelectable(true);
        if (isRtl) {
            if (urduFont != null) tvText.setTypeface(urduFont);
            tvText.setTextDirection(View.TEXT_DIRECTION_RTL);
        }
        container.addView(tvText);

        // Divider
        View divider = new View(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.topMargin = 12;
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(theme.getDividerColor());
        container.addView(divider);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) return;
        // Expand to show more content
        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }
}
