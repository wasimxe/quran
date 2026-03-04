package com.tanxe.quran.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.tanxe.quran.data.QuranDatabase;
import com.tanxe.quran.data.dao.*;
import com.tanxe.quran.data.entity.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuranRepository {
    private static volatile QuranRepository INSTANCE;

    private final AyahDao ayahDao;
    private final TranslationDao translationDao;
    private final TafseerDao tafseerDao;
    private final WordByWordDao wordByWordDao;
    private final BookmarkDao bookmarkDao;
    private final KnownWordDao knownWordDao;
    private final SharedPreferences prefs;
    private final ExecutorService executor;

    private QuranRepository(Context context) {
        QuranDatabase db = QuranDatabase.getInstance(context);
        ayahDao = db.ayahDao();
        translationDao = db.translationDao();
        tafseerDao = db.tafseerDao();
        wordByWordDao = db.wordByWordDao();
        bookmarkDao = db.bookmarkDao();
        knownWordDao = db.knownWordDao();
        prefs = context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE);
        executor = Executors.newFixedThreadPool(4);
    }

    public static QuranRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (QuranRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new QuranRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // Executor for background work
    public ExecutorService getExecutor() { return executor; }

    // === Ayah operations ===
    public void insertAyahs(List<Ayah> ayahs) { ayahDao.insertAll(ayahs); }
    public Ayah getAyah(int surah, int ayah) { return ayahDao.getAyah(surah, ayah); }
    public List<Ayah> getAyahsBySurah(int surah) { return ayahDao.getAyahsBySurah(surah); }
    public List<Ayah> getAyahsByJuz(int juz) { return ayahDao.getAyahsByJuz(juz); }
    public int getAyahCount(int surah) { return ayahDao.getAyahCount(surah); }
    public int getTotalAyahCount() { return ayahDao.getTotalAyahCount(); }
    public List<AyahDao.SurahInfo> getAllSurahs() { return ayahDao.getAllSurahs(); }
    public Ayah getRandomAyah() { return ayahDao.getRandomAyah(); }
    public int getMaxAyahInSurah(int surah) { return ayahDao.getMaxAyahInSurah(surah); }

    // === Translation operations ===
    public void insertTranslations(List<Translation> translations) { translationDao.insertAll(translations); }
    public Translation getTranslation(int surah, int ayah, String edition) { return translationDao.getTranslation(surah, ayah, edition); }
    public List<String> getAvailableTranslations() { return translationDao.getAvailableEditions(); }
    public List<String> getTranslationsByLanguage(String lang) { return translationDao.getEditionsByLanguage(lang); }
    public void deleteTranslation(String edition) { translationDao.deleteEdition(edition); }

    // === Tafseer operations ===
    public void insertTafseers(List<Tafseer> tafseers) { tafseerDao.insertAll(tafseers); }
    public Tafseer getTafseer(int surah, int ayah, String edition) { return tafseerDao.getTafseer(surah, ayah, edition); }
    public List<String> getAvailableTafseers() { return tafseerDao.getAvailableEditions(); }
    public List<String> getTafseersByLanguage(String lang) { return tafseerDao.getEditionsByLanguage(lang); }
    public void deleteTafseer(String edition) { tafseerDao.deleteEdition(edition); }

    // === Word by Word operations ===
    public void insertWords(List<WordByWord> words) { wordByWordDao.insertAll(words); }
    public List<WordByWord> getWords(int surah, int ayah, String language) { return wordByWordDao.getWords(surah, ayah, language); }
    public List<String> getAvailableWbwLanguages() { return wordByWordDao.getAvailableLanguages(); }
    public List<com.tanxe.quran.data.dao.WordByWordDao.WordFrequency> getWords_frequencies(String language) { return wordByWordDao.getWordFrequencies(language); }
    public void deleteWbw(String language) { wordByWordDao.deleteLanguage(language); }

    // === Bookmark operations ===
    public void addBookmark(Bookmark bookmark) { bookmarkDao.insert(bookmark); }
    public void removeBookmark(int surah, int ayah) { bookmarkDao.deleteByAyah(surah, ayah); }
    public List<Bookmark> getAllBookmarks() { return bookmarkDao.getAllBookmarks(); }
    public boolean isBookmarked(int surah, int ayah) { return bookmarkDao.isBookmarked(surah, ayah) > 0; }

    // === Known Words (Learn Mode) ===
    public void markWordKnown(KnownWord word) { knownWordDao.insert(word); }
    public void markWordUnknown(String word) { knownWordDao.deleteWord(word); }
    public List<KnownWord> getAllKnownWords() { return knownWordDao.getAllKnownWords(); }
    public boolean isWordKnown(String word) { return knownWordDao.isKnown(word) > 0; }
    public int getKnownWordCount() { return knownWordDao.getKnownCount(); }
    public int getTotalKnownFrequency() { return knownWordDao.getTotalKnownFrequency(); }

    // === Search ===
    public List<Ayah> searchAll(String query) { return ayahDao.searchAyahs(query); }
    public List<Ayah> searchArabic(String query) { return ayahDao.searchArabic(query); }
    public List<Ayah> searchTranslation(String query) { return ayahDao.searchTranslation(query); }
    public List<Translation> searchInTranslation(String edition, String query) { return translationDao.searchInEdition(edition, query); }
    public List<Tafseer> searchInTafseer(String edition, String query) { return tafseerDao.searchInEdition(edition, query); }

    // === Preferences ===
    public void saveCurrentPosition(int surah, int ayah) {
        prefs.edit().putInt("current_surah", surah).putInt("current_ayah", ayah).apply();
    }

    public int[] getCurrentPosition() {
        return new int[]{prefs.getInt("current_surah", 1), prefs.getInt("current_ayah", 1)};
    }

    public void saveDisplayMode(String mode) { prefs.edit().putString("display_mode", mode).apply(); }
    public String getDisplayMode() { return prefs.getString("display_mode", "translation"); }

    public void saveSelectedTranslation(String edition) { prefs.edit().putString("selected_translation", edition).apply(); }
    public String getSelectedTranslation() { return prefs.getString("selected_translation", "ur.jalandhry"); }

    public void saveSelectedTafseer(String edition) { prefs.edit().putString("selected_tafseer", edition).apply(); }
    public String getSelectedTafseer() { return prefs.getString("selected_tafseer", "ur.ibnkathir"); }

    public void saveSelectedWbwLanguage(String lang) { prefs.edit().putString("selected_wbw_lang", lang).apply(); }
    public String getSelectedWbwLanguage() { return prefs.getString("selected_wbw_lang", "ur"); }

    public void saveTheme(String theme) { prefs.edit().putString("theme", theme).apply(); }
    public String getTheme() { return prefs.getString("theme", "emerald"); }

    public void saveLanguage(String lang) { prefs.edit().putString("app_language", lang).apply(); }
    public String getLanguage() { return prefs.getString("app_language", "en"); }

    public void setRepeatMode(boolean repeat) { prefs.edit().putBoolean("repeat_mode", repeat).apply(); }
    public boolean getRepeatMode() { return prefs.getBoolean("repeat_mode", false); }

    public void setContinuousPlay(boolean continuous) { prefs.edit().putBoolean("continuous_play", continuous).apply(); }
    public boolean getContinuousPlay() { return prefs.getBoolean("continuous_play", false); }

    public boolean isDataLoaded() { return prefs.getBoolean("data_loaded", false); }
    public void setDataLoaded(boolean loaded) { prefs.edit().putBoolean("data_loaded", loaded).apply(); }

    public boolean isFirstRun() { return prefs.getBoolean("first_run", true); }
    public void setFirstRun(boolean firstRun) { prefs.edit().putBoolean("first_run", firstRun).apply(); }

    public float getArabicFontSize() { return prefs.getFloat("arabic_font_size", 36f); }
    public void setArabicFontSize(float size) { prefs.edit().putFloat("arabic_font_size", size).apply(); }

    public float getTranslationFontSize() { return prefs.getFloat("translation_font_size", 18f); }
    public void setTranslationFontSize(float size) { prefs.edit().putFloat("translation_font_size", size).apply(); }

    // === Download state tracking ===
    public void setDownloadState(String edition, String state) {
        prefs.edit().putString("dl_state_" + edition, state).apply();
    }
    public String getDownloadState(String edition) {
        return prefs.getString("dl_state_" + edition, "none");
    }
    public void setDownloadProgress(String edition, int progress) {
        prefs.edit().putInt("dl_progress_" + edition, progress).apply();
    }
    public int getDownloadProgress(String edition) {
        return prefs.getInt("dl_progress_" + edition, 0);
    }
}
