package com.tanxe.quran.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tanxe.quran.data.QuranDatabase;
import com.tanxe.quran.data.ReciterCatalog;
import com.tanxe.quran.data.api.ApiClient;
import com.tanxe.quran.data.api.QuranApiService;
import com.tanxe.quran.data.dao.*;
import com.tanxe.quran.data.entity.*;

import android.util.LruCache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class QuranRepository {
    private static final String TAG = "QuranRepository";
    private static volatile QuranRepository INSTANCE;

    private final AyahDao ayahDao;
    private final TranslationDao translationDao;
    private final TafseerDao tafseerDao;
    private final WordByWordDao wordByWordDao;
    private final BookmarkDao bookmarkDao;
    private final KnownWordDao knownWordDao;
    private final EditionInfoDao editionInfoDao;
    private final ReciterInfoDao reciterInfoDao;
    private final ReadingProgressDao readingProgressDao;
    private final SharedPreferences prefs;
    private final ExecutorService executor;

    // LRU caches for hot-path DB queries during scrolling
    // Key: "surah:ayah", caches individual ayah lookups
    private final LruCache<String, Ayah> ayahCache = new LruCache<>(256);
    // Key: surah number, caches full surah ayah lists (for MushafAdapter)
    private final LruCache<Integer, List<Ayah>> surahCache = new LruCache<>(10);

    private QuranRepository(Context context) {
        QuranDatabase db = QuranDatabase.getInstance(context);
        ayahDao = db.ayahDao();
        translationDao = db.translationDao();
        tafseerDao = db.tafseerDao();
        wordByWordDao = db.wordByWordDao();
        bookmarkDao = db.bookmarkDao();
        knownWordDao = db.knownWordDao();
        editionInfoDao = db.editionInfoDao();
        reciterInfoDao = db.reciterInfoDao();
        readingProgressDao = db.readingProgressDao();
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
    public Ayah getAyah(int surah, int ayah) {
        String key = surah + ":" + ayah;
        Ayah cached = ayahCache.get(key);
        if (cached != null) return cached;
        Ayah result = ayahDao.getAyah(surah, ayah);
        if (result != null) ayahCache.put(key, result);
        return result;
    }

    public List<Ayah> getAyahsBySurah(int surah) {
        List<Ayah> cached = surahCache.get(surah);
        if (cached != null) return cached;
        List<Ayah> result = ayahDao.getAyahsBySurah(surah);
        if (result != null && !result.isEmpty()) {
            surahCache.put(surah, result);
            // Also populate individual ayah cache
            for (Ayah a : result) {
                ayahCache.put(a.surahNumber + ":" + a.ayahNumber, a);
            }
        }
        return result;
    }
    public List<Ayah> getAyahsByJuz(int juz) { return ayahDao.getAyahsByJuz(juz); }
    public int getAyahCount(int surah) { return ayahDao.getAyahCount(surah); }
    public int getTotalAyahCount() { return ayahDao.getTotalAyahCount(); }
    public List<AyahDao.SurahInfo> getAllSurahs() { return ayahDao.getAllSurahs(); }
    public Ayah getRandomAyah() { return ayahDao.getRandomAyah(); }
    public int getMaxAyahInSurah(int surah) { return ayahDao.getMaxAyahInSurah(surah); }

    // === Translation operations ===
    public void insertTranslations(List<Translation> translations) { translationDao.insertAll(translations); }
    public Translation getTranslation(int surah, int ayah, String edition) { return translationDao.getTranslation(surah, ayah, edition); }
    public List<Translation> getTranslationsBySurah(int surah, String edition) { return translationDao.getTranslationsBySurah(surah, edition); }
    public List<String> getAvailableTranslations() { return translationDao.getAvailableEditions(); }
    public List<String> getTranslationsByLanguage(String lang) { return translationDao.getEditionsByLanguage(lang); }
    public void deleteTranslation(String edition) { translationDao.deleteEdition(edition); }
    public int getTranslationCount(String edition) { return translationDao.getEditionCount(edition); }
    public long getTranslationTextSize(String edition) { return translationDao.getEditionTextSize(edition); }
    public int getTranslationSurahCount(String edition) { return translationDao.getDistinctSurahCount(edition); }

    // === Tafseer operations ===
    public void insertTafseers(List<Tafseer> tafseers) { tafseerDao.insertAll(tafseers); }
    public Tafseer getTafseer(int surah, int ayah, String edition) { return tafseerDao.getTafseer(surah, ayah, edition); }
    public List<Tafseer> getTafseersBySurah(int surah, String edition) { return tafseerDao.getTafseersBySurah(surah, edition); }
    public List<String> getAvailableTafseers() { return tafseerDao.getAvailableEditions(); }
    public List<String> getTafseersByLanguage(String lang) { return tafseerDao.getEditionsByLanguage(lang); }
    public void deleteTafseer(String edition) { tafseerDao.deleteEdition(edition); }
    public int getTafseerCount(String edition) { return tafseerDao.getEditionCount(edition); }
    public long getTafseerTextSize(String edition) { return tafseerDao.getEditionTextSize(edition); }
    public int getTafseerSurahCount(String edition) { return tafseerDao.getDistinctSurahCount(edition); }
    public List<Integer> getTafseerDownloadedSurahs(String edition) { return tafseerDao.getDownloadedSurahs(edition); }

    // === Word by Word operations ===
    public void insertWords(List<WordByWord> words) { wordByWordDao.insertAll(words); }
    public List<WordByWord> getWords(int surah, int ayah, String language) { return wordByWordDao.getWords(surah, ayah, language); }
    public List<String> getAvailableWbwLanguages() { return wordByWordDao.getAvailableLanguages(); }
    public List<com.tanxe.quran.data.dao.WordByWordDao.WordFrequency> getWords_frequencies(String language) { return wordByWordDao.getWordFrequencies(language); }
    public List<com.tanxe.quran.data.dao.WordByWordDao.WordFrequency> getWordFrequenciesBySurah(String language, int surah) { return wordByWordDao.getWordFrequenciesBySurah(language, surah); }
    public List<com.tanxe.quran.data.dao.WordByWordDao.WordWithTranslation> getWordsWithTranslations(String language) { return wordByWordDao.getWordsWithTranslations(language); }
    public List<com.tanxe.quran.data.dao.WordByWordDao.WordWithTranslation> getWordsWithTranslationsBySurah(String language, int surah) { return wordByWordDao.getWordsWithTranslationsBySurah(language, surah); }
    public List<com.tanxe.quran.data.dao.WordByWordDao.TranslationCount> getTranslationsForWord(String language, String arabicWord) { return wordByWordDao.getTranslationsForWord(language, arabicWord); }
    public void deleteWbw(String language) { wordByWordDao.deleteLanguage(language); }
    public int getWbwWordCount(String language) { return wordByWordDao.getWordCount(language); }
    public long getWbwTextSize(String language) { return wordByWordDao.getLanguageTextSize(language); }
    public int getWbwSurahCount(String language) { return wordByWordDao.getDistinctSurahCount(language); }
    public List<Integer> getWbwDownloadedSurahs(String language) { return wordByWordDao.getDownloadedSurahs(language); }

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
    public void clearAllKnownWords() { knownWordDao.deleteAll(); }
    public int getKnownWordCount() { return knownWordDao.getKnownCount(); }
    public int getTotalKnownFrequency() { return knownWordDao.getTotalKnownFrequency(); }

    // === Edition Catalog ===
    public void fetchAndCacheEditions() {
        try {
            QuranApiService api = ApiClient.getApiService();

            // Fetch translations
            Response<JsonObject> transResponse = api.getEditions("translation", "text").execute();
            if (transResponse.isSuccessful() && transResponse.body() != null) {
                parseAndInsertEditions(transResponse.body(), "translation");
            }

            // Fetch tafseers
            Response<JsonObject> tafResponse = api.getEditions("tafseer", "text").execute();
            if (tafResponse.isSuccessful() && tafResponse.body() != null) {
                parseAndInsertEditions(tafResponse.body(), "tafseer");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching editions", e);
        }
    }

    private void parseAndInsertEditions(JsonObject response, String type) {
        try {
            JsonArray data = response.getAsJsonArray("data");
            if (data == null) return;

            List<EditionInfo> editions = new ArrayList<>();
            for (JsonElement el : data) {
                JsonObject obj = el.getAsJsonObject();
                String identifier = obj.get("identifier").getAsString();
                String name = obj.get("name").getAsString();
                String language = obj.get("language").getAsString();
                String englishName = obj.has("englishName") ? obj.get("englishName").getAsString() : name;
                String direction = obj.has("direction") ? obj.get("direction").getAsString() : "ltr";

                EditionInfo edition = new EditionInfo(identifier, englishName, language, name, type, direction);

                // Check if fully downloaded (all 114 surahs present)
                if ("translation".equals(type)) {
                    int surahCount = translationDao.getDistinctSurahCount(identifier);
                    edition.isDownloaded = surahCount >= 114;
                } else {
                    int surahCount = tafseerDao.getDistinctSurahCount(identifier);
                    edition.isDownloaded = surahCount >= 114;
                }

                editions.add(edition);
            }
            editionInfoDao.insertAll(editions);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing editions", e);
        }
    }

    public List<EditionInfo> getEditionsByLanguage(String language) { return editionInfoDao.getByLanguage(language); }
    public List<EditionInfo> getDownloadedEditions() { return editionInfoDao.getDownloaded(); }
    public List<EditionInfo> getDownloadedEditionsByType(String type) { return editionInfoDao.getDownloadedByType(type); }
    public List<String> getAllAvailableLanguages() { return editionInfoDao.getAllLanguages(); }
    public List<EditionInfoDao.LanguageInfo> getAllLanguagesWithNames() { return editionInfoDao.getAllLanguagesWithNames(); }
    public List<EditionInfo> getAllEditions() { return editionInfoDao.getAll(); }
    public List<EditionInfo> getEditionsByType(String type) { return editionInfoDao.getByType(type); }
    public EditionInfo getEditionByIdentifier(String identifier) { return editionInfoDao.getByIdentifier(identifier); }
    public int getEditionCount() { return editionInfoDao.getCount(); }

    public void updateEditionDownloadState(String identifier, boolean downloaded, int progress) {
        editionInfoDao.updateDownloadState(identifier, downloaded, progress,
                downloaded ? System.currentTimeMillis() : 0);
    }

    // === Reciter operations ===
    public void initReciters() {
        List<ReciterInfo> existing = reciterInfoDao.getAll();
        if (existing == null || existing.isEmpty()) {
            reciterInfoDao.insertAll(ReciterCatalog.getReciters());
        }
    }

    public List<ReciterInfo> getAllReciters() { return reciterInfoDao.getAll(); }
    public ReciterInfo getReciterByIdentifier(String identifier) { return reciterInfoDao.getByIdentifier(identifier); }
    public void updateReciterDownloadState(String identifier, boolean downloaded) { reciterInfoDao.updateDownloadState(identifier, downloaded); }

    public String getSelectedReciter() { return prefs.getString("selected_reciter", "Alafasy_128kbps"); }
    public void saveSelectedReciter(String identifier) { prefs.edit().putString("selected_reciter", identifier).apply(); }

    public String getSelectedReciterName() {
        String id = getSelectedReciter();
        ReciterInfo info = reciterInfoDao.getByIdentifier(id);
        return info != null ? info.name : "Mishary Rashid Alafasy";
    }

    // === Reading Progress ===
    public void recordReadingSession(int surahNumber, int ayahNumber, int durationSeconds, String type) {
        readingProgressDao.insert(new ReadingProgress(surahNumber, ayahNumber, durationSeconds, type));
    }

    public int getTodayReadingMinutes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return readingProgressDao.getTodayReadTimeSeconds(cal.getTimeInMillis()) / 60;
    }

    public float getKhatmahPercentage() {
        int uniqueAyahs = readingProgressDao.getUniqueAyahsRead();
        return (uniqueAyahs * 100.0f) / 6236;
    }

    public int getReadingStreak() {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        return readingProgressDao.getReadingDaysCount(thirtyDaysAgo);
    }

    // === Daily Verse ===
    public Ayah getDailyVerse() {
        // Use date as seed for consistent daily verse
        Calendar cal = Calendar.getInstance();
        int seed = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);
        Random random = new Random(seed);
        int position = random.nextInt(6236);

        int remaining = position;
        for (int s = 0; s < com.tanxe.quran.util.QuranDataParser.SURAH_AYAH_COUNT.length; s++) {
            if (remaining < com.tanxe.quran.util.QuranDataParser.SURAH_AYAH_COUNT[s]) {
                return ayahDao.getAyah(s + 1, remaining + 1);
            }
            remaining -= com.tanxe.quran.util.QuranDataParser.SURAH_AYAH_COUNT[s];
        }
        return ayahDao.getRandomAyah();
    }

    // === Search ===
    public List<Ayah> searchAll(String query) { return ayahDao.searchAyahs(query); }
    public List<Ayah> searchArabic(String query) { return ayahDao.searchArabic(query); }
    public List<Ayah> searchTranslation(String query) { return ayahDao.searchTranslation(query); }
    public List<Translation> searchInTranslation(String edition, String query) { return translationDao.searchInEdition(edition, query); }
    public List<Tafseer> searchInTafseer(String edition, String query) { return tafseerDao.searchInEdition(edition, query); }

    // Search across all downloaded translations
    public List<Translation> searchAllTranslations(String query) {
        List<Translation> results = new ArrayList<>();
        List<String> editions = translationDao.getAvailableEditions();
        if (editions != null) {
            for (String edition : editions) {
                List<Translation> editionResults = translationDao.searchInEdition(edition, query);
                if (editionResults != null) results.addAll(editionResults);
            }
        }
        return results;
    }

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

    public void setPlaybackSpeed(float speed) { prefs.edit().putFloat("playback_speed", speed).apply(); }
    public float getPlaybackSpeed() { return prefs.getFloat("playback_speed", 1.0f); }

    public void setDailyReadingGoal(int minutes) { prefs.edit().putInt("daily_reading_goal", minutes).apply(); }
    public int getDailyReadingGoal() { return prefs.getInt("daily_reading_goal", 15); }

    public void setShowTranslation(boolean show) { prefs.edit().putBoolean("show_translation", show).apply(); }
    public boolean getShowTranslation() { return prefs.getBoolean("show_translation", true); }

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

    public boolean isEditionCatalogLoaded() { return prefs.getBoolean("edition_catalog_loaded", false); }
    public void setEditionCatalogLoaded(boolean loaded) { prefs.edit().putBoolean("edition_catalog_loaded", loaded).apply(); }

    public String getSelectedArabicFont() { return prefs.getString("arabic_font", "indopak.ttf"); }
    public void saveSelectedArabicFont(String fontFile) { prefs.edit().putString("arabic_font", fontFile).apply(); }
}
