package com.tanxe.quran.ui.reading;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tanxe.quran.R;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;

public class JuzSelectorBottomSheet extends BottomSheetDialogFragment {

    public interface OnJuzSelected {
        void onJuzSelected(int surah, int ayah);
    }

    private OnJuzSelected listener;

    // Juz boundaries: [surah, ayah] start of each juz
    private static final int[][] JUZ_STARTS = {
        {1,1}, {2,142}, {2,253}, {3,93}, {4,24}, {4,148}, {5,82}, {6,111},
        {7,88}, {8,41}, {9,93}, {11,6}, {12,53}, {15,1}, {17,1}, {18,75},
        {21,1}, {23,1}, {25,21}, {27,56}, {29,46}, {33,31}, {36,28}, {39,32},
        {41,47}, {46,1}, {51,31}, {58,1}, {67,1}, {78,1}
    };

    private static final String[] JUZ_ARABIC_NAMES = {
        "آلم", "سَيَقُولُ", "تِلْكَ الرُّسُلُ", "لَنْ تَنَالُوا", "وَالْمُحْصَنَاتُ",
        "لَا يُحِبُّ اللَّهُ", "وَإِذَا سَمِعُوا", "وَلَوْ أَنَّنَا", "قَالَ الْمَلَأُ", "وَاعْلَمُوا",
        "يَعْتَذِرُونَ", "وَمَا مِنْ دَابَّةٍ", "وَمَا أُبَرِّئُ", "رُبَمَا", "سُبْحَانَ الَّذِي",
        "قَالَ أَلَمْ", "اقْتَرَبَ", "قَدْ أَفْلَحَ", "وَقَالَ الَّذِينَ", "أَمَّنْ خَلَقَ",
        "اتْلُ مَا أُوحِيَ", "وَمَنْ يَقْنُتْ", "وَمَا لِيَ", "فَمَنْ أَظْلَمُ", "إِلَيْهِ يُرَدُّ",
        "حم", "قَالَ فَمَا خَطْبُكُمْ", "قَدْ سَمِعَ اللَّهُ", "تَبَارَكَ الَّذِي", "عَمَّ"
    };

    public void setOnJuzSelectedListener(OnJuzSelected listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_juz_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ThemeManager theme = ThemeManager.getInstance(requireContext());

        View sheetContainer = view.findViewById(R.id.sheet_container);
        sheetContainer.setBackgroundColor(theme.getSurfaceColor());

        String lang = QuranRepository.getInstance(requireContext()).getLanguage();
        TextView title = view.findViewById(R.id.tv_sheet_title);
        title.setText(Localization.get(lang, Localization.SELECT_JUZ));
        title.setTextColor(theme.getPrimaryTextColor());

        RecyclerView rv = view.findViewById(R.id.rv_juz);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new JuzListAdapter(theme));
    }

    private class JuzListAdapter extends RecyclerView.Adapter<JuzListAdapter.VH> {
        private final ThemeManager theme;

        JuzListAdapter(ThemeManager theme) {
            this.theme = theme;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_juz, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            int juzNum = position + 1;

            // Theme the card background
            if (holder.itemView instanceof MaterialCardView) {
                ((MaterialCardView) holder.itemView).setCardBackgroundColor(theme.getCardColor());
            }

            holder.tvNumber.setText(String.valueOf(juzNum));
            holder.tvNumber.setTextColor(theme.getAccentColor());
            String lang = QuranRepository.getInstance(holder.itemView.getContext()).getLanguage();
            String juzL = Localization.get(lang, Localization.JUZ);
            holder.tvTitle.setText(juzL + " " + juzNum);
            holder.tvTitle.setTextColor(theme.getPrimaryTextColor());

            int startSurah = JUZ_STARTS[position][0];
            int startAyah = JUZ_STARTS[position][1];
            String startsL = Localization.get(lang, Localization.STARTS);
            holder.tvStart.setText(startsL + ": " + startSurah + ":" + startAyah);
            holder.tvStart.setTextColor(theme.getSecondaryTextColor());
            holder.tvSurahs.setVisibility(View.GONE);
            holder.tvNameAr.setText(JUZ_ARABIC_NAMES[position]);
            holder.tvNameAr.setTextColor(theme.getArabicTextColor());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJuzSelected(startSurah, startAyah);
                }
                dismiss();
            });
        }

        @Override
        public int getItemCount() { return 30; }

        class VH extends RecyclerView.ViewHolder {
            TextView tvNumber, tvTitle, tvStart, tvSurahs, tvNameAr;
            VH(@NonNull View itemView) {
                super(itemView);
                tvNumber = itemView.findViewById(R.id.tv_juz_number);
                tvTitle = itemView.findViewById(R.id.tv_juz_title);
                tvStart = itemView.findViewById(R.id.tv_juz_start);
                tvSurahs = itemView.findViewById(R.id.tv_juz_surahs);
                tvNameAr = itemView.findViewById(R.id.tv_juz_name_ar);
            }
        }
    }
}
