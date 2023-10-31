package com.example.firebaseecom.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.firebaseecom.model.ProductHomeModel


@Database(entities = [ProductHomeModel::class], version = 1, exportSchema = false)
abstract class EkartDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: EkartDatabase? = null

        fun getDatabase(context: Context): EkartDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EkartDatabase::class.java,
                    "product_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}