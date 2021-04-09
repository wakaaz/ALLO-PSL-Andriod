package com.net.pslapllication.interfaces

import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.favouriteList.Data
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.room.datamodel.DictionaryDataAPI

interface OnVideoSelectedListener {
    fun onVideoSelect(selectedModel: DictionaryData)
    fun onVideoSelect(selectedModel: DictionaryDataAPI)
    fun onVideoSelect(selectedModel: TutorialData)
    fun onVideoSelect(selectedModel: StoryData)
    fun onVideoSelect(selectedModel: LearningData)
    fun onVideoSelect(selectedModel: Data)
    fun onVideoSelect(selectedModel: DownloadListModel)

}