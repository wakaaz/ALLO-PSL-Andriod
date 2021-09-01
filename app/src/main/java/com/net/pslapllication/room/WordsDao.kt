package com.net.pslapllication.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.net.pslapllication.room.datamodel.DictionaryDataAPI

@Dao
interface WordsDao {
    @Query("SELECT * FROM WORDSTABLE")
    fun getAllWords(): LiveData<List<DictionaryDataAPI>>
    @Query("SELECT * FROM  WORDSTABLE  ORDER BY  english_word  ASC LIMIT 50 OFFSET  :offset")
    fun getOffsetWords(offset:Int): List<DictionaryDataAPI>
    @Query("SELECT * FROM WORDSTABLE")
    fun getAllWordsCollection(): List<DictionaryDataAPI>

    @Query("SELECT * FROM WORDSTABLE WHERE english_word LIKE '%' || :querryText || '%' ORDER BY english_word LIKE :querryText || '%' DESC")
    fun getAllFilteredWords(querryText: String): List<DictionaryDataAPI>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllWords(entity: DictionaryDataAPI?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllWordsList(entity: List<DictionaryDataAPI>?)

    @Query("DELETE FROM WORDSTABLE")
    fun delete()


}