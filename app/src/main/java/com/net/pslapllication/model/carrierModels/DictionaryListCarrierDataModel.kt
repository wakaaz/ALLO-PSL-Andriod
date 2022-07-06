package com.net.pslapllication.model.carrierModels
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.data.Data
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.preferences.Story_types
import com.net.pslapllication.model.preferences.Subjects
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.room.WordsViewModel
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import java.io.Serializable

class DictionaryListCarrierDataModel : Serializable {
    private var modelList: List<DictionaryData>? =  ArrayList<DictionaryData>()
    private var modelTutorialList: List<TutorialData>? = ArrayList<TutorialData>()
    private var modelDictionaryList: List<DownloadListModel>? = ArrayList<DownloadListModel>()
    private var modelLearningList: List<LearningData>? = ArrayList<LearningData>()
    private var modelListApi: List<DictionaryDataAPI>? = ArrayList<DictionaryDataAPI>()
    private var modelFavList: List<com.net.pslapllication.model.favouriteList.Data>? = ArrayList<com.net.pslapllication.model.favouriteList.Data>()
    private var list: List<Data>? = ArrayList<Data>()
    private var storyList: List<StoryData>? = ArrayList<StoryData>()
    private var englistStoriesList: List<StoryData>? = ArrayList<StoryData>()
    private var urduStoriesList: List<StoryData>? = ArrayList<StoryData>()


    private var subjectList: List<Subjects>? = ArrayList<Subjects>()
    private var wordsViewModel: WordsViewModel? = null
    private var snackbar : Snackbar? = null
   


    fun getsubjectList(): List<Subjects>? {
        return subjectList
    }

    fun setsubjectList(subjectList: List<Subjects>?) {
        this.subjectList = subjectList
    }

    fun getModelList(): List<DictionaryData>? {
        return modelList
    }

    fun setModelList(cartModelList: List<DictionaryData>?) {
        this.modelList = cartModelList
    }

    fun getTutorialModelList(): List<TutorialData>? {
        return modelTutorialList
    }

    fun setTutorialModelList(cartModelList: List<TutorialData>?) {
        this.modelTutorialList = cartModelList
    }

    fun getDownloadModelList(): List<DownloadListModel>? {
        return modelDictionaryList
    }

    fun setDownloadModelList(cartModelList: List<DownloadListModel>?) {
        this.modelDictionaryList = cartModelList
    }

    fun getFavModelList(): List<com.net.pslapllication.model.favouriteList.Data>? {
        return modelFavList
    }

    fun setFavModelList(cartModelList: List<com.net.pslapllication.model.favouriteList.Data>?) {
        this.modelFavList = cartModelList
    }

    fun getLeaningModelList(): List<LearningData>? {
        return modelLearningList
    }

    fun setLeaningModelList(cartModelList: List<LearningData>?) {
        this.modelLearningList = cartModelList
    }

    fun getModelListOffline(): List<DictionaryDataAPI>? {
        return modelListApi
    }

    fun setModelListOffline(cartModelList: List<DictionaryDataAPI>?) {
        this.modelListApi = cartModelList
    }

    fun getList(): List<Data>? {
        return list
    }

    fun setList(favModelList: List<Data>?) {
        this.list = favModelList
    }

    fun getStoryList(): List<StoryData>? {
        return storyList
    }

    fun setStoryList(storyList: List<StoryData>?) {
        this.storyList = storyList
    }

    fun getEnglishStoryList(): List<StoryData>? {
        return englistStoriesList
    }

    fun setEnglishStoryList(storyList: List<StoryData>?) {
        this.englistStoriesList = storyList
    }
    fun getUrduStoryList(): List<StoryData>? {
        return urduStoriesList
    }

    fun setUrduStoryList(storyList: List<StoryData>?) {
        this.urduStoriesList = storyList
    }

    fun getViewModel(): WordsViewModel {
        return wordsViewModel!!
    }

    fun setViewModel(wordsViewModel: WordsViewModel) {
        this.wordsViewModel = wordsViewModel
    }

    fun setSnackBar(snackbar: Snackbar){
        this.snackbar = snackbar
    }

    fun getSnackBar() : Snackbar?{
        return snackbar
    }

}
