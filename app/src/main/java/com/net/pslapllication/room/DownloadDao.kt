package com.net.pslapllication.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.room.datamodel.DownloadData

@Dao
interface DownloadDao {
    @Query("SELECT * FROM DOWNLOADTABLE")
    fun getAllDownload(): LiveData<List<DownloadData>>


    @Query("SELECT * FROM DOWNLOADTABLE WHERE name = :name")
    fun getSingleDownload(name:String): DownloadData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSingleDownload(entity: DownloadData?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllMultiple(entity: List<DownloadData>?)

    @Query("DELETE FROM DOWNLOADTABLE")
    fun delete()


}