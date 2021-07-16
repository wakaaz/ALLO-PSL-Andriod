package com.net.pslapllication.util

import android.app.DownloadManager
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData

class Constants {
    companion object {
        //home
        const val TYPE_DICTIONARY: String = "DICTIONARY"
        const val TYPE_TEACHER_TUTORIAL: String = "TEACHER TUTORIALS"
        const val TYPE_LEARNING_TUTORIAL: String = "LIFE SKILLS"
        const val TYPE_LEARNING_TUTORIAL_REAL: String = "STUDENT TUTORIALS"
        const val TYPE_STORIES: String = "STORIES"

        //user type
        const val USERTYPE_LOGIN: String = "user"
        const val USERTYPE_GUEST: String = "guest"

        //Favourite type video
        const val FAV_DICTIONARY: String = "FAV_DICTIONARY"
        const val FAV_TEACHER_TUT: String = "FAV_TEACHER_TUT"
        const val FAV_STORY: String = "FAV_STORY"
        const val FAV_SKILL: String = "FAV_SKILL"
        const val FAV_LEARNING_TUT: String = "FAV_LEARNING_TUT"

        //profile
        const val TYPE_RECENT: String = "RECENT"
        const val TYPE_PLAYLIST: String = "PLAYLIST"
        const val TYPE_FAVOURITE: String = "FAVORITES"
        const val TYPE_DOWNLOAD: String = "DOWNLOAD"

        //Network type
        const val NetworkTYPE_WIFI: String = "WIFI"
        const val NetworkTYPE_MOB_DATA: String = "MOB_DATA"

        //Sub Classes
        const val TYPE_CLASS_SUBJECT: String = "TYPE_CLASS_SUB"
        const val TYPE_SUB_TOPIC: String = "TYPE_SUB_TOPIC"
        const val TYPE_VIDEO: String = "TYPE_VIDEO"
        const val TYPE_VIDEO_QUALITY: String = "TYPE_VIDEO_QUALITY"

        //Request code
        const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE: Int = 111
        const val PENDING_INTENT_REQ_CODE: Int = 123

        //intent constants
        const val KEY_URL: String = "KEY_URL"
        const val DICTIONARY_LIST_MODEL: String = "DICTIONARY_LIST_MODEL"
        const val SELECTED_DICTIONARY_LIST_MODEL: String = "SELECTED_DICTIONARY_LIST_MODEL"
        const val CATEGORY_LIST: String = "CATEGORY_LIST"
        const val CETAGORY_TYPE: String = "CATAGORY_TYPE"
        const val IS_LANGUAGE: String = "IS_LANGUAGE"

        const val TUTORIAL_TYPE: String = "TUTORIAL_TYPE"
        const val DOWNLOAD_TYPE: String = "DOWNLOAD_TYPE"
        const val DOWNLOAD_LIST_TYPE: String = "DOWNLOAD_LIST_TYPE"
        const val FILE_NAME: String = "FILE_NAME"
        const val THUMBNAIL: String = "THUMBNAIL"
        const val CAT_NAME: String = "CAT_NAME"
        const val QUALITY_ID: String = "QUALITY_ID"

        //Download type
        const val SINGLE_VIDEO_DOWNLOAD: String = "SINGLE_VIDEO_DOWNLOAD"
        const val MULTI_VIDEO_DOWNLOAD: String = "MULTI_VIDEO_DOWNLOAD"

        //API Codes
        const val SUCCESS_CODE: Int = 200
        const val SESSION_ERROR_CODE: Int = 100
        const val FIELD_VALIDATION_ERROR_CODE: Int = 101
        const val EMAIL_NOT_FOUND_ERROR: Int = 103
        const val AUTHENTICATION_ERROR: Int = 104
        const val RECOVERY_EMAIL_NOT_FOUND_ERROR: Int = 105
        const val INVALID_RECOVERY_CODE: Int = 106

        //Internet quality Codes
        const val AUTO: Int = 0
        const val p240p: Int = 1  //Poor
        const val p360p: Int = 2  //Moderate
        const val p480p: Int = 3  //Good
        const val p720p: Int = 4  //Excellent

        //Folder name
        const val CAT_FOLDER_NAME: String = "/CATPSLVIDEO/"
        const val FOLDER_NAME: String = "/PSLVIDEO/"


        //download references
        var singleVidoedownloadList: List<DictionaryData>? = emptyList()
        var downloadList: List<DictionaryData>? = emptyList()
        var constantHashMap: HashMap<Long, DictionaryData> = HashMap<Long, DictionaryData>()
        var constantHashMapStoryData: HashMap<Long, StoryData> = HashMap<Long, StoryData>()
        var constantHashMapTutorialData: HashMap<Long, TutorialData> = HashMap<Long, TutorialData>()
        var constantHashMapLearningData: HashMap<Long, LearningData> = HashMap<Long, LearningData>()
        var dm: DownloadManager? = null
        var idLong: Long = 0

        //download statuse

        var DOWNLOAD_STATUS: String = ""
        const val DOWNLOAD_REQUESTED: String = "DOWNLOAD_REQUESTED"
        const val DOWNLOAD_START: String = "DOWNLOAD_START"
        const val DOWNLOAD_COMPLETED: String = "DOWNLOAD_COMPLETED"
        const val NO_ACTIVE_DOWNLOAD: String = "NO_ACTIVE_DOWNLOAD"


        const val SEARCHEDWORD: String = "SEARCHEDWORD"

    }
}