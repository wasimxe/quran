package com.tanxe.quran.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.tanxe.quran.R;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;

import java.util.ArrayList;
import java.util.List;

public class JuzAdapter extends RecyclerView.Adapter<JuzAdapter.ViewHolder> {

    public interface OnJuzClick {
        void onJuzSelected(int surah, int ayah);
    }

    private final ThemeManager theme;
    private final Typeface arabicFont;
    private final OnJuzClick listener;
    private List<JuzItem> items = new ArrayList<>();

    public JuzAdapter(ThemeManager theme, Typeface arabicFont, OnJuzClick listener) {
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.listener = listener;
    }

    public void setItems(List<JuzItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_juz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JuzItem item = items.get(position);

        // Theme the card background
        if (holder.itemView instanceof MaterialCardView) {
            ((MaterialCardView) holder.itemView).setCardBackgroundColor(theme.getCardColor());
        }

        String lang = QuranRepository.getInstance(holder.itemView.getContext()).getLanguage();
        holder.tvNumber.setText(String.valueOf(item.juzNumber));
        holder.tvNumber.setTextColor(theme.getAccentColor());
        holder.tvTitle.setText(Localization.get(lang, Localization.JUZ) + " " + item.juzNumber);
        holder.tvTitle.setTextColor(theme.getPrimaryTextColor());
        holder.tvStart.setText(Localization.get(lang, Localization.STARTS) + ": " + item.startSurahName + " " + item.startSurah + ":" + item.startAyah);
        holder.tvStart.setTextColor(theme.getSecondaryTextColor());
        holder.tvSurahs.setText(item.surahRange);
        holder.tvSurahs.setTextColor(theme.getSecondaryTextColor());
        holder.tvNameAr.setText(item.arabicName);
        holder.tvNameAr.setTextColor(theme.getArabicTextColor());
        if (arabicFont != null) holder.tvNameAr.setTypeface(arabicFont);

        holder.itemView.setOnClickListener(v -> listener.onJuzSelected(item.startSurah, item.startAyah));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvTitle, tvStart, tvSurahs, tvNameAr;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_juz_number);
            tvTitle = itemView.findViewById(R.id.tv_juz_title);
            tvStart = itemView.findViewById(R.id.tv_juz_start);
            tvSurahs = itemView.findViewById(R.id.tv_juz_surahs);
            tvNameAr = itemView.findViewById(R.id.tv_juz_name_ar);
        }
    }

    public static class JuzItem {
        public int juzNumber;
        public int startSurah;
        public int startAyah;
        public String startSurahName;
        public String surahRange;
        public String arabicName;

        public JuzItem(int juzNumber, int startSurah, int startAyah, String startSurahName, String surahRange, String arabicName) {
            this.juzNumber = juzNumber;
            this.startSurah = startSurah;
            this.startAyah = startAyah;
            this.startSurahName = startSurahName;
            this.surahRange = surahRange;
            this.arabicName = arabicName;
        }
    }
}
