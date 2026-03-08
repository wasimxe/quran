package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tanxe.quran.data.entity.Translation;

import java.util.List;

@Dao
public interface TranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Translation> translations);

    @Query("SELECT * FROM translations WHERE surahNumber = :surah AND ayahNumber = :ayah AND edition = :edition LIMIT 1")
    Translation getTranslation(int surah, int ayah, String edition);

    @Query("SELECT * FROM translations WHERE surahNumber = :surah AND edition = :edition ORDER BY ayahNumber")
    List<Translation> getTranslationsBySurah(int surah, String edition);

    @Query("SELECT DISTINCT edition FROM translations")
    List<String> getAvailableEditions();

    @Query("SELECT DISTINCT edition FROM translations WHERE language = :language")
    List<String> getEditionsByLanguage(String language);

    @Query("SELECT * FROM translations WHERE edition = :edition AND (text LIKE '%' || :query || '%') ORDER BY surahNumber, ayahNumber")
    List<Translation> searchInEdition(String edition, String query);

    @Query("DELETE FROM translations WHERE edition = :edition")
    void deleteEdition(String edition);

    @Query("SELECT COUNT(*) FROM translations WHERE edition = :edition")
    int getEditionCount(String edition);

    @Query("SELECT COALESCE(SUM(LENGTH(text)), 0) FROM translations WHERE edition = :edition")
    long getEditionTextSize(String edition);

    @Query("SELECT COUNT(DISTINCT surahNumber) FROM translations WHERE edition = :edition")
    int getDistinctSurahCount(String edition);

    @Query("SELECT edition, COUNT(*) as ayahCount, COUNT(DISTINCT surahNumber) as surahCount, COALESCE(SUM(LENGTH(text)), 0) as textSize FROM translations GROUP BY edition")
    List<com.tanxe.quran.data.entity.EditionStats> getAllEditionStats();
}
