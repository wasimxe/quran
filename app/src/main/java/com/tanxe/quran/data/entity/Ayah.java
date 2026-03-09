package com.tanxe.quran.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "ayahs", indices = {
    @Index(value = {"surahNumber", "ayahNumber"}),
    @Index(value = {"surahNumber"}),
    @Index(value = {"juzNumber"})
})
public class Ayah {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int surahNumber;
    public int ayahNumber;
    public String surahNameEn;
    public String surahNameAr;
    public String arabicText;
    public String defaultTranslation; // Urdu Jalandhry (built-in)
    public int juzNumber;

    public Ayah() {}

    @Ignore
    public Ayah(int surahNumber, int ayahNumber, String surahNameEn, String surahNameAr,
                String arabicText, String defaultTranslation) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.surahNameEn = surahNameEn;
        this.surahNameAr = surahNameAr;
        this.arabicText = arabicText;
        this.defaultTranslation = defaultTranslation;
        this.juzNumber = calculateJuz(surahNumber, ayahNumber);
    }

    public static int calculateJuz(int surah, int ayah) {
        // Juz boundaries (surah:ayah that starts each juz)
        int[][] juzStarts = {
            {1,1}, {2,142}, {2,253}, {3,93}, {4,24}, {4,148}, {5,82}, {6,111},
            {7,88}, {8,41}, {9,93}, {11,6}, {12,53}, {15,1}, {17,1}, {18,75},
            {21,1}, {23,1}, {25,21}, {27,56}, {29,46}, {33,31}, {36,28}, {39,32},
            {41,47}, {46,1}, {51,31}, {58,1}, {67,1}, {78,1}
        };
        for (int i = juzStarts.length - 1; i >= 0; i--) {
            if (surah > juzStarts[i][0] || (surah == juzStarts[i][0] && ayah >= juzStarts[i][1])) {
                return i + 1;
            }
        }
        return 1;
    }

    public String getReference() {
        return surahNumber + ":" + ayahNumber;
    }
}
