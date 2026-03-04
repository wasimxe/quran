package com.tanxe.quran.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.theme.ThemeManager;

import java.util.List;

public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.ViewHolder> {

    public static class DownloadItem {
        public String edition;
        public String displayName;
        public String type; // "translation", "tafseer", "wbw"
        public String language;
        public boolean isBuiltIn;
        public String status; // "none", "downloading", "paused", "downloaded"
        public int progress; // 0-100

        public DownloadItem(String edition, String displayName, String type, String language, boolean isBuiltIn) {
            this.edition = edition;
            this.displayName = displayName;
            this.type = type;
            this.language = language;
            this.isBuiltIn = isBuiltIn;
            this.status = isBuiltIn ? "downloaded" : "none";
            this.progress = isBuiltIn ? 100 : 0;
        }
    }

    public interface OnDownloadAction {
        void onDownload(DownloadItem item, int position);
        void onDelete(DownloadItem item, int position);
    }

    private final List<DownloadItem> items;
    private final ThemeManager theme;
    private final OnDownloadAction listener;

    public DownloadItemAdapter(List<DownloadItem> items, ThemeManager theme, OnDownloadAction listener) {
        this.items = items;
        this.theme = theme;
        this.listener = listener;
    }

    public void updateItemStatus(int position, String status, int progress) {
        if (position >= 0 && position < items.size()) {
            items.get(position).status = status;
            items.get(position).progress = progress;
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadItem item = items.get(position);

        holder.tvName.setText(item.displayName);
        holder.tvName.setTextColor(theme.getPrimaryTextColor());

        holder.tvEdition.setText(item.edition);
        holder.tvEdition.setTextColor(theme.getSecondaryTextColor());

        holder.container.setBackgroundColor(theme.getCardColor());

        switch (item.status) {
            case "downloaded":
                holder.tvStatus.setText(item.isBuiltIn ? "Built-in" : "✓");
                holder.tvStatus.setTextColor(theme.getDownloadedColor());
                holder.progressBar.setVisibility(View.GONE);
                holder.btnAction.setImageResource(item.isBuiltIn ?
                    R.drawable.ic_check_circle : R.drawable.ic_delete);
                holder.btnAction.setColorFilter(item.isBuiltIn ?
                    theme.getDownloadedColor() : theme.getErrorColor());
                holder.btnAction.setEnabled(!item.isBuiltIn);
                holder.btnAction.setAlpha(item.isBuiltIn ? 0.5f : 1.0f);
                break;

            case "downloading":
                holder.tvStatus.setText(item.progress + "%");
                holder.tvStatus.setTextColor(theme.getAccentColor());
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setProgress(item.progress);
                holder.btnAction.setImageResource(R.drawable.ic_pause);
                holder.btnAction.setColorFilter(theme.getAccentColor());
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1.0f);
                break;

            case "paused":
                holder.tvStatus.setText("Paused");
                holder.tvStatus.setTextColor(theme.getSecondaryTextColor());
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setProgress(item.progress);
                holder.btnAction.setImageResource(R.drawable.ic_download);
                holder.btnAction.setColorFilter(theme.getAccentColor());
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1.0f);
                break;

            default: // "none"
                holder.tvStatus.setText("");
                holder.tvStatus.setTextColor(theme.getSecondaryTextColor());
                holder.progressBar.setVisibility(View.GONE);
                holder.btnAction.setImageResource(R.drawable.ic_download);
                holder.btnAction.setColorFilter(theme.getAccentColor());
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1.0f);
                break;
        }

        holder.btnAction.setOnClickListener(v -> {
            if ("downloaded".equals(item.status) && !item.isBuiltIn) {
                listener.onDelete(item, holder.getAdapterPosition());
            } else if (!"downloaded".equals(item.status)) {
                listener.onDownload(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView tvName, tvEdition, tvStatus;
        ProgressBar progressBar;
        ImageButton btnAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.download_item_container);
            tvName = itemView.findViewById(R.id.tv_download_name);
            tvEdition = itemView.findViewById(R.id.tv_download_edition);
            tvStatus = itemView.findViewById(R.id.tv_download_status);
            progressBar = itemView.findViewById(R.id.progress_download);
            btnAction = itemView.findViewById(R.id.btn_download_action);
        }
    }
}
