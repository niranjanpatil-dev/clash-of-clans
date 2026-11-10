package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.TownHallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TownHallDao {
    @Query("SELECT * FROM town_halls WHERE village = :village ORDER BY level ASC")
    fun getByVillageFlow(village: String): Flow<List<TownHallEntity>>

    @Query("SELECT * FROM town_halls WHERE village = :village AND isActive = 1 LIMIT 1")
    fun getActiveFlow(village: String): Flow<TownHallEntity?>

    @Query("SELECT * FROM town_halls WHERE village = :village AND isActive = 1 LIMIT 1")
    suspend fun getActive(village: String): TownHallEntity?

    @Query("SELECT * FROM town_halls WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TownHallEntity?

    @Query("SELECT COUNT(*) FROM town_halls")
    suspend fun count(): Int

    @Query("UPDATE town_halls SET isActive = 0 WHERE village = :village")
    suspend fun clearActive(village: String)

    @Query("UPDATE town_halls SET isActive = 1 WHERE id = :id")
    suspend fun setActiveById(id: Long)

    @Transaction
    suspend fun setActiveTownHall(id: Long, village: String) {
        clearActive(village)
        setActiveById(id)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(townHall: TownHallEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(townHalls: List<TownHallEntity>)

    @Update
    suspend fun update(townHall: TownHallEntity)

    @Delete
    suspend fun delete(townHall: TownHallEntity)
}
