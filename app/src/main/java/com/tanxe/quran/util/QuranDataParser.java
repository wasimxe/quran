package com.tanxe.quran.util;

import android.content.Context;
import android.util.Log;

import com.tanxe.quran.data.entity.Ayah;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuranDataParser {
    private static final String TAG = "QuranDataParser";

    public static List<Ayah> parseQuranTsv(Context context) {
        List<Ayah> ayahs = new ArrayList<>();
        try (InputStream is = context.getAssets().open("quran_full.tsv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = stripBom(line).trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\t", -1);
                if (parts.length < 6) continue;

                try {
                    int surahNum = Integer.parseInt(parts[0].trim());
                    int ayahNum = Integer.parseInt(parts[1].trim());
                    String nameEn = parts[2].trim();
                    String nameAr = parts[3].trim();
                    String arabicText = stripInvisibleChars(stripBom(parts[4])).trim();
                    String urduTranslation = parts[5].trim();

                    Ayah ayah = new Ayah(surahNum, ayahNum, nameEn, nameAr, arabicText, urduTranslation);
                    ayahs.add(ayah);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Skipping invalid line: " + line.substring(0, Math.min(50, line.length())));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading quran_full.tsv", e);
        }

        Log.i(TAG, "Parsed " + ayahs.size() + " ayahs from TSV");
        return ayahs;
    }

    private static String stripBom(String s) {
        if (s != null && s.length() > 0 && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }

    /** Strip invisible Unicode control chars that break Arabic text rendering */
    private static String stripInvisibleChars(String s) {
        if (s == null) return s;
        return s.replace("\u200F", "")  // Right-to-Left Mark
                .replace("\u200B", "")  // Zero-Width Space
                .replace("\u200E", "")  // Left-to-Right Mark
                .replace("\uFEFF", ""); // BOM / Zero-Width No-Break Space
    }

    // Surah info: number of ayahs per surah
    public static final int[] SURAH_AYAH_COUNT = {
        7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
        123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
        112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
        34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
        54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
        60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
        14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
        28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
        29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
        15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
        11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
        5, 4, 5, 6
    };

    public static final int TOTAL_SURAHS = 114;
    public static final int TOTAL_AYAHS = 6236;
}
