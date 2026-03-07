package com.tanxe.quran.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tanxe.quran.data.dao.*;
import com.tanxe.quran.data.entity.*;

@Database(entities = {
        Ayah.class, Translation.class, Tafseer.class, WordByWord.class,
        Bookmark.class, KnownWord.class, EditionInfo.class, ReciterInfo.class,
        ReadingProgress.class
}, version = 2, exportSchema = false)
public abstract class QuranDatabase extends RoomDatabase {
    private static volatile QuranDatabase INSTANCE;

    public abstract AyahDao ayahDao();
    public abstract TranslationDao translationDao();
    public abstract TafseerDao tafseerDao();
    public abstract WordByWordDao wordByWordDao();
    public abstract BookmarkDao bookmarkDao();
    public abstract KnownWordDao knownWordDao();
    public abstract EditionInfoDao editionInfoDao();
    public abstract ReciterInfoDao reciterInfoDao();
    public abstract ReadingProgressDao readingProgressDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `edition_info` ("
                    + "`identifier` TEXT NOT NULL, "
                    + "`name` TEXT, "
                    + "`language` TEXT, "
                    + "`languageName` TEXT, "
                    + "`type` TEXT, "
                    + "`direction` TEXT, "
                    + "`isDownloaded` INTEGER NOT NULL DEFAULT 0, "
                    + "`downloadProgress` INTEGER NOT NULL DEFAULT 0, "
                    + "`downloadedAt` INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY(`identifier`))");

            database.execSQL("CREATE TABLE IF NOT EXISTS `reciter_info` ("
                    + "`identifier` TEXT NOT NULL, "
                    + "`name` TEXT, "
                    + "`style` TEXT, "
                    + "`subfolder` TEXT, "
                    + "`bitrate` INTEGER NOT NULL DEFAULT 0, "
                    + "`isDownloaded` INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY(`identifier`))");

            database.execSQL("CREATE TABLE IF NOT EXISTS `reading_progress` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`surahNumber` INTEGER NOT NULL DEFAULT 0, "
                    + "`ayahNumber` INTEGER NOT NULL DEFAULT 0, "
                    + "`timestamp` INTEGER NOT NULL DEFAULT 0, "
                    + "`sessionDurationSeconds` INTEGER NOT NULL DEFAULT 0, "
                    + "`type` TEXT)");
        }
    };

    public static QuranDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (QuranDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            QuranDatabase.class,
                            "quran_database"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
