package com.tanxe.quran.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.tanxe.quran.R;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {
    private final String[][] languages; // [code, nativeName, englishName]
    private final OnLanguageSelected listener;
    private int selectedPosition = 0;

    public interface OnLanguageSelected {
        void onSelected(String code, int position);
    }

    public LanguageAdapter(String[][] languages, OnLanguageSelected listener) {
        this.languages = languages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] lang = languages[position];
        holder.tvNative.setText(lang[1]);
        holder.tvEnglish.setText(lang[2]);

        MaterialCardView card = (MaterialCardView) holder.itemView;
        card.setChecked(position == selectedPosition);
        card.setStrokeWidth(position == selectedPosition ? 4 : 1);

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onSelected(lang[0], selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return languages.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNative, tvEnglish;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNative = itemView.findViewById(R.id.tv_lang_native);
            tvEnglish = itemView.findViewById(R.id.tv_lang_english);
        }
    }
}
