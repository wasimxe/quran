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
        void onToggleKnown(WordByWordDao.WordWithTranslation word, boolean markKnown);
    }

    public interface OnWordClick {
        void onWordSelected(WordByWordDao.WordWithTranslation word, int index);
    }

    private List<WordByWordDao.WordWithTranslation> allWords;
    private List<WordByWordDao.WordWithTranslation> filteredWords;
    private final ThemeManager theme;
    private final Typeface arabicFont;
    private final OnWordAction actionListener;
    private OnWordClick clickListener;
    private final Set<String> knownWords;
    private String filter = "all"; // all, known, unknown
    private int selectedIndex = -1;

    // Lazy loading: only show PAGE_SIZE items at a time, load more on scroll
    private static final int PAGE_SIZE = 200;
    private int visibleCount = PAGE_SIZE;
    private boolean allLoaded = false;

    public WordFrequencyAdapter(List<WordByWordDao.WordWithTranslation> words, Set<String> knownWords,
                                ThemeManager theme, Typeface arabicFont, OnWordAction listener) {
        this.allWords = words;
        this.filteredWords = new ArrayList<>(words);
        this.knownWords = knownWords != null ? knownWords : new HashSet<>();
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.actionListener = listener;
    }

    public void setClickListener(OnWordClick listener) {
        this.clickListener = listener;
    }

    public void setSelectedIndex(int index) {
        int oldSelected = selectedIndex;
        selectedIndex = index;
        if (oldSelected >= 0 && oldSelected < filteredWords.size()) notifyItemChanged(oldSelected);
        if (index >= 0 && index < filteredWords.size()) notifyItemChanged(index);
    }

    public void setFilter(String filter) {
        this.filter = filter;
        applyFilter();
    }

    private void applyFilter() {
        filteredWords.clear();
        for (WordByWordDao.WordWithTranslation w : allWords) {
            boolean isKnown = knownWords.contains(w.arabicWord);
            if ("all".equals(filter) ||
                ("known".equals(filter) && isKnown) ||
                ("unknown".equals(filter) && !isKnown)) {
                filteredWords.add(w);
            }
        }
        // Reset lazy loading on filter change
        visibleCount = PAGE_SIZE;
        allLoaded = filteredWords.size() <= PAGE_SIZE;
        notifyDataSetChanged();
    }

    public void updateWords(List<WordByWordDao.WordWithTranslation> words) {
        this.allWords = words;
        applyFilter();
    }

    public void toggleKnown(String word, boolean known) {
        if (known) knownWords.add(word); else knownWords.remove(word);
        applyFilter();
    }

    public int getTotalFrequency() {
        int total = 0;
        for (WordByWordDao.WordWithTranslation w : allWords) total += w.frequency;
        return total;
    }

    public int getKnownFrequency() {
        int total = 0;
        for (WordByWordDao.WordWithTranslation w : allWords) {
            if (knownWords.contains(w.arabicWord)) total += w.frequency;
        }
        return total;
    }

    public int getKnownCount() { return knownWords.size(); }
    public int getTotalCount() { return allWords.size(); }
    public WordByWordDao.WordWithTranslation getWordAt(int filteredIndex) {
        if (filteredIndex >= 0 && filteredIndex < filteredWords.size()) return filteredWords.get(filteredIndex);
        return null;
    }
    public int getFilteredCount() { return filteredWords.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_frequency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordByWordDao.WordWithTranslation word = filteredWords.get(position);
        boolean isKnown = knownWords.contains(word.arabicWord);

        holder.tvArabic.setText(word.arabicWord);
        if (arabicFont != null) holder.tvArabic.setTypeface(arabicFont);
        holder.tvArabic.setTextColor(theme.getArabicTextColor());

        holder.tvFrequency.setText("×" + word.frequency);
        holder.tvFrequency.setTextColor(theme.getAccentColor());

        // Show translation
        holder.tvTranslation.setText(word.translation != null ? word.translation : "");
        holder.tvTranslation.setTextColor(isKnown ? theme.getDownloadedColor() : theme.getSecondaryTextColor());

        // Toggle button
        holder.btnToggle.setImageResource(isKnown ? R.drawable.ic_check_circle : R.drawable.ic_bookmark);
        holder.btnToggle.setColorFilter(isKnown ? theme.getDownloadedColor() : theme.getSecondaryTextColor());

        holder.btnToggle.setOnClickListener(v -> {
            boolean newState = !isKnown;
            if (actionListener != null) actionListener.onToggleKnown(word, newState);
            toggleKnown(word.arabicWord, newState);
        });

        // Highlight selected
        boolean isSelected = position == selectedIndex;
        holder.container.setBackgroundColor(isSelected ? theme.getAccentColor() & 0x33FFFFFF :
            (position % 2 == 0 ? theme.getBackgroundColor() : theme.getCardColor()));

        // Click to select word and show detail
        holder.container.setOnClickListener(v -> {
            setSelectedIndex(position);
            if (clickListener != null) clickListener.onWordSelected(word, position);
        });
    }

    /** Load next page of items. Returns true if more items were loaded. */
    public boolean loadMore() {
        if (allLoaded) return false;
        int oldCount = visibleCount;
        visibleCount = Math.min(visibleCount + PAGE_SIZE, filteredWords.size());
        allLoaded = visibleCount >= filteredWords.size();
        if (visibleCount > oldCount) {
            notifyItemRangeInserted(oldCount, visibleCount - oldCount);
            return true;
        }
        return false;
    }

    public boolean isAllLoaded() { return allLoaded; }

    @Override
    public int getItemCount() {
        return Math.min(visibleCount, filteredWords.size());
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
