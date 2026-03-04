package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tanxe.quran.data.entity.Tafseer;

import java.util.List;

@Dao
public interface TafseerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Tafseer> tafseers);

    @Query("SELECT * FROM tafseers WHERE surahNumber = :surah AND ayahNumber = :ayah AND edition = :edition LIMIT 1")
    Tafseer getTafseer(int surah, int ayah, String edition);

    @Query("SELECT DISTINCT edition FROM tafseers")
    List<String> getAvailableEditions();

    @Query("SELECT DISTINCT edition FROM tafseers WHERE language = :language")
    List<String> getEditionsByLanguage(String language);

    @Query("SELECT * FROM tafseers WHERE edition = :edition AND (text LIKE '%' || :query || '%') ORDER BY surahNumber, ayahNumber")
    List<Tafseer> searchInEdition(String edition, String query);

    @Query("DELETE FROM tafseers WHERE edition = :edition")
    void deleteEdition(String edition);

    @Query("SELECT COUNT(*) FROM tafseers WHERE edition = :edition")
    int getEditionCount(String edition);
}
