package com.net.pslapllication.room

import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.room.datamodel.DownloadData


class DownloadRepository(private val downloadDao: DownloadDao) {
    var alldownload = downloadDao.getAllDownload()
    //var allFilteredWords = wordsDao.getAllFilteredWords()

    fun insertWords(downloadEntity: DownloadData) {
         insertWordsAsyncTask(downloadDao).execute(downloadEntity)
    }

    fun insertWords(downloadList: List<DownloadData>) {
        insertWordsListAsyncTask(downloadDao).execute(downloadList)
    }

    fun deleteWords() {
        DeleteListAsyncTask(downloadDao).execute()
    }

    fun getFilteredDownload(download_id: Int):  List<DownloadData> {
        var allFilteredDownload = downloadDao.getSingleDownload(download_id)
        return allFilteredDownload
    }


    class insertWordsListAsyncTask(private var downloadDao: DownloadDao) :
        AsyncTask<List<DownloadData>, Void, Void>() {
        override fun doInBackground(vararg params: List<DownloadData>?): Void? {
            downloadDao.addAllMultiple(params[0])
            return null
        }

    }
    class DeleteListAsyncTask(private var downloadDao: DownloadDao) :
        AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        downloadDao.delete()
        return null

    }
    }
    class insertWordsAsyncTask(private var downloadDao: DownloadDao) :
        AsyncTask<DownloadData, Void, Void>() {
        override fun doInBackground(vararg params: DownloadData?): Void? {
             var wordsEntity = params[0]
            downloadDao.addSingleDownload(params[0]!!)

            return null
        }
    }

    class GetFilteredWordsAsyncTask(private var wordsDao: WordsDao) :
        AsyncTask<String, Void, List<DictionaryDataAPI>?>() {
        var list: List<DictionaryDataAPI>? = null
        override fun doInBackground(vararg params: String?): List<DictionaryDataAPI>? {
            var wordsEntity = params[0]
             list = wordsDao.getAllFilteredWords(params[0]!!)
            return list
        }

        override fun onPostExecute(result: List<DictionaryDataAPI>?) {
            super.onPostExecute(result)
        }
    }
}

