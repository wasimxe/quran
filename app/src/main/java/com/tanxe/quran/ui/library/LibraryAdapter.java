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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<String> activeIdentifiers = new HashSet<>();
    private String currentLanguageFilter = "all";
    private String currentQuery = "";

    public LibraryAdapter(ThemeManager theme, OnEditionAction listener) {
        this.theme = theme;
        this.listener = listener;
    }

    public void setActiveIdentifier(String identifier) {
        this.activeIdentifier = identifier != null ? identifier : "";
        notifyDataSetChanged();
    }

    public void setActiveIdentifiers(Set<String> identifiers) {
        this.activeIdentifiers = identifiers != null ? identifiers : new HashSet<>();
        notifyDataSetChanged();
    }

    public void setEditions(List<EditionInfo> editions) {
        // Preserve download progress from currently in-progress editions
        java.util.Map<String, int[]> inProgress = new java.util.HashMap<>();
        for (EditionInfo old : allEditions) {
            if (old.downloadProgress > 0 && old.downloadProgress < 100) {
                inProgress.put(old.identifier, new int[]{old.downloadProgress});
            }
        }
        this.allEditions = editions != null ? editions : new ArrayList<>();
        // Restore in-progress state
        for (EditionInfo e : allEditions) {
            int[] prog = inProgress.get(e.identifier);
            if (prog != null) {
                e.downloadProgress = prog[0];
            }
        }
        applyFilters();
    }

    /** Find edition by identifier in the current list (for updating download progress) */
    public EditionInfo findByIdentifier(String identifier) {
        for (EditionInfo e : allEditions) {
            if (e.identifier != null && e.identifier.equals(identifier)) {
                return e;
            }
        }
        return null;
    }

    /** Update an edition by identifier and refresh its row */
    public void updateByIdentifier(String identifier) {
        for (int i = 0; i < filteredEditions.size(); i++) {
            if (filteredEditions.get(i).identifier != null &&
                    filteredEditions.get(i).identifier.equals(identifier)) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void filterByLanguage(String language) {
        this.currentLanguageFilter = (language == null || language.isEmpty()) ? "all" : language;
        applyFilters();
    }

    public void filterByQuery(String query) {
        this.currentQuery = (query == null) ? "" : query;
        applyFilters();
    }

    private void applyFilters() {
        filteredEditions = new ArrayList<>();
        String lower = currentQuery.toLowerCase();
        for (EditionInfo e : allEditions) {
            // Language filter
            if (!"all".equals(currentLanguageFilter) && !currentLanguageFilter.equals(e.language)) {
                continue;
            }
            // Query filter
            if (!currentQuery.isEmpty()) {
                if (!((e.name != null && e.name.toLowerCase().contains(lower)) ||
                        (e.language != null && e.language.toLowerCase().contains(lower)) ||
                        (e.languageName != null && e.languageName.toLowerCase().contains(lower)) ||
                        (e.identifier != null && e.identifier.toLowerCase().contains(lower)))) {
                    continue;
                }
            }
            filteredEditions.add(e);
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

        // Size display
        if (edition.sizeText != null && !edition.sizeText.isEmpty()) {
            holder.tvSize.setVisibility(View.VISIBLE);
            holder.tvSize.setText(edition.sizeText);
            holder.tvSize.setTextColor(theme.getSecondaryTextColor());
        } else {
            holder.tvSize.setVisibility(View.GONE);
        }

        holder.container.setBackgroundColor(theme.getBackgroundColor());

        // Show tick for active/selected edition (multi-select or single)
        boolean isActive = edition.isDownloaded && edition.identifier != null
                && (edition.identifier.equals(activeIdentifier) || activeIdentifiers.contains(edition.identifier));
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
        TextView tvName, tvLanguage, tvType, tvSize;
        ImageView ivActiveTick;
        ImageButton btnAction;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.edition_item_container);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLanguage = itemView.findViewById(R.id.tv_language);
            tvType = itemView.findViewById(R.id.tv_type);
            tvSize = itemView.findViewById(R.id.tv_size);
            ivActiveTick = itemView.findViewById(R.id.iv_active_tick);
            btnAction = itemView.findViewById(R.id.btn_action);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
