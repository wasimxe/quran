package com.tanxe.quran.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.theme.ThemeManager;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<Ayah> results;
    private final ThemeManager theme;
    private final OnResultClick listener;

    public interface OnResultClick {
        void onClick(Ayah ayah);
    }

    public SearchAdapter(List<Ayah> results, ThemeManager theme, OnResultClick listener) {
        this.results = results;
        this.theme = theme;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ayah ayah = results.get(position);
        holder.tvRef.setText(ayah.surahNameEn + " " + ayah.surahNumber + ":" + ayah.ayahNumber);
        holder.tvArabic.setText(ayah.arabicText);
        holder.tvTranslation.setText(ayah.defaultTranslation);

        holder.tvRef.setTextColor(theme.getAccentColor());
        holder.tvArabic.setTextColor(theme.getArabicTextColor());
        holder.tvTranslation.setTextColor(theme.getTranslationTextColor());

        if (holder.itemView instanceof com.google.android.material.card.MaterialCardView) {
            com.google.android.material.card.MaterialCardView card =
                    (com.google.android.material.card.MaterialCardView) holder.itemView;
            card.setCardBackgroundColor(theme.getCardColor());
            card.setStrokeColor(theme.getDividerColor());
            card.setStrokeWidth(1);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(ayah));
    }

    /** Update results without recreating the adapter */
    public void updateResults(List<Ayah> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRef, tvArabic, tvTranslation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRef = itemView.findViewById(R.id.tv_search_ref);
            tvArabic = itemView.findViewById(R.id.tv_search_arabic);
            tvTranslation = itemView.findViewById(R.id.tv_search_translation);
        }
    }
}
