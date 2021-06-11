package com.net.pslapllication.activities

import android.os.Bundle
import android.util.Log

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.net.pslapllication.R
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.adpters.CustomGridAdapter
import com.net.pslapllication.adpters.DictionaryListAdapter
import com.net.pslapllication.adpters.VideoQualityOptionAdapter
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.interfaces.onQualityChangSelectedListener
import com.net.pslapllication.model.preferences.*
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.room.WordViewModelFactory
import com.net.pslapllication.room.WordsViewModel
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_main_listing_new.*
import kotlinx.android.synthetic.main.activity_main_listing_new.tv_sort
import kotlinx.android.synthetic.main.bottom_layout_video_quality_list.view.*
import kotlinx.android.synthetic.main.activity_main_listing_new.shimmer_layout

import kotlinx.android.synthetic.main.toolbaar_layout.*
import retrofit2.Call


class MainListing : BaseActivity(), View.OnClickListener, RetrofitResponseListener,
    onQualityChangSelectedListener {
    private var type: String = "null"
    private var isSorted: Boolean = true
    private var isInternetConnected: Boolean = true
    lateinit var apiService: ApiService
    private var list: List<Dictionary_categories>? = null
    private var listTeacherTut: List<Tut_grades>? = null
    private var listLearningTut: List<Life_skills>? = null
    private var listStories: List<Story_types>? = null
    lateinit var adapter: CustomGridAdapter
    lateinit var listAdapter: DictionaryListAdapter
    private var dialog_sort: BottomSheetDialog? = null
    private var selectedSortyId: Int = 0
    private var isGrid : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_listing_new)
        shimmer_layout.startShimmerAnimation()
        checkIntent()
        initializeViews()
        setListener()
        setSearchView()
        setTitleText()
    }

    private fun initializeViews() {
        adapter = CustomGridAdapter(this, type)
        grid_view.adapter = adapter

        recycler_view.layoutManager = LinearLayoutManager(this)
        listAdapter = DictionaryListAdapter(this, type)
        recycler_view.adapter = listAdapter

        img_btn_grid.setOnClickListener(this)
        img_btn_list.setOnClickListener(this)

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            val call = ApiCallClass.apiService.getPreferenceData(
                SharedPreferenceClass.getInstance(this)?.getSession().toString(),
                SharedPreferenceClass.getInstance(this)?.getUserType().toString()
            )
            ApiCallClass.retrofitCall(this, call as Call<Any>)
            isInternetConnected = isConnected
            constraintInternet.visibility = View.GONE
        } else {
            if (!this.isDestroyed) {
                constraintInternet.visibility = View.VISIBLE

                isInternetConnected = isConnected
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun setListener() {
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
        opsBackBtn.setOnClickListener(this)
        tv_sort.setOnClickListener(this)
        btn_downloads.setOnClickListener(this)
        btn_retry.setOnClickListener(this)
    }

    private fun checkIntent() {
        if (intent != null && intent.getStringExtra("TYPE") != null) {
            type = intent.getStringExtra("TYPE")!!
            if (type == Constants.TYPE_DICTIONARY){
                tv_sort.visibility = View.GONE
                img_btn_list.visibility = View.VISIBLE
                img_btn_grid.visibility = View.VISIBLE
            }else{
                tv_sort.visibility = View.VISIBLE
                img_btn_list.visibility = View.GONE
                img_btn_grid.visibility = View.GONE
            }
        }
    }

    private fun setTitleText() {
        txt_title.text = ReuseFunctions.firstLetterCap(type)
    }

    private fun setMainListingDetailRecycler(preferenceData: PreferenceData) {

        when (type) {
            Constants.TYPE_DICTIONARY -> {
                if (preferenceData.dictionary_categories.isNotEmpty()) {
                    isGrid = true
                    grid_view.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                    list = preferenceData.dictionary_categories
                    adapter.setDicWords(preferenceData.dictionary_categories)
                    adapter.notifyDataSetChanged()
                    img_btn_grid.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_grid_green,
                            null
                        )
                    );
                    img_btn_list.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_list_grey,
                            null
                        )
                    )

                }
            }
            Constants.TYPE_TEACHER_TUTORIAL -> {
                if (preferenceData.tut_grades.isNotEmpty()) {
                    listTeacherTut = preferenceData.tut_grades
                    adapter.setTutGrades(preferenceData.tut_grades)
                    adapter.notifyDataSetChanged()
                }
            }
            Constants.TYPE_LEARNING_TUTORIAL -> {
                if (preferenceData.life_skills.isNotEmpty()) {
                    listLearningTut = preferenceData.life_skills
                    adapter.setLearningTut(preferenceData.life_skills)
                    adapter.notifyDataSetChanged()
                }

            }
            Constants.TYPE_LEARNING_TUTORIAL_REAL -> {
                if (preferenceData.tut_grades.isNotEmpty()) {
                    listTeacherTut = preferenceData.learning_tut_grades
                    adapter.setTutGrades(preferenceData.learning_tut_grades)
                    adapter.notifyDataSetChanged()
                }

            }
            Constants.TYPE_STORIES -> {
                if (preferenceData.story_types.isNotEmpty()) {

                    listStories = preferenceData.story_types
                    adapter.setStoriesTypes(preferenceData.story_types)
                    adapter.notifyDataSetChanged()
                }

            }
            Constants.TYPE_FAVOURITE -> {
                val dicWordList = listOf(
                    DictionaryListModel(1, "188 Bahadurabad - Nunno’s House", "", "00:10"),
                    DictionaryListModel(2, "A Carrot, an Egg and Tea Leaves", "", "00:10"),
                    DictionaryListModel(3, "A Carrot, an Egg and Tea Leaves", "", "00:10"),
                    DictionaryListModel(4, "188 Bahadurabad - Nunno’s House", "", "00:10"),
                    DictionaryListModel(5, "A Carrot, an Egg and Tea Leaves", "", "00:10"),
                    DictionaryListModel(6, "188 Bahadurabad - Nunno’s House", "", "00:10"),
                    DictionaryListModel(7, "A Carrot, an Egg and Tea Leaves", "", "00:10")
                )

                adapter.setWords(dicWordList)
                adapter.notifyDataSetChanged()
            }
        }
        shimmer_layout.stopShimmerAnimation()
        shimmer_layout.visibility =  View.GONE

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.opsBackBtn -> {
                onBackPressed()
            }
            R.id.tv_sort -> {
                openSortBottomSheet()

            }
            R.id.btn_retry -> {
                if (isConnected) {
                    val call = ApiCallClass.apiService.getPreferenceData(
                        SharedPreferenceClass.getInstance(this)?.getSession().toString(),
                        SharedPreferenceClass.getInstance(this)?.getUserType().toString()
                    )
                    ApiCallClass.retrofitCall(this, call as Call<Any>)
                    isInternetConnected = isConnected
                    constraintInternet.visibility = View.GONE
                } else {
                    if (!this.isDestroyed) {
                        constraintInternet.visibility = View.VISIBLE

                        isInternetConnected = isConnected
                    }
                }
            }
            R.id.btn_downloads -> {
                ReuseFunctions.startNewActivityTaskWithParameter(
                    this,
                    HomeActivity::class.java,
                    "NOINTERNET"
                )
            }
            R.id.img_btn_grid -> {
                if (list!=null && list!!.isNotEmpty()) {
                    isGrid = true
                    grid_view.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                    adapter.setDicWords(list!!)
                    adapter.notifyDataSetChanged()
                    img_btn_grid.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_grid_green,
                            null
                        )
                    );
                    img_btn_list.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_list_grey,
                            null
                        )
                    )
                }
            }
            R.id.img_btn_list -> {
                if (list!=null && list!!.isNotEmpty()){
                    isGrid = false
                    grid_view.visibility = View.GONE
                    recycler_view.visibility = View.VISIBLE
                    listAdapter.setWords(list!!)
                    adapter.notifyDataSetChanged()
                    img_btn_grid.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_grid_grey,
                            null
                        )
                    )
                    img_btn_list.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_list_green,
                            null
                        )
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    override fun onSuccess(model: Any?) {
        if (model as PreferenceMainModel? != null) {
            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    if (model?.data != null) {
                        setMainListingDetailRecycler(model?.object1!!)
                    }
                }
                Constants.SESSION_ERROR_CODE -> {
                    if (!this.isDestroyed) {
                        ReuseFunctions.startNewActivityTaskTop(
                            this@MainListing,
                            LoginScreen::class.java
                        )
                        finish()
                    }
                }
                Constants.FIELD_VALIDATION_ERROR_CODE -> {
                    //no field here

                }
            }

        }
    }

    override fun onFailure(error: String) {
        if (!this.isDestroyed) {
            ReuseFunctions.showToast(this, error)
            Log.d("errormessage", "" + error)
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

        //searchText.setTextColor(context!!.resources.getColor(R.color.text_grey2))
        searchView_dic.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (isGrid){
                    if (adapter != null)
                        adapter.filter.filter(query)
                }else{
                    if (listAdapter != null)
                        listAdapter.filter.filter(query)
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (isGrid){
                    if (adapter != null)
                        adapter.filter.filter(newText)
                }else{
                    if (listAdapter != null)
                        listAdapter.filter.filter(newText)
                }
                return false
            }

        })
    }

    private fun openSortBottomSheet() {
        var qualityList: List<DictionaryListModel> = ArrayList<DictionaryListModel>()
        qualityList = listOf(

            DictionaryListModel(0, "Default", "", ""),
            DictionaryListModel(1, "Ascending", "", ""),
            DictionaryListModel(2, "Descending", "", "")

        )
        dialog_sort = BottomSheetDialog(this)
        val dialogView: View = layoutInflater.inflate(
            R.layout.bottom_layout_video_quality_list,
            null
        )

        dialogView.recycler_video_quality_option.layoutManager = LinearLayoutManager(this)
        val adapter = VideoQualityOptionAdapter(
            this,
            Constants.TYPE_VIDEO_QUALITY,
            selectedSortyId.toInt(),
            this
        )
        dialogView.recycler_video_quality_option.adapter = adapter
        adapter.setWords(qualityList)
        /*dialogView.recycler_video_quality_option.setOnClickListener {
            openBottomSheetQualityList()

        }*/
        dialog_sort!!.setContentView(dialogView)
        dialog_sort!!.show()
    }

    override fun onQualityChangeSelected(qualityId: String) {

        dialog_sort!!.dismiss()
        selectedSortyId = qualityId.toInt()
        when (qualityId) {
            "0" -> {
                sortList("0")
            }
            "1" -> {
                sortList("1")
            }
            "2" -> {
                sortList("2")
            }

        }
    }

    override fun onSpeedChangeSelected(speedId: String) {

    }

    private fun sortList(sortType: String) {
        if (!this.isDestroyed) {
            when (sortType) {

                "1" -> {
                    if (type == Constants.TYPE_DICTIONARY) {
                        if (list != null && list?.size != 0) {
                            val sortedAppsList1 = list!!.sortedBy { it.title }
                            adapter.setDicWords(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    } else if (type == Constants.TYPE_TEACHER_TUTORIAL) {
                        if (listTeacherTut != null && listTeacherTut?.size != 0) {
                            val sortedAppsList1 = listTeacherTut!!.sortedBy { it.grade }
                            adapter.setTutGrades(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
                        if (listLearningTut != null && listLearningTut?.size != 0) {
                            val sortedAppsList1 = listLearningTut!!.sortedBy { it.title }
                            adapter.setLearningTut(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    }else if (type == Constants.TYPE_LEARNING_TUTORIAL_REAL){
                        if (listTeacherTut != null && listTeacherTut?.size != 0) {
                            val sortedAppsList1 = listTeacherTut!!.sortedBy { it.grade }
                            adapter.setTutGrades(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    } else if (type == Constants.TYPE_STORIES) {
                        if (listStories != null && listStories?.size != 0) {
                            val sortedAppsList1 = listStories!!.sortedBy { it.title }
                            adapter.setStoriesTypes(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    }

                    adapter.notifyDataSetChanged()
                    isSorted = true
                    ReuseFunctions.snackMessage(
                        constraint_main_listing,
                        resources.getString(R.string.sortedByAscending)
                    )
                }
                "2" -> {
                    if (type == Constants.TYPE_DICTIONARY) {
                        if (list != null && list?.size != 0) {
                            val sortedAppsList1 = list!!.sortedByDescending { it.title }
                            adapter.setDicWords(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    } else if (type == Constants.TYPE_TEACHER_TUTORIAL) {
                        if (listTeacherTut != null && listTeacherTut?.size != 0) {
                            val sortedAppsList1 = listTeacherTut!!.sortedByDescending { it.grade }
                            adapter.setTutGrades(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    } else if (type == Constants.TYPE_STORIES) {
                        if (listStories != null && listStories?.size != 0) {
                            val sortedAppsList1 = listStories!!.sortedByDescending { it.title }
                            adapter.setStoriesTypes(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    } else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
                        if (listLearningTut != null && listLearningTut?.size != 0) {
                            val sortedAppsList1 = listLearningTut!!.sortedByDescending { it.title }
                            adapter.setLearningTut(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    }else if(type == Constants.TYPE_LEARNING_TUTORIAL_REAL){
                        if (listTeacherTut != null && listTeacherTut?.size != 0) {
                            val sortedAppsList1 = listTeacherTut!!.sortedByDescending { it.grade }
                            adapter.setTutGrades(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    }
                    adapter.notifyDataSetChanged()
                    ReuseFunctions.snackMessage(
                        constraint_main_listing,
                        resources.getString(R.string.sortedByDescending)
                    )
                    isSorted = false
                }
                else -> {
                    when (type) {
                        Constants.TYPE_DICTIONARY -> {
                            adapter.setDicWords(list!!)
                        }
                        Constants.TYPE_TEACHER_TUTORIAL -> {
                            adapter.setTutGrades(listTeacherTut!!)
                        }
                        Constants.TYPE_STORIES -> {
                            adapter.setStoriesTypes(listStories!!)
                        }
                        Constants.TYPE_LEARNING_TUTORIAL -> {
                            adapter.setLearningTut(listLearningTut!!)
                        }
                        Constants.TYPE_LEARNING_TUTORIAL_REAL -> {
                           // adapter.setLearningTut(listLearningTut!!)
                            adapter.setTutGrades(listTeacherTut!!)

                        }
                    }

                    adapter.notifyDataSetChanged()
                    ReuseFunctions.snackMessage(
                        constraint_main_listing,
                        resources.getString(R.string.sortedByDefault)
                    )
                }
            }
        }
    }

    override fun onPause() {
        if (dialog_sort != null) {
            dialog_sort!!.cancel()
        }
        super.onPause()
    }

}