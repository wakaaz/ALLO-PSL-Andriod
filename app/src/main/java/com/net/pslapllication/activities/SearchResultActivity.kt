package com.net.pslapllication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.TextView
 import androidx.appcompat.widget.SearchView
import com.net.pslapllication.R
import com.net.pslapllication.adpters.SearchedWordsAdapter
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.interfaces.OnQuerryChangeListener
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
 import kotlinx.android.synthetic.main.activity_search_result.*


class SearchResultActivity : AppCompatActivity(),View.OnClickListener, OnQuerryChangeListener {
    lateinit var adapter: SearchedWordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        setToolBar()
        setAdapter()
        getIntentData()
        setSearchView()
    }

    private fun setToolBar() {

        txt_title.text = "Search Results"
        opsBackBtn.setOnClickListener(this)

    }
    private fun setAdapter() {
        var gridView = findViewById<GridView>(R.id.grid_view)
        adapter = SearchedWordsAdapter(this@SearchResultActivity,this)
        gridView.adapter = adapter
    }
    private fun getIntentData() {
         if (intent!=null && intent.getStringExtra(Constants.SEARCHEDWORD)!=null &&
             intent.getStringExtra(Constants.SEARCHEDWORD)!=""){
              getDataForQuerry(intent.getStringExtra(Constants.SEARCHEDWORD).toString())
         }
    }

    private fun getDataForQuerry(querry: String) {
        var viewModel = ProgressHelper.getInstance(this@SearchResultActivity).getViewModel()
        if (viewModel!=null) {
            val list1: List<DictionaryDataAPI>? = viewModel?.getFilteredWords(querry)
            if (list1!!.isNotEmpty()) {
                adapter.setWords(list1!!)
                constraint_noDownload.visibility = View.GONE
                grid_view.visibility = View.VISIBLE

                tv_count.text = "" + list1.size
                tv_word.text = '"' + querry + '"'
            }else{
                constraint_noDownload.visibility = View.VISIBLE
                grid_view.visibility = View.GONE
                tv_count.text = "0"
                tv_word.text = '"'+querry+'"'
            }
        }
        else{

            constraint_noDownload.visibility = View.VISIBLE
            grid_view.visibility = View.GONE
            tv_count.text = "0"
            tv_word.text = '"'+querry+'"'
        }

     }


    override fun onClick(v: View?) {
         when(v?.id){
             R.id.opsBackBtn->
                 finish()
         }
    }
    private fun setSearchView() {
        val searchView: SearchView =
            findViewById(R.id.searchView_dic)
        val searchText =
            searchView.findViewById<View>(R.id.search_src_text) as TextView
        searchText.typeface = ReuseFunctions.regularFont(this)
        searchText.textSize = 16f
        searchText.setPadding(0, 0, 0, 0);
        searchView_dic.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (adapter != null) {
                    adapter!!.filter.filter(query)

                }
                 return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                 if (adapter != null) {
                    adapter.filter.filter(newText)
                      tv_count.text =  adapter.diclistFilterResult.size.toString()
                }
                tv_word.text = newText
                return false
            }

        })
    }

    override fun onSearchResult(size: Int) {
        tv_count.text =  adapter.diclistFilterResult.size.toString()
    }
}