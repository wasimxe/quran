package com.tanxe.quran.ui.reading;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tanxe.quran.R;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;

public class AyahActionsBottomSheet extends BottomSheetDialogFragment {

    public interface ActionListener {
        void onBookmarkToggle();
        void onSetReadingPoint();
        void onPlay();
        void onShare();
        void onShareAsImage();
        void onCopyArabic();
        default void onSelectText() {}
    }

    private ActionListener listener;
    private int surah, ayah;
    private String surahName;
    private String arabicText;
    private boolean isBookmarked;
    private Typeface arabicFont;

    public void setData(int surah, int ayah, String surahName, String arabicText,
                        boolean isBookmarked, Typeface arabicFont) {
        this.surah = surah;
        this.ayah = ayah;
        this.surahName = surahName;
        this.arabicText = arabicText;
        this.isBookmarked = isBookmarked;
        this.arabicFont = arabicFont;
    }

    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_ayah_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ThemeManager theme = ThemeManager.getInstance(requireContext());

        View sheetContainer = view.findViewById(R.id.sheet_container);
        sheetContainer.setBackgroundColor(theme.getSurfaceColor());

        // Header
        TextView tvRef = view.findViewById(R.id.tv_ayah_ref);
        tvRef.setText(surahName + " " + surah + ":" + ayah);
        tvRef.setTextColor(theme.getAccentColor());

        TextView tvArabicPreview = view.findViewById(R.id.tv_arabic_preview);
        tvArabicPreview.setText(arabicText != null ? arabicText : "");
        tvArabicPreview.setTextColor(theme.getArabicTextColor());
        if (arabicFont != null) tvArabicPreview.setTypeface(arabicFont);

        // Bookmark button
        String lang = QuranRepository.getInstance(requireContext()).getLanguage();
        TextView tvBookmark = view.findViewById(R.id.tv_bookmark);
        tvBookmark.setText(Localization.get(lang, isBookmarked ? Localization.REMOVE_BOOKMARK : Localization.ADD_BOOKMARK));

        // Localize all action labels
        TextView tvReadingPoint = view.findViewById(R.id.tv_reading_point);
        if (tvReadingPoint != null) tvReadingPoint.setText(Localization.get(lang, Localization.SET_READING_POINT));
        TextView tvPlay = view.findViewById(R.id.tv_play);
        if (tvPlay != null) tvPlay.setText(Localization.get(lang, Localization.PLAY_AYAH));
        TextView tvShare = view.findViewById(R.id.tv_share);
        if (tvShare != null) tvShare.setText(Localization.get(lang, Localization.SHARE));
        TextView tvShareImage = view.findViewById(R.id.tv_share_image);
        if (tvShareImage != null) tvShareImage.setText(Localization.get(lang, Localization.SHARE_IMAGE));
        TextView tvCopy = view.findViewById(R.id.tv_copy);
        if (tvCopy != null) tvCopy.setText(Localization.get(lang, Localization.COPY_ARABIC));
        TextView tvSelectText = view.findViewById(R.id.tv_select_text);
        if (tvSelectText != null) tvSelectText.setText(Localization.get(lang, Localization.SELECT_TEXT));
        tvBookmark.setTextColor(theme.getPrimaryTextColor());
        ImageView icBookmark = view.findViewById(R.id.ic_bookmark);
        icBookmark.setImageResource(isBookmarked ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
        icBookmark.setColorFilter(theme.getAccentColor());

        // Set text colors for all action labels
        int textColor = theme.getPrimaryTextColor();
        int iconColor = theme.getSecondaryTextColor();

        // Apply colors to all action rows
        setupActionRow(view, R.id.action_bookmark, textColor, iconColor);
        setupActionRow(view, R.id.action_reading_point, textColor, iconColor);
        setupActionRow(view, R.id.action_play, textColor, iconColor);
        setupActionRow(view, R.id.action_share, textColor, iconColor);
        setupActionRow(view, R.id.action_share_image, textColor, iconColor);
        setupActionRow(view, R.id.action_select_text, textColor, iconColor);
        setupActionRow(view, R.id.action_copy, textColor, iconColor);

        // Click handlers
        view.findViewById(R.id.action_bookmark).setOnClickListener(v -> {
            if (listener != null) listener.onBookmarkToggle();
            dismiss();
        });
        view.findViewById(R.id.action_reading_point).setOnClickListener(v -> {
            if (listener != null) listener.onSetReadingPoint();
            dismiss();
        });
        view.findViewById(R.id.action_play).setOnClickListener(v -> {
            if (listener != null) listener.onPlay();
            dismiss();
        });
        view.findViewById(R.id.action_share).setOnClickListener(v -> {
            if (listener != null) listener.onShare();
            dismiss();
        });
        view.findViewById(R.id.action_share_image).setOnClickListener(v -> {
            if (listener != null) listener.onShareAsImage();
            dismiss();
        });
        view.findViewById(R.id.action_select_text).setOnClickListener(v -> {
            if (listener != null) listener.onSelectText();
            dismiss();
        });
        view.findViewById(R.id.action_copy).setOnClickListener(v -> {
            if (listener != null) listener.onCopyArabic();
            dismiss();
        });
    }

    private void setupActionRow(View root, int rowId, int textColor, int iconColor) {
        ViewGroup row = root.findViewById(rowId);
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(textColor);
            } else if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(iconColor);
            }
        }
    }
}
