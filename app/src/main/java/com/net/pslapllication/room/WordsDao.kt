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
    @Query("SELECT * from WORDSTABLE ORDER BY case WHEN english_word LIKE '-%' THEN 2 WHEN english_word LIKE '0%'  THEN 1 WHEN english_word LIKE '1%'  THEN 1 WHEN english_word LIKE '2%'  THEN 1 WHEN english_word LIKE '3%'  THEN 1 WHEN english_word LIKE '4%'  THEN 1 WHEN english_word LIKE '5%'  THEN 1 WHEN english_word LIKE '6%'  THEN 1 WHEN english_word LIKE '7%'  THEN 1 WHEN english_word LIKE '8%'  THEN 1 WHEN english_word LIKE '9%'  THEN 1 ELSE 0 end ASC, lower(english_word) ASC LIMIT 100 OFFSET :offset")
    fun getOffsetWords(offset:Int): List<DictionaryDataAPI>
    @Query("SELECT * FROM WORDSTABLE")
    fun getAllWordsCollection(): List<DictionaryDataAPI>

    @Query("SELECT * FROM WORDSTABLE WHERE english_word LIKE '%' || :querryText || '%' ORDER BY english_word LIKE :querryText || '%' DESC")
    fun getAllFilteredWords(querryText: String): List<DictionaryDataAPI>


    @Query("SELECT * FROM WORDSTABLE WHERE english_word LIKE '%' || :querryText || '%' ORDER BY english_word LIKE :querryText || '%' DESC")
    fun getFilteredItems(querryText: String): List<DictionaryDataAPI>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllWords(entity: DictionaryDataAPI?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllWordsList(entity: List<DictionaryDataAPI>?)

    @Query("DELETE FROM WORDSTABLE")
    fun delete()

    @Query("UPDATE WORDSTABLE SET favorite = :fav WHERE id = :id")
    fun updateFav(id: Int, fav: Int): Int


}