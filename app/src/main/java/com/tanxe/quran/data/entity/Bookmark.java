package com.tanxe.quran.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarks")
public class Bookmark {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int surahNumber;
    public int ayahNumber;
    public String surahName;
    public String note;
    public long timestamp;

    public Bookmark() {}

    @Ignore
    public Bookmark(int surahNumber, int ayahNumber, String surahName, String note) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.surahName = surahName;
        this.note = note;
        this.timestamp = System.currentTimeMillis();
    }
}
