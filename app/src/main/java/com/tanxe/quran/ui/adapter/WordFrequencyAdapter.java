package com.tanxe.quran.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.dao.WordByWordDao;
import com.tanxe.quran.theme.ThemeManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordFrequencyAdapter extends RecyclerView.Adapter<WordFrequencyAdapter.ViewHolder> {

    public interface OnWordAction {
        void onToggleKnown(WordByWordDao.WordFrequency word, boolean markKnown);
    }

    private List<WordByWordDao.WordFrequency> allWords;
    private List<WordByWordDao.WordFrequency> filteredWords;
    private final ThemeManager theme;
    private final Typeface arabicFont;
    private final OnWordAction listener;
    private final Set<String> knownWords;
    private String filter = "all"; // all, known, unknown

    public WordFrequencyAdapter(List<WordByWordDao.WordFrequency> words, Set<String> knownWords,
                                ThemeManager theme, Typeface arabicFont, OnWordAction listener) {
        this.allWords = words;
        this.filteredWords = new ArrayList<>(words);
        this.knownWords = knownWords != null ? knownWords : new HashSet<>();
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.listener = listener;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        applyFilter();
    }

    private void applyFilter() {
        filteredWords.clear();
        for (WordByWordDao.WordFrequency w : allWords) {
            boolean isKnown = knownWords.contains(w.arabicWord);
            if ("all".equals(filter) ||
                ("known".equals(filter) && isKnown) ||
                ("unknown".equals(filter) && !isKnown)) {
                filteredWords.add(w);
            }
        }
        notifyDataSetChanged();
    }

    public void toggleKnown(String word, boolean known) {
        if (known) {
            knownWords.add(word);
        } else {
            knownWords.remove(word);
        }
        applyFilter();
    }

    public int getTotalFrequency() {
        int total = 0;
        for (WordByWordDao.WordFrequency w : allWords) total += w.frequency;
        return total;
    }

    public int getKnownFrequency() {
        int total = 0;
        for (WordByWordDao.WordFrequency w : allWords) {
            if (knownWords.contains(w.arabicWord)) total += w.frequency;
        }
        return total;
    }

    public int getKnownCount() {
        return knownWords.size();
    }

    public int getTotalCount() {
        return allWords.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_frequency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordByWordDao.WordFrequency word = filteredWords.get(position);
        boolean isKnown = knownWords.contains(word.arabicWord);

        holder.tvArabic.setText(word.arabicWord);
        if (arabicFont != null) holder.tvArabic.setTypeface(arabicFont);
        holder.tvArabic.setTextColor(theme.getArabicTextColor());

        holder.tvFrequency.setText("×" + word.frequency);
        holder.tvFrequency.setTextColor(theme.getAccentColor());

        // We don't have translation in WordFrequency, so show frequency info
        holder.tvTranslation.setText(isKnown ? "✓ Known" : "");
        holder.tvTranslation.setTextColor(isKnown ? theme.getDownloadedColor() : theme.getSecondaryTextColor());

        // Toggle button: X means "mark as known" (remove from unknown)
        holder.btnToggle.setImageResource(isKnown ? R.drawable.ic_check_circle : R.drawable.ic_bookmark);
        holder.btnToggle.setColorFilter(isKnown ? theme.getDownloadedColor() : theme.getSecondaryTextColor());

        holder.btnToggle.setOnClickListener(v -> {
            boolean newState = !isKnown;
            listener.onToggleKnown(word, newState);
            toggleKnown(word.arabicWord, newState);
        });

        holder.container.setBackgroundColor(position % 2 == 0 ?
            theme.getBackgroundColor() : theme.getCardColor());
    }

    @Override
    public int getItemCount() {
        return filteredWords.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View container;
        ImageButton btnToggle;
        TextView tvArabic, tvFrequency, tvTranslation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.word_freq_container);
            btnToggle = itemView.findViewById(R.id.btn_word_toggle);
            tvArabic = itemView.findViewById(R.id.tv_wf_arabic);
            tvFrequency = itemView.findViewById(R.id.tv_wf_frequency);
            tvTranslation = itemView.findViewById(R.id.tv_wf_translation);
        }
    }
}
