package com.tanxe.quran.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tafseers")
public class Tafseer {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int surahNumber;
    public int ayahNumber;
    public String text;
    public String edition; // e.g., "ur.ibnkathir"
    public String language;

    public Tafseer() {}

    @Ignore
    public Tafseer(int surahNumber, int ayahNumber, String text, String edition, String language) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.text = text;
        this.edition = edition;
        this.language = language;
    }
}
