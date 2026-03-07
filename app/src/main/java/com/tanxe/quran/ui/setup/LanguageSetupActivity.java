package com.tanxe.quran.ui.setup;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.tanxe.quran.MainActivity;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.download.DownloadManager;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.LanguageAdapter;
import com.tanxe.quran.util.QuranDataParser;

import java.util.List;

public class LanguageSetupActivity extends AppCompatActivity {
    private RecyclerView rvLanguages;
    private MaterialButton btnContinue;
    private LinearProgressIndicator progressBar;
    private TextView tvLoading;
    private QuranRepository repository;
    private String selectedLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        repository = QuranRepository.getInstance(this);

        // Skip setup if not first run
        if (!repository.isFirstRun()) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_language_setup);

        ThemeManager theme = ThemeManager.getInstance(this);
        View container = findViewById(R.id.setup_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        // Set bismillah font
        TextView tvBismillah = findViewById(R.id.tv_bismillah);
        try {
            Typeface arabicFont = Typeface.createFromAsset(getAssets(), "fonts/al_mushaf.ttf");
            tvBismillah.setTypeface(arabicFont);
        } catch (Exception e) {
            // Use default font
        }
        tvBismillah.setTextColor(theme.getArabicTextColor());

        TextView tvTitle = findViewById(R.id.tv_app_title);
        tvTitle.setTextColor(theme.getAccentColor());

        TextView tvSelect = findViewById(R.id.tv_select_lang);
        tvSelect.setTextColor(theme.getPrimaryTextColor());

        rvLanguages = findViewById(R.id.rv_languages);
        btnContinue = findViewById(R.id.btn_continue);
        progressBar = findViewById(R.id.progress_bar);
        tvLoading = findViewById(R.id.tv_loading);
        tvLoading.setTextColor(theme.getSecondaryTextColor());

        setupLanguageGrid();

        btnContinue.setOnClickListener(v -> {
            repository.saveLanguage(selectedLanguage);
            loadQuranData();
        });
    }

    private void setupLanguageGrid() {
        rvLanguages.setLayoutManager(new GridLayoutManager(this, 2));

        String[][] languages = {
            {"en", "English", "English"},
            {"ur", "اردو", "Urdu"},
            {"ar", "العربية", "Arabic"},
            {"tr", "Türkçe", "Turkish"},
            {"id", "Indonesia", "Indonesian"},
            {"bn", "বাংলা", "Bangla"},
            {"fr", "Français", "French"},
            {"fa", "فارسی", "Persian"},
        };

        LanguageAdapter adapter = new LanguageAdapter(languages, (code, position) -> {
            selectedLanguage = code;
        });
        rvLanguages.setAdapter(adapter);
    }

    private void loadQuranData() {
        btnContinue.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvLoading.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        repository.getExecutor().execute(() -> {
            if (repository.getTotalAyahCount() == 0) {
                List<Ayah> ayahs = QuranDataParser.parseQuranTsv(this);
                if (!ayahs.isEmpty()) {
                    // Insert in batches
                    int batchSize = 500;
                    for (int i = 0; i < ayahs.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, ayahs.size());
                        repository.insertAyahs(ayahs.subList(i, end));
                        int progress = (int) ((i + batchSize) * 100.0 / ayahs.size());
                        runOnUiThread(() -> {
                            progressBar.setIndeterminate(false);
                            progressBar.setProgress(Math.min(progress, 100));
                        });
                    }
                }
            }

            repository.setDataLoaded(true);
            repository.setFirstRun(false);

            // Initialize reciter catalog and fetch edition catalog in background
            runOnUiThread(() -> tvLoading.setText("Setting up library..."));
            repository.initReciters();
            try {
                repository.fetchAndCacheEditions();
                repository.setEditionCatalogLoaded(true);
            } catch (Exception e) {
                // Non-fatal: catalog can be fetched later from Library tab
            }

            // Auto-download Urdu word-by-word data for Learn Mode
            runOnUiThread(() -> tvLoading.setText("Downloading word-by-word data..."));
            repository.saveSelectedWbwLanguage("ur");
            DownloadManager dm = new DownloadManager(LanguageSetupActivity.this);
            final boolean[] wbwDone = {false};
            dm.downloadWordByWord("ur", status -> {
                runOnUiThread(() -> tvLoading.setText(status));
                if (status.startsWith("Complete") || status.startsWith("Failed")) {
                    synchronized (wbwDone) {
                        wbwDone[0] = true;
                        wbwDone.notify();
                    }
                }
            });
            // Wait for WBW download to finish
            synchronized (wbwDone) {
                while (!wbwDone[0]) {
                    try { wbwDone.wait(1000); } catch (InterruptedException ignored) {}
                }
            }

            runOnUiThread(this::startMainActivity);
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
