package com.net.pslapllication.fragments

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.DictionarySingleWordActivity
import com.net.pslapllication.activities.dictionary.DictionaryTabListActivity
import com.net.pslapllication.data.Data
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.FastScroller
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.fragment_categories.view.*
import java.net.SocketException
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList

class CategoriesFragment : Fragment() {

    private var items: List<Data>? =
        null
    private var alphabetString: String? = null
    private var act: Activity? = null
    var adapter: Adapter? = null
    private var prePosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_categories, container, false)
        act = activity
        view.scroller!!.setTextView(view.section_title)
        view.scroller!!.setOnTouchingLetterChangedListener { s ->
            Handler(act!!.mainLooper).post {
                if (s.equals(FastScroller.HEART)) {
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
        // items = initData()


        return view

    }

    fun setAdapter(list: List<Data>?) {
        if (view != null) {
            items = list
            adapter = Adapter(act!!, items)
            view!!.list!!.adapter = adapter

        }
    }

    class Adapter(
        val context: Activity,
        var items: List<Data>?
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
                convertView = context.layoutInflater.inflate(R.layout.list_item_cat, parent, false)
                viewHolder = ViewHolder(convertView)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }

            if (position == 0) {
                alphabetString = getItem(position).index
            }
            Glide.with(context).load(getItem(position).image).into(viewHolder.image)
            Glide.with(context)
                .load(URLDecoder.decode(getItem(position).image, "UTF-8"))
                .into(viewHolder.image)

            viewHolder.tv_words.text = getItem(position).words.toString() + " Words"
            viewHolder.index.text = getItem(position).index
            viewHolder.content.text = getItem(position).content
            viewHolder.index.visibility =
                if (getItem(position).showIndex) View.VISIBLE else View.GONE
            viewHolder.word_row.setOnClickListener {
                Log.d("datasetset",getItem(position).words.toString())
                ReuseFunctions.preventTwoClick(viewHolder.word_row)
                var wordsInteger: Int = 0
                try {
                    if (getItem(position).words != "0" && getItem(position).words.isEmpty() &&
                        getItem(position).words != "null"
                    ) {
                        wordsInteger =  getItem(position).words.toInt()
                        Log.d("datasetset12", wordsInteger.toString())

                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                }

                ReuseFunctions.startNewActivityTaskWithParameterSingleWordCount(
                    context,
                    DictionarySingleWordActivity::class.java,
                    getItem(position).id.toString(),
                    getItem(position).content,
                    Constants.TYPE_DICTIONARY,
                    getItem(position).words.toInt()
                )
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
                                    .startsWith(charSearch.toLowerCase(Locale.ROOT))
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
        val image: ImageView = view.findViewById(R.id.image)
        val tv_words: TextView = view.findViewById(R.id.tv_words)
        val word_row: RelativeLayout = view.findViewById(R.id.word_row)

    }

    /*override fun onDetach() {
        super.onDetach()
        if ((activity as DictionaryTabListActivity).callCat != null)
            (activity as DictionaryTabListActivity).callCat!!.cancel()
    }*/
}