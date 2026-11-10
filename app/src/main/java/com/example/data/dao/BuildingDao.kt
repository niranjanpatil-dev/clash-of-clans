package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.BuildingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings WHERE village = :village ORDER BY typeId ASC, name ASC")
    fun getByVillageFlow(village: String): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings WHERE village = :village")
    suspend fun getByVillage(village: String): List<BuildingEntity>

    @Query("SELECT * FROM buildings WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BuildingEntity?

    @Query("SELECT COUNT(*) FROM buildings")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(building: BuildingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(buildings: List<BuildingEntity>)

    @Update
    suspend fun update(building: BuildingEntity)

    @Update
    suspend fun updateAll(buildings: List<BuildingEntity>)

    @Delete
    suspend fun delete(building: BuildingEntity)

    @Query("DELETE FROM buildings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
