package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BuildingDao
import com.example.data.dao.BuildingTypeDao
import com.example.data.dao.SyncRecordDao
import com.example.data.dao.TownHallDao
import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import com.example.data.entity.SyncRecordEntity
import com.example.data.dao.ActiveUpgradeDao
import com.example.data.entity.ActiveUpgradeEntity
import com.example.data.entity.TownHallEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BuildingTypeEntity::class,
        TownHallEntity::class,
        BuildingEntity::class,
        SyncRecordEntity::class,
        ActiveUpgradeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buildingTypeDao(): BuildingTypeDao
    abstract fun townHallDao(): TownHallDao
    abstract fun buildingDao(): BuildingDao
    abstract fun syncRecordDao(): SyncRecordDao
    abstract fun activeUpgradeDao(): ActiveUpgradeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clash_building_manager.db"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        InitialDataSeeder.seedDatabase(database)
                    }
                }
            }
        }
    }
}
