package com.net.pslapllication.room
import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Room
import androidx.room.RoomDatabase
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.room.datamodel.DownloadData
import kotlinx.coroutines.CoroutineScope

@Database(entities = [DownloadData::class],version = 1)
abstract class DownloadDatabase: RoomDatabase() {
    abstract fun wordsDao():WordsDao
    companion object {

        @Volatile
        private var INSTANCE: DownloadDatabase? = null

        fun getInstance(context: Context): DownloadDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DownloadDatabase::class.java,
                        "DOWNLOADDATABASE"
                    ).allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}