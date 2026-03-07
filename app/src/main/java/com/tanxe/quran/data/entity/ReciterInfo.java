package com.tanxe.quran.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "reciter_info")
public class ReciterInfo {
    @PrimaryKey
    @NonNull
    public String identifier; // e.g. "Alafasy_128kbps"

    public String name;
    public String style;
    public String subfolder;
    public int bitrate;
    public boolean isDownloaded;

    public ReciterInfo() {}

    @Ignore
    public ReciterInfo(@NonNull String identifier, String name, String style,
                       String subfolder, int bitrate) {
        this.identifier = identifier;
        this.name = name;
        this.style = style;
        this.subfolder = subfolder;
        this.bitrate = bitrate;
        this.isDownloaded = false;
    }
}
