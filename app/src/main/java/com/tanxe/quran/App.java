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

        // Initialize theme
        ThemeManager.getInstance(this).applyTheme();

        // Initialize reciters in background
        QuranRepository repo = QuranRepository.getInstance(this);
        repo.getExecutor().execute(() -> {
            repo.initReciters();
            // Fetch edition catalog if not yet loaded
            if (!repo.isEditionCatalogLoaded()) {
                repo.fetchAndCacheEditions();
                repo.setEditionCatalogLoaded(true);
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
