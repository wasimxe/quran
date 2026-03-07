package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tanxe.quran.data.entity.EditionInfo;

import java.util.List;

@Dao
public interface EditionInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EditionInfo> editions);

    @Query("SELECT * FROM edition_info WHERE language = :language ORDER BY name")
    List<EditionInfo> getByLanguage(String language);

    @Query("SELECT * FROM edition_info WHERE type = :type ORDER BY language, name")
    List<EditionInfo> getByType(String type);

    @Query("SELECT * FROM edition_info WHERE isDownloaded = 1 ORDER BY name")
    List<EditionInfo> getDownloaded();

    @Query("SELECT * FROM edition_info WHERE isDownloaded = 1 AND type = :type ORDER BY name")
    List<EditionInfo> getDownloadedByType(String type);

    @Query("SELECT DISTINCT language FROM edition_info ORDER BY language")
    List<String> getAllLanguages();

    @Query("SELECT DISTINCT language, languageName FROM edition_info ORDER BY languageName")
    List<LanguageInfo> getAllLanguagesWithNames();

    @Query("UPDATE edition_info SET isDownloaded = :downloaded, downloadProgress = :progress, downloadedAt = :downloadedAt WHERE identifier = :identifier")
    void updateDownloadState(String identifier, boolean downloaded, int progress, long downloadedAt);

    @Query("SELECT * FROM edition_info WHERE identifier = :identifier")
    EditionInfo getByIdentifier(String identifier);

    @Query("SELECT * FROM edition_info ORDER BY language, name")
    List<EditionInfo> getAll();

    @Query("SELECT COUNT(*) FROM edition_info")
    int getCount();

    class LanguageInfo {
        public String language;
        public String languageName;
    }
}
