package com.tanxe.quran.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.dao.AyahDao;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;
import com.tanxe.quran.util.QuranDataParser;

import java.util.ArrayList;
import java.util.List;

public class SurahListAdapter extends RecyclerView.Adapter<SurahListAdapter.ViewHolder> {
    private List<AyahDao.SurahInfo> allSurahs;
    private List<AyahDao.SurahInfo> filteredSurahs;
    private final ThemeManager theme;
    private final OnSurahClick listener;
    private final String lang;

    public interface OnSurahClick {
        void onClick(int surahNumber);
    }

    public SurahListAdapter(List<AyahDao.SurahInfo> surahs, ThemeManager theme, OnSurahClick listener) {
        this(surahs, theme, listener, "en");
    }

    public SurahListAdapter(List<AyahDao.SurahInfo> surahs, ThemeManager theme, OnSurahClick listener, String lang) {
        this.allSurahs = surahs;
        this.filteredSurahs = new ArrayList<>(surahs);
        this.theme = theme;
        this.listener = listener;
        this.lang = lang;
    }

    public void filter(String query) {
        filteredSurahs.clear();
        if (query == null || query.isEmpty()) {
            filteredSurahs.addAll(allSurahs);
        } else {
            String lower = query.toLowerCase();
            for (AyahDao.SurahInfo s : allSurahs) {
                if (s.surahNameEn.toLowerCase().contains(lower)
                    || s.surahNameAr.contains(query)
                    || String.valueOf(s.surahNumber).equals(query)) {
                    filteredSurahs.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AyahDao.SurahInfo surah = filteredSurahs.get(position);

        holder.tvNumber.setText(String.valueOf(surah.surahNumber));
        holder.tvNumber.setTextColor(theme.getAccentColor());

        holder.tvNameEn.setText(surah.surahNameEn);
        holder.tvNameEn.setTextColor(theme.getPrimaryTextColor());

        int ayahCount = surah.surahNumber <= QuranDataParser.SURAH_AYAH_COUNT.length
            ? QuranDataParser.SURAH_AYAH_COUNT[surah.surahNumber - 1] : 0;
        boolean medinan = surah.surahNumber <= 114 && QuranDataParser.IS_MEDINAN[surah.surahNumber];
        String makkiMadani = Localization.get(lang, medinan ? Localization.MEDINAN : Localization.MECCAN);
        holder.tvAyahCount.setText(ayahCount + " " + Localization.get(lang, Localization.AYAHS) + " \u2022 " + makkiMadani);
        holder.tvAyahCount.setTextColor(theme.getSecondaryTextColor());

        holder.tvNameAr.setText(surah.surahNameAr);
        holder.tvNameAr.setTextColor(theme.getArabicTextColor());

        holder.itemView.setOnClickListener(v -> listener.onClick(surah.surahNumber));
    }

    @Override
    public int getItemCount() {
        return filteredSurahs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvNameEn, tvAyahCount, tvNameAr;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_surah_number);
            tvNameEn = itemView.findViewById(R.id.tv_surah_name_en);
            tvAyahCount = itemView.findViewById(R.id.tv_surah_ayah_count);
            tvNameAr = itemView.findViewById(R.id.tv_surah_name_ar);
        }
    }
}
