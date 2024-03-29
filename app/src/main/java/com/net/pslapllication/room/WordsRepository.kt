package com.net.pslapllication.room

import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.net.pslapllication.room.datamodel.DictionaryDataAPI


class WordsRepository(private val wordsDao: WordsDao) {
    var allWords = wordsDao.getAllWords()
    var allWordsCollection = wordsDao.getAllWordsCollection()
    //var allFilteredWords = wordsDao.getAllFilteredWords()

    fun insertWords(wordsEntity: DictionaryDataAPI) {
         insertWordsAsyncTask(wordsDao).execute(wordsEntity)
    }

    fun insertWords(wordsList: List<DictionaryDataAPI>) {
        insertWordsListAsyncTask(wordsDao).execute(wordsList)
    }

    fun deleteWords() {
        DeleteListAsyncTask(wordsDao).execute()
    }

    fun getFilteredWords(query: String):  List<DictionaryDataAPI> {
        var allFilteredWords  = wordsDao.getAllFilteredWords(query)
        return allFilteredWords
    }
    /*fun getFilteredWords(querryString: String):List<DictionaryDataAPI> {
       // return wordsDao.getAllFilteredWords(querryString)
          var list = GetFilteredWordsAsyncTask(wordsDao).execute(querryString)
        return list
    }
*/

    class insertWordsListAsyncTask(private var wordsDao: WordsDao) :
        AsyncTask<List<DictionaryDataAPI>, Void, Void>() {
        override fun doInBackground(vararg params: List<DictionaryDataAPI>?): Void? {
             wordsDao.addAllWordsList(params[0])
            return null
        }

    }
class DeleteListAsyncTask(private var wordsDao: WordsDao) :
        AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        wordsDao.delete()
        return null

    }
}
    class insertWordsAsyncTask(private var wordsDao: WordsDao) :
        AsyncTask<DictionaryDataAPI, Void, Void>() {
        override fun doInBackground(vararg params: DictionaryDataAPI?): Void? {
             var wordsEntity = params[0]
            wordsDao.addAllWords(params[0]!!)

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




       /* override fun doInBackground(vararg params: String?) {
            var wordsEntity = params[0]
            Log.d("datasetnewp",""+wordsEntity)
            list = wordsDao.getAllFilteredWords(params[0]!!)
            return null
        }

        override fun onPostExecute(result: Void?): List<DictionaryDataAPI>? {
            super.onPostExecute(result)
            return list

        }*/


}

