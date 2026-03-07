package com.tanxe.quran.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.entity.Tafseer;
import com.tanxe.quran.data.entity.Translation;
import com.tanxe.quran.data.entity.WordByWord;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.QuranDataParser;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class AyahPagerAdapter extends RecyclerView.Adapter<AyahPagerAdapter.AyahViewHolder> {

    private final QuranRepository repository;
    private final ThemeManager theme;
    private final Typeface arabicFont;
    private final Typeface urduFont;
    private final ExecutorService executor;

    // Display modes: "arabic", "translation", "tafseer", "wbw"
    private String displayMode = "translation";

    /** Callback for long-press on ayah */
    public interface OnAyahLongPressListener {
        void onAyahLongPress(int surah, int ayah, int position);
    }

    private OnAyahLongPressListener longPressListener;

    // Font sizes for pinch-to-zoom
    private float arabicFontSize;
    private float translationFontSize;

    // Currently playing position for highlighting
    private int playingPosition = -1;

    private static final int TOTAL_AYAHS = QuranDataParser.TOTAL_AYAHS;

    public AyahPagerAdapter(QuranRepository repository, ThemeManager theme,
                            Typeface arabicFont, Typeface urduFont) {
        this.repository = repository;
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.urduFont = urduFont;
        this.executor = repository.getExecutor();
        this.arabicFontSize = repository.getArabicFontSize();
        this.translationFontSize = repository.getTranslationFontSize();
        setHasStableIds(true);
    }

    public void setLongPressListener(OnAyahLongPressListener listener) {
        this.longPressListener = listener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDisplayMode(String mode) {
        if (!this.displayMode.equals(mode)) {
            this.displayMode = mode;
            notifyDataSetChanged();
        }
    }

    public String getDisplayMode() {
        return displayMode;
    }

    // Keep for backward compat — no-ops
    public void setLearningMode(boolean learning) {}
    public boolean isLearningMode() { return false; }
    public void setShowTranslation(boolean show) {
        setDisplayMode(show ? "translation" : "arabic");
    }
    public boolean isShowTranslation() { return !"arabic".equals(displayMode); }

    public void updateFontSize(float arabicSp, float transSp) {
        this.arabicFontSize = arabicSp;
        this.translationFontSize = transSp;
        notifyDataSetChanged();
    }

    public float getArabicFontSize() { return arabicFontSize; }
    public float getTranslationFontSize() { return translationFontSize; }

    /** Set the currently-playing position and highlight it */
    public void setPlayingPosition(int position) {
        int oldPos = playingPosition;
        playingPosition = position;
        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (position >= 0) notifyItemChanged(position);
    }

    public int getPlayingPosition() { return playingPosition; }

    /** Convert flat position (0-6235) to [surah, ayah] */
    public static int[] positionToSurahAyah(int position) {
        int remaining = position;
        for (int s = 0; s < QuranDataParser.SURAH_AYAH_COUNT.length; s++) {
            if (remaining < QuranDataParser.SURAH_AYAH_COUNT[s]) {
                return new int[]{s + 1, remaining + 1};
            }
            remaining -= QuranDataParser.SURAH_AYAH_COUNT[s];
        }
        return new int[]{114, 6};
    }

    /** Convert (surah, ayah) to flat position */
    public static int surahAyahToPosition(int surah, int ayah) {
        int pos = 0;
        for (int s = 0; s < surah - 1 && s < QuranDataParser.SURAH_AYAH_COUNT.length; s++) {
            pos += QuranDataParser.SURAH_AYAH_COUNT[s];
        }
        return pos + ayah - 1;
    }

    @NonNull
    @Override
    public AyahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ayah_page, parent, false);
        return new AyahViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahViewHolder holder, int position) {
        int[] sa = positionToSurahAyah(position);
        int surah = sa[0];
        int ayah = sa[1];
        String mode = this.displayMode; // capture for background thread

        // Apply theme
        boolean isPlayingItem = (position == playingPosition);
        holder.container.setBackgroundColor(isPlayingItem ? theme.getPlayingAyahBg() : theme.getBackgroundColor());
        holder.tvArabic.setTextColor(isPlayingItem ? theme.getActiveAyahTextColor() : theme.getArabicTextColor());
        holder.tvArabic.setTypeface(arabicFont);
        holder.tvAyahMarker.setTextColor(theme.getSecondaryTextColor());
        holder.tvBismillah.setTextColor(theme.getAccentColor());
        if (arabicFont != null) holder.tvBismillah.setTypeface(arabicFont);
        holder.dividerBottom.setBackgroundColor(theme.getDividerColor());
        holder.dividerTop.setBackgroundColor(theme.getDividerColor());
        holder.tvSurahHeader.setTextColor(theme.getAccentColor());

        // Font sizes
        holder.tvArabic.setTextSize(arabicFontSize);
        holder.tvTranslation.setTextSize(translationFontSize);
        holder.tvExtraContent.setTextSize(translationFontSize);

        // Reset text selectability on rebind (may have been enabled by "Select Text" action)
        holder.tvArabic.setTextIsSelectable(false);
        holder.tvTranslation.setTextIsSelectable(false);
        holder.tvExtraContent.setTextIsSelectable(false);

        // Long press listener
        holder.container.setOnLongClickListener(v -> {
            if (longPressListener != null) {
                longPressListener.onAyahLongPress(surah, ayah, position);
                return true;
            }
            return false;
        });

        // Load data on background thread
        executor.execute(() -> {
            Ayah ayahData = repository.getAyah(surah, ayah);
            if (ayahData == null) return;

            // Load content based on display mode
            String contentText = "";
            String tafseerText = "";
            List<WordByWord> words = null;

            switch (mode) {
                case "arabic":
                    // No extra content needed
                    break;
                case "translation":
                    String edition = repository.getSelectedTranslation();
                    if ("ur.jalandhry".equals(edition)) {
                        contentText = ayahData.defaultTranslation;
                    } else {
                        Translation trans = repository.getTranslation(surah, ayah, edition);
                        contentText = trans != null ? trans.text : ayahData.defaultTranslation;
                    }
                    break;
                case "tafseer":
                    // Load translation first
                    String transEdition = repository.getSelectedTranslation();
                    if ("ur.jalandhry".equals(transEdition)) {
                        contentText = ayahData.defaultTranslation;
                    } else {
                        Translation trans = repository.getTranslation(surah, ayah, transEdition);
                        contentText = trans != null ? trans.text : ayahData.defaultTranslation;
                    }
                    // Then load tafseer
                    String tafseerEdition = repository.getSelectedTafseer();
                    Tafseer tafseer = repository.getTafseer(surah, ayah, tafseerEdition);
                    tafseerText = tafseer != null ? tafseer.text : "Tafseer not downloaded — go to Library to download";
                    break;
                case "wbw":
                    String wbwLang = repository.getSelectedWbwLanguage();
                    words = repository.getWords(surah, ayah, wbwLang);
                    break;
            }

            String finalContentText = contentText;
            String finalTafseerText = tafseerText;
            List<WordByWord> finalWords = words;

            holder.itemView.post(() -> {
                holder.tvArabic.setText(ayahData.arabicText);
                holder.tvAyahMarker.setText("\uFD3E " + ayah + " \uFD3F");

                // Surah header
                if (ayah == 1) {
                    String headerText = surah + ". " + ayahData.surahNameEn + " (" + ayahData.surahNameAr + ")";
                    holder.tvSurahHeader.setText(headerText);
                    holder.tvSurahHeader.setVisibility(View.VISIBLE);
                } else {
                    holder.tvSurahHeader.setVisibility(View.GONE);
                }

                // Bismillah
                if (ayah == 1 && surah != 1 && surah != 9) {
                    holder.tvBismillah.setVisibility(View.VISIBLE);
                    holder.dividerTop.setVisibility(View.VISIBLE);
                } else {
                    holder.tvBismillah.setVisibility(View.GONE);
                    holder.dividerTop.setVisibility(View.GONE);
                }

                // Content area: depends on display mode
                if ("arabic".equals(mode)) {
                    // Arabic only - hide all content below
                    holder.tvTranslation.setVisibility(View.GONE);
                    holder.learningContent.setVisibility(View.GONE);
                } else if ("translation".equals(mode)) {
                    // Translation text
                    holder.tvTranslation.setVisibility(View.VISIBLE);
                    holder.tvTranslation.setText(finalContentText);
                    holder.tvTranslation.setTextColor(theme.getTranslationTextColor());
                    applyTranslationFont(holder.tvTranslation);
                    holder.learningContent.setVisibility(View.GONE);
                } else if ("tafseer".equals(mode)) {
                    // Translation first
                    holder.tvTranslation.setVisibility(View.VISIBLE);
                    holder.tvTranslation.setText(finalContentText);
                    holder.tvTranslation.setTextColor(theme.getTranslationTextColor());
                    applyTranslationFont(holder.tvTranslation);
                    // Tafseer below
                    holder.learningContent.setVisibility(View.VISIBLE);
                    holder.rvWords.setVisibility(View.GONE);
                    holder.tvExtraContent.setVisibility(View.VISIBLE);
                    holder.tvExtraContent.setText(finalTafseerText);
                    holder.tvExtraContent.setTextColor(theme.getSecondaryTextColor());
                    applyTafseerFont(holder.tvExtraContent);
                    if (holder.tvExtraLabel != null) {
                        holder.tvExtraLabel.setVisibility(View.VISIBLE);
                        holder.tvExtraLabel.setText("Tafseer");
                        holder.tvExtraLabel.setTextColor(theme.getAccentColor());
                    }
                } else if ("wbw".equals(mode)) {
                    // Word by word
                    holder.tvTranslation.setVisibility(View.GONE);
                    if (finalWords != null && !finalWords.isEmpty()) {
                        holder.learningContent.setVisibility(View.VISIBLE);
                        holder.tvExtraContent.setVisibility(View.GONE);
                        holder.rvWords.setVisibility(View.VISIBLE);
                        holder.rvWords.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 4));
                        holder.rvWords.setAdapter(new WordAdapter(finalWords, theme, arabicFont, arabicFontSize, translationFontSize));
                        if (holder.tvExtraLabel != null) {
                            holder.tvExtraLabel.setVisibility(View.GONE);
                        }
                    } else {
                        holder.learningContent.setVisibility(View.VISIBLE);
                        holder.tvExtraContent.setVisibility(View.VISIBLE);
                        holder.rvWords.setVisibility(View.GONE);
                        holder.tvExtraContent.setText("Word by Word not downloaded — go to Library to download");
                        holder.tvExtraContent.setTextColor(theme.getSecondaryTextColor());
                        if (holder.tvExtraLabel != null) holder.tvExtraLabel.setVisibility(View.GONE);
                    }
                }

                // Ruku end marker
                if (com.tanxe.quran.util.RukuData.isRukuEnd(surah, ayah)) {
                    int surahRuku = com.tanxe.quran.util.RukuData.getSurahRukuNumber(surah, ayah);
                    int juzRuku = com.tanxe.quran.util.RukuData.getJuzRukuNumber(surah, ayah);
                    String lang = repository.getLanguage();
                    String ruku = com.tanxe.quran.util.Localization.get(lang, com.tanxe.quran.util.Localization.RUKU);
                    String surahL = com.tanxe.quran.util.Localization.get(lang, com.tanxe.quran.util.Localization.SURAH);
                    String juzL = com.tanxe.quran.util.Localization.get(lang, com.tanxe.quran.util.Localization.JUZ);
                    holder.tvRukuMarker.setVisibility(View.VISIBLE);
                    holder.tvRukuMarker.setText("\u2500\u2500  \u06DC " + surahL + " " + ruku + " " + surahRuku
                            + " \u2022 " + juzL + " " + ruku + " " + juzRuku + "  \u2500\u2500");
                    holder.tvRukuMarker.setTextColor(theme.getAccentColor());
                } else {
                    holder.tvRukuMarker.setVisibility(View.GONE);
                }
            });
        });
    }

    private void applyTranslationFont(TextView tv) {
        String lang = repository.getLanguage();
        String transEdition = repository.getSelectedTranslation();
        if (transEdition.startsWith("ur.") || "ur".equals(lang) || "fa".equals(lang)) {
            tv.setTypeface(urduFont);
        } else {
            tv.setTypeface(Typeface.DEFAULT);
        }
    }

    private void applyTafseerFont(TextView tv) {
        String lang = repository.getLanguage();
        String tafseerEdition = repository.getSelectedTafseer();
        if (tafseerEdition != null && (tafseerEdition.contains(".ur.") || tafseerEdition.startsWith("ur.") || tafseerEdition.contains("urdu"))) {
            tv.setTypeface(urduFont);
        } else if ("ur".equals(lang) || "fa".equals(lang)) {
            tv.setTypeface(urduFont);
        } else {
            tv.setTypeface(Typeface.DEFAULT);
        }
    }

    @Override
    public int getItemCount() {
        return TOTAL_AYAHS;
    }

    static class AyahViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView tvSurahHeader, tvBismillah, tvArabic, tvAyahMarker, tvTranslation;
        TextView tvExtraLabel, tvExtraContent, tvRukuMarker;
        View dividerTop, dividerBottom;
        View learningContent;
        RecyclerView rvWords;

        AyahViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.ayah_page_container);
            tvSurahHeader = itemView.findViewById(R.id.tv_surah_header);
            tvBismillah = itemView.findViewById(R.id.tv_bismillah);
            tvArabic = itemView.findViewById(R.id.tv_arabic);
            tvAyahMarker = itemView.findViewById(R.id.tv_ayah_marker);
            tvTranslation = itemView.findViewById(R.id.tv_translation);
            tvExtraLabel = itemView.findViewById(R.id.tv_extra_label);
            tvExtraContent = itemView.findViewById(R.id.tv_extra_content);
            tvRukuMarker = itemView.findViewById(R.id.tv_ruku_marker);
            dividerTop = itemView.findViewById(R.id.divider_top);
            dividerBottom = itemView.findViewById(R.id.divider_bottom);
            learningContent = itemView.findViewById(R.id.learning_content);
            rvWords = itemView.findViewById(R.id.rv_words);
        }
    }
}
