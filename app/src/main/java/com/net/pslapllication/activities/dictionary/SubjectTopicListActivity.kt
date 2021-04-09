package com.net.pslapllication.activities.dictionary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.net.pslapllication.R
import com.net.pslapllication.activities.BaseActivity
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.adpters.CustomGridAdapter
import com.net.pslapllication.adpters.CustomGridLargeAdapter
import com.net.pslapllication.adpters.VideoQualityOptionAdapter
import com.net.pslapllication.broadcastReceiver.DownloadVideoBroadcastReceiver
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.interfaces.onQualityChangSelectedListener
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.model.tutorial.TutorialMainModel
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_dictionary_single_word.*
import kotlinx.android.synthetic.main.activity_main_listing_new.*
import kotlinx.android.synthetic.main.activity_subject_topic_list.*
import kotlinx.android.synthetic.main.bottom_layout_download_video.view.*
import kotlinx.android.synthetic.main.bottom_layout_video_quality_list.view.*
import kotlinx.android.synthetic.main.toolbaar_layout.*
import retrofit2.Call
import java.io.File
import java.io.Serializable
import java.io.UnsupportedEncodingException

class SubjectTopicListActivity : BaseActivity(), View.OnClickListener, RetrofitResponseListener,
    onQualityChangSelectedListener {
    private var isInternetConnected = false
    lateinit var apiService: ApiService
    private var grade_id = "0"
    private var sub_id = "0"
    private var adapter: CustomGridLargeAdapter? = null
    private var name: String? = ""
    private var gradename: String? = ""
    private var type: String? = ""
    private var listTutorialSubVideoList: List<TutorialData>? = null
    private var dialogDownloadBottom: BottomSheetDialog? = null
    private var selected_quality_type: Int = -1
    private var folderName: String? = null
    private var dialog_sort: BottomSheetDialog? = null
    private var selectedSortyId: Int = 0
    private var isSorted: Boolean = true
    private var tutorialType:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_topic_list)
        checkIntent()
        setListener()
        setSearchView()
    }

    private fun checkIntent() {
        if (intent != null && intent.getIntExtra("GRADE_ID", 0) != 0 &&
            intent.getIntExtra("SUBJECT_ID", 0) != 0
            && intent.getStringExtra("SUBJECT_NAME") != null
            && intent.getStringExtra(Constants.CETAGORY_TYPE) != null
        ) {
            grade_id = intent.getIntExtra("GRADE_ID", 0).toString()
            sub_id = intent.getIntExtra("SUBJECT_ID", 0).toString()
            name = intent.getStringExtra("SUBJECT_NAME")!!
            gradename = intent.getStringExtra("GRADE_NAME")!!
            type = intent.getStringExtra(Constants.CETAGORY_TYPE)
            setTitleText(intent.getStringExtra("SUBJECT_NAME")!!)
            img_btn_download.visibility = View.VISIBLE
            folderName = gradename + "_" + name
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {

            if(type == Constants.TYPE_TEACHER_TUTORIAL  ){
                tutorialType = "TEACHER"
                val call = ApiCallClass.apiService.getTutorialData(
                    SharedPreferenceClass.getInstance(this)?.getSession().toString(),
                    SharedPreferenceClass.getInstance(this)?.getUserType().toString(),
                    grade_id, sub_id
                )
                ApiCallClass.retrofitCall(this, call as Call<Any>)

            }else if( type == Constants.TYPE_LEARNING_TUTORIAL_REAL ){
                tutorialType = "LEARNING"
                val call = ApiCallClass.apiService.getLearningTutorialData(
                    SharedPreferenceClass.getInstance(this)?.getSession().toString(),
                    SharedPreferenceClass.getInstance(this)?.getUserType().toString(),
                    grade_id, sub_id
                )
                ApiCallClass.retrofitCall(this, call as Call<Any>)

            }

        } else {
            if (!this.isDestroyed) {
                ReuseFunctions.snackMessage(
                    main_layout,
                    this.getString(R.string.no_internet_text)
                )
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun setListener() {
        opsBackBtn.setOnClickListener(this)
        tv_sort_sub.setOnClickListener(this)
        img_btn_download.setOnClickListener(this)
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)

    }

    private fun setTitleText(title: String) {
        txt_title.text = ReuseFunctions.firstLetterCap(title)

    }

    private fun setRecycler(listTutorial: List<TutorialData>?) {

        var mainMenu = findViewById<GridView>(R.id.grid_view)
        adapter = CustomGridLargeAdapter(this, Constants.TYPE_SUB_TOPIC)
        adapter!!.setTutorialType(tutorialType.toString())
        mainMenu.adapter = adapter
        if (isCatAlreadyExist()){
            adapter!!.downloded(true)
        }else{
            adapter!!.downloded(false)
        }
        adapter!!.setTeacherTutorialSubjectList(listTutorial!!)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.opsBackBtn -> {
                onBackPressed()
            }
            R.id.img_btn_download -> {
                setDownloadFunctionality()
            }
            R.id.tv_sort_sub -> {
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

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun setSearchView() {
        val searchView: SearchView =
            findViewById(R.id.searchView_sub_topic)
        val searchText =
            searchView.findViewById<View>(R.id.search_src_text) as TextView
        searchText.typeface = ReuseFunctions.regularFont(this)
        searchText.textSize = 16f
        searchText.setPadding(0, 0, 0, 0);
        searchView_sub_topic.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (adapter != null) {
                    adapter!!.filter.filter(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (adapter != null) {
                    adapter!!.filter.filter(newText)
                }
                return false
            }
        })
    }

    override fun onSuccess(model: Any?) {
        if (model as TutorialMainModel? != null) {
            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    if (model?.data != null) {
                        if (model?.data.isNotEmpty()) {
                            listTutorialSubVideoList = model?.data
                            for (i in model?.data.indices) {
                                model?.data[i].indexPosition = i
                            }
                            constraint_no_Videos.visibility = View.GONE
                            setRecycler(model?.data)
                        } else {
                            constraint_no_Videos.visibility = View.VISIBLE
                        }
                    }

                }
                Constants.SESSION_ERROR_CODE -> {
                    if (!this.isDestroyed) {
                        ReuseFunctions.startNewActivityTaskTop(
                            this,
                            LoginScreen::class.java
                        )
                        finish()
                    }
                }
                Constants.FIELD_VALIDATION_ERROR_CODE -> {
                }
            }
        }
    }

    override fun onFailure(error: String) {
        if (!this.isDestroyed) {
            ReuseFunctions.showToast(this, error)
        }
    }

    private fun setDownloadFunctionality() {

        if (isCatAlreadyExist()) {
            ReuseFunctions.snackMessage(
                main_layout,
                resources.getString(R.string.alreadydownloaded)
            )
        } else {

            if (listTutorialSubVideoList!!.isNotEmpty()) {
                openDownloadBottomSheet()
            }

        }
    }

    fun isCatAlreadyExist(): Boolean {
        var file: File
        file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.CAT_FOLDER_NAME + folderName
        )
        return file.exists()
    }

    private fun openDownloadBottomSheet() {

        dialogDownloadBottom = BottomSheetDialog(this)
        var dialogView: View = layoutInflater.inflate(
            R.layout.bottom_layout_download_video,
            null
        )
        /**
         * thumbnail not available yet --set it later
         */
        /*try {
            Glide.with(this).load(URLDecoder.decode(selectedModel!!.poster))
                .into(dialogView.imageView_round)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }*/

        dialogView.constraint_video_card.visibility = View.GONE
        dialogView.tv_down_video.text = this.resources.getString(R.string.download_comp_cat)
        if (name != null) {
            dialogView.tv_main.text = name
        }
        //dialogView.tv_duration.text = size.toString() + " Words - Videos"
        dialogView.tv_translate.visibility = View.GONE
        dialogView.tv_high.text = ""
        dialogView.tv_medium.text = ""
        dialogView.tv_low.text = ""

        dialogView.radiogroup.setOnCheckedChangeListener { arg0, arg1 ->
            val selectedId = dialogView.radiogroup.checkedRadioButtonId
            when (selectedId) {
                R.id.radio_btn_high -> {
                    selected_quality_type = Constants.p720p
                }
                R.id.radio_btn_medium -> {
                    selected_quality_type = Constants.p360p
                }
                R.id.radio_btn_low -> {
                    selected_quality_type = Constants.p240p
                }
            }
        }
        dialogView.btn_download.setOnClickListener {
            checkPermission()

        }
        dialogDownloadBottom!!.setContentView(dialogView)
        dialogDownloadBottom!!.show()

    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), Constants.MY_PERMISSIONS_REQUEST_WRITE_STORAGE
            )
        } else {
            if (selected_quality_type == -1) {
                ReuseFunctions.showToast(this, "Select one category")
            } else {
                newDownload()

            }
        }
    }


    private fun newDownload() {
        val carrierModel: DictionaryListCarrierDataModel? =
            DictionaryListCarrierDataModel()
        carrierModel?.setTutorialModelList(listTutorialSubVideoList)

        ReuseFunctions.showToast(this, "Download started")

        val downloadVideoBroadcastReceiver = DownloadVideoBroadcastReceiver()
        val intent = Intent(this, downloadVideoBroadcastReceiver::class.java)
        val bundle = Bundle()
        bundle.putSerializable(Constants.CATEGORY_LIST, carrierModel as Serializable)
        intent.putExtra(Constants.DOWNLOAD_TYPE, Constants.MULTI_VIDEO_DOWNLOAD)
        intent.putExtra(Constants.CAT_NAME, folderName)
        intent.putExtra("TYPE", Constants.TYPE_SUB_TOPIC)
        Constants.p720p
        intent.putExtra(Constants.QUALITY_ID, selected_quality_type)
        intent.putExtra("LIST", bundle)
        sendBroadcast(intent)
        // progressBar.visibility = View.VISIBLE

        Constants.DOWNLOAD_STATUS = Constants.DOWNLOAD_REQUESTED
        if (dialogDownloadBottom != null) {
            dialogDownloadBottom!!.dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.MY_PERMISSIONS_REQUEST_WRITE_STORAGE -> if (grantResults.size > 0 && permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                // check whether storage permission granted or not.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (selected_quality_type == -1) {
                        ReuseFunctions.showToast(this, "Select one category")
                    } else {
                        try {
                            newDownload()
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }

                    }
                }
            }
            else -> {
            }
        }
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

    private fun sortList(sortType: String) {
        if (!this.isDestroyed && adapter!=null) {
            when (sortType) {

                "1" -> {

                    if (listTutorialSubVideoList != null && listTutorialSubVideoList?.size != 0) {
                        val sortedAppsList1 = listTutorialSubVideoList!!.sortedBy { it.title }
                        adapter!!.setTeacherTutorialSubjectList(sortedAppsList1)
                    } else {
                        ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                    }


                    adapter!!.notifyDataSetChanged()
                    isSorted = true
                    ReuseFunctions.snackMessage(
                        constraint_main_listing,
                        resources.getString(R.string.sortedByAscending)
                    )
                }
                "2" -> {

                    if (listTutorialSubVideoList != null && listTutorialSubVideoList?.size != 0) {
                        val sortedAppsList1 =
                            listTutorialSubVideoList!!.sortedByDescending { it.title }
                        adapter!!.setTeacherTutorialSubjectList(sortedAppsList1)
                    } else {
                        ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                    }

                    adapter!!.notifyDataSetChanged()
                    ReuseFunctions.snackMessage(
                        constraint_main_listing,
                        resources.getString(R.string.sortedByDescending)
                    )
                    isSorted = false
                }
                else -> {
                    adapter!!.setTeacherTutorialSubjectList(listTutorialSubVideoList!!)
                    adapter!!.notifyDataSetChanged()
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

    override fun onSpeedChangeSelected(speedId: String) {
    }
}