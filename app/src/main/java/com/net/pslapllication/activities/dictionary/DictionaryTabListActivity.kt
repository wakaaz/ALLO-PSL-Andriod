package com.net.pslapllication.activities.dictionary

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.net.pslapllication.R
import com.net.pslapllication.activities.BaseActivity
import com.net.pslapllication.activities.SearchResultActivity
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.adpters.AutoCompleteAdapter
import com.net.pslapllication.data.Data
import com.net.pslapllication.fragments.CategoriesFragment
import com.net.pslapllication.fragments.WordsFragment
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.dictionary.DictionaryMainModel
import com.net.pslapllication.model.preferences.Dictionary_categories
import com.net.pslapllication.model.preferences.PreferenceMainModel
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.room.WordViewModelFactory
import com.net.pslapllication.room.WordsViewModel
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ListSorting
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_dictionary_tab_list.*
import kotlinx.android.synthetic.main.fragment_home_new.view.*
import retrofit2.Call
import java.util.*

class DictionaryTabListActivity : BaseActivity(), OnTabSelectedListener, View.OnClickListener,
    RetrofitResponseListener {
    private val tabIcons1 = intArrayOf(R.drawable.ic_categories_tab, R.drawable.ic_words_grey_tab)
    private val tabIcons2 = intArrayOf(R.drawable.ic_categories_grey_tab, R.drawable.ic_words_tab)
    lateinit var apiService: ApiService
    private var alphabetString: String? = null
    private var isInternetConnected: Boolean = false
    public var callCat: Call<PreferenceMainModel>?=null
    public var callWords: Call<DictionaryMainModel>?=null
    lateinit var searchView_tab : SearchView
    lateinit var autoCompleteTextView : AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary_tab_list)
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        searchView_tab = findViewById(R.id.searchView_tab)
        setTabData()
        setListener()
        searchBarSetting()
    }

    private fun setListener() {
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
        opsBackBtn.setOnClickListener(this)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        isInternetConnected = isConnected
        if (isConnected) {
            var fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
            if (fragment is CategoriesFragment) {
                val call = ApiCallClass.apiService.getPreferenceData(
                        SharedPreferenceClass.getInstance(this)!!.getSession(),
                        SharedPreferenceClass.getInstance(this)?.getUserType().toString()
                )
                ApiCallClass.retrofitCall(this, call as Call<Any>)
            } else if (fragment is WordsFragment) {
                val call = ApiCallClass.apiService.getAllDictionaryData(
                        SharedPreferenceClass.getInstance(this)!!.getSession(),
                        SharedPreferenceClass.getInstance(this)?.getUserType().toString(), ""
                )
                ApiCallClass.retrofitCall(this, call as Call<Any>)
            }

        } else {
            if (!this.isDestroyed) {
                ReuseFunctions.snackMessage(
                        constrainrt_main,
                        this.resources.getString(R.string.no_internet_text)
                )
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun requestDataCat() {
        if (isInternetConnected){
              callCat = ApiCallClass.apiService.getPreferenceData(
                      SharedPreferenceClass.getInstance(this)!!.getSession(),
                      SharedPreferenceClass.getInstance(this)?.getUserType().toString()
              )
            ApiCallClass.retrofitCall(this, callCat as Call<Any>)

        } else {
            if (!this.isDestroyed) {
                ReuseFunctions.snackMessage(
                        constrainrt_main,
                        this.resources.getString(R.string.no_internet_text)
                )
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun requestDataWords() {
        if (isInternetConnected) {
               callWords = ApiCallClass.apiService.getAllDictionaryData(
                       SharedPreferenceClass.getInstance(this)!!.getSession(),
                       SharedPreferenceClass.getInstance(this)?.getUserType().toString(), ""
               )
            ApiCallClass.retrofitCall(this, callWords as Call<Any>)
        } else {
            if (!this.isDestroyed) {
                ReuseFunctions.snackMessage(
                        constrainrt_main,
                        this.resources.getString(R.string.no_internet_text)
                )
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun setTabData() {

        tabLayout_dic.addTab(tabLayout_dic.newTab().setText("Categories"))
        tabLayout_dic.addTab(tabLayout_dic.newTab().setText("Words"))
        setTabIcon(tabIcons1)
        tabLayout_dic.addOnTabSelectedListener(this)
        replaceFragment(CategoriesFragment())
    }

    private fun setTabIcon(tabIcons: IntArray) {
        tabLayout_dic.getTabAt(0)!!.setIcon(tabIcons[0])
        tabLayout_dic.getTabAt(1)!!.setIcon(tabIcons[1])
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment)
            .commit()
    }

    override fun onTabSelected(tab: TabLayout.Tab) {

        if (tab.parent != null) {
            /* val tv =
                ((tabLayout_dic.getChildAt(0) as LinearLayout).getChildAt(tab.position) as LinearLayout).getChildAt(
                   1) as TextView
            tv.typeface = ResourcesCompat.getFont(
                this ,R.font.lato_heavy)*/

            if (tab.position == 0) {
                searchView_tab.visibility = View.VISIBLE
                autoCompleteTextView.visibility = View.INVISIBLE
                if (callWords != null)
                    callWords!!.cancel()
                replaceFragment(CategoriesFragment())
                setTabIcon(tabIcons1)
                requestDataCat()
            } else if (tab.position == 1) {
                searchView_tab.visibility = View.INVISIBLE
                autoCompleteTextView.visibility = View.VISIBLE
                if (callCat != null)
                    callCat!!.cancel()
                replaceFragment(WordsFragment())
                setTabIcon(tabIcons2)
                ProgressHelper.getInstance(this).getViewModel().allWords.observe(this,
                    androidx.lifecycle.Observer {
                        if (it!!.isNotEmpty()) {
                            autoCompleteSearch(it)
                        }
                    })

            }


            val nextChild = tab.parent!!.getChildAt(tab.position)
            if (nextChild is TextView) {
                nextChild.setTextColor(resources.getColor(R.color.red))
                nextChild.setTypeface(
                        ResourcesCompat.getFont(
                                this,
                                R.font.lato_heavy
                        )
                )
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        if (tab.parent != null) {
            /* val tv =
                 ((tabLayout_dic.getChildAt(0) as LinearLayout).getChildAt(tab.position) as LinearLayout).getChildAt(
                     1
                 ) as TextView
             tv.typeface = ResourcesCompat.getFont(
                 this,
                 R.font.lato_regular
             )*/
            val nextChild = tab.parent!!.getChildAt(tab.position)
            if (nextChild is TextView) {
                nextChild.setTypeface(ReuseFunctions.regularFont(this))
            }
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.opsBackBtn -> {
                finish()
            }
        }
    }

    override fun onSuccess(model: Any?) {
        var fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
        if (fragment is CategoriesFragment) {
            if (model is PreferenceMainModel) {
                if (model as PreferenceMainModel? != null) {
                when (model?.code) {
                    Constants.SUCCESS_CODE -> {
                        if (model?.object1?.dictionary_categories != null && model?.object1.dictionary_categories.isNotEmpty()) {
                            setDataCategories(model?.object1.dictionary_categories)

                        }
                    }

                    Constants.SESSION_ERROR_CODE -> {
                        if (!this.isDestroyed) {
                            ReuseFunctions.startNewActivityTaskTop(this, LoginScreen::class.java)
                            finish()
                        }
                    }
                }
            }

            }
        } else if (fragment is WordsFragment) {
            if (model is DictionaryMainModel) {
                if (model as DictionaryMainModel? != null) {
                    when (model?.code) {
                        Constants.SUCCESS_CODE -> {
                            if (model?.data != null && model?.data.isNotEmpty()) {
                                setData(model?.data)
                            }
                        }

                        Constants.SESSION_ERROR_CODE -> {
                            if (!this.isDestroyed) {
                                ReuseFunctions.startNewActivityTaskTop(this, LoginScreen::class.java)
                                finish()
                            }
                        }
                    }
                }
            }
        }

    }

    override fun onFailure(error: String) {
        if (!this.isDestroyed) {
            //ReuseFunctions.showToast(this, error)
        }
    }

    private fun setDataCategories(dictionaryCategoriesList: List<Dictionary_categories>) {
        var fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
        if (fragment is CategoriesFragment) {

            dictionaryCategoriesList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })


            val list: MutableList<Data> = ArrayList()

            for (i in dictionaryCategoriesList.indices) {

                if (i == 0) {
                    alphabetString = dictionaryCategoriesList[0].title.substring(0, 1)
                    list.add(
                            Data(i,
                                    dictionaryCategoriesList[0].title.substring(0, 1)
                                            .toUpperCase(Locale.ROOT),
                                    dictionaryCategoriesList[0].title,
                                    true,
                                    dictionaryCategoriesList[0].id,
                                    dictionaryCategoriesList[0].image,
                                    dictionaryCategoriesList[0].videos.toString()
                            )
                    )
                } else {

                    if (alphabetString!!.equals(
                                    dictionaryCategoriesList[i].title.substring(0, 1),
                                    true
                            )
                    ) {
                        list.add(
                                Data(i,
                                        dictionaryCategoriesList[i].title.substring(0, 1)
                                                .toUpperCase(Locale.ROOT),
                                        dictionaryCategoriesList[i].title,
                                        false,
                                        dictionaryCategoriesList[i].id,
                                        dictionaryCategoriesList[i].image,
                                        dictionaryCategoriesList[i].videos.toString()
                                )
                        )
                    } else {
                        list.add(
                                Data(i,
                                        dictionaryCategoriesList[i].title.substring(0, 1)
                                                .toUpperCase(Locale.ROOT),
                                        dictionaryCategoriesList[i].title,
                                        true,
                                        dictionaryCategoriesList[i].id,
                                        dictionaryCategoriesList[i].image,
                                        dictionaryCategoriesList[i].videos.toString()
                                )
                        )
                        alphabetString = dictionaryCategoriesList[i].title.substring(0, 1)
                    }
                }

            }
            fragment.setAdapter(list)
        }


    }

    private fun  setData(listData: List<DictionaryData>) {
        var fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
        if (fragment is WordsFragment) {
             var dictionaryCategoriesList = listData.sortedBy { it.english_word }
            for (i in dictionaryCategoriesList.indices) {
                dictionaryCategoriesList[i].indexPosition = i
            }
             //  dictionaryCategoriesList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.english_word })
            val list: MutableList<Data> = ArrayList()

            for (i in dictionaryCategoriesList.indices) {
                if (i == 0) {
                    alphabetString = dictionaryCategoriesList[0].english_word.substring(0, 1)
                    list.add(
                            Data(i,
                                    dictionaryCategoriesList[0].english_word.substring(0, 1)
                                            .toUpperCase(Locale.ROOT),
                                    dictionaryCategoriesList[0].english_word,
                                    true,
                                    dictionaryCategoriesList[0].id,
                                    "",
                                    dictionaryCategoriesList[0].urdu_word
                            )
                    )
                } else {

                    if (alphabetString!!.equals(
                                    dictionaryCategoriesList[i].english_word.substring(0, 1),
                                    true
                            )
                    ) {
                        list.add(
                                Data(i,
                                        dictionaryCategoriesList[i].english_word.substring(0, 1)
                                                .toUpperCase(Locale.ROOT),
                                        dictionaryCategoriesList[i].english_word,
                                        false,
                                        dictionaryCategoriesList[i].id,
                                        "",
                                        dictionaryCategoriesList[i].urdu_word
                                )
                        )
                    } else {
                        list.add(
                                Data(i,
                                        dictionaryCategoriesList[i].english_word.substring(0, 1)
                                                .toUpperCase(Locale.ROOT),
                                        dictionaryCategoriesList[i].english_word,
                                        true,
                                        dictionaryCategoriesList[i].id,
                                        "",
                                        dictionaryCategoriesList[i].urdu_word
                                )
                        )
                        alphabetString = dictionaryCategoriesList[i].english_word.substring(0, 1)
                    }
                }

            }
            fragment.setAdapter(list, dictionaryCategoriesList)
        }


    }

    private fun searchBarSetting() {
        val searchView: SearchView =
            findViewById(R.id.searchView_tab)
        val v: View =
            searchView.findViewById(androidx.appcompat.R.id.search_plate)
        searchView.setQuery("", false)
        searchView.queryHint = "Search"
        searchView.clearFocus()
        var searchText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text) as TextView
        searchText.setTypeface(ResourcesCompat.getFont(this, R.font.lato_regular))
        searchText.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimensionPixelSize(R.dimen._13ssp).toFloat()
        )
        //searchText.currentHintTextColor =activity!!.resources.getColor(R.color.black)
        searchText.setTextColor(this.resources.getColor(R.color.text_grey2))
        searchText.setPadding(0, 0, 0, 0)
        val searchManager =
            this.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        if (searchManager != null) {
            searchView.setSearchableInfo(
                    searchManager
                            .getSearchableInfo(this.componentName)
            )
        }
        searchView.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                var fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
                if (fragment is CategoriesFragment) {
                    if (fragment.adapter != null) {
                        fragment.adapter!!.filter.filter(query)
                    }
                } else if (fragment is WordsFragment) {
                    if (fragment.adapter != null) {
                        fragment.adapter!!.getFilter().filter(query)
                    }
                }

                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                var fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
                if (fragment is CategoriesFragment) {
                    if (fragment.adapter != null) {
                        fragment.adapter!!.filter.filter(query)
                    }
                } else if (fragment is WordsFragment) {
                    if (fragment.adapter != null) {
                        fragment.adapter!!.getFilter().filter(query)
                    }
                }
                return false
            }
        })
    }

    override fun onResume() {
        if (isConnected) {
            var fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
            if (fragment is CategoriesFragment) {
                val call = ApiCallClass.apiService.getPreferenceData(
                        SharedPreferenceClass.getInstance(this)!!.getSession(),
                        SharedPreferenceClass.getInstance(this)?.getUserType().toString()
                )
                ApiCallClass.retrofitCall(this, call as Call<Any>)
                searchView_tab.setQuery("", false);
                searchView_tab.clearFocus()


            } else if (fragment is WordsFragment) {
                val call = ApiCallClass.apiService.getAllDictionaryData(
                        SharedPreferenceClass.getInstance(this)!!.getSession(),
                        SharedPreferenceClass.getInstance(this)?.getUserType().toString(), ""
                )
                ApiCallClass.retrofitCall(this, call as Call<Any>)
                searchView_tab.setQuery("", false);
                searchView_tab.clearFocus()
            }

        }

        super.onResume()
    }

    private fun autoCompleteSearch(listData: List<DictionaryDataAPI>) {
            val adapter =
                AutoCompleteAdapter(this, android.R.layout.simple_list_item_1, listData!!)
            autoCompleteTextView.setAdapter(adapter)
            autoCompleteTextView.threshold = 1
            autoCompleteTextView.setTextColor(Color.BLACK)
            autoCompleteTextView.setOnItemClickListener() { parent, _, position, id ->
                val selectedPoi = parent.adapter.getItem(position) as DictionaryDataAPI?

                var newIndexSortedList = emptyList<DictionaryDataAPI>()

                    ProgressHelper.getInstance(this)?.setListOffline(newIndexSortedList)

                    selectedPoi?.let {
                        ReuseFunctions.startNewActivityDataModelParam(
                            this,
                            VideoPreviewOfflineActivity::class.java,
                            it, Constants.TYPE_DICTIONARY
                        )
                    }



                //autoCompleteTextView.setText(selectedPoi?.english_word)

               // newActivity(selectedPoi?.english_word.toString())
                autoCompleteTextView.text.clear()

            }//click end
            autoCompleteTextView.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    newActivity(v.text.toString())
                    autoCompleteTextView.text.clear()
                    return@OnEditorActionListener true
                }
                false
            })


    }
    private fun newActivity(searchString :String){
            var intent = Intent(this, SearchResultActivity::class.java)
            intent.putExtra(Constants.SEARCHEDWORD, searchString)
            startActivity(intent)

    }
}