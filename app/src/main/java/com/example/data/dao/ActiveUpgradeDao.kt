package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ActiveUpgradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveUpgradeDao {
    @Query("SELECT * FROM active_upgrades WHERE isCompleted = 0 ORDER BY finishTimestamp ASC")
    fun getActiveUpgradesFlow(): Flow<List<ActiveUpgradeEntity>>

    @Query("SELECT * FROM active_upgrades WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ActiveUpgradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(upgrade: ActiveUpgradeEntity): Long

    @Update
    suspend fun update(upgrade: ActiveUpgradeEntity)

    @Delete
    suspend fun delete(upgrade: ActiveUpgradeEntity)

    @Query("DELETE FROM active_upgrades WHERE id = :id")
    suspend fun deleteById(id: Long)
}
