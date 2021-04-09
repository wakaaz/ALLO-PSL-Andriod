package com.net.pslapllication.fragments

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.net.pslapllication.R
import com.net.pslapllication.activities.HomeActivity
import com.net.pslapllication.activities.MainListing
import com.net.pslapllication.activities.SearchResultActivity
import com.net.pslapllication.activities.dictionary.DictionaryTabListActivity
import com.net.pslapllication.adpters.AutoCompleteAdapter
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_new.view.*


class HomeFragmentNew : Fragment(), View.OnClickListener {
    var listData: List<DictionaryDataAPI>? = null
      override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the bottom_layout_delete_video for this fragment
        var view: View = inflater.inflate(R.layout.fragment_home_new, container, false)
        setClickListener(view)
        searchBarSetting(view)

         if (activity!=null ) {
             if (ProgressHelper.getInstance(activity!!)!= null && ProgressHelper.getInstance(activity!!).getViewModel()!=null){
             Log.d("datasetNew", "datasetNew1")
             var list = ProgressHelper.getInstance(activity!!).getViewModel().allWords
             ProgressHelper.getInstance(activity!!).getViewModel().allWords.observe(activity!!,
                 androidx.lifecycle.Observer {
                     listData = it
                     if (it!!.isNotEmpty()) {

                         autoCompleteSearch(view)
                     }
                 })
         }
         }else{
             Log.d("datasetNew","datasetNew22")
         }

        return view
    }

    private fun autoCompleteSearch(view: View) {
        val adapter =
            AutoCompleteAdapter(activity!!, android.R.layout.simple_list_item_1, listData!!)
        view.autoCompleteTextView.setAdapter(adapter)
        view.autoCompleteTextView.threshold = 1
        view.autoCompleteTextView.setTextColor(Color.BLACK)
        view.autoCompleteTextView.setOnItemClickListener() { parent, _, position, id ->
            val selectedPoi = parent.adapter.getItem(position) as DictionaryDataAPI?
            view.autoCompleteTextView.setText(selectedPoi?.english_word)
            newActivity(selectedPoi?.english_word.toString())


        }//click end
        view.autoCompleteTextView.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
             if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                  newActivity(v.text.toString())
                 return@OnEditorActionListener true
            }
            false
        })

    }
    private fun newActivity(searchString :String){
        if (activity!=null) {
            var intent = Intent(activity, SearchResultActivity::class.java)
            intent.putExtra(Constants.SEARCHEDWORD, searchString)
            activity!!.startActivity(intent)
        }
    }

    private fun setClickListener(view: View) {
        view.constraint_dic.setOnClickListener(this)
        view.constraint_tea_tutorial.setOnClickListener(this)
        view.constraint_stories.setOnClickListener(this)
        view.constraint_lea_tutorial.setOnClickListener(this)
        view.constraint_learning_tutorial_real.setOnClickListener(this)
        view.imageview.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.constraint_dic -> {
                ReuseFunctions.preventTwoClick(v)

                ReuseFunctions.startNewActivityTaskWithParameter(
                    activity!!.applicationContext,
                    MainListing::class.java,
                    Constants.TYPE_DICTIONARY
                )
            }
            R.id.constraint_tea_tutorial -> {
                ReuseFunctions.preventTwoClick(v)

                /*startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://player.vimeo.com/video/"+"78329998")
                    )
                )*/

                ReuseFunctions.startNewActivityTaskWithParameter(
                    activity!!.applicationContext,
                    MainListing::class.java,
                    Constants.TYPE_TEACHER_TUTORIAL
                )
            }
            R.id.constraint_stories -> {
                ReuseFunctions.preventTwoClick(v)
                ReuseFunctions.startNewActivityTaskWithParameter(
                    activity!!.applicationContext,
                    MainListing::class.java,
                    Constants.TYPE_STORIES

                    /*ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                        this.activity!!, DictionarySingleWordActivity::class.java,
                        "", "", Constants.TYPE_STORIES*/
                )
            }
            R.id.constraint_lea_tutorial -> {
                ReuseFunctions.preventTwoClick(v)
                ReuseFunctions.startNewActivityTaskWithParameter(
                    activity!!.applicationContext,
                    MainListing::class.java,
                    Constants.TYPE_LEARNING_TUTORIAL
                )

            }
            R.id.constraint_learning_tutorial_real -> {
                ReuseFunctions.preventTwoClick(v)
                ReuseFunctions.startNewActivityTaskWithParameter(
                    activity!!.applicationContext,
                    MainListing::class.java,
                    Constants.TYPE_LEARNING_TUTORIAL_REAL
                )
            }
 R.id.autoCompleteTextView -> {

 }
            R.id.imageview -> {
                if(activity!=null) {
                    ReuseFunctions.preventTwoClick(v)
                    ReuseFunctions.startNewActivity(
                        activity!!,
                        DictionaryTabListActivity::class.java
                    )
                }
                //setSpecificTab()
            }
        }
    }

    private fun setSpecificTab() {
        if (activity != null) {
            (activity as HomeActivity).bottom_navigation_view.selectedItemId = R.id.favourite
        }
    }

    private fun searchBarSetting(view: View) {
        val searchView: SearchView =
            view.findViewById(R.id.searchView_dic_words)
        val v: View =
            searchView.findViewById(androidx.appcompat.R.id.search_plate)
        searchView.setQuery("", false)
        searchView.queryHint = "Search Dictionary Words"
        searchView.clearFocus()
        var searchText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text) as TextView
        searchText.setTypeface(ResourcesCompat.getFont(activity!!, R.font.lato_regular))
        searchText.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimensionPixelSize(R.dimen._13ssp).toFloat()
        )
        //searchText.currentHintTextColor =activity!!.resources.getColor(R.color.black)
        searchText.setTextColor(activity!!.resources.getColor(R.color.text_grey2))
        searchText.setPadding(0, 0, 0, 0)
        val searchManager =
            activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        if (searchManager != null) {
            searchView.setSearchableInfo(
                searchManager
                    .getSearchableInfo(activity!!.componentName)
            )
        }
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                /* if (adapter != null) {
                     adapter.getFilter().filter(query)
                 }*/
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                /*if (adapter != null) {
                    adapter.getFilter().filter(query)
                }*/
                return false
            }
        })
    }
    fun watchYoutubeVideo(context: Context, id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$id")
        )
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }
}