package com.net.pslapllication.helperClass


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.net.pslapllication.data.Data
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.interfaces.OnProgressResultListener
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.room.WordViewModelFactory
import com.net.pslapllication.room.WordsViewModel
import com.net.pslapllication.room.datamodel.DictionaryDataAPI

class ProgressHelper(var context: Context) {
    private var progressListener: OnProgressResultListener? = null
    private var dictionaryListCarrierDataModel: DictionaryListCarrierDataModel? = DictionaryListCarrierDataModel()

    companion object {
        private var progressHelper: ProgressHelper? = null

        @Synchronized
        fun getInstance(context: Context): ProgressHelper {
            if (progressHelper == null) {
                progressHelper = ProgressHelper(context)
            }
            return progressHelper!!
        }
    }

    fun setListener(progressListener: OnProgressResultListener) {
        this.progressListener = progressListener
    }

    fun setDownloadStatusListener(progressListener: OnProgressResultListener) {
        this.progressListener = progressListener
    }

    fun setModelInstance(dictionaryListCarrierDataModel: DictionaryListCarrierDataModel) {
        this.dictionaryListCarrierDataModel = dictionaryListCarrierDataModel
    }

    fun sendValues(progress: Int) {

        this.progressListener?.onProgressResult(progress)
    }

    fun sendValues(progress: Int, referenceId: Long) {

        this.progressListener?.onProgressResult(progress, referenceId)
    }

    fun setCompletion(isCompleted: Boolean) {
        this.progressListener?.onDownloadComplete(isCompleted)
    }

    fun setCompletion(isCompleted: Boolean, referenceId: Long) {
        this.progressListener?.onDownloadComplete(isCompleted, referenceId)
    }

    fun setList(list: List<DictionaryData>) {

        this.dictionaryListCarrierDataModel?.setModelList(list)
    }

    fun getList(): List<DictionaryData> {
        return this.dictionaryListCarrierDataModel?.getModelList()!!
    }

    fun setTutorialList(list: List<TutorialData>) {

        this.dictionaryListCarrierDataModel?.setTutorialModelList(list)
    }

    fun getTutorialList(): List<TutorialData> {
        return this.dictionaryListCarrierDataModel?.getTutorialModelList()!!
    }

    fun setDownloadList(list: List<DownloadListModel>) {

        this.dictionaryListCarrierDataModel?.setDownloadModelList(list)
    }

    fun getDownloadList(): List<DownloadListModel> {
        return this.dictionaryListCarrierDataModel?.getDownloadModelList()!!
    }

    fun setFavList(list: List<com.net.pslapllication.model.favouriteList.Data>) {

        this.dictionaryListCarrierDataModel?.setFavModelList(list)
    }

    fun getFavList(): List<com.net.pslapllication.model.favouriteList.Data> {
        return this.dictionaryListCarrierDataModel?.getFavModelList()!!
    }

    fun setLearningList(list: List<LearningData>) {

        this.dictionaryListCarrierDataModel?.setLeaningModelList(list)
    }

    fun getLearningList(): List<LearningData> {
        return this.dictionaryListCarrierDataModel?.getLeaningModelList()!!
    }

    fun setListOffline(list: List<DictionaryDataAPI>) {

        this.dictionaryListCarrierDataModel?.setModelListOffline(list)
    }

    fun getListOffline(): List<DictionaryDataAPI> {
        return this.dictionaryListCarrierDataModel?.getModelListOffline()!!
    }

    fun setListStory(list: List<StoryData>) {

        this.dictionaryListCarrierDataModel?.setStoryList(list)
    }
    fun getListStory(): List<StoryData> {
        return this.dictionaryListCarrierDataModel?.getStoryList()!!
    }

    fun setEnglishListStory(list: List<StoryData>) {

        this.dictionaryListCarrierDataModel?.setEnglishStoryList(list)
    }
    fun getEnglishListStory(): List<StoryData> {

        return this.dictionaryListCarrierDataModel?.getEnglishStoryList()!!
    }

    fun setUrduListStory(list: List<StoryData>) {

        this.dictionaryListCarrierDataModel?.setUrduStoryList(list)!!
    }
    fun getUrduListStory(): List<StoryData> {

        return this.dictionaryListCarrierDataModel?.getUrduStoryList()!!
    }



    fun getViewModel(): WordsViewModel {
        return dictionaryListCarrierDataModel!!.getViewModel()
    }

    fun setViewModel(viewModel: WordsViewModel) {
        this.dictionaryListCarrierDataModel!!.setViewModel(viewModel)

    }

    fun getListWordsTab(): List<Data> {
        return this.dictionaryListCarrierDataModel?.getList()!!
    }

    fun setOnSnackBarCreated(snackbar: Snackbar){
        this.dictionaryListCarrierDataModel!!.setSnackBar(snackbar)
    }

    fun getOnSnackBarCreated() : Snackbar?{
        return this.dictionaryListCarrierDataModel?.getSnackBar()
    }

}