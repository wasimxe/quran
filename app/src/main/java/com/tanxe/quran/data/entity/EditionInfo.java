package com.tanxe.quran.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "edition_info")
public class EditionInfo {
    @PrimaryKey
    @NonNull
    public String identifier; // e.g. "en.sahih"

    public String name;
    public String language;
    public String languageName;
    public String type; // "translation" or "tafseer"
    public String direction; // "ltr" or "rtl"
    public boolean isDownloaded;
    public int downloadProgress;
    public long downloadedAt;

    @Ignore
    public String sizeText; // transient - for UI display only (e.g. "145 MB")

    public EditionInfo() {}

    @Ignore
    public EditionInfo(@NonNull String identifier, String name, String language,
                       String languageName, String type, String direction) {
        this.identifier = identifier;
        this.name = name;
        this.language = language;
        this.languageName = languageName;
        this.type = type;
        this.direction = direction;
        this.isDownloaded = false;
        this.downloadProgress = 0;
        this.downloadedAt = 0;
    }
}
