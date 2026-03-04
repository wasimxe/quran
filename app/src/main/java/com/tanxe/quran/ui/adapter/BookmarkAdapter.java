package com.tanxe.quran.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Bookmark;
import com.tanxe.quran.theme.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private final List<Bookmark> bookmarks;
    private final ThemeManager theme;
    private final OnBookmarkClick clickListener;
    private final OnBookmarkDelete deleteListener;

    public interface OnBookmarkClick {
        void onClick(Bookmark bookmark);
    }

    public interface OnBookmarkDelete {
        void onDelete(Bookmark bookmark);
    }

    public BookmarkAdapter(List<Bookmark> bookmarks, ThemeManager theme, OnBookmarkClick clickListener, OnBookmarkDelete deleteListener) {
        this.bookmarks = bookmarks;
        this.theme = theme;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bookmark bookmark = bookmarks.get(position);
        holder.tvSurah.setText(bookmark.surahName);
        holder.tvRef.setText(bookmark.surahNumber + ":" + bookmark.ayahNumber);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvNote.setText(sdf.format(new Date(bookmark.timestamp)));

        holder.tvSurah.setTextColor(theme.getPrimaryTextColor());
        holder.tvRef.setTextColor(theme.getAccentColor());
        holder.tvNote.setTextColor(theme.getSecondaryTextColor());

        holder.itemView.setOnClickListener(v -> clickListener.onClick(bookmark));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(bookmark));
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSurah, tvRef, tvNote;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSurah = itemView.findViewById(R.id.tv_bookmark_surah);
            tvRef = itemView.findViewById(R.id.tv_bookmark_ref);
            tvNote = itemView.findViewById(R.id.tv_bookmark_note);
            btnDelete = itemView.findViewById(R.id.btn_delete_bookmark);
        }
    }
}
