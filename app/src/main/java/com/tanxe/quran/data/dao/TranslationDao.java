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
}
