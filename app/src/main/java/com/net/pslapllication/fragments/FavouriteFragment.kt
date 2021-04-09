package com.net.pslapllication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.net.pslapllication.R
import com.net.pslapllication.activities.FavouriteActivity
import com.net.pslapllication.activities.HomeActivity
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.adpters.DictionarySingleWordAdapter
import com.net.pslapllication.adpters.VideoQualityOptionAdapter
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.interfaces.onQualityChangSelectedListener
import com.net.pslapllication.model.dictionary.DictionaryMainModel
import com.net.pslapllication.model.favouriteList.Data
import com.net.pslapllication.model.favouriteList.FavouriteMain
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_main_listing_new.*
import kotlinx.android.synthetic.main.bottom_layout_video_quality_list.view.*
import kotlinx.android.synthetic.main.fragment_favourite.*
import kotlinx.android.synthetic.main.fragment_favourite.view.*
import kotlinx.android.synthetic.main.fragment_favourite.view.tv_sort
import kotlinx.android.synthetic.main.fragment_home_new.view.*
import kotlinx.android.synthetic.main.toolbaar_layout.view.*
import retrofit2.Call
import java.net.URLDecoder

class FavouriteFragment : Fragment(), RetrofitResponseListener, View.OnClickListener,
    onQualityChangSelectedListener {
    private lateinit var apiService: ApiService
    lateinit var adapter: DictionarySingleWordAdapter
    private var viewlayout: View? = null
    var favList: List<Data>? = null
    public var call: Call<FavouriteMain>?=null

    private var isSorted: Boolean = true
    private var dialog_sort: BottomSheetDialog? = null
    private var selectedSortyId: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the bottom_layout_delete_video for this fragment
        viewlayout = inflater.inflate(R.layout.fragment_favourite, container, false)
        setTitle(viewlayout!!)
        initialization()
        setClickListener(viewlayout!!)
        searchBarSetting(viewlayout!!)
        setAdapter(viewlayout!!)
        apiCall()
        return viewlayout!!
    }

    private fun setClickListener(view: View) {
        view.tv_sort.setOnClickListener(this)
    }

    private fun setAdapter(view: View) {
        var mainMenu = view.findViewById<GridView>(R.id.grid_view)
        adapter =
            DictionarySingleWordAdapter(activity!!.applicationContext, Constants.TYPE_FAVOURITE, "")
        mainMenu.adapter = adapter
    }

    private fun initialization() {
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)

    }

    private fun apiCall() {
        if (activity != null) {
            if (arguments != null && arguments!!.getString("TYPE") != null) {
                if ((activity as FavouriteActivity).isInternetConnected) {
                     call = apiService.favouriteList(SharedPreferenceClass.getInstance(activity!!)!!.getSession(),
                        SharedPreferenceClass.getInstance(activity!!)?.getUserType().toString())
                    ApiCallClass.retrofitCall(this, call as Call<Any>)
                }
            } else {
                if ((activity as HomeActivity).isInternetConnected) {
                      call = apiService.favouriteList(SharedPreferenceClass.getInstance(activity!!)!!.getSession(),
                        SharedPreferenceClass.getInstance(activity!!)!!.getUserType().toString())
                    ApiCallClass.retrofitCall(this, call as Call<Any>)
                }
            }
        }
    }

    private fun setTitle(view: View) {
        if (arguments != null && arguments!!.getString("TYPE") != null) {
            var datadfav = arguments!!.getString("TYPE")
            view.txt_title.text = ReuseFunctions.firstLetterCap(arguments!!.getString("TYPE"))
            view.opsBackBtn.visibility = View.INVISIBLE

        } else {
            view.txt_title.text = ReuseFunctions.firstLetterCap(Constants.TYPE_FAVOURITE)
            view.opsBackBtn.visibility = View.INVISIBLE
        }
    }

    private fun setFavouriteRecycler(list: List<Data>?) {
        adapter.setFavWords(list!!)
        adapter.notifyDataSetChanged()
    }

    private fun searchBarSetting(view: View) {
        val searchView: SearchView =
            view.findViewById(R.id.searchView_fav)
        val searchText =
            searchView.findViewById<View>(R.id.search_src_text) as TextView
        searchText.typeface = ReuseFunctions.regularFont(activity)
        searchText.textSize = 16f
        searchText.setPadding(0, 0, 0, 0);

        view.searchView_fav.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (adapter != null) {
                    adapter!!.filter.filter(query)
                }
                return false
            }
            override fun onQueryTextChange(query: String): Boolean {
                if (adapter != null) {
                    adapter!!.filter.filter(query)
                }
                return false
            }
        })
    }

    override fun onSuccess(model: Any?) {
        if (model as FavouriteMain? != null) {
            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    favList = model?.data
                    if (model?.data?.size != 0) {
                        constraint_no_Videos.visibility = View.GONE
                        for (i in model?.data!!.indices) {
                            model?.data[i].indexPosition = i
                            model?.data[i].favourites = 1
                            if (model?.data[i].dict_video_id != 0 && model?.data[i].learning_tut_video_id == 0 &&
                                model?.data[i].tut_video_id == 0 && model?.data[i].lesson_video_id == 0 && model?.data[i].story_video_id == 0
                            ){
                                 model?.data[i].videoname = model?.data[i].dictionary.english_word
                                 model?.data[i].postermain = model?.data[i].dictionary.poster

                            } else if (model?.data[i].dict_video_id == 0 && model?.data[i].learning_tut_video_id != 0 &&
                                model?.data[i].tut_video_id == 0 && model?.data[i].lesson_video_id == 0 && model?.data[i].story_video_id == 0
                            ){
                                model?.data[i].videoname = model?.data[i].learningTutorial.title
                                model?.data[i].postermain = model?.data[i].learningTutorial.poster

                            } else if (model?.data[i].dict_video_id == 0 && model?.data[i].learning_tut_video_id == 0 &&
                                model?.data[i].tut_video_id != 0 && model?.data[i].lesson_video_id == 0 && model?.data[i].story_video_id == 0
                            ){
                                model?.data[i].videoname = model?.data[i].tutorial.title
                                model?.data[i].postermain = model?.data[i].tutorial.poster

                            } else if (model?.data[i].dict_video_id == 0 && model?.data[i].learning_tut_video_id == 0 &&
                                model?.data[i].tut_video_id == 0 && model?.data[i].lesson_video_id != 0 && model?.data[i].story_video_id == 0
                            ){
                                model?.data[i].videoname = model?.data[i].lesson.title
                                model?.data[i].postermain = model?.data[i].lesson.poster

                            } else if (model?.data[i].dict_video_id == 0 && model?.data[i].learning_tut_video_id == 0 &&
                                model?.data[i].tut_video_id == 0 && model?.data[i].lesson_video_id == 0 && model?.data[i].story_video_id != 0
                            ){
                                model?.data[i].videoname = model?.data[i].story.title
                                model?.data[i].postermain = model?.data[i].story.poster

                            }
                        }
                        setFavouriteRecycler(model?.data)
                    }
                    else{
                        constraint_no_Videos.visibility = View.VISIBLE
                    }
                }
                Constants.SESSION_ERROR_CODE -> {
                    if (activity != null) {
                        ReuseFunctions.startNewActivityTaskTop(
                            activity!!,
                            LoginScreen::class.java
                        )
                        activity!!.finish()
                    }
                }
                Constants.FIELD_VALIDATION_ERROR_CODE -> {
                    //no field here
                }
            }
        }
    }

    override fun onFailure(error: String) {
        if (activity != null) {
         }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_sort -> {
                ReuseFunctions.preventTwoClick(v)
                openSortBottomSheet()
            }
        }
    }

    private fun openSortBottomSheet() {
        var qualityList: List<DictionaryListModel> = ArrayList<DictionaryListModel>()
        qualityList = listOf(

            DictionaryListModel(0, "Default", "", ""),
            DictionaryListModel(1, "Ascending", "", ""),
            DictionaryListModel(2, "Descending", "", "")

        )
        dialog_sort = BottomSheetDialog(activity!!)
        val dialogView: View = layoutInflater.inflate(
            R.layout.bottom_layout_video_quality_list,
            null
        )

        dialogView.recycler_video_quality_option.layoutManager = LinearLayoutManager(activity!!)
        val adapter = VideoQualityOptionAdapter(
            activity!!,
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
        if (favList != null && favList?.size != 0) {
            if (activity != null) {
                when (sortType) {

                    "1" -> {
                        val sortedAppsList1: List<Data> =
                            favList!!.sortedBy { it.videoname }
                        adapter.setFavWords(sortedAppsList1)
                        adapter.notifyDataSetChanged()
                        isSorted = true
                        ReuseFunctions.snackMessage(
                            viewlayout!!.constraint_fav,
                            resources.getString(R.string.sortedByAscending)
                        )
                    }
                    "2" -> {

                        val reverseSortedAppsList: List<Data> =
                            favList!!.sortedByDescending { it.videoname }
                        adapter.setFavWords(reverseSortedAppsList)
                        adapter.notifyDataSetChanged()
                        ReuseFunctions.snackMessage(
                            viewlayout!!.constraint_fav,
                            resources.getString(R.string.sortedByDescending)
                        )
                        isSorted = false
                    }
                    else -> {
                        adapter.setFavWords(favList!!)
                        adapter.notifyDataSetChanged()
                        isSorted = true
                        ReuseFunctions.snackMessage(
                            viewlayout!!.constraint_fav,
                            resources.getString(R.string.sortedByDefault))
                    }
                }
            }

        } else {
            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
        }
    }

    override fun onPause() {
        if (dialog_sort != null) {
            dialog_sort!!.cancel()
        }
        super.onPause()
    }

    override fun onDetach() {
if (call!=null){
    call!!.cancel()
}
        super.onDetach()
    }
}
