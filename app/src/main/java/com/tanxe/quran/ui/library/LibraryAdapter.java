package com.tanxe.quran.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.EditionInfo;
import com.tanxe.quran.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    public interface OnEditionAction {
        void onDownload(EditionInfo edition, int position);
        void onDelete(EditionInfo edition, int position);
        void onCancelDownload(EditionInfo edition, int position);
        void onSelect(EditionInfo edition, int position);
    }

    private List<EditionInfo> allEditions = new ArrayList<>();
    private List<EditionInfo> filteredEditions = new ArrayList<>();
    private final ThemeManager theme;
    private final OnEditionAction listener;
    private String activeIdentifier = "";

    public LibraryAdapter(ThemeManager theme, OnEditionAction listener) {
        this.theme = theme;
        this.listener = listener;
    }

    public void setActiveIdentifier(String identifier) {
        this.activeIdentifier = identifier != null ? identifier : "";
        notifyDataSetChanged();
    }

    public void setEditions(List<EditionInfo> editions) {
        this.allEditions = editions != null ? editions : new ArrayList<>();
        this.filteredEditions = new ArrayList<>(this.allEditions);
        notifyDataSetChanged();
    }

    public void filterByLanguage(String language) {
        if (language == null || language.isEmpty() || "all".equals(language)) {
            filteredEditions = new ArrayList<>(allEditions);
        } else {
            filteredEditions = new ArrayList<>();
            for (EditionInfo e : allEditions) {
                if (language.equals(e.language)) {
                    filteredEditions.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByQuery(String query) {
        if (query == null || query.isEmpty()) {
            filteredEditions = new ArrayList<>(allEditions);
        } else {
            String lower = query.toLowerCase();
            filteredEditions = new ArrayList<>();
            for (EditionInfo e : allEditions) {
                if ((e.name != null && e.name.toLowerCase().contains(lower)) ||
                        (e.language != null && e.language.toLowerCase().contains(lower)) ||
                        (e.languageName != null && e.languageName.toLowerCase().contains(lower)) ||
                        (e.identifier != null && e.identifier.toLowerCase().contains(lower))) {
                    filteredEditions.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateItem(int position) {
        if (position >= 0 && position < filteredEditions.size()) {
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_edition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EditionInfo edition = filteredEditions.get(position);

        holder.tvName.setText(edition.name != null ? edition.name : edition.identifier);
        holder.tvName.setTextColor(theme.getPrimaryTextColor());

        holder.tvLanguage.setText(edition.language != null ? edition.language.toUpperCase() : "");
        holder.tvLanguage.setTextColor(theme.getAccentColor());

        String typeLabel = edition.type != null ? edition.type.substring(0, 1).toUpperCase() + edition.type.substring(1) : "";
        holder.tvType.setText(typeLabel);
        holder.tvType.setTextColor(theme.getSecondaryTextColor());

        holder.container.setBackgroundColor(theme.getBackgroundColor());

        // Show tick for active edition
        boolean isActive = edition.isDownloaded && edition.identifier != null
                && edition.identifier.equals(activeIdentifier);
        holder.ivActiveTick.setVisibility(isActive ? View.VISIBLE : View.GONE);
        holder.ivActiveTick.setColorFilter(theme.getDownloadedColor());

        // Highlight active item name
        if (isActive) {
            holder.tvName.setTextColor(theme.getDownloadedColor());
        } else {
            holder.tvName.setTextColor(theme.getPrimaryTextColor());
        }

        // Click on row to select (only for downloaded editions)
        holder.itemView.setOnClickListener(v -> {
            if (edition.isDownloaded) {
                listener.onSelect(edition, holder.getAdapterPosition());
            }
        });

        if (edition.isDownloaded) {
            holder.btnAction.setImageResource(R.drawable.ic_delete);
            holder.btnAction.setColorFilter(theme.getErrorColor());
            holder.progressBar.setVisibility(View.GONE);
            holder.btnAction.setOnClickListener(v -> listener.onDelete(edition, holder.getAdapterPosition()));
        } else if (edition.downloadProgress > 0 && edition.downloadProgress < 100) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(edition.downloadProgress);
            holder.btnAction.setImageResource(R.drawable.ic_delete);
            holder.btnAction.setColorFilter(theme.getErrorColor());
            holder.btnAction.setOnClickListener(v -> listener.onCancelDownload(edition, holder.getAdapterPosition()));
        } else {
            holder.btnAction.setImageResource(R.drawable.ic_download);
            holder.btnAction.setColorFilter(theme.getAccentColor());
            holder.progressBar.setVisibility(View.GONE);
            holder.btnAction.setOnClickListener(v -> listener.onDownload(edition, holder.getAdapterPosition()));
        }
    }

    @Override
    public int getItemCount() {
        return filteredEditions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView tvName, tvLanguage, tvType;
        ImageView ivActiveTick;
        ImageButton btnAction;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.edition_item_container);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLanguage = itemView.findViewById(R.id.tv_language);
            tvType = itemView.findViewById(R.id.tv_type);
            ivActiveTick = itemView.findViewById(R.id.iv_active_tick);
            btnAction = itemView.findViewById(R.id.btn_action);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
