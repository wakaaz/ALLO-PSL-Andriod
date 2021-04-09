package com.net.pslapllication.activities.dictionary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.net.pslapllication.R
import com.net.pslapllication.adpters.CustomGridAdapter
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.activity_class_subject_list.*
import kotlinx.android.synthetic.main.toolbaar_layout.*

class ClassSubjectListActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_subject_list)
        checkIntent()
        setListener()
        setSearchView()
    }

    private fun checkIntent() {
        if (intent != null && intent.getStringExtra("ID") != null && intent.getStringExtra("NAME") != null
            && intent.getStringExtra("TYPE") != null
        ) {

            setTitleText(intent.getStringExtra("NAME")!!)
            setRecycler()

        }
    }

    private fun setListener() {
        opsBackBtn.setOnClickListener(this)
    }

    private fun setTitleText(type: String) {
        txt_title.text = ReuseFunctions.firstLetterCap(type)

    }

    private fun setRecycler() {
        var mainMenu = findViewById<GridView>(R.id.grid_view)

        val dicWordList = listOf(
            DictionaryListModel(1, "Urdu", "", "3 videos"),
            DictionaryListModel(2, "English", "", "5 videos"),
            DictionaryListModel(3, "Math", "", "5 videos"),
            DictionaryListModel(4, "Science", "", "3 videos"),
            DictionaryListModel(5, "Drawing", "", "5 videos")
        )
        val adapter = CustomGridAdapter(this, Constants.TYPE_CLASS_SUBJECT)
        mainMenu.adapter = adapter
        adapter.setWords(dicWordList)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.opsBackBtn -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
    private fun setSearchView() {
        val searchView: SearchView =
            findViewById(R.id.searchView_sub_list)
        val searchText =
            searchView.findViewById<View>(R.id.search_src_text) as TextView
        searchText.typeface = ReuseFunctions.regularFont(this)
        searchText.textSize = 16f
        searchText.setPadding(0, 0, 0, 0);
        searchView_sub_list.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
               /* if (adapter != null) {
                    adapter!!.filter.filter(query)
                }*/
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
              /*  if (adapter != null) {
                    adapter!!.filter.filter(newText)
                }*/
                return false
            }

        })
    }

}