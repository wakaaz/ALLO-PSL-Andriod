package com.net.pslapllication.adpters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.*
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.favouriteList.Data
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.preferences.Subjects
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ListSorting
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.row_single_word_detail_new.view.*
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList

class LargeCardAdapter(var context: Context, var type: String, var name: String) :
    BaseAdapter(), Filterable {
    private var diclist = emptyList<DictionaryData>()
    private var diclistFilter = emptyList<DictionaryData>()
    private var sublist = emptyList<Subjects>()
    private var sublistFilter = emptyList<Subjects>()
    private var storyList = emptyList<StoryData>()
    private var storyListFilter = emptyList<StoryData>()
    private var favList = emptyList<Data>()
    private var favListFilter = emptyList<Data>()
    private var learningList = emptyList<LearningData>()
    private var learningListFilter = emptyList<LearningData>()
    private var isDownloaded = false
    private var subjectName: String = ""
    private var fav_video_type: String = ""
    private var isEnglishVersion: Boolean = true
    private var overAllstoryList = emptyList<StoryData>()

    fun downloded(isDownloaded: Boolean) {
        this.isDownloaded = isDownloaded
    }

    fun changeLanguage(isEnglishVersion: Boolean){
        this.isEnglishVersion =  isEnglishVersion
    }

    fun setAllStories(overAllstoryList: List<StoryData>){
        this.overAllstoryList =  overAllstoryList
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var gridView: View? = convertView

        if (gridView == null) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            gridView = inflater.inflate(R.layout.row_large_cards, null)

        } else {
            gridView = convertView as View
        }
        val tv_word: TextView? = gridView?.tv_word
        val tv_no_videos: TextView? = gridView?.tv_no_videos
        //val tv_word2: TextView? = gridView?.tv_word2
        val tv_translate: TextView? = gridView?.tv_translate
        val imageViewRecent: ImageView? = gridView?.imageViewRecent
        val imageViewTick: ImageView? = gridView?.imageViewTick
        val constraint_click: ConstraintLayout? = gridView?.constraint_click
        // set font
        tv_word?.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
        //tv_word2?.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
        tv_translate?.typeface =
            ResourcesCompat.getFont(context, R.font.jameelnoorinastaleeqregular)
        if (isDownloaded) {
            imageViewTick?.visibility = View.VISIBLE
        } else {
            imageViewTick?.visibility = View.GONE
        }
        if (type == Constants.TYPE_FAVOURITE) {
            //set text
            if (favListFilter[position].dict_video_id != 0 && favListFilter[position].learning_tut_video_id == 0 &&
                favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
            ) {
                tv_word?.text = favListFilter[position].dictionary.english_word
             tv_translate?.text = favListFilter[position].dictionary.urdu_word
            if (!favListFilter[position].dictionary.poster.equals("")) {
                val poster: String = URLDecoder.decode(favListFilter[position].dictionary.poster)
                Glide.with(context).load(poster)
                     .into(imageViewRecent!!)
            }
            } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id != 0 &&
                favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
            ) {
                tv_word?.text = favListFilter[position].learningTutorial.title
               // tv_translate?.text = favListFilter[position].dictionary.urdu_word
                if (!favListFilter[position].learningTutorial.poster.equals("")) {
                    val poster: String = URLDecoder.decode(favListFilter[position].learningTutorial.poster)
                    Glide.with(context).load(poster)
                        .into(imageViewRecent!!)
                }
            } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
                favListFilter[position].tut_video_id != 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
            ) {
                tv_word?.text = favListFilter[position].tutorial.title
                // tv_translate?.text = favListFilter[position].dictionary.urdu_word
                if (!favListFilter[position].tutorial.poster.equals("")) {
                    val poster: String = URLDecoder.decode(favListFilter[position].tutorial.poster)
                    Glide.with(context).load(poster)
                        .into(imageViewRecent!!)
                }
            } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
                favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id != 0 && favListFilter[position].story_video_id == 0
            ) {
                tv_word?.text = favListFilter[position].lesson.title
                // tv_translate?.text = favListFilter[position].dictionary.urdu_word
                if (!favListFilter[position].lesson.poster.equals("")) {
                    val poster: String = URLDecoder.decode(favListFilter[position].lesson.poster)
                    Glide.with(context).load(poster)
                        .into(imageViewRecent!!)
                }
            } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
                favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id != 0
            ) {
                tv_word?.text = favListFilter[position].story.title
                // tv_translate?.text = favListFilter[position].dictionary.urdu_word
                if (!favListFilter[position].story.poster.equals("")) {
                    val poster: String = URLDecoder.decode(favListFilter[position].story.poster)
                    Glide.with(context).load(poster)
                        .into(imageViewRecent!!)
                }
            }


        } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
            //set text
            tv_word?.setLines(1)
            tv_word?.ellipsize = TextUtils.TruncateAt.END
            tv_word?.text = sublistFilter[position].title
            //tv_word2?.text = favList[position].urdu_word
            tv_no_videos?.visibility = View.VISIBLE
            tv_translate?.visibility = View.GONE
            tv_no_videos?.text = sublistFilter[position].videos.toString() + " Videos"
             if (!sublistFilter[position].icon.equals("")) {
                 val poster: String = URLDecoder.decode(sublistFilter[position].icon)
                 Glide.with(context).load(poster)
                     .listener(object : RequestListener<Drawable> {
                         override fun onLoadFailed(
                                 p0: GlideException?,
                                 p1: Any?,
                                 target: com.bumptech.glide.request.target.Target<Drawable>?,
                                 p3: Boolean
                         ): Boolean {
                             var errorString = p0?.localizedMessage
                             return false
                         }

                         override fun onResourceReady(
                                 p0: Drawable?,
                                 p1: Any?,
                                 target: com.bumptech.glide.request.target.Target<Drawable>?,
                                 p3: DataSource?,
                                 p4: Boolean
                         ): Boolean {
                             //do something when picture already loaded
                             return false
                         }
                     })
                     .into(imageViewRecent!!)
             }
        } else if (type == Constants.TYPE_STORIES) {
            //set text


            tv_word?.text = storyListFilter[position].title
            tv_word?.setLines(3)
            tv_word?.ellipsize = TextUtils.TruncateAt.END
            tv_translate?.visibility = View.GONE

            if (!storyListFilter[position].poster.equals("")) {
                val poster: String = URLDecoder.decode(storyListFilter[position].poster)
                Glide.with(context).load(poster)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                                p0: GlideException?,
                                p1: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                p3: Boolean
                        ): Boolean {
                            var errorString = p0?.localizedMessage
                            return false
                        }

                        override fun onResourceReady(
                                p0: Drawable?,
                                p1: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                p3: DataSource?,
                                p4: Boolean
                        ): Boolean {
                            //do something when picture already loaded
                            return false
                        }
                    })
                    .into(imageViewRecent!!)
            }
        } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
            //set text
            tv_word?.text = learningListFilter[position].title
            tv_word?.setLines(3)
            tv_word?.ellipsize = TextUtils.TruncateAt.END
            tv_translate?.visibility = View.GONE

            if (!learningListFilter[position].poster.equals("")) {
                val poster: String = URLDecoder.decode(learningListFilter[position].poster)
                Glide.with(context).load(poster)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                                p0: GlideException?,
                                p1: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                p3: Boolean
                        ): Boolean {
                            var errorString = p0?.localizedMessage
                            return false
                        }

                        override fun onResourceReady(
                                p0: Drawable?,
                                p1: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                p3: DataSource?,
                                p4: Boolean
                        ): Boolean {
                            //do something when picture already loaded
                            return false
                        }
                    })
                    .into(imageViewRecent!!)
            }
        } else {

            //set text
            tv_word?.text = diclistFilter[position].english_word
            //tv_word2?.text = diclistFilter[position].urdu_word
            tv_translate?.text = " " + diclistFilter[position].urdu_word
            if (!diclistFilter[position].poster.equals("")) {
                val poster: String = URLDecoder.decode(diclistFilter[position].poster)
                Glide.with(context).load(poster)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                                p0: GlideException?,
                                p1: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                p3: Boolean
                        ): Boolean {
                            var errorString = p0?.localizedMessage
                            return false
                        }

                        override fun onResourceReady(
                                p0: Drawable?,
                                p1: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                p3: DataSource?,
                                p4: Boolean
                        ): Boolean {
                            //do something when picture already loaded
                            return false
                        }
                    })
                    .into(imageViewRecent!!)
            }
        }


        constraint_click?.setOnClickListener(
                View.OnClickListener {
                    //ReuseFunctions.preventTwoClick(gridView)

                    if (type == Constants.TYPE_FAVOURITE) {
                        if (favListFilter[position].dict_video_id != 0 && favListFilter[position].learning_tut_video_id == 0 &&
                                favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
                        ) {
                            fav_video_type = Constants.FAV_DICTIONARY
                        } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id != 0 &&
                                favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
                        ) {
                            fav_video_type = Constants.FAV_LEARNING_TUT

                        } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
                                favListFilter[position].tut_video_id != 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
                        ) {
                            fav_video_type = Constants.FAV_TEACHER_TUT

                        } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
                                favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id != 0 && favListFilter[position].story_video_id == 0
                        ) {
                            fav_video_type = Constants.FAV_SKILL

                        } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
                                favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id != 0
                        ) {
                            fav_video_type = Constants.FAV_STORY

                        }


                        //setList for preview
                        var isDecrement = false
                        var newPos = 0
                        if (position != 0) {
                            isDecrement = true
                            newPos = position
                            newPos--
                        } else {
                            isDecrement = false
                        }
                        //because selected index also required
                        try {

                            var newIndexSortedList = ListSorting.sortListFavourite(isDecrement,
                                    favListFilter[newPos].indexPosition,
                                    favListFilter
                            )


                            if (5 >= favListFilter.size) {
                                //not index found
                            } else {
                                newIndexSortedList = newIndexSortedList.subList(0, 6)
                            }
                            //    ProgressHelper.getInstance(context)?.setModelInstance(dictionaryListCarrierDataModel!!)
                            ProgressHelper.getInstance(context)?.setFavList(newIndexSortedList)


                            ReuseFunctions.startNewActivityDataModelParam(
                                    context,
                                    VideoPreviewFavouriteActivity::class.java,
                                    favListFilter[position],
                                    fav_video_type
                            )
                        } catch (e: IndexOutOfBoundsException) {
                            e.printStackTrace()
                        }

                    } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                        try {
                            val intent = Intent(context, SubjectTopicListActivity::class.java)
                            intent.putExtra("GRADE_ID", sublistFilter[position].grade_id)
                            intent.putExtra("GRADE_NAME", subjectName)
                            intent.putExtra("SUBJECT_ID", sublistFilter[position].id)
                            intent.putExtra("SUBJECT_NAME", sublistFilter[position].title)
                            intent.putExtra(Constants.CETAGORY_TYPE, type)
                            context.startActivity(intent)

                        } catch (e: IndexOutOfBoundsException) {
                            e.printStackTrace()
                        }
                    } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
                        var isDecrement = false
                        var newPos = 0
                        if (position != 0) {
                            isDecrement = true
                            newPos = position
                            newPos--
                        } else {
                            isDecrement = false
                        }

                        //because selected index also required
                        try {
                            //model.setModelList(diclistFilter)
                            //       val dictionaryListCarrierDataModel:DictionaryListCarrierDataModel? = DictionaryListCarrierDataModel()
                            var newIndexSortedList = ListSorting.sortListLearningTutorial(isDecrement,
                                    learningListFilter[newPos].indexPosition,
                                    learningListFilter)


                            if (5 >= learningListFilter.size) {
                                //not index found
                            } else {
                                newIndexSortedList = newIndexSortedList.subList(0, 6)
                            }
                            //    ProgressHelper.getInstance(context)?.setModelInstance(dictionaryListCarrierDataModel!!)
                            ProgressHelper.getInstance(context)?.setLearningList(newIndexSortedList)


                            //ReuseFunctions.preventTwoClick(gridView)
                            ReuseFunctions.startNewActivityDataModelParam(
                                    context,
                                    VideoPreviewLearningTutorialActivity::class.java,
                                    learningListFilter[position], type
                            )
                        } catch (e: IndexOutOfBoundsException) {
                            e.printStackTrace()
                        }
                    } else if (type == Constants.TYPE_DICTIONARY) {
                        var isDecrement = false
                        var newPos = 0
                        if (position != 0) {
                            isDecrement = true
                            newPos = position
                            newPos--
                        } else {
                            isDecrement = false
                        }

                        //because selected index also required
                        try {
                            //model.setModelList(diclistFilter)
                            //       val dictionaryListCarrierDataModel:DictionaryListCarrierDataModel? = DictionaryListCarrierDataModel()
                            var newIndexSortedList = ListSorting.sortList(isDecrement,
                                    diclistFilter[newPos].indexPosition,
                                    diclistFilter
                            )


                            if (5 >= diclist.size) {
                                //not index found
                            } else {
                                newIndexSortedList = newIndexSortedList.subList(0, 6)
                            }
                            //    ProgressHelper.getInstance(context)?.setModelInstance(dictionaryListCarrierDataModel!!)
                            ProgressHelper.getInstance(context)?.setList(newIndexSortedList)

                            if (type == Constants.TYPE_DICTIONARY)
                            //ReuseFunctions.preventTwoClick(gridView)
                                ReuseFunctions.startNewActivityDataModelParam(
                                        context,
                                        VideoPreviewActivity::class.java,
                                        diclistFilter[position], type
                                )
                        } catch (e: IndexOutOfBoundsException) {
                            e.printStackTrace()
                        }
                    } else if (type == Constants.TYPE_STORIES) {
                        var isDecrement = false
                        var newPos = 0
                        if (position != 0) {
                            isDecrement = true
                            newPos = position
                            newPos--
                        } else {
                            isDecrement = false
                        }

                        //because selected index also required
                        try {
                            //model.setModelList(diclistFilter)
                            //       val dictionaryListCarrierDataModel:DictionaryListCarrierDataModel? = DictionaryListCarrierDataModel()
                            var newIndexSortedList = ListSorting.sortListStory(isDecrement,
                                    storyListFilter[newPos].indexPosition,
                                    storyListFilter
                            )

                            var model: StoryData?
                            if (5 >= storyListFilter.size) {
                                //not index found

                            } else {
                                newIndexSortedList = newIndexSortedList.subList(0, 6)

                            }

                          val  alternativelist =  f(overAllstoryList,newIndexSortedList);
                            Log.e("",""+alternativelist.size)


                            //    ProgressHelper.getInstance(context)?.setModelInstance(dictionaryListCarrierDataModel!!)
                            ProgressHelper.getInstance(context)?.setListStory(newIndexSortedList)


                            if(isEnglishVersion){
                                ProgressHelper.getInstance(context)?.setEnglishListStory(newIndexSortedList)
                                ProgressHelper.getInstance(context)?.setUrduListStory(alternativelist)


                            }else{
                                ProgressHelper.getInstance(context)?.setEnglishListStory(alternativelist)
                                ProgressHelper.getInstance(context)?.setUrduListStory(newIndexSortedList)
                            }


                            //ReuseFunctions.preventTwoClick(gridView)
                            ReuseFunctions.startNewActivityDataModelParam(
                                    context,
                                    VideoPreviewStoryActivity::class.java,
                                    storyListFilter[position], type,isEnglishVersion
                            )
                        } catch (e: IndexOutOfBoundsException) {
                            e.printStackTrace()
                        }
                    }

                }
        )

        return gridView
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return if (type == Constants.TYPE_FAVOURITE) {
            favListFilter.size
        } else if (type == Constants.TYPE_STORIES) {
            storyListFilter.size
        } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
            sublistFilter.size
        } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
            learningListFilter.size
        } else {
            diclistFilter.size
        }

    }


    internal fun setWords(users: List<DictionaryData>) {
        this.diclist = users
        this.diclistFilter = users
        notifyDataSetChanged()
    }

    internal fun setStories(storyList: List<StoryData>?) {
        this.storyList = storyList!!
        this.storyListFilter = storyList!!
        notifyDataSetChanged()
    }

    internal fun setFavWords(users: List<Data>) {
        this.favList = users
        this.favListFilter = users
        notifyDataSetChanged()
    }

    internal fun setSubjects(subList: List<Subjects>) {
        this.sublist = subList
        this.sublistFilter = subList
        notifyDataSetChanged()
    }

    internal fun setLearningTutorial(subList: List<LearningData>) {
        this.learningList = subList
        this.learningListFilter = subList
        notifyDataSetChanged()
    }

    internal fun setSubjects(subList: List<Subjects>, subjectName: String) {
        this.sublist = subList
        this.sublistFilter = subList
        this.subjectName = subjectName
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val filterResults = FilterResults()

                if (type == Constants.TYPE_DICTIONARY) {
                    if (charSearch.isEmpty()) {
                        diclistFilter = diclist
                    } else {
                        val resultList = ArrayList<DictionaryData>()
                        for (row: DictionaryData in diclist) {
                            if (row.english_word.toLowerCase()
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        diclistFilter = resultList
                    }

                    filterResults.values = diclistFilter
                } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                    if (charSearch.isEmpty()) {
                        sublistFilter = sublist
                    } else {
                        val resultList = ArrayList<Subjects>()
                        for (row: Subjects in sublist) {
                            if (row.title.toLowerCase()
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        sublistFilter = resultList
                    }

                    filterResults.values = sublistFilter
                }   else if (type == Constants.TYPE_STORIES) {
                    if (charSearch.isEmpty()) {
                        storyListFilter = storyList
                    } else {
                        val resultList = ArrayList<StoryData>()
                        for (row: StoryData in storyList) {
                            if (row.title.toLowerCase()
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        storyListFilter = resultList
                    }

                    filterResults.values = storyListFilter
                } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
                    if (charSearch.isEmpty()) {
                        learningListFilter = learningList
                    } else {
                        val resultList = ArrayList<LearningData>()
                        for (row: LearningData in learningList) {
                            if (row.title.toLowerCase()
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        learningListFilter = resultList
                    }

                    filterResults.values = learningListFilter
                }else if (type == Constants.TYPE_FAVOURITE) {
                    if (charSearch.isEmpty()) {
                        favListFilter = favList
                    } else {
                        val resultList = ArrayList<Data>()
                        for (row: Data in favList) {
                            if (row.videoname.toLowerCase()
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        favListFilter = resultList
                    }

                    filterResults.values = favListFilter
                }
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (type == Constants.TYPE_DICTIONARY) {
                    diclistFilter = results?.values as List<DictionaryData>
                } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                    sublistFilter = results?.values as List<Subjects>
                } else if (type == Constants.TYPE_FAVOURITE) {
                    favListFilter = results?.values as List<Data>
                } else if (type == Constants.TYPE_STORIES) {
                    storyListFilter = results?.values as List<StoryData>
                } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
                    learningListFilter = results?.values as List<LearningData>
                }

                notifyDataSetChanged()
            }

        }
    }

    private fun intersect(A: List<String>, B: List<String>): List<String>? {
        val rtnList: MutableList<String> = LinkedList()
        for (dto in A) {
            if (B.contains(dto)) {
                rtnList.add(dto)
            }
        }
        return rtnList
    }


    fun f(mainModelList: List<StoryData>, selectedList: List<StoryData>) : List<StoryData> {
        return mainModelList.filter { selectedList.map { it.parent }.contains ( it.id ) }
    }
}