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

    /** true = Medinan (Madani), false = Meccan (Makki). Index 0 unused, 1-114. */
    public static final boolean[] IS_MEDINAN = {
        false, // 0 (unused)
        false, // 1 Al-Fatiha
        true,  // 2 Al-Baqarah
        true,  // 3 Aal-Imran
        true,  // 4 An-Nisa
        true,  // 5 Al-Ma'idah
        false, // 6 Al-An'am
        false, // 7 Al-A'raf
        true,  // 8 Al-Anfal
        true,  // 9 At-Tawbah
        false, // 10 Yunus
        false, // 11 Hud
        false, // 12 Yusuf
        true,  // 13 Ar-Ra'd
        false, // 14 Ibrahim
        false, // 15 Al-Hijr
        false, // 16 An-Nahl
        false, // 17 Al-Isra
        false, // 18 Al-Kahf
        false, // 19 Maryam
        false, // 20 Ta-Ha
        false, // 21 Al-Anbiya
        true,  // 22 Al-Hajj
        false, // 23 Al-Mu'minun
        true,  // 24 An-Nur
        false, // 25 Al-Furqan
        false, // 26 Ash-Shu'ara
        false, // 27 An-Naml
        false, // 28 Al-Qasas
        false, // 29 Al-Ankabut
        false, // 30 Ar-Rum
        false, // 31 Luqman
        false, // 32 As-Sajdah
        true,  // 33 Al-Ahzab
        false, // 34 Saba
        false, // 35 Fatir
        false, // 36 Ya-Sin
        false, // 37 As-Saffat
        false, // 38 Sad
        false, // 39 Az-Zumar
        false, // 40 Ghafir
        false, // 41 Fussilat
        false, // 42 Ash-Shura
        false, // 43 Az-Zukhruf
        false, // 44 Ad-Dukhan
        false, // 45 Al-Jathiyah
        false, // 46 Al-Ahqaf
        true,  // 47 Muhammad
        true,  // 48 Al-Fath
        true,  // 49 Al-Hujurat
        false, // 50 Qaf
        false, // 51 Adh-Dhariyat
        false, // 52 At-Tur
        false, // 53 An-Najm
        false, // 54 Al-Qamar
        true,  // 55 Ar-Rahman
        false, // 56 Al-Waqi'ah
        true,  // 57 Al-Hadid
        true,  // 58 Al-Mujadilah
        true,  // 59 Al-Hashr
        true,  // 60 Al-Mumtahanah
        true,  // 61 As-Saff
        true,  // 62 Al-Jumu'ah
        true,  // 63 Al-Munafiqun
        true,  // 64 At-Taghabun
        true,  // 65 At-Talaq
        true,  // 66 At-Tahrim
        false, // 67 Al-Mulk
        false, // 68 Al-Qalam
        false, // 69 Al-Haqqah
        false, // 70 Al-Ma'arij
        false, // 71 Nuh
        false, // 72 Al-Jinn
        false, // 73 Al-Muzzammil
        false, // 74 Al-Muddaththir
        false, // 75 Al-Qiyamah
        true,  // 76 Al-Insan
        false, // 77 Al-Mursalat
        false, // 78 An-Naba
        false, // 79 An-Nazi'at
        false, // 80 Abasa
        false, // 81 At-Takwir
        false, // 82 Al-Infitar
        false, // 83 Al-Mutaffifin
        false, // 84 Al-Inshiqaq
        false, // 85 Al-Buruj
        false, // 86 At-Tariq
        false, // 87 Al-A'la
        false, // 88 Al-Ghashiyah
        false, // 89 Al-Fajr
        false, // 90 Al-Balad
        false, // 91 Ash-Shams
        false, // 92 Al-Layl
        false, // 93 Ad-Duha
        false, // 94 Ash-Sharh
        false, // 95 At-Tin
        false, // 96 Al-Alaq
        false, // 97 Al-Qadr
        true,  // 98 Al-Bayyinah
        true,  // 99 Az-Zalzalah
        false, // 100 Al-Adiyat
        false, // 101 Al-Qari'ah
        false, // 102 At-Takathur
        false, // 103 Al-Asr
        false, // 104 Al-Humazah
        false, // 105 Al-Fil
        false, // 106 Quraysh
        false, // 107 Al-Ma'un
        false, // 108 Al-Kawthar
        false, // 109 Al-Kafirun
        true,  // 110 An-Nasr
        false, // 111 Al-Masad
        false, // 112 Al-Ikhlas
        false, // 113 Al-Falaq
        false  // 114 An-Nas
    };
}
