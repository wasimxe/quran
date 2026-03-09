package com.tanxe.quran;

import android.app.Application;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.notification.DailyVerseWorker;
import com.tanxe.quran.theme.ThemeManager;

import java.util.concurrent.TimeUnit;

public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize theme (fast, just reads SharedPrefs)
        ThemeManager.getInstance(this).applyTheme();

        // Pre-warm repository singleton (creates DB instance early)
        QuranRepository repo = QuranRepository.getInstance(this);

        // Defer all non-critical init to background
        repo.getExecutor().execute(() -> {
            // Pre-warm surah list cache for instant header display
            repo.getAllSurahs();

            // Pre-warm ayah cache for the initial position surahs (faster first render)
            int[] pos = repo.getCurrentPosition();
            int startSurah = Math.max(1, pos[0] - 1);
            int endSurah = Math.min(114, pos[0] + 2);
            for (int s = startSurah; s <= endSurah; s++) {
                repo.getAyahsBySurah(s);
            }

            repo.initReciters();
            // Fetch edition catalog if not yet loaded
            if (!repo.isEditionCatalogLoaded()) {
                try {
                    repo.fetchAndCacheEditions();
                    repo.setEditionCatalogLoaded(true);
                } catch (Exception ignored) {}
            }
        });

        // Schedule daily verse notification
        scheduleDailyVerse();
    }

    private void scheduleDailyVerse() {
        PeriodicWorkRequest dailyVerseRequest = new PeriodicWorkRequest.Builder(
                DailyVerseWorker.class, 24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_verse",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyVerseRequest);
    }

    public static App getInstance() {
        return instance;
    }
}
