package com.net.pslapllication.util

import android.util.Log
import com.net.pslapllication.data.Data
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.preferences.Story_types
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.room.datamodel.DictionaryDataAPI

class ListSorting {
    companion object {
        fun sortList(indexPosition: Int, list: List<DictionaryData>): List<DictionaryData> {
            var beforeIndexList: List<DictionaryData>? = null
            var afterIndexList: List<DictionaryData>? = null

            if (indexPosition == 0) {
                beforeIndexList = list.subList(0, indexPosition)
                if (list.size != 1) {
                    //afterIndexList = list.subList(indexPosition+1, list.size)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    return beforeIndexList
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList

        }

        fun sortList(
            isDecrement: Boolean,
            indexPosition: Int,
            list: List<DictionaryData>
        ): List<DictionaryData> {
            var beforeIndexList: List<DictionaryData>? = null
            var afterIndexList: List<DictionaryData>? = null

            if (indexPosition == 0) {
                if (isDecrement) {
                    beforeIndexList = list.subList(0, indexPosition + 1)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    beforeIndexList = list.subList(0, indexPosition)
                    if (list.size != 1) {
                        //afterIndexList = list.subList(indexPosition+1, list.size)
                        afterIndexList = list.subList(indexPosition, list.size)
                    } else {
                        beforeIndexList = list.subList(0, 1)
                        return beforeIndexList
                    }
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition + 1)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList

        }

        fun sortList(list: List<Story_types>, indexPosition: Int): List<Story_types> {
            var beforeIndexList: List<Story_types>? = null
            var afterIndexList: List<Story_types>? = null
            if (indexPosition == 0) {
                beforeIndexList = list.subList(0, indexPosition)
                if (list.size != 1) {
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    return beforeIndexList
                }

            } else {
                beforeIndexList = list.subList(0, indexPosition)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }


            return afterIndexList + beforeIndexList
        }

        fun sortList(
            isDecrement: Boolean,
            list: List<Story_types>,
            indexPosition: Int
        ): List<Story_types> {
            var beforeIndexList: List<Story_types>? = null
            var afterIndexList: List<Story_types>? = null

            if (indexPosition == 0) {
                if (isDecrement) {
                    beforeIndexList = list.subList(0, indexPosition + 1)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    beforeIndexList = list.subList(0, indexPosition)
                    if (list.size != 1) {
                        //afterIndexList = list.subList(indexPosition+1, list.size)
                        afterIndexList = list.subList(indexPosition, list.size)
                    } else {
                        beforeIndexList = list.subList(0, 1)
                        return beforeIndexList
                    }
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition + 1)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList
        }

        fun sortListTutorial(indexPosition: Int, list: List<TutorialData>): List<TutorialData> {
            var beforeIndexList: List<TutorialData>? = null
            var afterIndexList: List<TutorialData>? = null
            if (indexPosition == 0) {
                beforeIndexList = list.subList(0, indexPosition)
                if (list.size != 1) {
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    return beforeIndexList
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            Log.d(
                "fagfgfgf11",
                "" + indexPosition + "|a" + list.size + "b|" + afterIndexList.size + "c|" + beforeIndexList.size
            )

            return afterIndexList + beforeIndexList
        }

        fun sortListTutorial(
            isDecrement: Boolean,
            indexPosition: Int,
            list: List<TutorialData>
        ): List<TutorialData> {
            var beforeIndexList: List<TutorialData>? = null
            var afterIndexList: List<TutorialData>? = null

            if (indexPosition == 0) {
                if (isDecrement) {
                    beforeIndexList = list.subList(0, indexPosition + 1)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    beforeIndexList = list.subList(0, indexPosition)
                    if (list.size != 1) {
                        //afterIndexList = list.subList(indexPosition+1, list.size)
                        afterIndexList = list.subList(indexPosition, list.size)
                    } else {
                        beforeIndexList = list.subList(0, 1)
                        return beforeIndexList
                    }
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition + 1)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList
        }

        fun sortListDownload(list: List<DownloadListModel>, indexPosition: Int): List<DownloadListModel> {
            var beforeIndexList: List<DownloadListModel>? = null
            var afterIndexList: List<DownloadListModel>? = null
            if (indexPosition == 0) {
                beforeIndexList = list.subList(0, indexPosition)
                if (list.size != 1) {
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    return beforeIndexList
                }

            } else {
                beforeIndexList = list.subList(0, indexPosition)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }


            return afterIndexList + beforeIndexList
        }
fun sortListDownload(
            isDecrement: Boolean,
            indexPosition: Int,
            list: List<DownloadListModel>
        ): List<DownloadListModel> {
            var beforeIndexList: List<DownloadListModel>? = null
            var afterIndexList: List<DownloadListModel>? = null

            if (indexPosition == 0) {
                if (isDecrement) {
                    beforeIndexList = list.subList(0, indexPosition + 1)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    beforeIndexList = list.subList(0, indexPosition)
                    if (list.size != 1) {
                        //afterIndexList = list.subList(indexPosition+1, list.size)
                        afterIndexList = list.subList(indexPosition, list.size)
                    } else {
                        beforeIndexList = list.subList(0, 1)
                        return beforeIndexList
                    }
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition + 1)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList
        }

        fun sortListFavourite(
            indexPosition: Int,
            list: List<com.net.pslapllication.model.favouriteList.Data>
        ): List<com.net.pslapllication.model.favouriteList.Data> {
            var beforeIndexList: List<com.net.pslapllication.model.favouriteList.Data>? = null
            var afterIndexList: List<com.net.pslapllication.model.favouriteList.Data>? = null


            if (indexPosition == 0) {
                beforeIndexList = list.subList(0, indexPosition)
                if (list.size != 1) {
                    //afterIndexList = list.subList(indexPosition+1, list.size)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    return beforeIndexList
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList
        }

        fun sortListFavourite(
            isDecrement: Boolean,
            indexPosition: Int,
            list: List<com.net.pslapllication.model.favouriteList.Data>
        ): List<com.net.pslapllication.model.favouriteList.Data> {
            var beforeIndexList: List<com.net.pslapllication.model.favouriteList.Data>? = null
            var afterIndexList: List<com.net.pslapllication.model.favouriteList.Data>? = null

            if (indexPosition == 0) {
                if (isDecrement) {
                    beforeIndexList = list.subList(0, indexPosition + 1)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    beforeIndexList = list.subList(0, indexPosition)
                    if (list.size != 1) {
                        //afterIndexList = list.subList(indexPosition+1, list.size)
                        afterIndexList = list.subList(indexPosition, list.size)
                    } else {
                        beforeIndexList = list.subList(0, 1)
                        return beforeIndexList
                    }
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition + 1)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList
        }

        fun sortListStory(indexPosition: Int, list: List<StoryData>): List<StoryData> {
            var beforeIndexList: List<StoryData>? = null
            var afterIndexList: List<StoryData>? = null
            if (indexPosition == 0) {
                beforeIndexList = list.subList(0, indexPosition)
                if (list.size != 1) {
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    return beforeIndexList
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            Log.d(
                "fagfgfgf11",
                "" + indexPosition + "|a" + list.size + "b|" + afterIndexList.size + "c|" + beforeIndexList.size
            )

            return afterIndexList + beforeIndexList
        }

        fun sortListStory(
            isDecrement: Boolean,
            indexPosition: Int,
            list: List<StoryData>
        ): List<StoryData> {
            var beforeIndexList: List<StoryData>? = null
            var afterIndexList: List<StoryData>? = null

            if (indexPosition == 0) {
                if (isDecrement) {
                    beforeIndexList = list.subList(0, indexPosition + 1)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    beforeIndexList = list.subList(0, indexPosition)
                    if (list.size != 1) {
                        //afterIndexList = list.subList(indexPosition+1, list.size)
                        afterIndexList = list.subList(indexPosition, list.size)
                    } else {
                        beforeIndexList = list.subList(0, 1)
                        return beforeIndexList
                    }
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition + 1)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList
        }

        fun sortListLearningTutorial(
            indexPosition: Int,
            list: List<LearningData>
        ): List<LearningData> {
            var beforeIndexList: List<LearningData>? = null
            var afterIndexList: List<LearningData>? = null
            if (indexPosition == 0) {
                beforeIndexList = list.subList(0, indexPosition)
                if (list.size != 1) {
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    return beforeIndexList
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            Log.d(
                "fagfgfgf11",
                "" + indexPosition + "|a" + list.size + "b|" + afterIndexList.size + "c|" + beforeIndexList.size
            )

            return afterIndexList + beforeIndexList
        }

        fun sortListLearningTutorial(
            isDecrement: Boolean,
            indexPosition: Int,
            list: List<LearningData>
        ): List<LearningData> {
            var beforeIndexList: List<LearningData>? = null
            var afterIndexList: List<LearningData>? = null

            if (indexPosition == 0) {
                if (isDecrement) {
                    beforeIndexList = list.subList(0, indexPosition + 1)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    beforeIndexList = list.subList(0, indexPosition)
                    if (list.size != 1) {
                        //afterIndexList = list.subList(indexPosition+1, list.size)
                        afterIndexList = list.subList(indexPosition, list.size)
                    } else {
                        beforeIndexList = list.subList(0, 1)
                        return beforeIndexList
                    }
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition + 1)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList
        }

        fun sortListWordsOffline(
            indexPosition: Int,
            list: List<DictionaryDataAPI>
        ): List<DictionaryDataAPI> {
            var beforeIndexList: List<DictionaryDataAPI>? = null
            var afterIndexList: List<DictionaryDataAPI>? = null

            if (indexPosition == 0) {
                beforeIndexList = list.subList(0, indexPosition)
                if (list.size != 1) {
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    return beforeIndexList
                }

            } else {
                beforeIndexList = list.subList(0, indexPosition)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            //1|a5b|3c|1
            Log.d(
                "fagfgfgf",
                "" + indexPosition + "|a" + list.size + "b|" + afterIndexList.size + "c|" + beforeIndexList.size
            )
            return afterIndexList + beforeIndexList
        }

        fun sortListWordsOffline(
            isDecrement: Boolean,
            indexPosition: Int,
            list: List<DictionaryDataAPI>
        ): List<DictionaryDataAPI> {
            var beforeIndexList: List<DictionaryDataAPI>? = null
            var afterIndexList: List<DictionaryDataAPI>? = null

            if (indexPosition == 0) {
                if (isDecrement) {
                    beforeIndexList = list.subList(0, indexPosition + 1)
                    afterIndexList = list.subList(indexPosition + 1, list.size)
                } else {
                    beforeIndexList = list.subList(0, indexPosition)
                    if (list.size != 1) {
                        //afterIndexList = list.subList(indexPosition+1, list.size)
                        afterIndexList = list.subList(indexPosition, list.size)
                    } else {
                        beforeIndexList = list.subList(0, 1)
                        return beforeIndexList
                    }
                }
            } else {
                beforeIndexList = list.subList(0, indexPosition + 1)
                afterIndexList = list.subList(indexPosition + 1, list.size)
            }
            return afterIndexList + beforeIndexList
        }

    }
}