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
}, version = 3, exportSchema = false)
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

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add performance indexes for fast lookups
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ayahs_surahNumber_ayahNumber` ON `ayahs` (`surahNumber`, `ayahNumber`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ayahs_surahNumber` ON `ayahs` (`surahNumber`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ayahs_juzNumber` ON `ayahs` (`juzNumber`)");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_translations_surahNumber_ayahNumber_edition` ON `translations` (`surahNumber`, `ayahNumber`, `edition`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_translations_edition_surahNumber` ON `translations` (`edition`, `surahNumber`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_translations_edition` ON `translations` (`edition`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_translations_language` ON `translations` (`language`)");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tafseers_surahNumber_ayahNumber_edition` ON `tafseers` (`surahNumber`, `ayahNumber`, `edition`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tafseers_edition_surahNumber` ON `tafseers` (`edition`, `surahNumber`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tafseers_edition` ON `tafseers` (`edition`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tafseers_language` ON `tafseers` (`language`)");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_word_by_word_surahNumber_ayahNumber_language` ON `word_by_word` (`surahNumber`, `ayahNumber`, `language`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_word_by_word_language_surahNumber` ON `word_by_word` (`language`, `surahNumber`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_word_by_word_language` ON `word_by_word` (`language`)");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_surahNumber_ayahNumber` ON `bookmarks` (`surahNumber`, `ayahNumber`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reading_progress_timestamp` ON `reading_progress` (`timestamp`)");
        }
    };

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

    // Direct migration 1→3 for users who never got version 2
    static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            MIGRATION_1_2.migrate(database);
            MIGRATION_2_3.migrate(database);
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                    .fallbackToDestructiveMigration()
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
