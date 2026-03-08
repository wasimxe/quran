package com.tanxe.quran.ui.reading;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Bookmark;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarksBottomSheet extends BottomSheetDialogFragment {

    public interface OnBookmarkClickListener {
        void onBookmarkClick(int surah, int ayah);
    }

    private OnBookmarkClickListener listener;
    private final List<Bookmark> bookmarks = new ArrayList<>();
    private final Map<String, String> translationMap = new HashMap<>();
    private BookmarkAdapter adapter;
    private TextView tvEmpty;

    public void setOnBookmarkClickListener(OnBookmarkClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_bookmarks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ThemeManager theme = ThemeManager.getInstance(requireContext());
        QuranRepository repository = QuranRepository.getInstance(requireContext());
        String lang = repository.getLanguage();

        View sheetContainer = view.findViewById(R.id.bookmarks_container);
        sheetContainer.setBackgroundColor(theme.getSurfaceColor());

        TextView tvTitle = view.findViewById(R.id.tv_bookmarks_title);
        tvTitle.setText(Localization.get(lang, Localization.BOOKMARKS));
        tvTitle.setTextColor(theme.getAccentColor());

        tvEmpty = view.findViewById(R.id.tv_empty);
        tvEmpty.setText(Localization.get(lang, Localization.NO_BOOKMARKS));
        tvEmpty.setTextColor(theme.getSecondaryTextColor());

        RecyclerView rv = view.findViewById(R.id.rv_bookmarks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new BookmarkAdapter(theme, lang);
        rv.setAdapter(adapter);

        // Load bookmarks with translations
        repository.getExecutor().execute(() -> {
            List<Bookmark> loaded = repository.getAllBookmarks();
            Map<String, String> transMap = new HashMap<>();
            if (loaded != null) {
                for (Bookmark b : loaded) {
                    com.tanxe.quran.data.entity.Ayah ayah = repository.getAyah(b.surahNumber, b.ayahNumber);
                    if (ayah != null && ayah.defaultTranslation != null) {
                        transMap.put(b.surahNumber + ":" + b.ayahNumber, ayah.defaultTranslation);
                    }
                }
            }
            if (getActivity() == null || !isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                bookmarks.clear();
                translationMap.clear();
                if (loaded != null) bookmarks.addAll(loaded);
                translationMap.putAll(transMap);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(bookmarks.isEmpty() ? View.VISIBLE : View.GONE);
                rv.setVisibility(bookmarks.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) return;
        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    private class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.VH> {
        private final ThemeManager theme;
        private final String lang;

        BookmarkAdapter(ThemeManager theme, String lang) {
            this.theme = theme;
            this.lang = lang;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Bookmark b = bookmarks.get(position);

            String surahName = (b.surahName != null && !b.surahName.isEmpty())
                    ? b.surahNumber + ". " + b.surahName
                    : Localization.get(lang, Localization.SURAH) + " " + b.surahNumber;
            holder.tvSurah.setText(surahName);
            holder.tvSurah.setTextColor(theme.getPrimaryTextColor());

            String ayahRef = Localization.get(lang, Localization.AYAH) + " " + b.ayahNumber;
            holder.tvRef.setText(ayahRef);
            holder.tvRef.setTextColor(theme.getSecondaryTextColor());

            // Translation preview
            String trans = translationMap.get(b.surahNumber + ":" + b.ayahNumber);
            if (holder.tvTranslation != null) {
                if (trans != null && !trans.isEmpty()) {
                    holder.tvTranslation.setText(trans);
                    holder.tvTranslation.setTextColor(theme.getSecondaryTextColor());
                    holder.tvTranslation.setVisibility(View.VISIBLE);
                } else {
                    holder.tvTranslation.setVisibility(View.GONE);
                }
            }

            // Note field (hide if empty)
            if (holder.tvNote != null) {
                if (b.note != null && !b.note.isEmpty()) {
                    holder.tvNote.setText(b.note);
                    holder.tvNote.setTextColor(theme.getSecondaryTextColor());
                    holder.tvNote.setVisibility(View.VISIBLE);
                } else {
                    holder.tvNote.setVisibility(View.GONE);
                }
            }

            // Card background
            if (holder.itemView instanceof MaterialCardView) {
                ((MaterialCardView) holder.itemView).setCardBackgroundColor(theme.getCardColor());
                ((MaterialCardView) holder.itemView).setStrokeColor(theme.getDividerColor());
                ((MaterialCardView) holder.itemView).setStrokeWidth(1);
            }

            holder.btnDelete.setColorFilter(theme.getErrorColor());
            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                Bookmark bm = bookmarks.get(pos);
                QuranRepository.getInstance(requireContext()).getExecutor().execute(() -> {
                    QuranRepository.getInstance(requireContext()).removeBookmark(bm.surahNumber, bm.ayahNumber);
                    if (getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        bookmarks.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, bookmarks.size());
                        if (bookmarks.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(requireContext(),
                                Localization.get(lang, Localization.BOOKMARK_REMOVED),
                                Toast.LENGTH_SHORT).show();
                    });
                });
            });

            // Click → navigate to ayah
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookmarkClick(b.surahNumber, b.ayahNumber);
                }
                dismissAllowingStateLoss();
            });
        }

        @Override
        public int getItemCount() {
            return bookmarks.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvSurah, tvRef, tvTranslation, tvNote;
            ImageButton btnDelete;

            VH(@NonNull View itemView) {
                super(itemView);
                tvSurah = itemView.findViewById(R.id.tv_bookmark_surah);
                tvRef = itemView.findViewById(R.id.tv_bookmark_ref);
                tvTranslation = itemView.findViewById(R.id.tv_bookmark_translation);
                tvNote = itemView.findViewById(R.id.tv_bookmark_note);
                btnDelete = itemView.findViewById(R.id.btn_delete_bookmark);
            }
        }
    }
}
