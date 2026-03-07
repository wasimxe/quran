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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final String QURAN_API = "https://api.alquran.cloud/v1/quran/";
    private static final String WBW_API = "https://api.quran.com/api/v4/verses/by_chapter/";
    private static final String AUDIO_BASE = "https://everyayah.com/data/";

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

    public DownloadManager(Context context) {
        this.context = context;
        this.repository = QuranRepository.getInstance(context);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.executor = Executors.newFixedThreadPool(2);
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

                repository.setDownloadState(edition, "downloaded");
                repository.setDownloadProgress(edition, 100);
                repository.updateEditionDownloadState(edition, true, 100);
                callback.onStatus("\u2713 " + edition + " downloaded (" + translations.size() + " ayahs)");

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

                repository.setDownloadState(edition, "downloaded");
                repository.setDownloadProgress(edition, 100);
                repository.updateEditionDownloadState(edition, true, 100);
                callback.onStatus("\u2713 " + edition + " tafseer downloaded (" + allTafseers.size() + " ayahs)");

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

    public void downloadWordByWord(String language, StatusCallback callback) {
        String key = "wbw." + language;
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean paused = new AtomicBoolean(false);
        cancelFlags.put(key, cancelled);
        pauseFlags.put(key, paused);

        executor.execute(() -> {
            try {
                callback.onStatus("Downloading word-by-word (" + language + ")...");
                repository.setDownloadState(key, "downloading");

                List<WordByWord> allWords = new ArrayList<>();

                for (int surah = 1; surah <= 114; surah++) {
                    if (cancelled.get()) { cleanup(key); return; }

                    while (paused.get() && !cancelled.get()) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }

                    callback.onStatus("WBW: Surah " + surah + "/114...");
                    int progress = (int) (surah * 100.0 / 114);
                    repository.setDownloadProgress(key, progress);

                    int page = 1;
                    boolean hasMore = true;

                    while (hasMore) {
                        if (cancelled.get()) { cleanup(key); return; }

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

                                JsonArray words = verse.getAsJsonArray("words");
                                if (words == null) continue;

                                for (int w = 0; w < words.size(); w++) {
                                    JsonObject word = words.get(w).getAsJsonObject();
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
                                    allWords.add(wbw);
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

                    // Insert per surah to avoid memory issues
                    if (allWords.size() > 5000) {
                        repository.insertWords(new ArrayList<>(allWords));
                        allWords.clear();
                    }
                }

                if (!allWords.isEmpty()) {
                    repository.insertWords(allWords);
                }

                repository.setDownloadState(key, "downloaded");
                repository.setDownloadProgress(key, 100);
                callback.onStatus("\u2713 Word-by-word (" + language + ") downloaded");

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
    public void downloadTafseerFromQuranCom(String editionKey, int resourceId, String language, StatusCallback callback) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean paused = new AtomicBoolean(false);
        cancelFlags.put(editionKey, cancelled);
        pauseFlags.put(editionKey, paused);

        executor.execute(() -> {
            try {
                callback.onStatus("Downloading tafseer...");
                repository.setDownloadState(editionKey, "downloading");

                List<Tafseer> allTafseers = new ArrayList<>();

                for (int surah = 1; surah <= 114; surah++) {
                    if (cancelled.get()) { cleanup(editionKey); return; }

                    while (paused.get() && !cancelled.get()) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }

                    callback.onStatus("Downloading surah " + surah + "/114...");
                    int progress = (int) (surah * 100.0 / 114);
                    repository.setDownloadProgress(editionKey, progress);

                    String url = "https://api.quran.com/api/v4/tafsirs/" + resourceId + "/by_chapter/" + surah;
                    String json = fetchUrl(url);
                    if (json == null) continue;

                    try {
                        JsonObject root = gson.fromJson(json, JsonObject.class);
                        JsonArray tafsirs = root.getAsJsonArray("tafsirs");
                        if (tafsirs == null) continue;

                        for (JsonElement el : tafsirs) {
                            JsonObject t = el.getAsJsonObject();
                            String verseKey = t.get("verse_key").getAsString();
                            String[] parts = verseKey.split(":");
                            String ayahPart = parts[1];

                            // Strip HTML tags from text
                            String text = t.has("text") ? t.get("text").getAsString() : "";
                            text = text.replaceAll("<[^>]*>", "").trim();

                            // Handle verse ranges like "2:1-5"
                            if (ayahPart.contains("-")) {
                                String[] range = ayahPart.split("-");
                                int start = Integer.parseInt(range[0]);
                                int end = Integer.parseInt(range[1]);
                                for (int a = start; a <= end; a++) {
                                    allTafseers.add(new Tafseer(surah, a, text, editionKey, language));
                                }
                            } else {
                                int ayahNum = Integer.parseInt(ayahPart);
                                allTafseers.add(new Tafseer(surah, ayahNum, text, editionKey, language));
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing tafseer surah " + surah, e);
                    }
                }

                // Insert in batches
                int batchSize = 500;
                for (int i = 0; i < allTafseers.size(); i += batchSize) {
                    if (cancelled.get()) { cleanup(editionKey); return; }
                    int end = Math.min(i + batchSize, allTafseers.size());
                    repository.insertTafseers(allTafseers.subList(i, end));
                }

                repository.setDownloadState(editionKey, "downloaded");
                repository.setDownloadProgress(editionKey, 100);
                repository.updateEditionDownloadState(editionKey, true, 100);
                callback.onStatus("\u2713 Tafseer downloaded (" + allTafseers.size() + " ayahs)");

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

                for (int ayah = 1; ayah <= ayahCount; ayah++) {
                    String filename = String.format("%03d%03d.mp3", surah, ayah);
                    File file = new File(audioDir, filename);

                    if (file.exists() && file.length() > 0) {
                        callback.onStatus("Skipping " + filename + " (exists)");
                        continue;
                    }

                    callback.onStatus("Audio: " + surah + ":" + ayah + "/" + ayahCount);

                    String url = AUDIO_BASE + reciterFolder + "/" + filename;
                    downloadFile(url, file);
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

    private void cleanup(String edition) {
        repository.setDownloadState(edition, "none");
        repository.setDownloadProgress(edition, 0);
        cancelFlags.remove(edition);
        pauseFlags.remove(edition);
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
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }
}
