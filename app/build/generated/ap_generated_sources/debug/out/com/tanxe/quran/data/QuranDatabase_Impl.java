package com.tanxe.quran.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.tanxe.quran.data.dao.AyahDao;
import com.tanxe.quran.data.dao.AyahDao_Impl;
import com.tanxe.quran.data.dao.BookmarkDao;
import com.tanxe.quran.data.dao.BookmarkDao_Impl;
import com.tanxe.quran.data.dao.KnownWordDao;
import com.tanxe.quran.data.dao.KnownWordDao_Impl;
import com.tanxe.quran.data.dao.TafseerDao;
import com.tanxe.quran.data.dao.TafseerDao_Impl;
import com.tanxe.quran.data.dao.TranslationDao;
import com.tanxe.quran.data.dao.TranslationDao_Impl;
import com.tanxe.quran.data.dao.WordByWordDao;
import com.tanxe.quran.data.dao.WordByWordDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class QuranDatabase_Impl extends QuranDatabase {
  private volatile AyahDao _ayahDao;

  private volatile TranslationDao _translationDao;

  private volatile TafseerDao _tafseerDao;

  private volatile WordByWordDao _wordByWordDao;

  private volatile BookmarkDao _bookmarkDao;

  private volatile KnownWordDao _knownWordDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `ayahs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `surahNumber` INTEGER NOT NULL, `ayahNumber` INTEGER NOT NULL, `surahNameEn` TEXT, `surahNameAr` TEXT, `arabicText` TEXT, `defaultTranslation` TEXT, `juzNumber` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `translations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `surahNumber` INTEGER NOT NULL, `ayahNumber` INTEGER NOT NULL, `text` TEXT, `edition` TEXT, `language` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `tafseers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `surahNumber` INTEGER NOT NULL, `ayahNumber` INTEGER NOT NULL, `text` TEXT, `edition` TEXT, `language` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_by_word` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `surahNumber` INTEGER NOT NULL, `ayahNumber` INTEGER NOT NULL, `wordPosition` INTEGER NOT NULL, `arabicWord` TEXT, `translation` TEXT, `transliteration` TEXT, `language` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `bookmarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `surahNumber` INTEGER NOT NULL, `ayahNumber` INTEGER NOT NULL, `surahName` TEXT, `note` TEXT, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `known_words` (`arabicWord` TEXT NOT NULL, `frequency` INTEGER NOT NULL, `learnedAt` INTEGER NOT NULL, PRIMARY KEY(`arabicWord`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '800ba0749cfbdead1232935ddd459e1f')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `ayahs`");
        db.execSQL("DROP TABLE IF EXISTS `translations`");
        db.execSQL("DROP TABLE IF EXISTS `tafseers`");
        db.execSQL("DROP TABLE IF EXISTS `word_by_word`");
        db.execSQL("DROP TABLE IF EXISTS `bookmarks`");
        db.execSQL("DROP TABLE IF EXISTS `known_words`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAyahs = new HashMap<String, TableInfo.Column>(8);
        _columnsAyahs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAyahs.put("surahNumber", new TableInfo.Column("surahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAyahs.put("ayahNumber", new TableInfo.Column("ayahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAyahs.put("surahNameEn", new TableInfo.Column("surahNameEn", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAyahs.put("surahNameAr", new TableInfo.Column("surahNameAr", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAyahs.put("arabicText", new TableInfo.Column("arabicText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAyahs.put("defaultTranslation", new TableInfo.Column("defaultTranslation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAyahs.put("juzNumber", new TableInfo.Column("juzNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAyahs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAyahs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAyahs = new TableInfo("ayahs", _columnsAyahs, _foreignKeysAyahs, _indicesAyahs);
        final TableInfo _existingAyahs = TableInfo.read(db, "ayahs");
        if (!_infoAyahs.equals(_existingAyahs)) {
          return new RoomOpenHelper.ValidationResult(false, "ayahs(com.tanxe.quran.data.entity.Ayah).\n"
                  + " Expected:\n" + _infoAyahs + "\n"
                  + " Found:\n" + _existingAyahs);
        }
        final HashMap<String, TableInfo.Column> _columnsTranslations = new HashMap<String, TableInfo.Column>(6);
        _columnsTranslations.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranslations.put("surahNumber", new TableInfo.Column("surahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranslations.put("ayahNumber", new TableInfo.Column("ayahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranslations.put("text", new TableInfo.Column("text", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranslations.put("edition", new TableInfo.Column("edition", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranslations.put("language", new TableInfo.Column("language", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTranslations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTranslations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTranslations = new TableInfo("translations", _columnsTranslations, _foreignKeysTranslations, _indicesTranslations);
        final TableInfo _existingTranslations = TableInfo.read(db, "translations");
        if (!_infoTranslations.equals(_existingTranslations)) {
          return new RoomOpenHelper.ValidationResult(false, "translations(com.tanxe.quran.data.entity.Translation).\n"
                  + " Expected:\n" + _infoTranslations + "\n"
                  + " Found:\n" + _existingTranslations);
        }
        final HashMap<String, TableInfo.Column> _columnsTafseers = new HashMap<String, TableInfo.Column>(6);
        _columnsTafseers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTafseers.put("surahNumber", new TableInfo.Column("surahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTafseers.put("ayahNumber", new TableInfo.Column("ayahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTafseers.put("text", new TableInfo.Column("text", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTafseers.put("edition", new TableInfo.Column("edition", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTafseers.put("language", new TableInfo.Column("language", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTafseers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTafseers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTafseers = new TableInfo("tafseers", _columnsTafseers, _foreignKeysTafseers, _indicesTafseers);
        final TableInfo _existingTafseers = TableInfo.read(db, "tafseers");
        if (!_infoTafseers.equals(_existingTafseers)) {
          return new RoomOpenHelper.ValidationResult(false, "tafseers(com.tanxe.quran.data.entity.Tafseer).\n"
                  + " Expected:\n" + _infoTafseers + "\n"
                  + " Found:\n" + _existingTafseers);
        }
        final HashMap<String, TableInfo.Column> _columnsWordByWord = new HashMap<String, TableInfo.Column>(8);
        _columnsWordByWord.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordByWord.put("surahNumber", new TableInfo.Column("surahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordByWord.put("ayahNumber", new TableInfo.Column("ayahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordByWord.put("wordPosition", new TableInfo.Column("wordPosition", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordByWord.put("arabicWord", new TableInfo.Column("arabicWord", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordByWord.put("translation", new TableInfo.Column("translation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordByWord.put("transliteration", new TableInfo.Column("transliteration", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordByWord.put("language", new TableInfo.Column("language", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWordByWord = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWordByWord = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWordByWord = new TableInfo("word_by_word", _columnsWordByWord, _foreignKeysWordByWord, _indicesWordByWord);
        final TableInfo _existingWordByWord = TableInfo.read(db, "word_by_word");
        if (!_infoWordByWord.equals(_existingWordByWord)) {
          return new RoomOpenHelper.ValidationResult(false, "word_by_word(com.tanxe.quran.data.entity.WordByWord).\n"
                  + " Expected:\n" + _infoWordByWord + "\n"
                  + " Found:\n" + _existingWordByWord);
        }
        final HashMap<String, TableInfo.Column> _columnsBookmarks = new HashMap<String, TableInfo.Column>(6);
        _columnsBookmarks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarks.put("surahNumber", new TableInfo.Column("surahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarks.put("ayahNumber", new TableInfo.Column("ayahNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarks.put("surahName", new TableInfo.Column("surahName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarks.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarks.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBookmarks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBookmarks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBookmarks = new TableInfo("bookmarks", _columnsBookmarks, _foreignKeysBookmarks, _indicesBookmarks);
        final TableInfo _existingBookmarks = TableInfo.read(db, "bookmarks");
        if (!_infoBookmarks.equals(_existingBookmarks)) {
          return new RoomOpenHelper.ValidationResult(false, "bookmarks(com.tanxe.quran.data.entity.Bookmark).\n"
                  + " Expected:\n" + _infoBookmarks + "\n"
                  + " Found:\n" + _existingBookmarks);
        }
        final HashMap<String, TableInfo.Column> _columnsKnownWords = new HashMap<String, TableInfo.Column>(3);
        _columnsKnownWords.put("arabicWord", new TableInfo.Column("arabicWord", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnownWords.put("frequency", new TableInfo.Column("frequency", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnownWords.put("learnedAt", new TableInfo.Column("learnedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysKnownWords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesKnownWords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoKnownWords = new TableInfo("known_words", _columnsKnownWords, _foreignKeysKnownWords, _indicesKnownWords);
        final TableInfo _existingKnownWords = TableInfo.read(db, "known_words");
        if (!_infoKnownWords.equals(_existingKnownWords)) {
          return new RoomOpenHelper.ValidationResult(false, "known_words(com.tanxe.quran.data.entity.KnownWord).\n"
                  + " Expected:\n" + _infoKnownWords + "\n"
                  + " Found:\n" + _existingKnownWords);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "800ba0749cfbdead1232935ddd459e1f", "ba1d299c6cc95dac55351932f4bcbc3d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "ayahs","translations","tafseers","word_by_word","bookmarks","known_words");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `ayahs`");
      _db.execSQL("DELETE FROM `translations`");
      _db.execSQL("DELETE FROM `tafseers`");
      _db.execSQL("DELETE FROM `word_by_word`");
      _db.execSQL("DELETE FROM `bookmarks`");
      _db.execSQL("DELETE FROM `known_words`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(AyahDao.class, AyahDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TranslationDao.class, TranslationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TafseerDao.class, TafseerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WordByWordDao.class, WordByWordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BookmarkDao.class, BookmarkDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(KnownWordDao.class, KnownWordDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public AyahDao ayahDao() {
    if (_ayahDao != null) {
      return _ayahDao;
    } else {
      synchronized(this) {
        if(_ayahDao == null) {
          _ayahDao = new AyahDao_Impl(this);
        }
        return _ayahDao;
      }
    }
  }

  @Override
  public TranslationDao translationDao() {
    if (_translationDao != null) {
      return _translationDao;
    } else {
      synchronized(this) {
        if(_translationDao == null) {
          _translationDao = new TranslationDao_Impl(this);
        }
        return _translationDao;
      }
    }
  }

  @Override
  public TafseerDao tafseerDao() {
    if (_tafseerDao != null) {
      return _tafseerDao;
    } else {
      synchronized(this) {
        if(_tafseerDao == null) {
          _tafseerDao = new TafseerDao_Impl(this);
        }
        return _tafseerDao;
      }
    }
  }

  @Override
  public WordByWordDao wordByWordDao() {
    if (_wordByWordDao != null) {
      return _wordByWordDao;
    } else {
      synchronized(this) {
        if(_wordByWordDao == null) {
          _wordByWordDao = new WordByWordDao_Impl(this);
        }
        return _wordByWordDao;
      }
    }
  }

  @Override
  public BookmarkDao bookmarkDao() {
    if (_bookmarkDao != null) {
      return _bookmarkDao;
    } else {
      synchronized(this) {
        if(_bookmarkDao == null) {
          _bookmarkDao = new BookmarkDao_Impl(this);
        }
        return _bookmarkDao;
      }
    }
  }

  @Override
  public KnownWordDao knownWordDao() {
    if (_knownWordDao != null) {
      return _knownWordDao;
    } else {
      synchronized(this) {
        if(_knownWordDao == null) {
          _knownWordDao = new KnownWordDao_Impl(this);
        }
        return _knownWordDao;
      }
    }
  }
}
