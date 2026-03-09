package com.tanxe.quran.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "reading_progress", indices = {
    @Index(value = {"timestamp"})
})
public class ReadingProgress {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int surahNumber;
    public int ayahNumber;
    public long timestamp;
    public int sessionDurationSeconds;
    public String type; // "read", "listen"

    public ReadingProgress() {}

    @Ignore
    public ReadingProgress(int surahNumber, int ayahNumber, int sessionDurationSeconds, String type) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.timestamp = System.currentTimeMillis();
        this.sessionDurationSeconds = sessionDurationSeconds;
        this.type = type;
    }
}
