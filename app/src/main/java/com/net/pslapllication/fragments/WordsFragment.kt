package com.net.pslapllication.fragments

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.DictionaryTabListActivity
import com.net.pslapllication.activities.dictionary.VideoPreviewActivity
import com.net.pslapllication.data.Data
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.FastScroller
import com.net.pslapllication.util.ListSorting
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.fragment_categories.view.*
import kotlinx.android.synthetic.main.fragment_words.shimmer_layout

import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList

class WordsFragment : Fragment() {

    private var items: List<Data>? =
        null
    private var act: Activity? = null
    public var adapter: Adapter? = null
    private var prePosition = 0
    public var mainDictionaryList: List<DictionaryData>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_words, container, false)
        view.shimmer_layout.startShimmerAnimation()

        act = activity
        view.scroller!!.setTextView(view.section_title)
        view.scroller!!.setOnTouchingLetterChangedListener { s ->
            Handler(act!!.mainLooper).post {
                if (s == FastScroller.HEART) {
                    prePosition = 0
                    view.list!!.setSelectionFromTop(0, 0)
                } else {
                    if (adapter != null) {
                        val p: Int = adapter!!.getPositionForSection(s)
                        prePosition = if (p == 0) prePosition else p
                        list!!.setSelectionFromTop(prePosition, 0)
                    }
                }
            }
        }
        return view

    }

    public fun setAdapter(
        list: List<Data>?,
        dictionaryCategoriesList: List<DictionaryData>
    ) {
        if (view != null) {
            mainDictionaryList = dictionaryCategoriesList
            items = list
            adapter = Adapter(act!!, items, mainDictionaryList!!)
            view!!.list!!.adapter = adapter


            view!!.shimmer_layout.stopShimmerAnimation()
            view!!.shimmer_layout.visibility =  View.GONE

        }
    }

    class Adapter(
        val context: Activity,
        var items: List<Data>?,
        var mainDictionaryList: List<DictionaryData>
    ) : BaseAdapter(), Filterable {
        var filteredList: List<Data>? = items
        override fun getCount(): Int {

            return filteredList!!.size
        }

        override fun getItem(position: Int): Data {
            return filteredList!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var alphabetString: String? = null
            var convertView = convertView
            val viewHolder: ViewHolder
            if (convertView == null) {
                convertView = context.layoutInflater.inflate(R.layout.list_item, parent, false)
                viewHolder = ViewHolder(convertView)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }

            if (position == 0) {
                alphabetString = getItem(position).index
            }


            viewHolder.tv_words.text = getItem(position).words.toString()
            viewHolder.index.text = getItem(position).index
            viewHolder.content.text = getItem(position).content
            viewHolder.index.visibility =
                if (getItem(position).showIndex) View.VISIBLE else View.GONE
            viewHolder.word_row.setOnClickListener {

                ReuseFunctions.preventTwoClick(viewHolder.word_row)
                var newPos = 0
                if (filteredList!![position].indexPosition != 0) {

                    newPos = filteredList!![position].indexPosition
                    newPos--
                }

                //because selected index also required
                try {




                    var newIndexSortedList = ListSorting.sortList(
                        newPos,
                        mainDictionaryList
                    )
                    if (5 >= mainDictionaryList.size) {
                        //not index found
                    } else {
                        newIndexSortedList = newIndexSortedList.subList(0, 6)
                    }

                    ProgressHelper.getInstance(context)?.setList(newIndexSortedList)


                    //ReuseFunctions.preventTwoClick(gridView)
                    ReuseFunctions.startNewActivityDataModelParam(
                        context,
                        VideoPreviewActivity::class.java,
                        mainDictionaryList[filteredList!![position].indexPosition]
                        , Constants.TYPE_DICTIONARY
                    )
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
            return convertView!!
        }

        fun getPositionForSection(s: String): Int {
            for (i in filteredList!!.indices) {
                if (filteredList!![i].index == s) return i
            }
            return 0
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val filterResults = FilterResults()


                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        filteredList = items
                    } else {
                        val resultList = ArrayList<Data>()
                        for (row: Data in items!!) {
                            if (row.content.toLowerCase(Locale.ROOT)
                                    .contains(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        filteredList = resultList
                    }
                    filterResults.values = filteredList
                    return filterResults
                }

                @Suppress("UNCHECKED_CAST")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredList = results?.values as List<Data>
                    notifyDataSetChanged()
                }

            }
        }
    }

    internal class ViewHolder(view: View) {
        val index: TextView = view.findViewById<View>(R.id.index) as TextView
        val content: TextView = view.findViewById<View>(R.id.content) as TextView
         val tv_words: TextView = view.findViewById(R.id.tv_words)
        val word_row: RelativeLayout = view.findViewById(R.id.word_row)

    }
    /*override fun onDetach() {
        if ((activity as DictionaryTabListActivity).callWords!=null)
            (activity as DictionaryTabListActivity).callWords!!.cancel()
        super.onDetach()
    }*/
}