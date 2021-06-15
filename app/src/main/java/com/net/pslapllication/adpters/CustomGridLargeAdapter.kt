package com.net.pslapllication.adpters


import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.comix.rounded.RoundedCornerImageView
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.*
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.preferences.Dictionary_categories
import com.net.pslapllication.model.preferences.Life_skills
import com.net.pslapllication.model.preferences.Story_types
import com.net.pslapllication.model.preferences.Tut_grades
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ListSorting
import com.net.pslapllication.util.ReuseFunctions
import java.io.File
import java.net.URLDecoder
import java.security.AccessController.getContext
import java.util.*
import kotlin.collections.ArrayList


class CustomGridLargeAdapter(private val context: Context, var type: String) : BaseAdapter(),
    Filterable {
    private var diclist = emptyList<DictionaryListModel>()
    private var dicwordlist = emptyList<Dictionary_categories>()
    private var tutGradesList = emptyList<Tut_grades>()
    private var learningTutList = emptyList<Life_skills>()
    private var storyTypeList = emptyList<Story_types>()
    private var tutGradeSubVideoList = emptyList<TutorialData>()
    private var diclistFilter = emptyList<DictionaryListModel>()
    private var dicwordlistFilter = emptyList<Dictionary_categories>()
    private var tutGradesListFilter = emptyList<Tut_grades>()
    private var learningTutListFilter = emptyList<Life_skills>()
    private var storyTypeListFilter = emptyList<Story_types>()
    private var tutGradeSubVideoListFilter = emptyList<TutorialData>()
    private var tutorialType: String? = ""
    private var isDownloaded = false

    private var favList = emptyList<DictionaryData>()
    fun downloded(isDownloaded: Boolean) {
        this.isDownloaded = isDownloaded
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
        if (type == Constants.TYPE_DICTIONARY || type == Constants.TYPE_TEACHER_TUTORIAL ||
            type == Constants.TYPE_LEARNING_TUTORIAL ||
            type == Constants.TYPE_LEARNING_TUTORIAL_REAL ||
            type == Constants.TYPE_STORIES
        ) {

            val tv_word: TextView = gridView!!.findViewById(R.id.tv_word)
            val tv_translate: TextView = gridView!!.findViewById(R.id.tv_translate)
            val image_dic: ImageView = gridView.findViewById(R.id.image_dic)
            tv_word.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
            tv_translate.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
            when (type) {
                Constants.TYPE_DICTIONARY -> {
                    //set data
                    tv_word.text = dicwordlistFilter[position].title
                    tv_translate.text =
                        (dicwordlistFilter[position].videos.toString()) + context.getString(R.string.words)
                    Glide.with(context)
                        .load(URLDecoder.decode(dicwordlistFilter[position].image, "UTF-8"))
                        .into(image_dic)

                    gridView.setOnClickListener(
                        View.OnClickListener {
                            ReuseFunctions.preventTwoClick(gridView!!)
                            ReuseFunctions.startNewActivityTaskWithParameterSingleWordCount(
                                context,
                                DictionarySingleWordActivity::class.java,
                                dicwordlistFilter[position].id.toString(),
                                dicwordlistFilter[position].title,
                                type,
                                dicwordlistFilter[position].videos
                            )
                        }
                    )
                }
                Constants.TYPE_TEACHER_TUTORIAL -> {
                    var size = tutGradesListFilter[position].subjects.size
                    //set data
                    tv_word.text = tutGradesListFilter[position].grade
                    tv_translate.text = size.toString() + " " + context.getString(R.string.subjects)
                    if (tutGradesListFilter[position].icon != null &&
                        tutGradesList[position].icon != "null"
                    ) {
                        Glide.with(context)
                            .load(URLDecoder.decode(tutGradesListFilter[position].icon, "UTF-8"))
                            .into(image_dic)
                    } else {

                    }
                    gridView.setOnClickListener(
                        View.OnClickListener {
                            ReuseFunctions.preventTwoClick(gridView!!)

                            var dictionaryListCarrierDataModel = DictionaryListCarrierDataModel()
                            dictionaryListCarrierDataModel.setsubjectList(tutGradesListFilter[position].subjects)

                            ReuseFunctions.startNewActivityDataModelParam(
                                context,
                                DictionarySingleWordActivity::class.java,
                                tutGradesListFilter[position].id.toString(),

                                dictionaryListCarrierDataModel,
                                tutGradesListFilter[position].grade,
                                type
                            )
                        }
                    )
                }
                Constants.TYPE_LEARNING_TUTORIAL_REAL -> {
                    var size = tutGradesListFilter[position].subjects.size
                    //set data
                    tv_word.text = tutGradesListFilter[position].grade
                    tv_translate.text = size.toString() + " " + context.getString(R.string.subjects)
                    if (tutGradesListFilter[position].icon != null &&
                        tutGradesList[position].icon != "null"
                    ) {
                        Glide.with(context).load(tutGradesListFilter[position].icon)
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

                                    return false
                                }
                            })
                            .into(image_dic)

                    } else {

                    }
                    gridView.setOnClickListener(
                        View.OnClickListener {
                            ReuseFunctions.preventTwoClick(gridView!!)

                            var dictionaryListCarrierDataModel = DictionaryListCarrierDataModel()
                            dictionaryListCarrierDataModel.setsubjectList(tutGradesListFilter[position].subjects)

                            ReuseFunctions.startNewActivityDataModelParam(
                                context,
                                DictionarySingleWordActivity::class.java,
                                tutGradesListFilter[position].id.toString(),

                                dictionaryListCarrierDataModel,
                                tutGradesListFilter[position].grade,
                                type
                            )
                        }
                    )
                }

                Constants.TYPE_STORIES -> {
                    //set data
                    tv_word.text = storyTypeListFilter[position].title
                    tv_translate.text = storyTypeListFilter[position].videos.toString() + " Stories"
                    if (storyTypeListFilter[position].icon != null &&
                        storyTypeListFilter[position].icon != "null"
                    ) {
                        Glide.with(context).load(URLDecoder.decode(storyTypeListFilter[position].icon))
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

                                    return false
                                }
                            })
                            .into(image_dic)

                    } else {

                    }
                    gridView.setOnClickListener(
                        View.OnClickListener {
                            ReuseFunctions.preventTwoClick(gridView!!)
                            ReuseFunctions.startNewActivityTaskWithParameterSingleWordCount(
                                context,
                                DictionarySingleWordActivity::class.java,
                                storyTypeListFilter[position].id.toString(),
                                storyTypeListFilter[position].title,
                                type,
                                storyTypeListFilter[position].videos
                            )
                        }
                    )
                }
                Constants.TYPE_LEARNING_TUTORIAL -> {
                    //set data
                    tv_word.text = learningTutListFilter[position].title
                    tv_translate.text =
                        (learningTutListFilter[position].videos.toString()) + context.getString(R.string.subjects)

                    /*  Glide.with(context)
                          .load(URLDecoder.decode(tutGradesList[position].image, "UTF-8"))
                          .into(image_dic)*/

                    gridView.setOnClickListener(
                        View.OnClickListener {
                            ReuseFunctions.preventTwoClick(gridView!!)
                            ReuseFunctions.startNewActivityTaskWithParameterSingleWordCount(
                                context,
                                DictionarySingleWordActivity::class.java,
                                learningTutListFilter[position].id.toString(),
                                learningTutListFilter[position].title,
                                type,
                                learningTutListFilter[position].videos
                            )
                        }
                    )
                }

            }

        } else if (type == Constants.TYPE_CLASS_SUBJECT
            || type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_FAVOURITE ||
            type == Constants.TYPE_STORIES
        ) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            gridView = inflater.inflate(R.layout.row_large_cards, null)
            var tv_translate: TextView = gridView.findViewById(R.id.tv_translate)
            var tv_word: TextView = gridView.findViewById(R.id.tv_word)
            var tv_word2: TextView = gridView.findViewById(R.id.tv_word2)
            var imageViewRecent: RoundedCornerImageView =
                gridView.findViewById(R.id.imageViewRecent)
            tv_translate.visibility = View.GONE
            tv_word.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
            tv_word2.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
            /* if (type == Constants.TYPE_DOWNLOAD) {
                 Log.d("asdffgggggggh",""+Uri.parse(diclistFilter[position].wordTyhumb))
                 val filePathColumn =
                     arrayOf(MediaStore.Video.VideoColumns.DATA)
                 val cursor =
                     context.contentResolver.query(Uri.parse(diclistFilter[position].wordTyhumb), filePathColumn, null, null, null)
                 cursor!!.moveToFirst()
                 val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                 val picturePath = cursor.getString(columnIndex)
                 cursor.close()

                 val bitmap: Bitmap = ThumbnailUtils.createVideoThumbnail(
                     picturePath,
                     MediaStore.Video.Thumbnails.MICRO_KIND
                 )
                 Glide.with(context)
                     .load(bitmap)
                     .into(imageViewRecent)
                 */
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                     val thumbSize = Size(100, 100)
                    val VIDEO_THUMBNAIL_TABLE = arrayOf<String>(
                        MediaStore.Video.Media._ID,  // 0
                        MediaStore.Video.Media.DATA
                    )
                      val proj =
                         arrayOf(MediaStore.Images.Media.DATA)

                     val cur: Cursor? = context.contentResolver.query(
                        MediaStore.Video.Thumbnails.getContentUri("external"),
                        VIDEO_THUMBNAIL_TABLE,
                        MediaStore.Video.Thumbnails.VIDEO_ID
                            .toString() + " In ( select _id from video where _data =?)",
                         arrayOf(diclistFilter[position].wordTyhumb), null
                    )
                     val thumbColumn: Int =
                         cur!!.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                     val _thumpId: Int = cur.getInt(thumbColumn)
                     val imageUri_t: Uri = ContentUris.withAppendedId(
                         MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                         _thumpId.toLong()
                     )
                     Glide.with(context)
                         .load(imageUri_t)
                         .into(imageViewRecent)


                }else{

                    val requestOptions = RequestOptions()
                    requestOptions.isMemoryCacheable()
                    Glide.with(context).setDefaultRequestOptions(requestOptions).load(Uri.fromFile(
                        File(diclistFilter[position].wordTyhumb))
                    ).into(imageViewRecent)

                }*//*
            }*/
            tv_word.text = diclistFilter[position].wordName
            tv_word2.text = diclistFilter[position].wordDetail
            if (type == Constants.TYPE_CLASS_SUBJECT) {
                gridView.setOnClickListener(View.OnClickListener {
                    ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                        gridView!!.context, SubjectTopicListActivity::class.java,
                        diclistFilter[position].id.toString(), diclistFilter[position].wordName,
                        this@CustomGridLargeAdapter.type
                    )
                })
            } else if (type == Constants.TYPE_FAVOURITE) {
                gridView.setOnClickListener {
                    ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                        gridView!!.context, VideoPreviewActivity::class.java,
                        diclistFilter[position].id.toString(), diclistFilter[position].wordName,
                        this@CustomGridLargeAdapter.type
                    )
                }
            } else if (type == Constants.TYPE_DOWNLOAD) {
                gridView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(
                        Uri.parse(diclistFilter[position].wordTyhumb),
                        "video/mp4"
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            } else {
                gridView.setOnClickListener {
                    ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                        gridView!!.context, ClassSubjectListActivity::class.java,
                        diclistFilter[position].id.toString(), diclistFilter[position].wordName,
                        this@CustomGridLargeAdapter.type
                    )
                }
            }
        } else if (type == Constants.TYPE_SUB_TOPIC) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            gridView = inflater.inflate(R.layout.row_large_cards, null)
            var tv_translate: TextView = gridView.findViewById(R.id.tv_translate)
            var tv_word: TextView = gridView.findViewById(R.id.tv_word)
            var tv_no_videos: TextView = gridView.findViewById(R.id.tv_no_videos)
            var imageViewTick: ImageView = gridView.findViewById(R.id.imageViewTick)
            var imageViewRecent: ImageView =
                gridView.findViewById(R.id.imageViewRecent)
            tv_translate.visibility = View.GONE
            tv_no_videos.visibility = View.GONE
            tv_word.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
            tv_no_videos.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
            try {
                Glide.with(context)
                    .load(URLDecoder.decode(tutGradeSubVideoListFilter[position].poster))
                    .into(imageViewRecent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (isDownloaded) {
                imageViewTick?.visibility = View.VISIBLE
            } else {
                imageViewTick?.visibility = View.GONE
            }
            tv_word.text = tutGradeSubVideoListFilter[position].title


            gridView.setOnClickListener {
                ReuseFunctions.preventTwoClick(gridView!!)

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
                    //model.setModelList(diclistFilter)
                    // val dictionaryListCarrierDataModel:DictionaryListCarrierDataModel? = DictionaryListCarrierDataModel()
                    var newIndexSortedList = ListSorting.sortListTutorial(
                        isDecrement,
                        newPos,
                        tutGradeSubVideoListFilter
                    )


                    if (5 >= tutGradeSubVideoListFilter.size) {
                        //not index found
                    } else {
                        newIndexSortedList = newIndexSortedList.subList(0, 6)
                    }
                    //    ProgressHelper.getInstance(context)?.setModelInstance(dictionaryListCarrierDataModel!!)
                    ProgressHelper.getInstance(context)?.setTutorialList(newIndexSortedList)


                    ReuseFunctions.startNewActivityDataModelParam(
                        context,
                        VideoPreviewTutorialActivity::class.java,
                        tutGradeSubVideoListFilter[position],
                        tutorialType!!,
                        type,isDownloaded
                    )
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }

            }

        } else if (type == Constants.TYPE_DOWNLOAD) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            gridView = inflater.inflate(R.layout.row_large_cards, null)
            var tv_translate: TextView = gridView.findViewById(R.id.tv_translate)
            var tv_word: TextView = gridView.findViewById(R.id.tv_word)

            var imageViewRecent: ImageView =
                gridView.findViewById(R.id.imageViewRecent)
            tv_translate.visibility = View.GONE
            /*val bMap = ThumbnailUtils.createVideoThumbnail(
                File(diclistFilter[position].wordTyhumb).toString(),
                MediaStore.Video.Thumbnails.MINI_KIND
            )
             imageViewRecent.setImageBitmap(bMap)*/
            Glide
                .with(context)
                .load(File(diclistFilter[position].wordTyhumb).toString())
                .into(imageViewRecent)
            val requestOptions = RequestOptions()
            /*Glide.with(context)
                .load("video_url")
                .apply(requestOptions)
                .thumbnail(Glide.with(context).load("video_url"))
                .into(imageViewRecent)*/
            tv_word.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)

            tv_word.text = diclistFilter[position].wordName


            gridView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(
                    Uri.parse(diclistFilter[position].wordTyhumb),
                    "video/mp4"
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

        }


        return gridView
    }

    override fun getCount(): Int {
        return when (type) {
            Constants.TYPE_DICTIONARY -> {
                dicwordlistFilter.size
            }
            Constants.TYPE_TEACHER_TUTORIAL -> {
                tutGradesListFilter.size
            }
            Constants.TYPE_LEARNING_TUTORIAL -> {
                learningTutListFilter.size
            }
            Constants.TYPE_LEARNING_TUTORIAL_REAL -> {
                tutGradesListFilter.size
            }
            Constants.TYPE_STORIES -> {
                storyTypeListFilter.size
            }
            Constants.TYPE_SUB_TOPIC -> {
                tutGradeSubVideoListFilter.size
            }
            else -> {
                diclistFilter.size
            }
        }

    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    internal fun setWords(users: List<DictionaryListModel>) {
        this.diclist = users
        this.diclistFilter = users
        notifyDataSetChanged()
    }

    internal fun setDicWords(dictionaryCategories: List<Dictionary_categories>) {
        this.dicwordlist = dictionaryCategories
        this.dicwordlistFilter = dictionaryCategories
        notifyDataSetChanged()
    }

    internal fun setTutGrades(tutGradesList: List<Tut_grades>) {
        this.tutGradesList = tutGradesList
        this.tutGradesListFilter = tutGradesList
        notifyDataSetChanged()
    }

    internal fun setTeacherTutorialSubjectList(tutGradesSubList: List<TutorialData>) {
        this.tutGradeSubVideoList = tutGradesSubList
        this.tutGradeSubVideoListFilter = tutGradesSubList
        notifyDataSetChanged()
    }

    internal fun setLearningTut(learningTutList: List<Life_skills>) {
        this.learningTutList = learningTutList
        this.learningTutListFilter = learningTutList
        notifyDataSetChanged()
    }

    internal fun setStoriesTypes(storyTypeList: List<Story_types>) {
        this.storyTypeList = storyTypeList
        this.storyTypeListFilter = storyTypeList
        notifyDataSetChanged()
    }

    internal fun setTutorialType(tutorialType: String) {
        this.tutorialType = tutorialType

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (type == Constants.TYPE_DICTIONARY) {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        dicwordlistFilter = dicwordlist
                    } else {
                        val resultList = ArrayList<Dictionary_categories>()
                        for (row: Dictionary_categories in dicwordlist) {
                            if (row.title.toLowerCase(Locale.ROOT)
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        dicwordlistFilter = resultList
                    }

                    filterResults.values = dicwordlistFilter
                } else if (type == Constants.TYPE_SUB_TOPIC) {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        tutGradeSubVideoListFilter = tutGradeSubVideoList
                    } else {
                        val resultList = ArrayList<TutorialData>()
                        for (row: TutorialData in tutGradeSubVideoList) {
                            if (row.title.toLowerCase(Locale.ROOT)
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        tutGradeSubVideoListFilter = resultList
                    }
                    filterResults.values = tutGradeSubVideoListFilter
                } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        learningTutListFilter = learningTutList
                    } else {
                        val resultList = ArrayList<Life_skills>()
                        for (row: Life_skills in learningTutListFilter) {
                            if (row.title.toLowerCase(Locale.ROOT)
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        learningTutListFilter = resultList
                    }
                    filterResults.values = learningTutListFilter
                } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        tutGradesListFilter = tutGradesList
                    } else {
                        val resultList = ArrayList<Tut_grades>()
                        for (row: Tut_grades in tutGradesList) {
                            if (row.grade.toLowerCase(Locale.ROOT)
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        tutGradesListFilter = resultList
                    }

                    filterResults.values = tutGradesListFilter
                } else if (type == Constants.TYPE_STORIES) {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        storyTypeListFilter = storyTypeList
                    } else {
                        val resultList = ArrayList<Story_types>()
                        for (row: Story_types in storyTypeList) {
                            if (row.title.toLowerCase(Locale.ROOT)
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        storyTypeListFilter = resultList
                    }

                    filterResults.values = storyTypeListFilter
                }

                return filterResults


            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (type == Constants.TYPE_DICTIONARY) {
                    dicwordlistFilter = results?.values as List<Dictionary_categories>
                } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                    tutGradesListFilter = results?.values as List<Tut_grades>
                } else if (type == Constants.TYPE_STORIES) {
                    storyTypeListFilter = results?.values as List<Story_types>
                } else if (type == Constants.TYPE_SUB_TOPIC) {
                    tutGradeSubVideoListFilter = results?.values as List<TutorialData>
                } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
                    learningTutListFilter = results?.values as List<Life_skills>
                }

                notifyDataSetChanged()
            }

        }
    }
}