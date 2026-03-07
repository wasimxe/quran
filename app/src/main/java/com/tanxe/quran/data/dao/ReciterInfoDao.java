package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tanxe.quran.data.entity.ReciterInfo;

import java.util.List;

@Dao
public interface ReciterInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ReciterInfo> reciters);

    @Query("SELECT * FROM reciter_info ORDER BY name")
    List<ReciterInfo> getAll();

    @Query("SELECT * FROM reciter_info WHERE isDownloaded = 1 ORDER BY name")
    List<ReciterInfo> getDownloaded();

    @Query("SELECT * FROM reciter_info WHERE identifier = :identifier")
    ReciterInfo getByIdentifier(String identifier);

    @Query("UPDATE reciter_info SET isDownloaded = :downloaded WHERE identifier = :identifier")
    void updateDownloadState(String identifier, boolean downloaded);
}
