package com.net.pslapllication.activities.dictionary

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.net.pslapllication.R
import com.net.pslapllication.activities.BaseActivity
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.adpters.DictionarySingleWordAdapter
import com.net.pslapllication.adpters.LargeCardAdapter
import com.net.pslapllication.adpters.VideoQualityOptionAdapter
import com.net.pslapllication.broadcastReceiver.DownloadVideoBroadcastReceiver
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.interfaces.onQualityChangSelectedListener
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.dictionary.DictionaryMainModel
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.learningtutorial.LearningTutorialMainModel
import com.net.pslapllication.model.preferences.Story_types
import com.net.pslapllication.model.preferences.Subjects
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.stories.StoriesMainModel
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.Constants.Companion.TYPE_LEARNING_TUTORIAL
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_dictionary_single_word.*
import kotlinx.android.synthetic.main.activity_dictionary_single_word.tv_sort
import kotlinx.android.synthetic.main.activity_main_listing_new.*
import kotlinx.android.synthetic.main.bottom_layout_download_video.view.*
import kotlinx.android.synthetic.main.bottom_layout_video_quality_list.view.*
import kotlinx.android.synthetic.main.toolbaar_layout.*
import retrofit2.Call
import java.io.File
import java.io.Serializable
import java.io.UnsupportedEncodingException

class DictionarySingleWordActivity : BaseActivity(), View.OnClickListener,
    RetrofitResponseListener, onQualityChangSelectedListener {
    private var width: Int = 0
    private var selected_quality_type: Int = -1
    lateinit var apiService: ApiService
    private var id: String = ""
    private var name: String? = ""
    private var type: String = ""
    private var itemCount: Int = 0
    private var list: List<DictionaryData>? = ArrayList<DictionaryData>()
    private var listLearningTutorial: List<LearningData>? = ArrayList<LearningData>()
    private var listStories: List<StoryData>? = ArrayList<StoryData>()
    private var listSub: List<Subjects>? = ArrayList<Subjects>()
    private var dialogDownloadBottom: BottomSheetDialog? = null
    private var isSorted: Boolean = true
    private var adapter: DictionarySingleWordAdapter? = null
    private var adapterLarge: LargeCardAdapter? = null
    private var dialog_sort: BottomSheetDialog? = null
    private var selectedSortyId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary_single_word)

        checkIntent()
        initialization()
        setListener()
        setSearchView()
    }

    private fun initialization() {
        if (type != "") {
            if (type == Constants.TYPE_STORIES || type == Constants.TYPE_LEARNING_TUTORIAL){
                adapterLarge = LargeCardAdapter(this, type, name.toString())
                grid_view_single_word.adapter = adapterLarge
                grid_view_single_word.numColumns = 1
            }
            else{
                adapter = DictionarySingleWordAdapter(this, type, name.toString())
                grid_view_single_word.adapter = adapter
                grid_view_single_word.numColumns = 2
            }

            if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                img_btn_download.visibility = View.GONE
                setSubjectRecycler(listSub)
            }
        }
        circular_progress.visibility = View.GONE
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            if (itemCount == 0) {
                img_btn_download.visibility = View.GONE
                circular_progress.visibility = View.GONE
            } else {
                img_btn_download.visibility = View.VISIBLE
                circular_progress.visibility = View.VISIBLE
                if (type == Constants.TYPE_STORIES) {
                    val call = ApiCallClass.apiService.getStoriesData(
                        SharedPreferenceClass.getInstance(this)?.getSession().toString(),
                        SharedPreferenceClass.getInstance(this)?.getUserType().toString(),
                        id
                    )
                    ApiCallClass.retrofitCall(this, call as Call<Any>)
                } else if (type == Constants.TYPE_DICTIONARY) {
                    if (id != "") {
                        val call = apiService.getDictionaryData(
                            SharedPreferenceClass.getInstance(this)?.getSession().toString(),
                            SharedPreferenceClass.getInstance(this)?.getUserType().toString(),
                            id
                        )
                        ApiCallClass.retrofitCall(this, call as Call<Any>)
                    }
                } else if (type == TYPE_LEARNING_TUTORIAL ) {
                    if (id != "") {
                        val call = apiService.getLessonData(
                            SharedPreferenceClass.getInstance(this)?.getSession().toString(),
                            SharedPreferenceClass.getInstance(this)?.getUserType().toString(),
                            id
                        )
                        ApiCallClass.retrofitCall(this, call as Call<Any>)
                    }
                }
            }
        } else {
            if (!this.isDestroyed) {
                ReuseFunctions.snackMessage(
                    constraint_single_word,
                    this.getString(R.string.no_internet_text)
                )
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun setListener() {
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
        opsBackBtn.setOnClickListener(this)
        tv_sort.setOnClickListener(this)
        img_btn_download.setOnClickListener(this)
    }

    private fun checkIntent() {
        if (intent != null && intent.getStringExtra("ID") != null && intent.getStringExtra("ID") != ""
            && intent.getStringExtra(
                "NAME"
            ) != null
            && intent.getStringExtra("TYPE") != null
        ) {
            id = intent.getStringExtra("ID")!!
            name = intent.getStringExtra("NAME")
            type = intent.getStringExtra("TYPE")!!
            itemCount = intent.getIntExtra("ITEMSCOUNT", 0)
            if (itemCount == 0) {
                constraint_noDownload.visibility = View.VISIBLE
            } else {
                constraint_noDownload.visibility = View.GONE
            }

            setTitleText(name)
            if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                val dictionaryListCarrierDataModel =
                    intent.getSerializableExtra(Constants.DICTIONARY_LIST_MODEL) as DictionaryListCarrierDataModel
                listSub = dictionaryListCarrierDataModel.getsubjectList()
                if (listSub!!.isEmpty()) {
                    constraint_noDownload.visibility = View.VISIBLE
                } else {
                    constraint_noDownload.visibility = View.GONE
                }
            }

        }

    }

    private fun setTitleText(name: String?) {
        txt_title.text = ReuseFunctions.firstLetterCap(name)
    }


    private fun setStoryRecycler(data: List<StoryData>?) {
        if (adapterLarge != null) {
            if (isCatAlreadyExist(name!!,this)){
                adapterLarge!!.downloded(true)
            }else{
                adapterLarge!!.downloded(false)
            }
            adapterLarge!!.setStories(data)
            adapterLarge!!.notifyDataSetChanged()
        }
    }

    private fun setLearningRecycler(data: List<LearningData>?) {
        if (adapterLarge != null && data!!.isNotEmpty()) {
            if (isCatAlreadyExist(name!!,this)){
                adapterLarge!!.downloded(true)
            }else{
                adapterLarge!!.downloded(false)
            }
            adapterLarge!!.setLearningTutorial(data)
            adapterLarge!!.notifyDataSetChanged()
        }
    }

    private fun setSubjectRecycler(list: List<Subjects>?) {

        if (list!!.isNotEmpty()) {

            if (adapter != null) {
                if (isCatAlreadyExist(name!!,this)){
                    adapter!!.downloded(true)
                }else{
                    adapter!!.downloded(false)
                }
                adapter!!.setSubjects(list, name!!)
                adapter!!.notifyDataSetChanged()
            }

        }
    }

    private fun setDictionaryDetailRecycler(
        type: String,
        name: String,
        data: List<DictionaryData>
    ) {
        var dicWordList: List<DictionaryListModel> = ArrayList<DictionaryListModel>()
        when (type) {
            Constants.TYPE_DICTIONARY -> {
                if (isCatAlreadyExist(name, this)) {
                    adapter!!.downloded(true)
                } else {
                    adapter!!.downloded(false)
                }
                if (adapter != null) {
                    adapter!!.setWords(data)
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.opsBackBtn -> {
                onBackPressed()
            }
            R.id.img_btn_download -> {
                setDownloadFunctionality()

            }
            R.id.tv_sort -> {
                ReuseFunctions.preventTwoClick(v)
                openSortBottomSheet()

            }
            R.id.toolbar -> {

                try {
                    progressBar.visibility = View.VISIBLE

                    progressBar.progress = 70
                    width = progressBar.width
                    val widthprogress = (width * 70) / 100
                    val paramsdim =
                        tv_download1.layoutParams
                    paramsdim.width = widthprogress
                    tv_download1.layoutParams = paramsdim
                    tv_download1.text = "Download Complete Category"
                    tv_download1.setLines(1)
                    tv_download1.ellipsize = TextUtils.TruncateAt.END;
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                /*if (list != null && list?.size!! > 0) {
                    openDownloadBottomSheet(list?.size!!)
                }*/
            }
        }
    }

    private fun setDownloadFunctionality() {
        if (isCatAlreadyExist(name!!, this)) {
            ReuseFunctions.snackMessage(
                constraint_single_word,
                resources.getString(R.string.alreadydownloaded)
            )
        } else {
            if (type == Constants.TYPE_DICTIONARY) {
                if (list!!.size != 0) {
                    openDownloadBottomSheet()
                }
            } else if (type == Constants.TYPE_STORIES) {
                if (listStories!!.size != 0) {
                    openDownloadBottomSheet()
                }
            }else if (type == TYPE_LEARNING_TUTORIAL || type == TYPE_LEARNING_TUTORIAL) {
                if (listLearningTutorial!!.size != 0) {
                    openDownloadBottomSheet()
                }
            }

        }

    }

    private fun sortListFunction(list: List<DictionaryData>) {
        if (!this.isDestroyed) {
            if (isSorted) {
                val reverseSortedAppsList: List<DictionaryData> =
                    list.sortedByDescending { it.english_word }
                adapter!!.setWords(reverseSortedAppsList)
                adapter!!.notifyDataSetChanged()
                ReuseFunctions.snackMessage(
                    constraint_single_word,
                    resources.getString(R.string.sortedByDescending)
                )
                isSorted = false
            } else {


                val sortedAppsList1: List<DictionaryData> = list.sortedBy { it.english_word }
                adapter!!.setWords(sortedAppsList1)
                adapter!!.notifyDataSetChanged()
                isSorted = true
                ReuseFunctions.snackMessage(
                    constraint_single_word,
                    resources.getString(R.string.sortedByAscending)
                )
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    override fun onSuccess(model: Any?) {
        if (type == Constants.TYPE_STORIES) {
            if (model as StoriesMainModel? != null) {
                when (model?.code) {
                    Constants.SUCCESS_CODE -> {
                        if (model?.data != null) {
                            for (i in model?.data.indices) {
                                model?.data[i].indexPosition = i
                            }
                            listStories = model?.data
                            setStoryRecycler(model?.data)
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
                        //no field here

                    }
                }

            }
        }
        else if (type == Constants.TYPE_DICTIONARY) {
            if (model as DictionaryMainModel? != null) {
                when (model?.code) {
                    Constants.SUCCESS_CODE -> {
                        list = model?.data
                        if (list!!.size != 0) {
                            if (model?.data != null) {
                                constraint_noDownload.visibility = View.GONE
                                for (i in model?.data.indices) {
                                    model?.data[i].indexPosition = i
                                }
                                if (type != "" && name != "") {
                                    setDictionaryDetailRecycler(
                                        type,
                                        name!!,
                                        model?.data
                                    )
                                    setSum(model?.data)

                                }
                            }
                        } else {
                            constraint_noDownload.visibility = View.VISIBLE
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
                        //no field here

                    }
                }
            }
        }
        else if (type == Constants.TYPE_LEARNING_TUTORIAL ) {
            if (model as LearningTutorialMainModel? != null) {
                when (model?.code) {
                    Constants.SUCCESS_CODE -> {
                        listLearningTutorial = model?.data
                        if (listLearningTutorial!!.size != 0) {
                            if (model?.data != null) {
                                constraint_noDownload.visibility = View.GONE
                                for (i in model?.data.indices) {
                                    model?.data[i].indexPosition = i
                                }
                                if (type != "" && name != "") {
                                    setLearningRecycler(

                                        model?.data
                                    )


                                }
                            }
                        } else {
                            constraint_noDownload.visibility = View.VISIBLE
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
                        //no field here

                    }
                }
            }
        }

    }

    private fun setSum(data: List<DictionaryData>) {

        /*  data.filter { p -> data.any { it.p480p.filesize.removeRange(it.p480p.filesize.length-2) }

          val totalQuantity: Int = data.sumBy { it.p240p.filesize.toInt() }*/


    }

    override fun onFailure(error: String) {
        if (!this.isDestroyed) {
            ReuseFunctions.showToast(this, error)
        }
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

    override fun onPause() {
        /*  if (dialogDownloadBottom != null) {
              dialogDownloadBottom!!.cancel()
          }*/
        if (dialog_sort != null) {
            dialog_sort!!.cancel()
        }
        super.onPause()
    }

    override fun onDestroy() {
        /*if (dialogDownloadBottom != null) {
            dialogDownloadBottom!!.cancel()
        }*/
        super.onDestroy()
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
        if (type == Constants.TYPE_DICTIONARY) {
            carrierModel?.setModelList(list)
        } else if (type == Constants.TYPE_STORIES) {
            carrierModel?.setStoryList(listStories)
        }
else if (type == TYPE_LEARNING_TUTORIAL) {
            carrierModel?.setLeaningModelList(listLearningTutorial)
        }


        ReuseFunctions.showToast(this, "Download started")

        val downloadVideoBroadcastReceiver = DownloadVideoBroadcastReceiver()
        val intent = Intent(this, downloadVideoBroadcastReceiver::class.java)
        val bundle = Bundle()
        bundle.putSerializable(Constants.CATEGORY_LIST, carrierModel as Serializable)
        intent.putExtra(Constants.DOWNLOAD_TYPE, Constants.MULTI_VIDEO_DOWNLOAD)
        intent.putExtra(Constants.CAT_NAME, name)
        intent.putExtra("TYPE", type)
        Constants.p720p
        intent.putExtra(Constants.QUALITY_ID, selected_quality_type)
        intent.putExtra("LIST", bundle)
        sendBroadcast(intent)
        progressBar.visibility = View.VISIBLE

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
                            val sortedAppsList1: List<DictionaryData> =
                                list!!.sortedBy { it.english_word }
                            adapter!!.setWords(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                    } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                        if (listSub != null && listSub?.size != 0) {
                            val sortedAppsList1: List<Subjects> =
                                listSub!!.sortedBy { it.title }
                            adapter!!.setSubjects(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                        adapter!!.notifyDataSetChanged()
                    }else if (type == TYPE_LEARNING_TUTORIAL ) {
                        if (listLearningTutorial != null && listLearningTutorial?.size != 0) {
                            val sortedAppsList1: List<LearningData> =
                                listLearningTutorial!!.sortedBy { it.title }
                            adapterLarge!!.setLearningTutorial(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                        adapterLarge!!.notifyDataSetChanged()
                    } else if (type == Constants.TYPE_STORIES) {
                        if (listStories != null && listStories?.size != 0) {
                            val sortedAppsList1: List<StoryData> =
                                listStories!!.sortedBy { it.title }
                            adapterLarge!!.setStories(sortedAppsList1)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                        adapterLarge!!.notifyDataSetChanged()
                    }

                    isSorted = true
                    ReuseFunctions.snackMessage(
                        constraint_single_word,
                        resources.getString(R.string.sortedByAscending)
                    )
                }
                "2" -> {
                    if (type == Constants.TYPE_DICTIONARY) {
                        if (list != null && list?.size != 0) {
                            val reverseSortedAppsList: List<DictionaryData> =
                                list!!.sortedByDescending { it.english_word }
                            adapter!!.setWords(reverseSortedAppsList)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                        adapter!!.notifyDataSetChanged()
                    } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                        if (listSub != null && listSub?.size != 0) {
                            val reverseSortedAppsList: List<Subjects> =
                                listSub!!.sortedByDescending { it.title }
                            adapter!!.setSubjects(reverseSortedAppsList)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                        adapter!!.notifyDataSetChanged()
                    }else if (type == TYPE_LEARNING_TUTORIAL  ) {
                        if (listLearningTutorial != null && listLearningTutorial?.size != 0) {
                            val reverseSortedAppsList: List<LearningData> =
                                listLearningTutorial!!.sortedByDescending { it.title }
                            adapterLarge!!.setLearningTutorial(reverseSortedAppsList)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                        adapterLarge!!.notifyDataSetChanged()
                    } else if (type == Constants.TYPE_STORIES) {
                        if (listStories != null && listStories?.size != 0) {
                            val reverseSortedAppsList: List<StoryData> =
                                listStories!!.sortedByDescending { it.title }
                            adapterLarge!!.setStories(reverseSortedAppsList)
                        } else {
                            ReuseFunctions.snackMessage(constraint_main_listing, "Nothing to Sort")
                        }
                        adapterLarge!!.notifyDataSetChanged()
                    }

                    ReuseFunctions.snackMessage(
                        constraint_single_word,
                        resources.getString(R.string.sortedByDescending)
                    )
                }
                else -> {
                    if (type == Constants.TYPE_DICTIONARY) {
                        adapter!!.setWords(list!!)
                        adapter!!.notifyDataSetChanged()
                    } else if (type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_LEARNING_TUTORIAL_REAL) {
                        adapter!!.setSubjects(listSub!!)
                        adapter!!.notifyDataSetChanged()
                    } else if (type == Constants.TYPE_STORIES) {
                        adapterLarge!!.setStories(listStories!!)
                        adapterLarge!!.notifyDataSetChanged()
                    }else if (type == TYPE_LEARNING_TUTORIAL ) {
                        adapterLarge!!.setLearningTutorial(listLearningTutorial!!)
                        adapterLarge!!.notifyDataSetChanged()
                    }

                    ReuseFunctions.snackMessage(
                        constraint_single_word,
                        resources.getString(R.string.sortedByDefault)
                    )
                }
            }
        }
    }

    private fun setSearchView() {
        val searchView: SearchView =
            findViewById(R.id.searchView_dic_single_word)
        val searchText =
            searchView.findViewById<View>(R.id.search_src_text) as TextView
        searchText.typeface = ReuseFunctions.regularFont(this)
        searchText.textSize = 16f
        searchText.setPadding(0, 0, 0, 0);
        searchView_dic_single_word.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (type == Constants.TYPE_STORIES || type == TYPE_LEARNING_TUTORIAL) {
                    if (adapterLarge != null) {
                        adapterLarge!!.filter.filter(query)
                    }
                }
                else{
                    if (adapter != null) {
                        adapter!!.filter.filter(query)
                    }
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (type == Constants.TYPE_STORIES|| type == TYPE_LEARNING_TUTORIAL) {
                    if (adapterLarge != null) {
                        adapterLarge!!.filter.filter(newText)
                    }
                }
                else{
                    if (adapter != null) {
                        adapter!!.filter.filter(newText)
                    }
                }

                return false
            }

        })
    }

    fun isCatAlreadyExist(folderName: String, context: Context): Boolean {

        var file: File

        file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.CAT_FOLDER_NAME + folderName
        )
        return file.exists()
    }
}



