package com.tanxe.quran;

import android.app.Application;

import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;

public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize theme
        ThemeManager.getInstance(this).applyTheme();
    }

    public static App getInstance() {
        return instance;
    }
}
