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

    private boolean isLearningMode = false;
    private String displayMode = "translation"; // translation, tafseer, wbw

    // Flat index mapping: position -> (surah, ayah)
    private static final int TOTAL_AYAHS = QuranDataParser.TOTAL_AYAHS;

    public AyahPagerAdapter(QuranRepository repository, ThemeManager theme,
                            Typeface arabicFont, Typeface urduFont) {
        this.repository = repository;
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.urduFont = urduFont;
        this.executor = repository.getExecutor();
    }

    public void setLearningMode(boolean learning) {
        if (this.isLearningMode != learning) {
            this.isLearningMode = learning;
            notifyDataSetChanged();
        }
    }

    public boolean isLearningMode() {
        return isLearningMode;
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

    /** Convert flat position (0-6235) to [surah, ayah] */
    public static int[] positionToSurahAyah(int position) {
        int remaining = position;
        for (int s = 0; s < QuranDataParser.SURAH_AYAH_COUNT.length; s++) {
            if (remaining < QuranDataParser.SURAH_AYAH_COUNT[s]) {
                return new int[]{s + 1, remaining + 1};
            }
            remaining -= QuranDataParser.SURAH_AYAH_COUNT[s];
        }
        return new int[]{114, 6}; // fallback: last ayah
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

        // Apply theme to container
        holder.container.setBackgroundColor(theme.getBackgroundColor());
        holder.tvArabic.setTextColor(theme.getArabicTextColor());
        holder.tvArabic.setTypeface(arabicFont);
        holder.tvAyahMarker.setTextColor(theme.getSecondaryTextColor());
        holder.tvBismillah.setTextColor(theme.getAccentColor());
        if (arabicFont != null) holder.tvBismillah.setTypeface(arabicFont);
        holder.dividerBottom.setBackgroundColor(theme.getDividerColor());

        // Font sizes from prefs
        float arabicSize = repository.getArabicFontSize();
        float transSize = repository.getTranslationFontSize();
        holder.tvArabic.setTextSize(arabicSize);
        holder.tvTranslation.setTextSize(transSize);

        // Load data on background thread
        executor.execute(() -> {
            Ayah ayahData = repository.getAyah(surah, ayah);
            if (ayahData == null) return;

            String secondaryText = "";
            List<WordByWord> words = null;

            if (isLearningMode) {
                switch (displayMode) {
                    case "translation":
                        String edition = repository.getSelectedTranslation();
                        if ("ur.jalandhry".equals(edition)) {
                            secondaryText = ayahData.defaultTranslation;
                        } else {
                            Translation trans = repository.getTranslation(surah, ayah, edition);
                            secondaryText = trans != null ? trans.text : ayahData.defaultTranslation;
                        }
                        break;
                    case "tafseer":
                        String tafseerEdition = repository.getSelectedTafseer();
                        Tafseer tafseer = repository.getTafseer(surah, ayah, tafseerEdition);
                        secondaryText = tafseer != null ? tafseer.text : "Tafseer not downloaded";
                        break;
                    case "wbw":
                        String wbwLang = repository.getSelectedWbwLanguage();
                        words = repository.getWords(surah, ayah, wbwLang);
                        if (words == null || words.isEmpty()) {
                            secondaryText = "Word by word data not downloaded";
                        }
                        break;
                }
            }

            String finalText = secondaryText;
            List<WordByWord> finalWords = words;

            holder.itemView.post(() -> {
                holder.tvArabic.setText(ayahData.arabicText);
                holder.tvAyahMarker.setText("﴿ " + ayah + " ﴾");

                // Bismillah: show for ayah 1, except surah 1 (Fatiha) and surah 9 (Tawbah)
                if (ayah == 1 && surah != 1 && surah != 9) {
                    holder.tvBismillah.setVisibility(View.VISIBLE);
                    holder.dividerTop.setVisibility(View.VISIBLE);
                } else {
                    holder.tvBismillah.setVisibility(View.GONE);
                    holder.dividerTop.setVisibility(View.GONE);
                }

                // Learning mode content
                if (isLearningMode) {
                    holder.learningContent.setVisibility(View.VISIBLE);

                    if ("wbw".equals(displayMode) && finalWords != null && !finalWords.isEmpty()) {
                        holder.tvTranslation.setVisibility(View.GONE);
                        holder.rvWords.setVisibility(View.VISIBLE);
                        holder.rvWords.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 4));
                        holder.rvWords.setAdapter(new WordAdapter(finalWords, theme, arabicFont));
                    } else {
                        holder.tvTranslation.setVisibility(View.VISIBLE);
                        holder.rvWords.setVisibility(View.GONE);
                        holder.tvTranslation.setText(finalText);
                        holder.tvTranslation.setTextColor(theme.getTranslationTextColor());

                        String lang = repository.getLanguage();
                        if ("ur".equals(lang) || "fa".equals(lang)) {
                            holder.tvTranslation.setTypeface(urduFont);
                        }
                    }
                } else {
                    holder.learningContent.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return TOTAL_AYAHS;
    }

    static class AyahViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView tvBismillah, tvArabic, tvAyahMarker, tvTranslation;
        View dividerTop, dividerBottom;
        View learningContent;
        RecyclerView rvWords;

        AyahViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.ayah_page_container);
            tvBismillah = itemView.findViewById(R.id.tv_bismillah);
            tvArabic = itemView.findViewById(R.id.tv_arabic);
            tvAyahMarker = itemView.findViewById(R.id.tv_ayah_marker);
            tvTranslation = itemView.findViewById(R.id.tv_translation);
            dividerTop = itemView.findViewById(R.id.divider_top);
            dividerBottom = itemView.findViewById(R.id.divider_bottom);
            learningContent = itemView.findViewById(R.id.learning_content);
            rvWords = itemView.findViewById(R.id.rv_words);
        }
    }
}
