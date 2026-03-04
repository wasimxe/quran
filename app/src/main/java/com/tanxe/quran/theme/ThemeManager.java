package com.tanxe.quran.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class ThemeManager {
    private static volatile ThemeManager INSTANCE;

    public static final String THEME_EMERALD = "emerald";
    public static final String THEME_MIDNIGHT = "midnight";
    public static final String THEME_PURPLE = "purple";
    public static final String THEME_DESERT = "desert";

    private final SharedPreferences prefs;
    private String currentTheme;

    // Theme colors
    private int backgroundColor;
    private int surfaceColor;
    private int accentColor;
    private int arabicTextColor;
    private int translationTextColor;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int cardColor;
    private int dividerColor;
    private int toolbarColor;
    private int badgeColor;
    private int modePillColor;
    private int modePillActiveColor;
    private int downloadedColor;
    private int errorColor;

    private ThemeManager(Context context) {
        prefs = context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE);
        currentTheme = prefs.getString("theme", THEME_EMERALD);
        applyThemeColors();
    }

    public static ThemeManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ThemeManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ThemeManager(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void setTheme(String theme) {
        currentTheme = theme;
        prefs.edit().putString("theme", theme).apply();
        applyThemeColors();
    }

    public String getCurrentTheme() { return currentTheme; }

    public void applyTheme() {
        applyThemeColors();
    }

    private void applyThemeColors() {
        switch (currentTheme) {
            case THEME_MIDNIGHT:
                backgroundColor = Color.parseColor("#0D1117");
                surfaceColor = Color.parseColor("#161B22");
                accentColor = Color.parseColor("#58A6FF");
                arabicTextColor = Color.parseColor("#79C0FF");
                translationTextColor = Color.parseColor("#C9D1D9");
                primaryTextColor = Color.parseColor("#F0F6FC");
                secondaryTextColor = Color.parseColor("#8B949E");
                cardColor = Color.parseColor("#1C2128");
                dividerColor = Color.parseColor("#30363D");
                toolbarColor = Color.parseColor("#1C2128");
                badgeColor = Color.parseColor("#388BFD");
                modePillColor = Color.parseColor("#2D333B");
                modePillActiveColor = Color.parseColor("#58A6FF");
                downloadedColor = Color.parseColor("#3FB950");
                errorColor = Color.parseColor("#F85149");
                break;
            case THEME_PURPLE:
                backgroundColor = Color.parseColor("#1A0A2E");
                surfaceColor = Color.parseColor("#231242");
                accentColor = Color.parseColor("#BB86FC");
                arabicTextColor = Color.parseColor("#CF9FFF");
                translationTextColor = Color.parseColor("#E0D0F0");
                primaryTextColor = Color.parseColor("#F5F0FF");
                secondaryTextColor = Color.parseColor("#9E8EC0");
                cardColor = Color.parseColor("#2D1B4E");
                dividerColor = Color.parseColor("#3D2B5E");
                toolbarColor = Color.parseColor("#2D1B4E");
                badgeColor = Color.parseColor("#9B59B6");
                modePillColor = Color.parseColor("#3D2B5E");
                modePillActiveColor = Color.parseColor("#BB86FC");
                downloadedColor = Color.parseColor("#66BB6A");
                errorColor = Color.parseColor("#EF5350");
                break;
            case THEME_DESERT:
                backgroundColor = Color.parseColor("#FFF8F0");
                surfaceColor = Color.parseColor("#FFF0E0");
                accentColor = Color.parseColor("#8B6914");
                arabicTextColor = Color.parseColor("#5D4037");
                translationTextColor = Color.parseColor("#4E342E");
                primaryTextColor = Color.parseColor("#3E2723");
                secondaryTextColor = Color.parseColor("#8D6E63");
                cardColor = Color.parseColor("#FFECB3");
                dividerColor = Color.parseColor("#D7CCC8");
                toolbarColor = Color.parseColor("#FFE0B2");
                badgeColor = Color.parseColor("#8B6914");
                modePillColor = Color.parseColor("#FFE0B2");
                modePillActiveColor = Color.parseColor("#8B6914");
                downloadedColor = Color.parseColor("#2E7D32");
                errorColor = Color.parseColor("#C62828");
                break;
            case THEME_EMERALD:
            default:
                backgroundColor = Color.parseColor("#0A2E1F");
                surfaceColor = Color.parseColor("#0F3D2A");
                accentColor = Color.parseColor("#D4AF37");
                arabicTextColor = Color.parseColor("#F5D76E");
                translationTextColor = Color.parseColor("#C8E6C9");
                primaryTextColor = Color.parseColor("#E8F5E9");
                secondaryTextColor = Color.parseColor("#81C784");
                cardColor = Color.parseColor("#1A4D35");
                dividerColor = Color.parseColor("#2E7D52");
                toolbarColor = Color.parseColor("#1A4D35");
                badgeColor = Color.parseColor("#D4AF37");
                modePillColor = Color.parseColor("#1A4D35");
                modePillActiveColor = Color.parseColor("#D4AF37");
                downloadedColor = Color.parseColor("#66BB6A");
                errorColor = Color.parseColor("#EF5350");
                break;
        }
    }

    // Getters
    public int getBackgroundColor() { return backgroundColor; }
    public int getSurfaceColor() { return surfaceColor; }
    public int getAccentColor() { return accentColor; }
    public int getArabicTextColor() { return arabicTextColor; }
    public int getTranslationTextColor() { return translationTextColor; }
    public int getPrimaryTextColor() { return primaryTextColor; }
    public int getSecondaryTextColor() { return secondaryTextColor; }
    public int getCardColor() { return cardColor; }
    public int getDividerColor() { return dividerColor; }
    public int getToolbarColor() { return toolbarColor; }
    public int getBadgeColor() { return badgeColor; }
    public int getModePillColor() { return modePillColor; }
    public int getModePillActiveColor() { return modePillActiveColor; }
    public int getDownloadedColor() { return downloadedColor; }
    public int getErrorColor() { return errorColor; }

    public boolean isDarkTheme() {
        return !currentTheme.equals(THEME_DESERT);
    }

    public String[] getThemeNames() {
        return new String[]{THEME_EMERALD, THEME_MIDNIGHT, THEME_PURPLE, THEME_DESERT};
    }

    public String getThemeDisplayName(String theme) {
        switch (theme) {
            case THEME_MIDNIGHT: return "Midnight Blue";
            case THEME_PURPLE: return "Royal Purple";
            case THEME_DESERT: return "Desert Sand";
            case THEME_EMERALD:
            default: return "Emerald Gold";
        }
    }
}
