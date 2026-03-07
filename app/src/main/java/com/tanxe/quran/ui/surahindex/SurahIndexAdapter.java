package com.tanxe.quran.ui.surahindex;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SurahIndexAdapter extends RecyclerView.Adapter<SurahIndexAdapter.ViewHolder> {

    public interface OnSurahClick {
        void onSurahSelected(int surahNumber);
    }

    private List<JSONObject> allSurahs = new ArrayList<>();
    private List<JSONObject> filteredSurahs = new ArrayList<>();
    private final ThemeManager theme;
    private final Typeface arabicFont;
    private final OnSurahClick listener;

    public SurahIndexAdapter(ThemeManager theme, Typeface arabicFont, OnSurahClick listener) {
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.listener = listener;
    }

    public void setSurahs(List<JSONObject> surahs) {
        this.allSurahs = surahs;
        this.filteredSurahs = new ArrayList<>(surahs);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            filteredSurahs = new ArrayList<>(allSurahs);
        } else {
            String lower = query.toLowerCase();
            filteredSurahs = new ArrayList<>();
            for (JSONObject s : allSurahs) {
                try {
                    String nameEn = s.optString("nameEn", "").toLowerCase();
                    String nameAr = s.optString("nameAr", "");
                    String meaning = s.optString("meaningEn", "").toLowerCase();
                    int number = s.optInt("number", 0);
                    if (nameEn.contains(lower) || nameAr.contains(lower) ||
                            meaning.contains(lower) || String.valueOf(number).contains(lower)) {
                        filteredSurahs.add(s);
                    }
                } catch (Exception ignored) {}
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surah_index, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject surah = filteredSurahs.get(position);
            int number = surah.optInt("number", 0);
            String nameEn = surah.optString("nameEn", "");
            String nameAr = surah.optString("nameAr", "");
            String meaning = surah.optString("meaningEn", "");
            String revelation = surah.optString("revelationType", "Meccan");
            int ayahCount = surah.optInt("ayahCount", 0);

            holder.tvNumber.setText(String.valueOf(number));
            holder.tvNumber.setTextColor(theme.getAccentColor());
            // Themed number circle
            GradientDrawable numBg = new GradientDrawable();
            numBg.setShape(GradientDrawable.OVAL);
            numBg.setStroke(4, theme.getAccentColor());
            holder.tvNumber.setBackground(numBg);
            holder.tvNameEn.setText(nameEn);
            holder.tvNameEn.setTextColor(theme.getPrimaryTextColor());
            holder.tvMeaning.setText(meaning);
            holder.tvMeaning.setTextColor(theme.getSecondaryTextColor());
            holder.tvNameAr.setText(nameAr);
            holder.tvNameAr.setTextColor(theme.getArabicTextColor());
            if (arabicFont != null) holder.tvNameAr.setTypeface(arabicFont);

            boolean isMeccan = "Meccan".equals(revelation);
            holder.ivRevelation.setImageResource(isMeccan ? R.drawable.ic_meccan : R.drawable.ic_medinan);
            String lang = QuranRepository.getInstance(holder.itemView.getContext()).getLanguage();
            holder.tvRevelation.setText(Localization.get(lang, isMeccan ? Localization.MECCAN : Localization.MEDINAN));
            holder.tvRevelation.setTextColor(theme.getSecondaryTextColor());
            holder.tvAyahCount.setText(ayahCount + " " + Localization.get(lang, Localization.AYAHS));
            holder.tvAyahCount.setTextColor(theme.getSecondaryTextColor());

            holder.itemView.setOnClickListener(v -> listener.onSurahSelected(number));
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() {
        return filteredSurahs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvNameEn, tvMeaning, tvNameAr, tvRevelation, tvAyahCount;
        ImageView ivRevelation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_number);
            tvNameEn = itemView.findViewById(R.id.tv_name_en);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            tvNameAr = itemView.findViewById(R.id.tv_name_ar);
            tvRevelation = itemView.findViewById(R.id.tv_revelation);
            tvAyahCount = itemView.findViewById(R.id.tv_ayah_count);
            ivRevelation = itemView.findViewById(R.id.iv_revelation);
        }
    }
}
