package com.tanxe.quran.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tanxe.quran.data.dao.*;
import com.tanxe.quran.data.entity.*;

@Database(entities = {Ayah.class, Translation.class, Tafseer.class, WordByWord.class, Bookmark.class, KnownWord.class}, version = 1, exportSchema = false)
public abstract class QuranDatabase extends RoomDatabase {
    private static volatile QuranDatabase INSTANCE;

    public abstract AyahDao ayahDao();
    public abstract TranslationDao translationDao();
    public abstract TafseerDao tafseerDao();
    public abstract WordByWordDao wordByWordDao();
    public abstract BookmarkDao bookmarkDao();
    public abstract KnownWordDao knownWordDao();

    public static QuranDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (QuranDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            QuranDatabase.class,
                            "quran_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
