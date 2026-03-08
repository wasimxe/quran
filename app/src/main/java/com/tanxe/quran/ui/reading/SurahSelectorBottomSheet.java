package com.tanxe.quran.ui.reading;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tanxe.quran.R;
import com.tanxe.quran.data.dao.AyahDao;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.SurahListAdapter;
import com.tanxe.quran.util.Localization;

import java.util.List;

public class SurahSelectorBottomSheet extends BottomSheetDialogFragment {

    public interface OnSurahSelected {
        void onSurahSelected(int surahNumber);
    }

    private OnSurahSelected listener;
    private List<AyahDao.SurahInfo> surahs;

    public void setOnSurahSelectedListener(OnSurahSelected listener) {
        this.listener = listener;
    }

    public void setSurahs(List<AyahDao.SurahInfo> surahs) {
        this.surahs = surahs;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_surah_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ThemeManager theme = ThemeManager.getInstance(requireContext());

        View sheetContainer = view.findViewById(R.id.sheet_container);
        sheetContainer.setBackgroundColor(theme.getSurfaceColor());

        String lang = QuranRepository.getInstance(requireContext()).getLanguage();
        TextView title = view.findViewById(R.id.tv_sheet_title);
        title.setText(Localization.get(lang, Localization.SELECT_SURAH));
        title.setTextColor(theme.getPrimaryTextColor());

        RecyclerView rv = view.findViewById(R.id.rv_surahs);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (surahs == null) {
            surahs = QuranRepository.getInstance(requireContext()).getAllSurahs();
        }

        SurahListAdapter adapter = new SurahListAdapter(surahs, theme, surahNumber -> {
            if (listener != null) {
                listener.onSurahSelected(surahNumber);
            }
            dismiss();
        }, lang);
        rv.setAdapter(adapter);

        EditText search = view.findViewById(R.id.et_surah_search);
        search.setHint(Localization.get(lang, Localization.SEARCH_SURAH_HINT));
        search.setTextColor(theme.getPrimaryTextColor());
        search.setHintTextColor(theme.getSecondaryTextColor());
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
