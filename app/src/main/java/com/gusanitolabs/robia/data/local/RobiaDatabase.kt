package com.gusanitolabs.robia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ClothingItemEntity::class,
        TagCategoryEntity::class,
        GarmentTagEntity::class,
        ClothingItemTagCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RobiaConverters::class)
abstract class RobiaDatabase : RoomDatabase() {
    abstract fun wardrobeDao(): WardrobeDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile private var instance: RobiaDatabase? = null

        fun getInstance(context: Context): RobiaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RobiaDatabase::class.java,
                    "robia.db",
                ).build().also { instance = it }
            }
    }
}
