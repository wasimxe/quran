package com.tanxe.quran.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "known_words")
public class KnownWord {
    @PrimaryKey
    @NonNull
    public String arabicWord;

    public int frequency; // how many times it appears in Quran
    public long learnedAt;

    public KnownWord() {
        this.arabicWord = "";
    }

    @Ignore
    public KnownWord(@NonNull String arabicWord, int frequency) {
        this.arabicWord = arabicWord;
        this.frequency = frequency;
        this.learnedAt = System.currentTimeMillis();
    }
}
