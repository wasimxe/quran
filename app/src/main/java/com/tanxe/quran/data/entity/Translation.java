package com.tanxe.quran.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "translations")
public class Translation {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int surahNumber;
    public int ayahNumber;
    public String text;
    public String edition; // e.g., "ur.maududi", "en.sahih"
    public String language; // e.g., "ur", "en", "tr"

    public Translation() {}

    @Ignore
    public Translation(int surahNumber, int ayahNumber, String text, String edition, String language) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.text = text;
        this.edition = edition;
        this.language = language;
    }
}
