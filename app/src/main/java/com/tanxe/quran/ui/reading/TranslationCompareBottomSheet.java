package com.tanxe.quran.ui.reading;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Bookmark;
import com.tanxe.quran.data.entity.Translation;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.share.ShareCardGenerator;
import com.tanxe.quran.util.Localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TranslationCompareBottomSheet extends BottomSheetDialogFragment {

    private int surah, ayah;
    private String surahName;
    private String arabicText;
    private String defaultTranslation;
    private Typeface arabicFont;
    private Typeface urduFont;
    private String initialFilter = "all"; // "all", "translation", "tafseer"
    private Runnable onDismissListener;

    // Loaded entries with their type tag
    private final List<EntryData> allEntries = new ArrayList<>();
    private LinearLayout container;
    private ThemeManager theme;
    private boolean isRtl;
    private String currentFilter = "all";

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

    /** Set which filter chip to activate on open: "all", "translation", or "tafseer" */
    public void setInitialFilter(String filter) {
        this.initialFilter = filter;
    }

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
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

        // Arabic text (selectable for copy/paste + search)
        TextView tvArabic = view.findViewById(R.id.tv_arabic);
        tvArabic.setText(arabicText != null ? arabicText : "");
        tvArabic.setTextColor(theme.getArabicTextColor());
        tvArabic.setTextIsSelectable(true);
        if (arabicFont != null) tvArabic.setTypeface(arabicFont);
        tvArabic.setCustomSelectionActionModeCallback(createSearchActionCallback(tvArabic, "arabic"));

        container = view.findViewById(R.id.translations_container);

        String appLang = repository.getLanguage();
        isRtl = "ur".equals(appLang) || "ar".equals(appLang) || "fa".equals(appLang);

        // === Action buttons ===
        setupActionButtons(view, repository);

        // === Filter chips ===
        setupFilterChips(view, appLang);

        // Apply initial filter
        currentFilter = initialFilter;
        if ("translation".equals(initialFilter)) {
            ((Chip) view.findViewById(R.id.chip_translations)).setChecked(true);
        } else if ("tafseer".equals(initialFilter)) {
            ((Chip) view.findViewById(R.id.chip_tafseers)).setChecked(true);
        }

        // Load ALL selected translations and tafseers on background
        repository.getExecutor().execute(() -> {
            List<EntryData> entries = new ArrayList<>();

            // Built-in translation (always include)
            if (defaultTranslation != null && !defaultTranslation.isEmpty()) {
                entries.add(new EntryData("ur.jalandhry (Built-in)", defaultTranslation, "translation"));
            }

            // All selected translations
            Set<String> selectedTrans = repository.getSelectedTranslations();
            List<String> downloadedTrans = repository.getAvailableTranslations();
            if (downloadedTrans != null) {
                for (String edition : downloadedTrans) {
                    // Show if selected, or show all downloaded if none explicitly selected
                    if (selectedTrans.contains(edition)) {
                        Translation trans = repository.getTranslation(surah, ayah, edition);
                        if (trans != null) {
                            entries.add(new EntryData(edition, trans.text, "translation"));
                        }
                    }
                }
            }

            // All selected tafseers
            Set<String> selectedTafs = repository.getSelectedTafseers();
            List<String> downloadedTafs = repository.getAvailableTafseers();
            if (downloadedTafs != null) {
                for (String edition : downloadedTafs) {
                    if (selectedTafs.contains(edition)) {
                        com.tanxe.quran.data.entity.Tafseer tafseer = repository.getTafseer(surah, ayah, edition);
                        if (tafseer != null) {
                            entries.add(new EntryData(edition, tafseer.text, "tafseer"));
                        }
                    }
                }
            }

            // Sort by language: Urdu first, then Arabic, Persian, English, others
            entries.sort((a, b) -> Integer.compare(
                    editionLanguagePriority(a.label),
                    editionLanguagePriority(b.label)));

            if (getActivity() == null || !isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                allEntries.clear();
                allEntries.addAll(entries);
                renderEntries();
            });
        });
    }

    private void setupActionButtons(View view, QuranRepository repository) {
        int iconColor = theme.getAccentColor();

        // Bookmark button
        ImageButton btnBookmark = view.findViewById(R.id.btn_bookmark);
        btnBookmark.setColorFilter(iconColor);
        repository.getExecutor().execute(() -> {
            boolean isBookmarked = repository.isBookmarked(surah, ayah);
            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() -> {
                btnBookmark.setImageResource(isBookmarked ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
            });
        });
        btnBookmark.setOnClickListener(v -> {
            repository.getExecutor().execute(() -> {
                boolean wasBookmarked = repository.isBookmarked(surah, ayah);
                if (wasBookmarked) {
                    repository.removeBookmark(surah, ayah);
                } else {
                    repository.addBookmark(new Bookmark(surah, ayah, surahName, ""));
                }
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    btnBookmark.setImageResource(wasBookmarked ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_filled);
                    String lang = repository.getLanguage();
                    String msg = Localization.get(lang, wasBookmarked ? Localization.BOOKMARK_REMOVED : Localization.BOOKMARK_ADDED);
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                });
            });
        });

        // Reading point button
        ImageButton btnReadingPoint = view.findViewById(R.id.btn_reading_point);
        btnReadingPoint.setColorFilter(iconColor);
        btnReadingPoint.setOnClickListener(v -> {
            repository.saveCurrentPosition(surah, ayah);
            String msg = Localization.get(repository.getLanguage(), Localization.READING_POINT_SET);
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // Share button - show dialog with text/image options
        ImageButton btnShare = view.findViewById(R.id.btn_share);
        btnShare.setColorFilter(iconColor);
        btnShare.setOnClickListener(v -> {
            String lang = repository.getLanguage();
            String[] options = {
                Localization.get(lang, Localization.SHARE),
                Localization.get(lang, Localization.SHARE_IMAGE)
            };
            new AlertDialog.Builder(requireContext())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Share as text: Arabic + first available translation
                        String transText = "";
                        for (EntryData entry : allEntries) {
                            if ("translation".equals(entry.type)) {
                                transText = entry.text;
                                break;
                            }
                        }
                        String text = (arabicText != null ? arabicText : "") + "\n\n" +
                                transText + "\n\n[" + surahName + " " + surah + ":" + ayah + "]";
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_ayah)));
                    } else {
                        // Share as image
                        repository.getExecutor().execute(() -> {
                            com.tanxe.quran.data.entity.Ayah ayahData = repository.getAyah(surah, ayah);
                            if (ayahData != null && getActivity() != null) {
                                requireActivity().runOnUiThread(() ->
                                    ShareCardGenerator.shareAyahAsImage(requireContext(), ayahData,
                                            ayahData.defaultTranslation, arabicFont));
                            }
                        });
                    }
                })
                .show();
        });
    }

    private void setupFilterChips(View view, String appLang) {
        Chip chipAll = view.findViewById(R.id.chip_all);
        Chip chipTrans = view.findViewById(R.id.chip_translations);
        Chip chipTafs = view.findViewById(R.id.chip_tafseers);
        chipAll.setText(Localization.get(appLang, Localization.SEARCH_ALL));
        chipTrans.setText(Localization.get(appLang, Localization.TRANSLATIONS));
        chipTafs.setText(Localization.get(appLang, Localization.TAFSEERS));

        // Theme chips
        int accentColor = theme.getAccentColor();
        int chipTextColor = theme.getPrimaryTextColor();
        int chipBg = theme.getModePillColor();

        android.content.res.ColorStateList chipBgStates = new android.content.res.ColorStateList(
                new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                new int[]{accentColor, chipBg}
        );
        android.content.res.ColorStateList chipTextStates = new android.content.res.ColorStateList(
                new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                new int[]{0xFFFFFFFF, chipTextColor}
        );
        for (Chip chip : new Chip[]{chipAll, chipTrans, chipTafs}) {
            chip.setChipBackgroundColor(chipBgStates);
            chip.setTextColor(chipTextStates);
            chip.setChipStrokeWidth(0f);
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
            String lang = QuranRepository.getInstance(requireContext()).getLanguage();
            tvEmpty.setText(Localization.get(lang, Localization.NO_RESULTS));
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

        // Translation text (selectable for copy/paste)
        TextView tvText = new TextView(requireContext());
        tvText.setTextSize(15);
        tvText.setTextColor(theme.getPrimaryTextColor());
        tvText.setLineSpacing(0, 1.05f);
        tvText.setTextIsSelectable(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            tvText.setBreakStrategy(android.graphics.text.LineBreaker.BREAK_STRATEGY_SIMPLE);
        }
        // Detect RTL for this specific edition (handles ur.*, qc.ur.*, gh.ur.* etc.)
        boolean editionRtl = isRtlEdition(editionName);
        if (editionRtl) {
            if (urduFont != null) tvText.setTypeface(urduFont);
            tvText.setTextDirection(View.TEXT_DIRECTION_RTL);
            tvText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            tvText.setTextDirection(View.TEXT_DIRECTION_LTR);
            tvText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        // "Search in Quran" on text selection
        String searchType = editionName.contains("tafseer") || editionName.contains("Tafseer") ? "tafseer" : "translation";
        tvText.setCustomSelectionActionModeCallback(createSearchActionCallback(tvText, searchType));
        // Long text: show truncated, tap to expand
        final boolean isLong = text != null && text.length() > 800;
        if (isLong) {
            tvText.setText(text.substring(0, 800) + "\u2026\n\n\u25BC Tap to read more");
            tvText.setOnClickListener(v -> {
                if (tvText.getTag() == null) {
                    tvText.setText(text);
                    tvText.setTag("expanded");
                } else {
                    tvText.setText(text.substring(0, 800) + "\u2026\n\n\u25BC Tap to read more");
                    tvText.setTag(null);
                }
            });
        } else {
            tvText.setText(text);
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
        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.run();
        }
    }

    private android.view.ActionMode.Callback createSearchActionCallback(android.widget.TextView tv, String searchType) {
        return new android.view.ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) {
                menu.add(0, 0x7001, 10, "Search in Quran")
                    .setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
                return true;
            }
            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) { return false; }
            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) {
                if (item.getItemId() == 0x7001) {
                    int start = tv.getSelectionStart();
                    int end = tv.getSelectionEnd();
                    if (start >= 0 && end > start) {
                        String selected = tv.getText().subSequence(start, end).toString().trim();
                        if (!selected.isEmpty() && getActivity() instanceof com.tanxe.quran.MainActivity) {
                            dismissAllowingStateLoss();
                            ((com.tanxe.quran.MainActivity) getActivity()).navigateToSearch(selected);
                        }
                    }
                    mode.finish();
                    return true;
                }
                return false;
            }
            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {}
        };
    }

    /** Language priority for sorting: Urdu first, then Arabic, Persian, English, others */
    private static int editionLanguagePriority(String label) {
        if (label.startsWith("ur.") || label.contains(".ur.")) return 0;
        if (label.startsWith("ar.") || label.contains(".ar.")) return 1;
        if (label.startsWith("fa.") || label.contains(".fa.")) return 2;
        if (label.startsWith("en.") || label.contains(".en.")) return 3;
        return 4;
    }

    /** Check if edition is RTL (handles ur.*, qc.ur.*, gh.ur.* naming patterns) */
    private static boolean isRtlEdition(String label) {
        return label.startsWith("ur.") || label.contains(".ur.")
                || label.startsWith("ar.") || label.contains(".ar.")
                || label.startsWith("fa.") || label.contains(".fa.");
    }
}
