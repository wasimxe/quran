package com.tanxe.quran.download;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tanxe.quran.data.entity.Tafseer;
import com.tanxe.quran.data.entity.Translation;
import com.tanxe.quran.data.entity.WordByWord;
import com.tanxe.quran.data.repository.QuranRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final String QURAN_API = "https://api.alquran.cloud/v1/quran/";
    private static final String WBW_API = "https://api.quran.com/api/v4/verses/by_chapter/";
    private static final String AUDIO_BASE = "https://everyayah.com/data/";

    private static volatile DownloadManager instance;

    private final Context context;
    private final QuranRepository repository;
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;

    // Download control flags per edition
    private final Map<String, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> pauseFlags = new ConcurrentHashMap<>();

    public interface StatusCallback {
        void onStatus(String status);
    }

    public interface ProgressCallback {
        void onProgress(String edition, int percent, String status);
    }

    private static final int PARALLEL_DOWNLOADS = 8;

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DownloadManager(Context context) {
        this.context = context;
        this.repository = QuranRepository.getInstance(context);
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(16);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(16, 5, TimeUnit.MINUTES))
                .dispatcher(dispatcher)
                .build();
        this.gson = new Gson();
        this.executor = Executors.newFixedThreadPool(4);
    }


    public void cancelDownload(String edition) {
        AtomicBoolean flag = cancelFlags.get(edition);
        if (flag != null) flag.set(true);
    }

    public void pauseDownload(String edition) {
        AtomicBoolean flag = pauseFlags.get(edition);
        if (flag != null) flag.set(true);
    }

    public void resumeDownload(String edition) {
        AtomicBoolean flag = pauseFlags.get(edition);
        if (flag != null) flag.set(false);
    }

    public void downloadTranslation(String edition, String language, StatusCallback callback) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean paused = new AtomicBoolean(false);
        cancelFlags.put(edition, cancelled);
        pauseFlags.put(edition, paused);

        executor.execute(() -> {
            try {
                callback.onStatus("Downloading " + edition + "...");
                repository.setDownloadState(edition, "downloading");

                String url = QURAN_API + edition;
                String json = fetchUrl(url);
                if (json == null) {
                    callback.onStatus("Failed: network error");
                    repository.setDownloadState(edition, "none");
                    return;
                }

                if (cancelled.get()) { cleanup(edition); return; }

                JsonObject root = gson.fromJson(json, JsonObject.class);
                JsonObject data = root.getAsJsonObject("data");
                JsonArray surahs = data.getAsJsonArray("surahs");

                List<Translation> translations = new ArrayList<>();
                int surahCount = 0;

                for (JsonElement surahEl : surahs) {
                    if (cancelled.get()) { cleanup(edition); return; }

                    // Pause loop
                    while (paused.get() && !cancelled.get()) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }

                    JsonObject surah = surahEl.getAsJsonObject();
                    int surahNum = surah.get("number").getAsInt();
                    JsonArray ayahs = surah.getAsJsonArray("ayahs");

                    for (JsonElement ayahEl : ayahs) {
                        JsonObject ayah = ayahEl.getAsJsonObject();
                        int ayahNum = ayah.get("numberInSurah").getAsInt();
                        String text = ayah.get("text").getAsString();
                        translations.add(new Translation(surahNum, ayahNum, text, edition, language));
                    }

                    surahCount++;
                    int progress = (int) (surahCount * 100.0 / 114);
                    repository.setDownloadProgress(edition, progress);
                    callback.onStatus("Processing surah " + surahCount + "/114...");
                }

                // Insert in batches
                int batchSize = 500;
                for (int i = 0; i < translations.size(); i += batchSize) {
                    if (cancelled.get()) { cleanup(edition); return; }
                    int end = Math.min(i + batchSize, translations.size());
                    repository.insertTranslations(translations.subList(i, end));
                }

                // Verify completeness
                int actualSurahs = repository.getTranslationSurahCount(edition);
                int actualAyahs = repository.getTranslationCount(edition);
                long actualSize = repository.getTranslationTextSize(edition);
                if (actualSurahs >= 114) {
                    repository.setDownloadState(edition, "downloaded");
                    repository.setDownloadProgress(edition, 100);
                    repository.updateEditionDownloadState(edition, true, 100);
                    callback.onStatus("\u2713 " + edition + " downloaded (" + actualAyahs + " ayahs · " + formatBytes(actualSize) + ")");
                } else {
                    repository.setDownloadState(edition, "incomplete");
                    callback.onStatus("Incomplete: " + actualSurahs + "/114 surahs · " + actualAyahs + " ayahs — tap download to retry");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error downloading translation: " + edition, e);
                repository.setDownloadState(edition, "none");
                callback.onStatus("Failed: " + e.getMessage());
            } finally {
                cancelFlags.remove(edition);
                pauseFlags.remove(edition);
            }
        });
    }

    public void downloadTafseer(String edition, String language, StatusCallback callback) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean paused = new AtomicBoolean(false);
        cancelFlags.put(edition, cancelled);
        pauseFlags.put(edition, paused);

        executor.execute(() -> {
            try {
                callback.onStatus("Downloading tafseer " + edition + "...");
                repository.setDownloadState(edition, "downloading");

                // Use alquran.cloud API (same format as translations)
                String url = QURAN_API + edition;
                String json = fetchUrl(url);
                if (json == null) {
                    callback.onStatus("Failed: network error");
                    repository.setDownloadState(edition, "none");
                    return;
                }

                if (cancelled.get()) { cleanup(edition); return; }

                JsonObject root = gson.fromJson(json, JsonObject.class);
                JsonObject data = root.getAsJsonObject("data");
                JsonArray surahs = data.getAsJsonArray("surahs");

                List<Tafseer> allTafseers = new ArrayList<>();
                int surahCount = 0;

                for (JsonElement surahEl : surahs) {
                    if (cancelled.get()) { cleanup(edition); return; }

                    while (paused.get() && !cancelled.get()) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }

                    JsonObject surah = surahEl.getAsJsonObject();
                    int surahNum = surah.get("number").getAsInt();
                    JsonArray ayahs = surah.getAsJsonArray("ayahs");

                    for (JsonElement ayahEl : ayahs) {
                        JsonObject ayah = ayahEl.getAsJsonObject();
                        int ayahNum = ayah.get("numberInSurah").getAsInt();
                        String text = ayah.get("text").getAsString();
                        allTafseers.add(new Tafseer(surahNum, ayahNum, text, edition, language));
                    }

                    surahCount++;
                    int progress = (int) (surahCount * 100.0 / 114);
                    repository.setDownloadProgress(edition, progress);
                    callback.onStatus("Processing surah " + surahCount + "/114...");
                }

                // Insert in batches
                int batchSize = 500;
                for (int i = 0; i < allTafseers.size(); i += batchSize) {
                    if (cancelled.get()) { cleanup(edition); return; }
                    int end = Math.min(i + batchSize, allTafseers.size());
                    repository.insertTafseers(allTafseers.subList(i, end));
                }

                // Verify completeness
                int actualSurahs = repository.getTafseerSurahCount(edition);
                int actualAyahs = repository.getTafseerCount(edition);
                long actualSize = repository.getTafseerTextSize(edition);
                if (actualSurahs >= 114) {
                    repository.setDownloadState(edition, "downloaded");
                    repository.setDownloadProgress(edition, 100);
                    repository.updateEditionDownloadState(edition, true, 100);
                    callback.onStatus("\u2713 " + edition + " downloaded (" + actualAyahs + " ayahs · " + formatBytes(actualSize) + ")");
                } else {
                    repository.setDownloadState(edition, "incomplete");
                    callback.onStatus("Incomplete: " + actualSurahs + "/114 surahs · " + actualAyahs + " ayahs — tap download to retry");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error downloading tafseer", e);
                repository.setDownloadState(edition, "none");
                callback.onStatus("Failed: " + e.getMessage());
            } finally {
                cancelFlags.remove(edition);
                pauseFlags.remove(edition);
            }
        });
    }

    /** Fetch and parse WBW data for a single surah (all pages) */
    private List<WordByWord> fetchWbwForSurah(int surah, String language) {
        List<WordByWord> words = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            String url = WBW_API + surah + "?language=" + language + "&words=true&word_fields=text_uthmani&page=" + page + "&per_page=50";
            String json = fetchUrl(url);
            if (json == null) break;

            try {
                JsonObject root = gson.fromJson(json, JsonObject.class);
                JsonArray verses = root.getAsJsonArray("verses");
                if (verses == null || verses.size() == 0) break;

                for (JsonElement verseEl : verses) {
                    JsonObject verse = verseEl.getAsJsonObject();
                    String verseKey = verse.get("verse_key").getAsString();
                    String[] parts = verseKey.split(":");
                    int ayahNum = Integer.parseInt(parts[1]);

                    JsonArray wordsArr = verse.getAsJsonArray("words");
                    if (wordsArr == null) continue;

                    for (int w = 0; w < wordsArr.size(); w++) {
                        JsonObject word = wordsArr.get(w).getAsJsonObject();
                        String charType = word.has("char_type_name") ? word.get("char_type_name").getAsString() : "word";
                        if (!"word".equals(charType)) continue;

                        int pos = word.has("position") ? word.get("position").getAsInt() : w + 1;
                        String arabicWord = word.has("text_uthmani") ? word.get("text_uthmani").getAsString()
                                : (word.has("text") ? word.get("text").getAsString() : "");

                        String translation = "";
                        if (word.has("translation")) {
                            JsonObject trans = word.getAsJsonObject("translation");
                            translation = trans.has("text") ? trans.get("text").getAsString() : "";
                        }

                        String transliteration = "";
                        if (word.has("transliteration")) {
                            JsonObject translit = word.getAsJsonObject("transliteration");
                            transliteration = translit.has("text") ? translit.get("text").getAsString() : "";
                        }

                        WordByWord wbw = new WordByWord(surah, ayahNum, pos, arabicWord, translation, language);
                        wbw.transliteration = transliteration;
                        words.add(wbw);
                    }
                }

                JsonObject pagination = root.getAsJsonObject("pagination");
                if (pagination != null) {
                    int totalPages = pagination.has("total_pages") ? pagination.get("total_pages").getAsInt() : 1;
                    hasMore = page < totalPages;
                } else {
                    hasMore = false;
                }
                page++;

            } catch (Exception e) {
                Log.w(TAG, "Error parsing WBW surah " + surah + " page " + page, e);
                hasMore = false;
            }
        }
        return words;
    }

    public void downloadWordByWord(String language, StatusCallback callback) {
        String key = "wbw." + language;
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean paused = new AtomicBoolean(false);
        cancelFlags.put(key, cancelled);
        pauseFlags.put(key, paused);

        executor.execute(() -> {
            try {
                // Resume: find surahs already downloaded
                List<Integer> doneSurahs = repository.getWbwDownloadedSurahs(language);
                java.util.Set<Integer> doneSet = new java.util.HashSet<>();
                if (doneSurahs != null) doneSet.addAll(doneSurahs);

                List<Integer> pendingSurahs = new ArrayList<>();
                for (int s = 1; s <= 114; s++) {
                    if (!doneSet.contains(s)) pendingSurahs.add(s);
                }

                if (pendingSurahs.isEmpty()) {
                    repository.setDownloadState(key, "downloaded");
                    repository.setDownloadProgress(key, 100);
                    callback.onStatus("\u2713 Word-by-word (" + language + ") downloaded");
                    return;
                }

                int alreadyDone = doneSet.size();
                callback.onStatus("Downloading word-by-word (" + language + ")... (" + alreadyDone + "/114 already done)");
                repository.setDownloadState(key, "downloading");

                AtomicInteger completedSurahs = new AtomicInteger(alreadyDone);
                ExecutorService pool = Executors.newFixedThreadPool(PARALLEL_DOWNLOADS);

                int batchSize = PARALLEL_DOWNLOADS;
                for (int i = 0; i < pendingSurahs.size(); i += batchSize) {
                    if (cancelled.get()) { cleanup(key); pool.shutdownNow(); return; }

                    while (paused.get() && !cancelled.get()) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }

                    int end = Math.min(i + batchSize, pendingSurahs.size());
                    CountDownLatch latch = new CountDownLatch(end - i);
                    List<List<WordByWord>> batchResults = new ArrayList<>();
                    for (int k = 0; k < end - i; k++) batchResults.add(null);

                    for (int j = i; j < end; j++) {
                        final int surah = pendingSurahs.get(j);
                        final int idx = j - i;
                        pool.execute(() -> {
                            try {
                                if (!cancelled.get()) {
                                    List<WordByWord> words = fetchWbwForSurah(surah, language);
                                    synchronized (batchResults) {
                                        batchResults.set(idx, words);
                                    }
                                }
                            } finally {
                                int done = completedSurahs.incrementAndGet();
                                int progress = (int) (done * 100.0 / 114);
                                callback.onStatus("WBW: " + done + "/114 (" + progress + "%)");
                                latch.countDown();
                            }
                        });
                    }

                    latch.await();

                    // Insert batch results into DB
                    List<WordByWord> toInsert = new ArrayList<>();
                    for (List<WordByWord> result : batchResults) {
                        if (result != null) toInsert.addAll(result);
                    }
                    if (!toInsert.isEmpty()) {
                        repository.insertWords(toInsert);
                    }
                }

                pool.shutdown();

                if (!cancelled.get()) {
                    // Verify download completeness
                    int actualSurahs = repository.getWbwSurahCount(language);
                    int actualWords = repository.getWbwWordCount(language);
                    long actualSize = repository.getWbwTextSize(language);
                    if (actualSurahs >= 114) {
                        repository.setDownloadState(key, "downloaded");
                        repository.setDownloadProgress(key, 100);
                        callback.onStatus("\u2713 Word-by-word (" + language + ") downloaded (" + actualWords + " words · " + formatBytes(actualSize) + ")");
                    } else {
                        repository.setDownloadState(key, "incomplete");
                        repository.setDownloadProgress(key, (int)(actualSurahs * 100.0 / 114));
                        callback.onStatus("Incomplete: " + actualSurahs + "/114 surahs · " + actualWords + " words — tap download to resume");
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error downloading WBW", e);
                repository.setDownloadState(key, "none");
                callback.onStatus("Failed: " + e.getMessage());
            } finally {
                cancelFlags.remove(key);
                pauseFlags.remove(key);
            }
        });
    }

    /**
     * Download tafseer from quran.com API (supports more editions in multiple languages).
     * Uses tafsir resource IDs from quran.com.
     */
    /** Fetch tafseer for a single surah from quran.com (all pages) */
    private List<Tafseer> fetchTafseerForSurah(int surah, int resourceId, String editionKey, String language) {
        List<Tafseer> tafseers = new ArrayList<>();
        int page = 1;
        while (true) {
            String url = "https://api.quran.com/api/v4/tafsirs/" + resourceId
                    + "/by_chapter/" + surah + "?page=" + page;
            String json = fetchUrl(url);
            if (json == null) break;

            try {
                JsonObject root = gson.fromJson(json, JsonObject.class);
                JsonArray tafsirArr = root.getAsJsonArray("tafsirs");
                if (tafsirArr == null || tafsirArr.size() == 0) break;

                for (JsonElement el : tafsirArr) {
                    JsonObject t = el.getAsJsonObject();
                    String verseKey = t.get("verse_key").getAsString();
                    String[] parts = verseKey.split(":");
                    String ayahPart = parts[1];

                    String text = t.has("text") ? t.get("text").getAsString() : "";
                    text = text.replaceAll("<[^>]*>", "").trim();

                    if (ayahPart.contains("-")) {
                        String[] range = ayahPart.split("-");
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        for (int a = start; a <= end; a++) {
                            tafseers.add(new Tafseer(surah, a, text, editionKey, language));
                        }
                    } else {
                        int ayahNum = Integer.parseInt(ayahPart);
                        tafseers.add(new Tafseer(surah, ayahNum, text, editionKey, language));
                    }
                }

                JsonObject pagination = root.getAsJsonObject("pagination");
                if (pagination != null) {
                    int currentPage = pagination.get("current_page").getAsInt();
                    int totalPages = pagination.get("total_pages").getAsInt();
                    if (currentPage >= totalPages) break;
                } else {
                    break;
                }
                page++;
            } catch (Exception e) {
                Log.w(TAG, "Error parsing tafseer surah " + surah + " page " + page, e);
                break;
            }
        }
        return tafseers;
    }

    public void downloadTafseerFromQuranCom(String editionKey, int resourceId, String language, StatusCallback callback) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean paused = new AtomicBoolean(false);
        cancelFlags.put(editionKey, cancelled);
        pauseFlags.put(editionKey, paused);

        executor.execute(() -> {
            try {
                // Resume: find surahs that need (re)downloading
                // Note: quran.com tafseers may have fewer entries than ayah count (grouped entries).
                // Strategy: surahs with 0 entries = never downloaded; large surahs with very few
                // entries (< 50% expected) = likely partial download (pages missed).
                List<com.tanxe.quran.data.entity.SurahAyahCount> counts = repository.getTafseerAyahCountsBySurah(editionKey);
                java.util.Map<Integer, Integer> countMap = new java.util.HashMap<>();
                if (counts != null) {
                    for (com.tanxe.quran.data.entity.SurahAyahCount c : counts) countMap.put(c.surahNumber, c.cnt);
                }

                List<Integer> pendingSurahs = new ArrayList<>();
                int alreadyComplete = 0;
                for (int s = 1; s <= 114; s++) {
                    int expected = com.tanxe.quran.util.QuranDataParser.SURAH_AYAH_COUNT[s - 1];
                    Integer actual = countMap.get(s);
                    if (actual == null || actual == 0) {
                        // Never downloaded
                        pendingSurahs.add(s);
                    } else if (expected > 20 && actual < expected / 2) {
                        // Large surah with very few entries — likely missed pages during download
                        pendingSurahs.add(s);
                    } else {
                        alreadyComplete++;
                    }
                }

                if (pendingSurahs.isEmpty()) {
                    repository.setDownloadState(editionKey, "downloaded");
                    repository.setDownloadProgress(editionKey, 100);
                    repository.updateEditionDownloadState(editionKey, true, 100);
                    callback.onStatus("\u2713 Tafseer downloaded");
                    return;
                }

                // Count existing ayahs for progress reporting
                int initialAyahs = repository.getTafseerCount(editionKey);
                AtomicInteger totalAyahs = new AtomicInteger(initialAyahs);

                callback.onStatus("Downloading tafseer... (" + alreadyComplete + "/114 complete, " + pendingSurahs.size() + " remaining)");
                repository.setDownloadState(editionKey, "downloading");

                AtomicInteger completedSurahs = new AtomicInteger(alreadyComplete);
                ExecutorService pool = Executors.newFixedThreadPool(PARALLEL_DOWNLOADS);

                int batchSize = PARALLEL_DOWNLOADS;
                for (int i = 0; i < pendingSurahs.size(); i += batchSize) {
                    if (cancelled.get()) { cleanup(editionKey); pool.shutdownNow(); return; }

                    while (paused.get() && !cancelled.get()) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }

                    int end = Math.min(i + batchSize, pendingSurahs.size());
                    CountDownLatch latch = new CountDownLatch(end - i);
                    List<List<Tafseer>> batchResults = new ArrayList<>();
                    for (int k = 0; k < end - i; k++) batchResults.add(null);

                    for (int j = i; j < end; j++) {
                        final int surah = pendingSurahs.get(j);
                        final int idx = j - i;
                        pool.execute(() -> {
                            try {
                                if (!cancelled.get()) {
                                    List<Tafseer> result = fetchTafseerForSurah(surah, resourceId, editionKey, language);
                                    synchronized (batchResults) {
                                        batchResults.set(idx, result);
                                    }
                                }
                            } finally {
                                int done = completedSurahs.incrementAndGet();
                                int progress = (int) (done * 100.0 / 114);
                                callback.onStatus("Tafseer: " + done + "/114 (" + progress + "%) " + totalAyahs.get() + "/6236 ayahs");
                                latch.countDown();
                            }
                        });
                    }

                    latch.await();

                    // Insert batch results and update ayah count
                    List<Tafseer> toInsert = new ArrayList<>();
                    for (List<Tafseer> result : batchResults) {
                        if (result != null) toInsert.addAll(result);
                    }
                    if (!toInsert.isEmpty()) {
                        repository.insertTafseers(toInsert);
                        totalAyahs.addAndGet(toInsert.size());
                        // Report updated ayah count after batch insert
                        int done = completedSurahs.get();
                        int progress = (int) (done * 100.0 / 114);
                        callback.onStatus("Tafseer: " + done + "/114 (" + progress + "%) " + totalAyahs.get() + "/6236 ayahs");
                    }
                }

                pool.shutdown();

                if (!cancelled.get()) {
                    // Verify download completeness
                    int actualSurahs = repository.getTafseerSurahCount(editionKey);
                    int actualAyahs = repository.getTafseerCount(editionKey);
                    long actualSize = repository.getTafseerTextSize(editionKey);
                    // Consider complete if all 114 surahs have entries AND ayah count is reasonable
                    // (quran.com tafseers may have grouped entries, so total can be < 6236)
                    if (actualSurahs >= 114 && actualAyahs >= 5000) {
                        repository.setDownloadState(editionKey, "downloaded");
                        repository.setDownloadProgress(editionKey, 100);
                        repository.updateEditionDownloadState(editionKey, true, 100);
                        callback.onStatus("\u2713 Tafseer downloaded (" + actualAyahs + " ayahs · " + formatBytes(actualSize) + ")");
                    } else if (actualSurahs >= 114) {
                        // All surahs attempted but low ayah count — API may have fewer entries
                        // Mark as downloaded to avoid infinite retries
                        repository.setDownloadState(editionKey, "downloaded");
                        repository.setDownloadProgress(editionKey, 100);
                        repository.updateEditionDownloadState(editionKey, true, 100);
                        callback.onStatus("\u2713 Tafseer downloaded (" + actualAyahs + " ayahs · " + formatBytes(actualSize) + ")");
                    } else {
                        // Some surahs completely missing
                        repository.setDownloadState(editionKey, "incomplete");
                        repository.setDownloadProgress(editionKey, (int)(actualSurahs * 100.0 / 114));
                        callback.onStatus("Incomplete: " + actualSurahs + "/114 surahs · " + actualAyahs + " ayahs · " + formatBytes(actualSize) + " — tap download to resume");
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error downloading tafseer from quran.com", e);
                repository.setDownloadState(editionKey, "none");
                callback.onStatus("Failed: " + e.getMessage());
            } finally {
                cancelFlags.remove(editionKey);
                pauseFlags.remove(editionKey);
            }
        });
    }

    /**
     * Download audio for a surah with a specific reciter folder.
     */
    public void downloadAudioForSurah(int surah, String reciterFolder, StatusCallback callback) {
        executor.execute(() -> {
            try {
                File audioDir = new File(context.getFilesDir(), "audio/" + reciterFolder);
                if (!audioDir.exists()) audioDir.mkdirs();

                int ayahCount = com.tanxe.quran.util.QuranDataParser.SURAH_AYAH_COUNT[surah - 1];
                AtomicInteger completed = new AtomicInteger(0);

                // Build list of files to download
                List<String[]> toDownload = new ArrayList<>();
                for (int ayah = 1; ayah <= ayahCount; ayah++) {
                    String filename = String.format("%03d%03d.mp3", surah, ayah);
                    File file = new File(audioDir, filename);
                    if (file.exists() && file.length() > 0) {
                        completed.incrementAndGet();
                    } else {
                        toDownload.add(new String[]{
                                AUDIO_BASE + reciterFolder + "/" + filename,
                                file.getAbsolutePath()
                        });
                    }
                }

                if (!toDownload.isEmpty()) {
                    ExecutorService pool = Executors.newFixedThreadPool(PARALLEL_DOWNLOADS);
                    CountDownLatch latch = new CountDownLatch(toDownload.size());
                    for (String[] item : toDownload) {
                        pool.execute(() -> {
                            try {
                                downloadFile(item[0], new File(item[1]));
                            } catch (IOException e) {
                                Log.w(TAG, "Failed: " + item[0], e);
                            } finally {
                                int done = completed.incrementAndGet();
                                callback.onStatus("Audio: " + surah + " (" + done + "/" + ayahCount + ")");
                                latch.countDown();
                            }
                        });
                    }
                    latch.await();
                    pool.shutdown();
                }

                callback.onStatus("\u2713 Surah " + surah + " audio downloaded");

            } catch (Exception e) {
                Log.e(TAG, "Error downloading audio", e);
                callback.onStatus("Failed: " + e.getMessage());
            }
        });
    }

    /**
     * Legacy method - downloads with default reciter (Alafasy).
     */
    public void downloadAudioForSurah(int surah, StatusCallback callback) {
        downloadAudioForSurah(surah, "Alafasy_128kbps", callback);
    }

    /**
     * Download full Quran audio for a reciter (all 114 surahs) with parallel downloads.
     */
    public void downloadFullQuranAudio(String reciterFolder, StatusCallback callback) {
        String key = "audio_" + reciterFolder;
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean paused = new AtomicBoolean(false);
        cancelFlags.put(key, cancelled);
        pauseFlags.put(key, paused);

        executor.execute(() -> {
            try {
                File audioDir = new File(context.getFilesDir(), "audio/" + reciterFolder);
                if (!audioDir.exists()) audioDir.mkdirs();

                int totalAyahs = com.tanxe.quran.util.QuranDataParser.TOTAL_AYAHS;
                AtomicInteger completed = new AtomicInteger(0);
                AtomicBoolean hasError = new AtomicBoolean(false);
                java.util.concurrent.atomic.AtomicLong downloadedBytes = new java.util.concurrent.atomic.AtomicLong(0);

                // Count already downloaded and their sizes
                int alreadyDone = 0;
                long existingBytes = 0;
                // Build list of files to download
                List<String[]> toDownload = new ArrayList<>();
                for (int surah = 1; surah <= 114; surah++) {
                    int ayahCount = com.tanxe.quran.util.QuranDataParser.SURAH_AYAH_COUNT[surah - 1];
                    for (int ayah = 1; ayah <= ayahCount; ayah++) {
                        String filename = String.format("%03d%03d.mp3", surah, ayah);
                        File file = new File(audioDir, filename);
                        if (file.exists() && file.length() > 0) {
                            alreadyDone++;
                            existingBytes += file.length();
                        } else {
                            toDownload.add(new String[]{
                                    AUDIO_BASE + reciterFolder + "/" + filename,
                                    file.getAbsolutePath(),
                                    String.valueOf(surah)
                            });
                        }
                    }
                }
                completed.set(alreadyDone);
                downloadedBytes.set(existingBytes);

                if (toDownload.isEmpty()) {
                    repository.updateReciterDownloadState(reciterFolder, true);
                    callback.onStatus("\u2713 Full Quran audio downloaded");
                    return;
                }

                // Download in parallel batches
                ExecutorService downloadPool = new ThreadPoolExecutor(
                        PARALLEL_DOWNLOADS, PARALLEL_DOWNLOADS, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>());

                int batchSize = PARALLEL_DOWNLOADS * 2;
                for (int i = 0; i < toDownload.size(); i += batchSize) {
                    if (cancelled.get()) break;

                    while (paused.get() && !cancelled.get()) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }

                    int end = Math.min(i + batchSize, toDownload.size());
                    List<String[]> batch = toDownload.subList(i, end);
                    CountDownLatch latch = new CountDownLatch(batch.size());

                    for (String[] item : batch) {
                        downloadPool.execute(() -> {
                            try {
                                if (!cancelled.get()) {
                                    File f = new File(item[1]);
                                    downloadFile(item[0], f);
                                    downloadedBytes.addAndGet(f.length());
                                }
                            } catch (IOException e) {
                                Log.w(TAG, "Failed: " + item[0], e);
                                hasError.set(true);
                            } finally {
                                int done = completed.incrementAndGet();
                                int progress = (int) (done * 100.0 / totalAyahs);
                                long bytes = downloadedBytes.get();
                                callback.onStatus(done + "/" + totalAyahs
                                        + " (" + progress + "%) " + formatBytes(bytes));
                                latch.countDown();
                            }
                        });
                    }

                    latch.await();
                }

                downloadPool.shutdown();

                if (!cancelled.get()) {
                    repository.updateReciterDownloadState(reciterFolder, true);
                    callback.onStatus("\u2713 Full Quran audio downloaded");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error downloading full Quran audio", e);
                callback.onStatus("Failed: " + e.getMessage());
            } finally {
                cancelFlags.remove(key);
                pauseFlags.remove(key);
            }
        });
    }

    private void cleanup(String edition) {
        repository.setDownloadState(edition, "none");
        repository.setDownloadProgress(edition, 0);
        cancelFlags.remove(edition);
        pauseFlags.remove(edition);
    }

    /**
     * Download translation from fawazahmed0/quran-api (GitHub).
     * URL format: https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/{edition}/{surah}.json
     * JSON: {"chapter": [{"chapter": N, "verse": N, "text": "..."}]}
     */
    public void downloadTranslationFromGitHub(String editionKey, String ghEdition, String language, StatusCallback callback) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean paused = new AtomicBoolean(false);
        cancelFlags.put(editionKey, cancelled);
        pauseFlags.put(editionKey, paused);

        executor.execute(() -> {
            try {
                String baseUrl = "https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/" + ghEdition + "/";

                // Resume: check which surahs already downloaded
                int existingSurahs = repository.getTranslationSurahCount(editionKey);
                int existingAyahs = repository.getTranslationCount(editionKey);

                List<Integer> pendingSurahs = new ArrayList<>();
                int alreadyComplete = 0;
                for (int s = 1; s <= 114; s++) {
                    // Simple check: if we already have all ayahs, skip
                    // For resume, we re-download all pending surahs
                    pendingSurahs.add(s);
                }

                if (existingSurahs >= 114 && existingAyahs >= 6236) {
                    repository.setDownloadState(editionKey, "downloaded");
                    repository.setDownloadProgress(editionKey, 100);
                    repository.updateEditionDownloadState(editionKey, true, 100);
                    callback.onStatus("\u2713 Translation downloaded");
                    return;
                }

                AtomicInteger totalAyahs = new AtomicInteger(existingAyahs);
                AtomicInteger completedSurahs = new AtomicInteger(0);

                callback.onStatus("Downloading...");
                repository.setDownloadState(editionKey, "downloading");

                ExecutorService pool = Executors.newFixedThreadPool(PARALLEL_DOWNLOADS);

                int batchSize = PARALLEL_DOWNLOADS;
                for (int i = 0; i < pendingSurahs.size(); i += batchSize) {
                    if (cancelled.get()) { cleanup(editionKey); pool.shutdownNow(); return; }
                    while (paused.get() && !cancelled.get()) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }

                    int end = Math.min(i + batchSize, pendingSurahs.size());
                    CountDownLatch latch = new CountDownLatch(end - i);
                    List<List<Translation>> batchResults = new ArrayList<>();
                    for (int k = 0; k < end - i; k++) batchResults.add(null);

                    for (int j = i; j < end; j++) {
                        final int surah = pendingSurahs.get(j);
                        final int idx = j - i;
                        pool.execute(() -> {
                            try {
                                if (!cancelled.get()) {
                                    String json = fetchUrl(baseUrl + surah + ".json");
                                    if (json != null) {
                                        List<Translation> entries = new ArrayList<>();
                                        JsonObject root = gson.fromJson(json, JsonObject.class);
                                        JsonArray chapter = root.getAsJsonArray("chapter");
                                        if (chapter != null) {
                                            for (JsonElement el : chapter) {
                                                JsonObject v = el.getAsJsonObject();
                                                int ayahNum = v.get("verse").getAsInt();
                                                String text = v.has("text") ? v.get("text").getAsString() : "";
                                                entries.add(new Translation(surah, ayahNum, text, editionKey, language));
                                            }
                                        }
                                        synchronized (batchResults) {
                                            batchResults.set(idx, entries);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error downloading surah " + surah + " from GitHub", e);
                            } finally {
                                int done = completedSurahs.incrementAndGet();
                                int progress = (int) (done * 100.0 / 114);
                                callback.onStatus("Downloading: " + done + "/114 (" + progress + "%) " + totalAyahs.get() + "/6236 ayahs");
                                latch.countDown();
                            }
                        });
                    }

                    latch.await();

                    List<Translation> toInsert = new ArrayList<>();
                    for (List<Translation> result : batchResults) {
                        if (result != null) toInsert.addAll(result);
                    }
                    if (!toInsert.isEmpty()) {
                        repository.insertTranslations(toInsert);
                        totalAyahs.addAndGet(toInsert.size());
                    }
                }

                pool.shutdown();

                if (!cancelled.get()) {
                    int actualAyahs = repository.getTranslationCount(editionKey);
                    long actualSize = repository.getTranslationTextSize(editionKey);
                    int actualSurahs = repository.getTranslationSurahCount(editionKey);
                    if (actualSurahs >= 114 || actualAyahs >= 6236) {
                        repository.setDownloadState(editionKey, "downloaded");
                        repository.setDownloadProgress(editionKey, 100);
                        repository.updateEditionDownloadState(editionKey, true, 100);
                        callback.onStatus("\u2713 Downloaded (" + actualAyahs + " ayahs · " + formatBytes(actualSize) + ")");
                    } else {
                        repository.setDownloadState(editionKey, "incomplete");
                        callback.onStatus("Incomplete: " + actualSurahs + "/114 surahs · " + actualAyahs + "/6236 ayahs — tap to resume");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error downloading translation from GitHub", e);
                repository.setDownloadState(editionKey, "none");
                callback.onStatus("Failed: " + e.getMessage());
            } finally {
                cancelFlags.remove(editionKey);
                pauseFlags.remove(editionKey);
            }
        });
    }

    private String fetchUrl(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching: " + url, e);
        }
        return null;
    }

    private void downloadFile(String url, File outputFile) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                try (InputStream is = response.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[16384];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }

    static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
