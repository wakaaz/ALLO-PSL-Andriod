package com.net.pslapllication.interfaces

import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.favouriteList.Data
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.room.datamodel.DictionaryDataAPI

interface OnQuerryChangeListener {
    fun onSearchResult(size: Int)

}