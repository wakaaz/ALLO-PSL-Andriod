package com.net.pslapllication.room
import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Room
import androidx.room.RoomDatabase
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.room.datamodel.DownloadData
import kotlinx.coroutines.CoroutineScope

@Database(entities = [DictionaryDataAPI::class, DownloadData::class],version = 4)
abstract class WordsDatabase: RoomDatabase() {
    abstract fun wordsDao():WordsDao
    abstract fun downloadDao():DownloadDao

    companion object {

        @Volatile
        private var INSTANCE: WordsDatabase? = null

        fun getInstance(context: Context): WordsDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WordsDatabase::class.java,
                        "WORDSDATABASE"
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