package com.net.pslapllication.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.DictionaryTabListActivity
import com.net.pslapllication.activities.dictionary.VideoPreviewActivity
import com.net.pslapllication.data.Data
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.room.WordViewModelFactory
import com.net.pslapllication.room.WordsViewModel
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
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
import android.widget.TextView




class WordsFragment : Fragment() {

    private var items: List<Data>? =
        null
    private var act: Activity? = null
    public var adapter: Adapter? = null
    public var recyclerAdapter: RecyclerAdapter? = null
    private var prePosition = 0
    public var mainDictionaryList: List<DictionaryData>? = null


    var offset:Int  = 50
    lateinit var wordsViewModel: WordsViewModel

    private var alphabetString: String? = ""


    val parentlist: MutableList<Data> = java.util.ArrayList()
    public var childList: MutableList<DictionaryData>? =  java.util.ArrayList()


     var   datalist :  List<DictionaryDataAPI>?  =  null
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
        ProgressHelper.getInstance(activity!!).getViewModel().dataliveWords.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer {
                    data->
                if (data != null){
                    ///setData(data as List<DictionaryData>)
                    splitHeader(data)
                }
                else{

                }
            })

        ProgressHelper.getInstance(activity!!).getViewModel().getOffsetData(offset)
        setupAdapter(view)
//        val viewModelFactory = WordViewModelFactory(activity!!.applicationContext)
//        wordsViewModel = ViewModelProvider(this, viewModelFactory).get(WordsViewModel::class.java)

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

    class RecyclerAdapter (  val context: Activity,
                             var items: List<Data>?,
                             var mainDictionaryList: List<DictionaryData>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() , Filterable {
        var filteredList: List<Data>? = items
       /* override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): RecyclerAdapter.ViewHolder {
            if (viewType == null) {
                convertView = context.layoutInflater.inflate(R.layout.list_item, parent, false)
                viewHolder = WordsFragment.ViewHolder(convertView)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as WordsFragment.ViewHolder
            }
            return ViewHolder(binding)
        }*/

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val view: View = context.layoutInflater.inflate(R.layout.list_item, parent, false)
            return ViewHolder(view)
        }



        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
            holder.bind(filteredList!![position])
        }

        override fun getItemCount(): Int {
            return filteredList!!.size
        }

       inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
           val index: TextView
           val content: TextView
           val tv_words: TextView
           val word_row: RelativeLayout

           init {
                index = view.findViewById<View>(R.id.index) as TextView
                content = view.findViewById<View>(R.id.content) as TextView
                tv_words= view.findViewById(R.id.tv_words)
                word_row = view.findViewById(R.id.word_row)
               word_row.setOnClickListener(this)

           }

           override fun onClick(v: View?) {
               var position =  adapterPosition
               ReuseFunctions.preventTwoClick(word_row)
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


           fun bind(dataModel:Data){
               tv_words.text = dataModel.words.toString()
               index.text = dataModel.index
               content.text = dataModel.content
               index.visibility =
                   if (dataModel.showIndex) View.VISIBLE else View.GONE
           }
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
    /*override fun onDetach() {
        if ((activity as DictionaryTabListActivity).callWords!=null)
            (activity as DictionaryTabListActivity).callWords!!.cancel()
        super.onDetach()
    }*/


     fun setupAdapter(view: View){
         recyclerAdapter = RecyclerAdapter(act!!, parentlist, childList!!)

         var recycler =  view.findViewById<RecyclerView>(R.id.list)
        val  linearLayoutManager = LinearLayoutManager(context)
         recycler.layoutManager =  linearLayoutManager
         recycler.adapter = recyclerAdapter
         view.shimmer_layout.stopShimmerAnimation()
         view.shimmer_layout.visibility =  View.GONE


         recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
             override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                 super.onScrollStateChanged(recyclerView, newState)
                 val totalItemCount = recyclerView.layoutManager!!.itemCount
                 val lastVisiblePosition: Int = linearLayoutManager.findLastVisibleItemPosition()
                 if (lastVisiblePosition == childList!!.size - 1) {
                     Log.e("value",""+offset)
                     offset += 50
                     ProgressHelper.getInstance(activity!!).getViewModel().getOffsetData(offset)
                 }

             }
         })

     }
    fun splitHeader(dataList:List<DictionaryData>){

            var dictionaryCategoriesList = dataList.sortedBy { it.english_word }
            for (i in dictionaryCategoriesList.indices) {
                dictionaryCategoriesList[i].indexPosition = i
            }
      //  val list: MutableList<Data> = java.util.ArrayList()

        //  dictionaryCategoriesList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.english_word })

            for (i in dictionaryCategoriesList.indices) {


             var titleStr =  dictionaryCategoriesList[i].english_word.substring(0, 1)
                var isContain = parentlist.filter { it.index.toLowerCase().equals(titleStr.toLowerCase()) }

                var check =  isContain.isNullOrEmpty()
                var model =   Data(i,
                    dictionaryCategoriesList[i].english_word.substring(0, 1)
                        .toUpperCase(Locale.ROOT),
                    dictionaryCategoriesList[i].english_word,
                    check,
                    dictionaryCategoriesList[i].id,
                    "",
                    dictionaryCategoriesList[i].urdu_word
                )




                parentlist.add(
                    model
                )


                if(isContain.isNullOrEmpty()){

                   // alphabetString = dictionaryCategoriesList[i].english_word.substring(0, 1)
                }

               /* if (i == 0) {
                    alphabetString = dictionaryCategoriesList[0].english_word.substring(0, 1)


                    var inputstring =  dictionaryCategoriesList[i].english_word.substring(0, 1)
                        .toUpperCase(Locale.ROOT)

                    var isContain = parentlist.filter { it.index.equals(inputstring) }

                    if(isContain.isNullOrEmpty()){
                        var model =   Data(i,
                            dictionaryCategoriesList[i].english_word.substring(0, 1)
                                .toUpperCase(Locale.ROOT),
                            dictionaryCategoriesList[i].english_word,
                            true,
                            dictionaryCategoriesList[i].id,
                            "",
                            dictionaryCategoriesList[i].urdu_word
                        )




                        list.add(
                            model
                        )
                        alphabetString = dictionaryCategoriesList[i].english_word.substring(0, 1)
                    }



                   *//* var model =  Data(i,
                        dictionaryCategoriesList[0].english_word.substring(0, 1)
                            .toUpperCase(Locale.ROOT),
                        dictionaryCategoriesList[0].english_word,
                        true,
                        dictionaryCategoriesList[0].id,
                        "",
                        dictionaryCategoriesList[0].urdu_word
                    )
                    list.add(
                        model
                    )*//*
                } else {

                    if (alphabetString!!.equals(
                            dictionaryCategoriesList[i].english_word.substring(0, 1),
                            true
                        )
                    ) {
                        var inputstring =  dictionaryCategoriesList[i].english_word.substring(0, 1)
                            .toUpperCase(Locale.ROOT)

                        var isContain = parentlist.filter { it.index.equals(inputstring) }

                        if(isContain.isNullOrEmpty()){
                            var model =   Data(i,
                                dictionaryCategoriesList[i].english_word.substring(0, 1)
                                    .toUpperCase(Locale.ROOT),
                                dictionaryCategoriesList[i].english_word,
                                true,
                                dictionaryCategoriesList[i].id,
                                "",
                                dictionaryCategoriesList[i].urdu_word
                            )




                            list.add(
                                model
                            )
                            alphabetString = dictionaryCategoriesList[i].english_word.substring(0, 1)
                        }



                      *//*  var inputstring =  dictionaryCategoriesList[i].english_word.substring(0, 1)
                            .toUpperCase(Locale.ROOT)

                        var isContain = parentlist.filter { it.index.equals(inputstring) }
                        var  model =   Data(i,
                            dictionaryCategoriesList[i].english_word.substring(0, 1)
                                .toUpperCase(Locale.ROOT),
                            dictionaryCategoriesList[i].english_word,
                            false,
                            dictionaryCategoriesList[i].id,
                            "",
                            dictionaryCategoriesList[i].urdu_word
                        )

                        list.add(
                            model
                        )*//*
                    } else {
                        var inputstring =  dictionaryCategoriesList[i].english_word.substring(0, 1)
                            .toUpperCase(Locale.ROOT)

                        var isContain = parentlist.filter { it.index.equals(inputstring) }

                        if(isContain.isNullOrEmpty()){
                            var model =   Data(i,
                                dictionaryCategoriesList[i].english_word.substring(0, 1)
                                    .toUpperCase(Locale.ROOT),
                                dictionaryCategoriesList[i].english_word,
                                true,
                                dictionaryCategoriesList[i].id,
                                "",
                                dictionaryCategoriesList[i].urdu_word
                            )




                            list.add(
                                model
                            )
                            alphabetString = dictionaryCategoriesList[i].english_word.substring(0, 1)
                        }

                    }
                }*/

            }

            //     parentlist.addAll(list)
                childList?.addAll(dictionaryCategoriesList)

               recyclerAdapter!!.notifyDataSetChanged()
    }
}