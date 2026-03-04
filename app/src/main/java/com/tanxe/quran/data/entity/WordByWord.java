package com.tanxe.quran.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "word_by_word")
public class WordByWord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int surahNumber;
    public int ayahNumber;
    public int wordPosition;
    public String arabicWord;
    public String translation;
    public String transliteration;
    public String language; // "ur", "en"

    public WordByWord() {}

    @Ignore
    public WordByWord(int surahNumber, int ayahNumber, int wordPosition,
                      String arabicWord, String translation, String language) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.wordPosition = wordPosition;
        this.arabicWord = arabicWord;
        this.translation = translation;
        this.language = language;
    }
}
