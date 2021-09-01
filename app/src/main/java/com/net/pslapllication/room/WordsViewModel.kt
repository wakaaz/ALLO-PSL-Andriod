package com.net.pslapllication.room

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.room.datamodel.DictionaryDataAPI


class WordsViewModel(application: Application) :AndroidViewModel(application){


     val context : Context = getApplication<Application>().applicationContext
//    val viewModelJob = Job()

    private var wordsRepository : WordsRepository
    var allWords : LiveData<List<DictionaryDataAPI>>
    var allWordsCollection : List<DictionaryDataAPI>
    var allFilteredWords :  List<DictionaryDataAPI>? = null
    var dataWords = MutableLiveData<List<DictionaryData>>()
    var dataliveWords :LiveData<List<DictionaryData>> = dataWords

    init {
        val wordsDao = WordsDatabase.getInstance(application).wordsDao()
        wordsRepository = WordsRepository(wordsDao)
        allWords = wordsRepository.allWords
        allWordsCollection = wordsRepository.allWordsCollection
       // allFilteredWords = wordsRepository.allFilteredWords()
    }



    fun getOffsetData(offset:Int){

        dataWords.value =  wordsRepository.getOffsetData(offset)
    }

     /*    override fun onCleared() {
            clearAllJobs()
            super.onCleared()
        }

       fun clearAllJobs(){
            viewModelJob.cancel()
        }*/

    fun getAllWords():List<DictionaryDataAPI>?{
        if (wordsRepository.allWords != null){
            return wordsRepository.allWords.value
        }
        return null
    }

    fun getFilteredWords(querryText : String):List<DictionaryDataAPI>{
          allFilteredWords =  wordsRepository.getFilteredWords(querryText)

         return allFilteredWords!!
     }
    fun insertWords(wordsEntity: DictionaryDataAPI){
         Log.d("roomDbStatus","viewmodel "+wordsEntity.english_word)
         wordsRepository.insertWords(wordsEntity)
    }

    fun insertWords(wordsList: List<DictionaryDataAPI>) {
         wordsRepository.insertWords(wordsList)
    }
fun deleteWords() {
         wordsRepository.deleteWords()
    }

}
