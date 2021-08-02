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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.SubjectTopicListActivity
import com.net.pslapllication.activities.dictionary.VideoPreviewActivity
import com.net.pslapllication.activities.dictionary.VideoPreviewOfflineActivity
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.interfaces.OnQuerryChangeListener
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.favouriteList.Data
import com.net.pslapllication.model.preferences.Story_types
import com.net.pslapllication.model.preferences.Subjects
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ListSorting
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.row_single_word_detail_new.view.*
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList

class SearchedWordsAdapter(var context: Context,var listener : OnQuerryChangeListener) :
    BaseAdapter(), Filterable {
    private var diclist = emptyList<DictionaryDataAPI>()
    public var diclistFilter = emptyList<DictionaryDataAPI>()
    public var diclistFilterResult = emptyList<DictionaryDataAPI>()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var gridView: View? = convertView

        if (gridView == null) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            gridView = inflater.inflate(R.layout.row_single_word_detail_new, null)

        } else {
            gridView = convertView as View
        }
        val tv_word: TextView? = gridView?.tv_word
        val tv_translate: TextView? = gridView?.tv_translate
        val imageViewRecent: ImageView? = gridView?.imageViewRecent
        val constraint_click: ConstraintLayout? = gridView?.constraint_click
        // set font
        tv_word?.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
        //tv_word2?.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
        tv_translate?.typeface =
            ResourcesCompat.getFont(context, R.font.jameelnoorinastaleeqregular)


        //set text
        tv_word?.text = diclistFilter[position].english_word
        tv_translate?.text = " " + diclistFilter[position].urdu_word
        if (!diclistFilter[position].poster.equals("")) {
            val poster: String = URLDecoder.decode(diclistFilter[position].poster)
            Glide.with(context).load(poster).diskCacheStrategy(DiskCacheStrategy.DATA)
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



        constraint_click?.setOnClickListener(
            View.OnClickListener {

                var isDecrement =false
                var newPos = 0
                if (position != 0) {
                    isDecrement = true
                    newPos = position
                    newPos--
                }else{
                    isDecrement = false
                }
                var value = newPos
                //because selected index also required
                try {

                    var newIndexSortedList = ListSorting.sortListWordsOffline(isDecrement,
                        newPos,
                        diclistFilter
                    )


                    if (5 >= diclist.size) {
                        //not index found
                    } else {

                       val dummylist = newIndexSortedList.subList(0, 6)
                        val readylist:List<DictionaryDataAPI> =  arrayListOf<DictionaryDataAPI>()
                        for (item in dummylist.indices) {
                            // body of loop
                            var modelitem = dummylist[item]
                            modelitem.indexPosition = item
                            //dummylist.get(item) = modelitem
                        }
                        newIndexSortedList =  readylist
                    }
                    //    ProgressHelper.getInstance(context)?.setModelInstance(dictionaryListCarrierDataModel!!)
                    ProgressHelper.getInstance(context)?.setListOffline(newIndexSortedList)


                    //ReuseFunctions.preventTwoClick(gridView)
                    ReuseFunctions.startNewActivityDataModelParam(
                        context,
                        VideoPreviewOfflineActivity::class.java,
                        diclistFilter[position]
                        , Constants.TYPE_DICTIONARY
                    )
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
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
        return diclistFilter.size
    }


    internal fun setWords(users: List<DictionaryDataAPI>) {
        this.diclist = users
        this.diclistFilter = users
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val filterResults = FilterResults()


                if (charSearch.isEmpty()) {
                    diclistFilter = diclist
                } else {
                    val resultList = ArrayList<DictionaryDataAPI>()
                    for (row: DictionaryDataAPI in diclist) {
                        if (row.english_word.toLowerCase()
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    diclistFilter = resultList
                }

                filterResults.values = diclistFilter

                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                diclistFilter = results?.values as List<DictionaryDataAPI>
                diclistFilterResult = diclistFilter
                 listener.onSearchResult(diclistFilter.size)
                notifyDataSetChanged()
            }

        }
    }

}