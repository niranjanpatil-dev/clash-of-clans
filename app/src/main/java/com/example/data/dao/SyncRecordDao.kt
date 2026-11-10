package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.SyncRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncRecordDao {
    @Query("SELECT * FROM sync_records WHERE village = :village ORDER BY syncedAt DESC LIMIT 1")
    fun getLatestSyncFlow(village: String): Flow<SyncRecordEntity?>

    @Query("SELECT * FROM sync_records ORDER BY syncedAt DESC LIMIT 10")
    fun getRecentSyncsFlow(): Flow<List<SyncRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncRecord: SyncRecordEntity): Long
}
