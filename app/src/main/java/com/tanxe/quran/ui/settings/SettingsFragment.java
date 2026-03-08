package com.tanxe.quran.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.tanxe.quran.MainActivity;
import com.tanxe.quran.R;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;

public class SettingsFragment extends Fragment {
    private QuranRepository repository;
    private ThemeManager theme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = QuranRepository.getInstance(requireContext());
        theme = ThemeManager.getInstance(requireContext());

        initViews(view);
        applyTheme();
        loadReadingProgress();
    }

    private void initViews(View view) {
        // Theme cards (6 themes)
        MaterialCardView cardEmerald = view.findViewById(R.id.card_emerald);
        MaterialCardView cardMidnight = view.findViewById(R.id.card_midnight);
        MaterialCardView cardPurple = view.findViewById(R.id.card_purple);
        MaterialCardView cardDesert = view.findViewById(R.id.card_desert);
        MaterialCardView cardAmoled = view.findViewById(R.id.card_amoled);
        MaterialCardView cardOcean = view.findViewById(R.id.card_ocean);

        cardEmerald.setOnClickListener(v -> setTheme(ThemeManager.THEME_EMERALD));
        cardMidnight.setOnClickListener(v -> setTheme(ThemeManager.THEME_MIDNIGHT));
        cardPurple.setOnClickListener(v -> setTheme(ThemeManager.THEME_PURPLE));
        cardDesert.setOnClickListener(v -> setTheme(ThemeManager.THEME_DESERT));
        cardAmoled.setOnClickListener(v -> setTheme(ThemeManager.THEME_AMOLED));
        cardOcean.setOnClickListener(v -> setTheme(ThemeManager.THEME_OCEAN));

        // Language spinner
        Spinner spinnerLang = view.findViewById(R.id.spinner_language);
        String[] langs = {"English", "\u0627\u0631\u062f\u0648 (Urdu)", "\u0627\u0644\u0639\u0631\u0628\u064a\u0629 (Arabic)", "T\u00fcrk\u00e7e (Turkish)"};
        String[] langCodes = {"en", "ur", "ar", "tr"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, langs) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) ((TextView) v).setTextColor(theme.getAccentColor());
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(theme.getPrimaryTextColor());
                    v.setBackgroundColor(theme.getSurfaceColor());
                }
                return v;
            }
        };
        spinnerLang.setAdapter(langAdapter);

        String currentLang = repository.getLanguage();
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                spinnerLang.setSelection(i);
                break;
            }
        }

        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                repository.saveLanguage(langCodes[position]);
                // Refresh all UI labels in the user's new language
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshTheme();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Developer info click handlers
        view.findViewById(R.id.row_website).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tanxe.com"));
            startActivity(intent);
        });

        view.findViewById(R.id.row_email).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:info@tanxe.com"));
            startActivity(intent);
        });

        view.findViewById(R.id.row_whatsapp).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/923455407008"));
            startActivity(intent);
        });
    }

    private void loadReadingProgress() {
        repository.getExecutor().execute(() -> {
            int todayMinutes = repository.getTodayReadingMinutes();
            float khatmah = repository.getKhatmahPercentage();
            int streak = repository.getReadingStreak();

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (getView() == null) return;
                    TextView tvTodayMinutes = getView().findViewById(R.id.tv_today_minutes);
                    TextView tvKhatmah = getView().findViewById(R.id.tv_khatmah);
                    TextView tvStreak = getView().findViewById(R.id.tv_streak);

                    tvTodayMinutes.setText(todayMinutes + " min");
                    tvTodayMinutes.setTextColor(theme.getAccentColor());
                    tvKhatmah.setText(String.format("%.1f%%", khatmah));
                    tvKhatmah.setTextColor(theme.getAccentColor());
                    tvStreak.setText(streak + " days");
                    tvStreak.setTextColor(theme.getAccentColor());
                });
            }
        });
    }

    private void setTheme(String themeName) {
        theme.setTheme(themeName);
        applyTheme();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshTheme();
        }
    }

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());
        View container = getView().findViewById(R.id.settings_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        // Title
        TextView tvMoreTitle = getView().findViewById(R.id.tv_more_title);
        if (tvMoreTitle != null) tvMoreTitle.setTextColor(theme.getAccentColor());

        // Progress card
        MaterialCardView cardProgress = getView().findViewById(R.id.card_progress);
        if (cardProgress != null) cardProgress.setCardBackgroundColor(theme.getCardColor());

        TextView tvProgressTitle = getView().findViewById(R.id.tv_progress_title);
        if (tvProgressTitle != null) tvProgressTitle.setTextColor(theme.getPrimaryTextColor());

        // Style section headers
        int[] headerIds = {R.id.tv_theme_header,
                R.id.tv_language_header, R.id.tv_about_header};
        for (int id : headerIds) {
            TextView tv = getView().findViewById(id);
            if (tv != null) tv.setTextColor(theme.getAccentColor());
        }

        TextView tvAbout = getView().findViewById(R.id.tv_about);
        if (tvAbout != null) tvAbout.setTextColor(theme.getSecondaryTextColor());

        // Localize all labels
        localizeLabels();

        // Developer card
        MaterialCardView cardDev = getView().findViewById(R.id.card_developer);
        if (cardDev != null) cardDev.setCardBackgroundColor(theme.getCardColor());

        TextView tvDevLabel = getView().findViewById(R.id.tv_developer_label);
        if (tvDevLabel != null) tvDevLabel.setTextColor(theme.getSecondaryTextColor());

        TextView tvCompanyName = getView().findViewById(R.id.tv_company_name);
        if (tvCompanyName != null) tvCompanyName.setTextColor(theme.getAccentColor());

        int[] devLabelIds = {R.id.tv_website_label, R.id.tv_email_label, R.id.tv_whatsapp_label};
        for (int id : devLabelIds) {
            TextView tv = getView().findViewById(id);
            if (tv != null) tv.setTextColor(theme.getSecondaryTextColor());
        }

        int[] devValueIds = {R.id.tv_website, R.id.tv_email, R.id.tv_whatsapp};
        for (int id : devValueIds) {
            TextView tv = getView().findViewById(id);
            if (tv != null) tv.setTextColor(theme.getPrimaryTextColor());
        }
    }

    private void localizeLabels() {
        if (getView() == null) return;
        String lang = repository.getLanguage();

        TextView tvMoreTitle = getView().findViewById(R.id.tv_more_title);
        if (tvMoreTitle != null) tvMoreTitle.setText(Localization.get(lang, Localization.MORE));

        TextView tvProgressTitle = getView().findViewById(R.id.tv_progress_title);
        if (tvProgressTitle != null) tvProgressTitle.setText(Localization.get(lang, Localization.READING_PROGRESS));

        TextView tvTodayLabel = getView().findViewById(R.id.tv_today_label);
        if (tvTodayLabel != null) tvTodayLabel.setText(Localization.get(lang, Localization.TODAY));

        TextView tvKhatmahLabel = getView().findViewById(R.id.tv_khatmah_label);
        if (tvKhatmahLabel != null) tvKhatmahLabel.setText(Localization.get(lang, Localization.KHATMAH));

        TextView tvStreakLabel = getView().findViewById(R.id.tv_streak_label);
        if (tvStreakLabel != null) tvStreakLabel.setText(Localization.get(lang, Localization.STREAK));

        TextView tvThemeHeader = getView().findViewById(R.id.tv_theme_header);
        if (tvThemeHeader != null) tvThemeHeader.setText(Localization.get(lang, Localization.THEME));

        TextView tvLangHeader = getView().findViewById(R.id.tv_language_header);
        if (tvLangHeader != null) tvLangHeader.setText(Localization.get(lang, Localization.LANGUAGE));

        TextView tvAboutHeader = getView().findViewById(R.id.tv_about_header);
        if (tvAboutHeader != null) tvAboutHeader.setText(Localization.get(lang, Localization.ABOUT));

        TextView tvDevLabel = getView().findViewById(R.id.tv_developer_label);
        if (tvDevLabel != null) tvDevLabel.setText(Localization.get(lang, Localization.DEVELOPED_BY));

        TextView tvWebLabel = getView().findViewById(R.id.tv_website_label);
        if (tvWebLabel != null) tvWebLabel.setText(Localization.get(lang, Localization.WEBSITE));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reading progress already loaded in onViewCreated; only refresh if needed
    }

}
