package com.tanxe.quran.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.WordByWord;
import com.tanxe.quran.theme.ThemeManager;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {
    private final List<WordByWord> words;
    private final ThemeManager theme;
    private final Typeface arabicFont;
    private final float arabicFontSize;
    private final float translationFontSize;

    public WordAdapter(List<WordByWord> words, ThemeManager theme, Typeface arabicFont,
                       float arabicFontSize, float translationFontSize) {
        this.words = words;
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.arabicFontSize = arabicFontSize;
        this.translationFontSize = translationFontSize;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordByWord word = words.get(position);
        holder.tvArabic.setText(word.arabicWord);
        holder.tvArabic.setTypeface(arabicFont);
        holder.tvArabic.setTextSize(arabicFontSize * 0.7f);
        holder.tvTranslation.setText(word.translation);
        holder.tvTranslation.setTextSize(translationFontSize * 0.7f);
        holder.tvPosition.setText(String.valueOf(word.wordPosition));

        holder.tvArabic.setTextColor(theme.getArabicTextColor());
        holder.tvTranslation.setTextColor(theme.getTranslationTextColor());
        holder.tvPosition.setTextColor(theme.getSecondaryTextColor());
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvArabic, tvTranslation, tvPosition;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArabic = itemView.findViewById(R.id.tv_word_arabic);
            tvTranslation = itemView.findViewById(R.id.tv_word_translation);
            tvPosition = itemView.findViewById(R.id.tv_word_position);
        }
    }
}
