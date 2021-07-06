package com.net.pslapllication.activities.dictionary

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.net.pslapllication.R
import com.net.pslapllication.activities.BaseActivity
import com.net.pslapllication.adpters.*
import com.net.pslapllication.broadcastReceiver.DownloadVideoBroadcastReceiver
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.interfaces.OnVideoSelectedListener
import com.net.pslapllication.interfaces.onQualityChangSelectedListener
import com.net.pslapllication.model.addToFavourite.AddToFvouriteModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.favouriteList.Data
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import com.net.pslapllication.util.*
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.*
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.btn_replay
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_bottom_options
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_centerscreen
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_dimview
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_download
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_favourite
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_share
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_toolbaar
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_vimeo
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.constraint_youtube
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.error_layout
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.image_btn_menu
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.img_btn_play_next
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.img_btn_play_pause_center
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.img_btn_pre
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.mainlayout
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.nestedScrollView
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.progress
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.recycler_next
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.reload_layout
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.switch_next
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.tv_main
import kotlinx.android.synthetic.main.activity_video_preview_tutorial.videoview
import kotlinx.android.synthetic.main.bottom_layout_download_video.view.*
import kotlinx.android.synthetic.main.bottom_layout_download_video.view.tv_down_video
import kotlinx.android.synthetic.main.bottom_layout_video_option.view.*
import kotlinx.android.synthetic.main.bottom_layout_video_option.view.bottom_sheet_quality
import kotlinx.android.synthetic.main.bottom_layout_video_quality_list.view.*
import  kotlinx.android.synthetic.main.activity_video_preview_tutorial.layout_next

import kotlinx.android.synthetic.main.layout_videoview_error.*
import kotlinx.android.synthetic.main.playerbarlayout.*
import kotlinx.android.synthetic.main.toolbaar_layout.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class VideoPreviewLearningTutorialActivity : BaseActivity(), View.OnClickListener,
    onQualityChangSelectedListener,
    OnVideoSelectedListener, MediaPlayer.OnBufferingUpdateListener {
    //boolean
    private var autoPlayStatus: Boolean = false
    private var isReload: Boolean = false
    private var isNetworkAvailable: Boolean = false
    private var playToggle: Boolean = true
    private var pauseToggle: Boolean = true
    private var favClick: Boolean = false
    private var isAutoQuality: Boolean = true
    private var isSeekBarClick: Boolean = false

    //double
    private var currentPos: Double = 0.0
    private var totalDuration: Double = 0.0

    //int
    var selectedId: Int = -1
    private var selectedQualityId: Int = 0
    private var seekbaarProgress: Int = 0
    private var selectedSpeedId: Int = 3

    //string
    private var networkType: String = ""
    private var selected_url = ""
    private var categoryType = ""
    private var selectedSpeedString = "Normal"

    //data model
    var selectedModel: Any? = null

    //list
    public var list: List<LearningData>? = null

    //others
    private lateinit var dialog_quality: BottomSheetDialog
    private var dialogDownloadBottom: BottomSheetDialog? = null
    private var downloadDialog: Dialog? = null
    private var bnp: ProgressBar? = null
    private var tv_progress_txt: TextView? = null
    private lateinit var apiService: ApiService
    private var adapter: VideoPreviewLearningTutorialAdapter? = null
    var mediaPlayer: MediaPlayer? = null

    var context: Context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview_tutorial)
        SharedPreferenceClass.getInstance(this)
            ?.setPos("0.0")
        getIntentUrl()
        checkConnectionQuality()
        nestedScrollView.isEnableScrolling = false
        setAdapter()
        getIntentData()

        setListener()
        setAlreadyFavourite()
        checkAutoPlaySwitch()
        Handler().postDelayed({
            if (isVideoAlreadyExist((selectedModel as LearningData?)!!.filename)) {
                constraint_download.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.ic_video_downloaded),
                    null,
                    null
                );

                constraint_download.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                constraint_download.text = resources.getString(R.string.downloaded)

            }
        }, 500)
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected){
            isNetworkAvailable = isConnected
        }
        else{
            isNetworkAvailable = isConnected

            ReuseFunctions.snackMessage(mainlayout,   this.resources.getString(R.string.no_internet_text))
        }
        super.onNetworkConnectionChanged(isConnected)
    }
    private fun setAdapter() {
        val mLayoutManager =
            LinearLayoutManagerClass(this, LinearLayout.VERTICAL, false)
        recycler_next.layoutManager = mLayoutManager
        adapter = VideoPreviewLearningTutorialAdapter(this, Constants.TYPE_VIDEO, "", this)
        recycler_next.adapter = adapter
    }

    private fun getIntentData() {
        if (intent != null &&
            intent.getSerializableExtra(Constants.SELECTED_DICTIONARY_LIST_MODEL) != null &&
            intent.getStringExtra(Constants.CETAGORY_TYPE) != null
        ) {
            /********get categoty type*********/
            categoryType =
                intent.getStringExtra(Constants.CETAGORY_TYPE)!!

            /********get selected video list data*********/
            selectedModel =
                intent.getSerializableExtra(Constants.SELECTED_DICTIONARY_LIST_MODEL) as LearningData
            if (selectedModel != null) {
                setTitleText((selectedModel as LearningData))
            }

            /********get complete list data*********/

            Handler().postDelayed({
                list = ProgressHelper.getInstance(context)?.getLearningList()
                if (list?.size!=0){
                    val recyclerList = list!!.drop(1)

                    setNextVideosList(recyclerList)
                }
            },500)

            /* Handler().postDelayed({
                var LearningData: LearningData? = null
                LearningData?.id = selectedId
                if (list != null && list!!.size != 0 && selectedModel != null && (selectedModel!! as LearningData).id != null) {
                    val finalList = ListSorting.sortList(
                        (selectedModel!! as LearningData).indexPosition,
                        list!!
                    )
                    setNextVideosList(list!!)
                }
            }, 2000)*/
        }
    }




    public fun getIntentUrl() {
        val intent = intent
        if (intent != null) {
            if (Intent.ACTION_VIEW == intent.action) {
                val uri = intent.data
                if (uri != null && videoview != null) {
                    setplayer(uri.toString())
                    videoview.start()
                }
            }
        }
    }

    private fun checkConnectionQuality() {
        if (isAutoQuality) {
            val handler = Handler()
            val runnableCode: Runnable = object : Runnable {
                override fun run() {
                    if (isNetworkAvailable) {
                        if (progress.visibility == View.GONE) {
                            if (networkType == Constants.NetworkTYPE_MOB_DATA) {
                                if (ReuseFunctions.networkQualityMobData(this@VideoPreviewLearningTutorialActivity) ==
                                    SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)
                                        ?.getQualityMob()
                                ) {
                                    //do nothing when quality doesn't change..
                                    ReuseFunctions.showToast(
                                        this@VideoPreviewLearningTutorialActivity,
                                        "do nothing"
                                    )
                                } else {

                                    SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)
                                        ?.setQualityMob(ReuseFunctions.networkQualityMobData(this@VideoPreviewLearningTutorialActivity))
                                    setUrlOfQuality(ReuseFunctions.networkQualityMobData(this@VideoPreviewLearningTutorialActivity))

                                }
                            } else if (networkType == Constants.NetworkTYPE_WIFI) {
                                if (ReuseFunctions.networkQualityWifi(this@VideoPreviewLearningTutorialActivity) ==
                                    SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)
                                        ?.getQualityWifi()
                                ) {
                                    //do nothing when quality doesn't change..
                                    ReuseFunctions.showToast(
                                        this@VideoPreviewLearningTutorialActivity,
                                        "do nothing"
                                    )
                                } else {
                                    if (SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)
                                            ?.getPos() != ""
                                    ) {
                                        SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)
                                            ?.setQualityWifi(ReuseFunctions.networkQualityWifi(this@VideoPreviewLearningTutorialActivity))
                                        setUrlOfQuality(ReuseFunctions.networkQualityWifi(this@VideoPreviewLearningTutorialActivity))
                                    }
                                }
                            }
                        }
                    }
                    handler.postDelayed(this, 1000)
                }
            }
            handler.post(runnableCode)
        }
    }

    private fun setUrlOfQuality(isStatusChabge: String) {
        when (isStatusChabge) {
            "POOR" -> {
                if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p240p?.url != null) {
                    var videoUrl: String =
                        URLDecoder.decode((selectedModel as LearningData?)!!.p240p.url)
                    setUrlBasedOnQuality(videoUrl)
                    ReuseFunctions.showToast(
                        this@VideoPreviewLearningTutorialActivity,
                        "  " + isStatusChabge
                    )
                    selectedQualityId = Constants.p240p
                }
            }
            "MODERATE" -> {
                if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p360p?.url != null) {
                    var videoUrl: String =
                        URLDecoder.decode((selectedModel as LearningData?)!!.p360p.url)
                    setUrlBasedOnQuality(videoUrl)
                }
                selectedQualityId = Constants.p360p

                ReuseFunctions.showToast(
                    this@VideoPreviewLearningTutorialActivity,
                    "  " + isStatusChabge
                )

            }
            "GOOD" -> {
                if (selectedModel != null && (selectedModel as LearningData?)?.p480p?.url != null) {
                    var videoUrl: String =
                        URLDecoder.decode((selectedModel as LearningData?)!!.p480p.url)
                    setUrlBasedOnQuality(videoUrl)
                }
                selectedQualityId = Constants.p480p

                ReuseFunctions.showToast(
                    this@VideoPreviewLearningTutorialActivity,
                    "  " + isStatusChabge
                )
            }
            "EXCELLENT" -> {
                if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p720p?.url != null) {
                    var videoUrl: String =
                        URLDecoder.decode((selectedModel as LearningData?)!!.p720p!!.url)
                    setUrlBasedOnQuality(videoUrl)
                }
                selectedQualityId = Constants.p720p
                ReuseFunctions.showToast(
                    this@VideoPreviewLearningTutorialActivity,
                    "  " + isStatusChabge
                )
            }
        }
    }

    private fun setUrlBasedOnQuality(url: String) {
        runOnUiThread {
            if (SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)?.getPos() == "") {
                //do nothing
            } else {
                setplayer(url)
                val roundedBalance =
                    SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)?.getPos()
                val temp = roundedBalance?.substringBefore(".")
                if (temp != null) {
                    videoview.seekTo(
                        temp.toInt()
                    )
                }
                videoview.start()
            }
        }
    }


    private fun setListener() {
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
        opsBackBtn.setOnClickListener(this)
        viewShadow.visibility=View.GONE
        constraint_favourite.setOnClickListener(this)
        constraint_share.setOnClickListener(this)
        constraint_download.setOnClickListener(this)

        image_btn_menu.setOnClickListener(this)
        img_btn_play_pause.setOnClickListener(this)
        img_btn_full_screen.setOnClickListener(this)
        img_btn_play_pause_center.setOnClickListener(this)
        img_btn_pre.setOnClickListener(this)
        img_btn_play_next.setOnClickListener(this)
        layout_click.setOnClickListener(this)
        btn_replay.setOnClickListener(this)
    }

    private fun setAlreadyFavourite() {
        if (selectedModel != null) {
            if ((selectedModel as LearningData?)!!.favorite != null && (selectedModel as LearningData?)!!.favorite == 0) {
                //do nothing default UI
            } else {
                setButtonTextColors(
                    constraint_favourite,
                    constraint_share,

                    constraint_vimeo,
                    constraint_youtube
                )
                setImageDrawables(
                    R.drawable.ic_favorite_green,
                    R.drawable.ic_share_grey,
                    R.drawable.ic_file_download_grey,
                    R.drawable.ic_vimeo_grey,
                    R.drawable.ic_youtube_grey
                )
                favClick = true
            }
        }
    }

    private fun checkAutoPlaySwitch() {
        switch_next.isChecked = SharedPreferenceClass.getInstance(this)?.getAutoPLayToggle()!!
        autoPlayStatus = SharedPreferenceClass.getInstance(this)?.getAutoPLayToggle()!!
        switch_next.setOnCheckedChangeListener { _, isChecked ->
            autoPlayStatus = isChecked
            SharedPreferenceClass.getInstance(this)?.setAutoPLayToggle(isChecked)
        }
    }


    private fun setTitleText(selectedModel: LearningData) {
        txt_title.text = ReuseFunctions.firstLetterCap(selectedModel.title)
        tv_main.text = ReuseFunctions.firstLetterCap(selectedModel.title)
       // tv_translate.text = " "+selectedModel!!.urdu_word

    }

    private fun setNextVideosList(list: List<LearningData>) {

        if (adapter != null) {
            if(list.size == 0){
                layout_next.visibility =  View.GONE
            }
            adapter!!.setWords(list)
            adapter!!.notifyDataSetChanged()
            nestedScrollView.smoothScrollTo(0, 0)
            nestedScrollView.isEnableScrolling = true
        }


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_btn_play_pause_center -> {
                if (videoview.isPlaying) {
                    img_btn_play_pause_center.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_play_white_large))
                    img_btn_play_pause.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_play_white))
                    videoview.pause()
                } else {
                    img_btn_play_pause_center.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_white_large))
                    img_btn_play_pause.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_white))
                    videoview.start()
                }
            }

            R.id.btn_replay -> {
                if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p720p?.url != null) {
                    try {
                        isReload = false
                        var videoUrl: String =
                            URLDecoder.decode((selectedModel as LearningData?)!!.p720p!!.url)
                        videoview.visibility = View.VISIBLE
                        image_btn_menu.visibility = View.GONE
                        progress.visibility = View.VISIBLE
                        reload_layout.visibility = View.GONE

                        setplayer(videoUrl)
                        videoview.start()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
            R.id.img_btn_play_next -> {
                setNextPreVideo(1)
            }
            R.id.img_btn_pre -> {
                setNextPreVideo(-1)
            }
            R.id.image_btn_menu -> {
                openBottomSheetQuality()
            }
            R.id.layout_click -> {
                videoview.visibility = View.VISIBLE
                constraint_centerscreen.visibility = View.GONE
                image_btn_menu.visibility = View.GONE
                constraint_dimview.visibility = View.VISIBLE
                progress.visibility = View.VISIBLE
                if (selectedModel != null) {
                    val videoUrl: String =
                        URLDecoder.decode((selectedModel as LearningData?)!!.p720p!!.url)
                    setplayer(videoUrl)
                }
                videoview.start()
            }
            R.id.img_btn_play_pause -> {
                setPlayPauseVideo()
            }
            R.id.img_btn_full_screen -> {
                if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    img_btn_full_screen.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_fullscreen_white_exit))
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    nestedScrollView.visibility = View.GONE
                    constraint_toolbaar.visibility = View.GONE
                    val metrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(metrics)
                    val params =
                        videoview.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = metrics.heightPixels
                    videoview.layoutParams = params
                    val paramsDim =
                        constraint_dimview.layoutParams
                    paramsDim.width = metrics.widthPixels
                    paramsDim.height = metrics.heightPixels
                    //constraint_dimview.layoutParams. = paramsDim
                    val paramsdimView =
                        constraint_dimview.layoutParams
                    paramsdimView.width = ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    nestedScrollView.visibility = View.VISIBLE
                    constraint_toolbaar.visibility = View.VISIBLE
                    img_btn_full_screen.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_fullscreen_white))

                    val paramsdim =
                        constraint_dimview.layoutParams
                    paramsdim.height = 250.px
                    paramsdim.width = ViewGroup.LayoutParams.MATCH_PARENT
                    constraint_dimview.layoutParams = paramsdim
                    val paramsvideo =
                        videoview.layoutParams
                    paramsvideo.height = 250.px
                    paramsvideo.width = ViewGroup.LayoutParams.MATCH_PARENT
                    videoview.layoutParams = paramsvideo
                }
            }
            R.id.constraint_favourite -> {
                setButtonTextColors(
                    constraint_favourite,
                    constraint_share,
                    constraint_vimeo,
                    constraint_youtube
                )
                setImageDrawables(
                    R.drawable.ic_favorite_green,
                    R.drawable.ic_share_grey,
                    R.drawable.ic_file_download_grey,
                    R.drawable.ic_vimeo_grey,
                    R.drawable.ic_youtube_grey
                )
                if (selectedModel != null)
                    if (!favClick) {
                        addToFav()
                    } else {
                        removeFromfav()
                    }
            }

            R.id.constraint_share -> {
                if (selectedModel != null) {
                    setDefaultTextColors(
                        constraint_favourite,
                        constraint_share,
                        constraint_download,
                        constraint_vimeo,
                        constraint_youtube
                    )
                    setImageDrawables(
                        R.drawable.ic_favorite_grey,
                        R.drawable.ic_share_grey,
                        R.drawable.ic_file_download_grey,
                        R.drawable.ic_vimeo_grey,
                        R.drawable.ic_youtube_grey
                    )

                    shareSheet()
                }
            }

            R.id.constraint_download -> {
                ReuseFunctions.preventTwoClick(constraint_download)
                checkAlreadyDownload(true, false)

            }

            R.id.opsBackBtn -> {
                onBackPressed()
            }
        }
    }

    fun checkAlreadyDownload(isDownloadClick: Boolean, isFromAutoNext: Boolean) {
        if (isVideoAlreadyExist((selectedModel as LearningData?)!!.filename)) {
            if (isDownloadClick)
                ReuseFunctions.snackMessage(mainlayout, "Already downloaded")
            constraint_download.setCompoundDrawablesWithIntrinsicBounds(
                null,
                resources.getDrawable(R.drawable.ic_video_downloaded),
                null,
                null
            );

            constraint_download.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            constraint_download.text = resources.getString(R.string.downloaded)

        } else {
            if (!isFromAutoNext) {
                setButtonTextColors(
                    constraint_download,
                    constraint_favourite,
                    constraint_vimeo,
                    constraint_youtube
                )

                constraint_download.setTextColor(resources.getColor(R.color.text_video_option_grey))

                setImageDrawables(
                    R.drawable.ic_favorite_grey,
                    R.drawable.ic_share_grey,
                    R.drawable.ic_file_download_grey,
                    R.drawable.ic_vimeo_grey,
                    R.drawable.ic_youtube_grey
                )
                constraint_download.text = resources.getString(R.string.download)
                if (isDownloadClick)
                    openDownloadBottomSheet()
            }
        }
    }

    private fun removeFromfav() {
        setDefaultTextColors(
            constraint_favourite,
            constraint_share,
            constraint_favourite,//fav two times bcz no need to set the download as it will set by chking already download cndtn
            constraint_vimeo,
            constraint_youtube
        )
        setImageDrawables(
            R.drawable.ic_favorite_grey,
            R.drawable.ic_share_grey,
            R.drawable.ic_file_download_grey,
            R.drawable.ic_vimeo_grey,
            R.drawable.ic_youtube_grey
        )
        when (categoryType) {
            Constants.TYPE_DICTIONARY -> {
                removeFromFavouriteCall(
                    (selectedModel as DictionaryData?)!!.id.toString(),
                    "0",
                    "0",
                    "0",
                    "0"
                )
            }
            Constants.TYPE_TEACHER_TUTORIAL -> {
                removeFromFavouriteCall(
                    "0",
                    (selectedModel as DictionaryData?)!!.id.toString(),
                    "0",
                    "0",
                    "0"
                )
            }Constants.TYPE_LEARNING_TUTORIAL_REAL -> {
            removeFromFavouriteCall(
                "0",
                "0",
                "0",
                "0",
                (selectedModel as DictionaryData?)!!.id.toString()
            )
        }
            Constants.TYPE_LEARNING_TUTORIAL -> {
                removeFromFavouriteCall(
                    "0",
                    "0",
                    (selectedModel as LearningData?)!!.id.toString(),
                    "0",
                    "0"
                )

            }
            Constants.TYPE_STORIES -> {
                removeFromFavouriteCall(
                    "0",
                    "0",
                    "0",
                    (selectedModel as DictionaryData?)!!.id.toString(),
                    "0"
                )

            }
        }
    }

    private fun addToFav() {
        //setTextColors(tv_detail, tv_pic, tv_tag)
        setImageDrawables(
            R.drawable.ic_favorite_green,
            R.drawable.ic_share_grey,
            R.drawable.ic_file_download_grey,
            R.drawable.ic_vimeo_grey,
            R.drawable.ic_youtube_grey
        )
        when (categoryType) {
            Constants.TYPE_DICTIONARY -> {

                addInFavouriteCall(
                    (selectedModel as DictionaryData?)!!.id.toString(),
                    "0",
                    "0",
                    "0",
                    "0"
                )
            }
            Constants.TYPE_TEACHER_TUTORIAL -> {
                addInFavouriteCall(
                    "0",
                    (selectedModel as DictionaryData?)!!.id.toString(),
                    "0",
                    "0",
                    "0"
                )

            }Constants.TYPE_LEARNING_TUTORIAL_REAL -> {
            addInFavouriteCall(
                "0",
                "0",
                "0",
                "0",
                (selectedModel as DictionaryData?)!!.id.toString()
            )

        }
            Constants.TYPE_LEARNING_TUTORIAL -> {
                addInFavouriteCall(
                    "0",
                    "0",
                    (selectedModel as LearningData?)!!.id.toString(),
                    "0",
                    "0"
                )

            }
            Constants.TYPE_STORIES -> {
                addInFavouriteCall(
                    "0",
                    "0",
                    "0",
                    (selectedModel as DictionaryData?)!!.id.toString(),
                    "0"
                )

            }
        }
    }


    private fun shareSheet() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = URLDecoder.decode((selectedModel as LearningData?)!!.shareablURL)
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    private fun openDownloadBottomSheet() {
        if (selectedModel != null) {
            dialogDownloadBottom = BottomSheetDialog(this)
            var dialogView: View = layoutInflater.inflate(
                R.layout.bottom_layout_download_video,
                null
            )
            try {
                Glide.with(context)
                    .load(URLDecoder.decode((selectedModel as LearningData?)!!.poster))
                    .into(dialogView.imageView_round)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }


            dialogView.tv_main.text = (selectedModel as LearningData?)!!.title
           // if((selectedModel as DictionaryData?)!!.urdu_word.isEmpty()){
                dialogView.tv_translate.visibility = View.GONE
          //  }
            //dialogView.tv_translate.text = (selectedModel as LearningData?)!!.urdu_word
            dialogView.tv_high.text = (selectedModel as LearningData?)!!.p720p!!.url
            dialogView.tv_medium.text = (selectedModel as LearningData?)!!.p480p.url
            dialogView.tv_low.text = (selectedModel as LearningData?)!!.p240p.url
            dialogView.radiogroup.setOnCheckedChangeListener { arg0, arg1 ->
                selectedId = dialogView.radiogroup.checkedRadioButtonId
                try {
                    if (selectedId == R.id.radio_btn_high) {

                        if ((selectedModel as LearningData?)?.p720p?.url != null)
                            selected_url = (selectedModel as LearningData?)!!.p720p!!.url
                    }
                    if (selectedId == R.id.radio_btn_medium) {
                        if ((selectedModel as LearningData?)?.p480p?.url != null)
                            selected_url =
                                (selectedModel as LearningData?)!!.p480p.url
                    }
                    if (selectedId == R.id.radio_btn_low) {
                        if ((selectedModel as LearningData?)?.p240p?.url != null)
                            selected_url =
                                (selectedModel as LearningData?)!!.p240p.url
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            dialogView.btn_download.setOnClickListener {
                checkPermission()
            }
            dialogDownloadBottom!!.setContentView(dialogView)
            dialogDownloadBottom!!.show()
        }
    }

    private fun openBottomSheetQuality() {

        val dialog = BottomSheetDialog(this)
        val dialogView: View = layoutInflater.inflate(
            R.layout.bottom_layout_video_option,
            null
        )

        dialogView.tv_down_video.text = getVideoQualityOfId(selectedQualityId)
        dialogView.tv_selected_video.text = selectedSpeedString

        dialogView.bottom_sheet_quality.setOnClickListener {
            dialog.dismiss()
            openBottomSheetQualityList()
        }
        dialogView.bottom_sheet_speed.setOnClickListener {
            dialog.dismiss()
            openBottomSheetSpeedList()
        }
        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun openBottomSheetQualityList() {
        var qualityList: List<DictionaryListModel> = ArrayList<DictionaryListModel>()
        qualityList = listOf(

            DictionaryListModel(0, "Auto", "", ""),
            DictionaryListModel(1, "240p", "", ""),
            DictionaryListModel(2, "360p", "", ""),
            DictionaryListModel(3, "480p", "", ""),
            DictionaryListModel(4, "720p", "", "")
        )
        dialog_quality = BottomSheetDialog(this)
        val dialogView: View = layoutInflater.inflate(
            R.layout.bottom_layout_video_quality_list,
            null
        )

        dialogView.recycler_video_quality_option.layoutManager = LinearLayoutManager(this)
        val adapter = VideoQualityOptionAdapter(
            this,
            Constants.TYPE_VIDEO_QUALITY,
            selectedQualityId.toInt(),
            this
        )
        dialogView.recycler_video_quality_option.adapter = adapter
        adapter.setWords(qualityList)
        /*dialogView.recycler_video_quality_option.setOnClickListener {
            openBottomSheetQualityList()

        }*/
        dialog_quality.setContentView(dialogView)
        dialog_quality.show()
    }

    private fun openBottomSheetSpeedList() {
        var qualityList: List<DictionaryListModel> = ArrayList<DictionaryListModel>()
        qualityList = listOf(

            DictionaryListModel(0, "0.25x", "", ""),
            DictionaryListModel(1, "0.5x", "", ""),
            DictionaryListModel(2, "0.75x", "", ""),
            DictionaryListModel(3, "Normal", "", ""),
            DictionaryListModel(4, "1.25x", "", ""),
            DictionaryListModel(5, "1.5x", "", ""),
            DictionaryListModel(6, "1.75x", "", ""),
            DictionaryListModel(7, "2x", "", "")
        )
        dialog_quality = BottomSheetDialog(this)
        val dialogView: View = layoutInflater.inflate(
            R.layout.bottom_layout_video_quality_list,
            null
        )

        dialogView.recycler_video_quality_option.layoutManager = LinearLayoutManager(this)
        val adapter = VideoSpeedOptionAdapter(
            this,
            Constants.TYPE_VIDEO_QUALITY,
            selectedSpeedId.toInt(),
            this
        )
        dialogView.recycler_video_quality_option.adapter = adapter
        adapter.setWords(qualityList)
        /*dialogView.recycler_video_quality_option.setOnClickListener {
            openBottomSheetQualityList()
        }*/
        dialog_quality.setContentView(dialogView)
        dialog_quality.show()
    }

    private fun setButtonTextColors(
        active: Button,
        Inactive: Button,
        Inactive1: Button,
        Inactive2: Button

    ) {
        active.setTextColor(resources.getColor(R.color.colorPrimaryDark))
        Inactive.setTextColor(resources.getColor(R.color.text_video_option_grey))
        Inactive1.setTextColor(resources.getColor(R.color.text_video_option_grey))
        Inactive2.setTextColor(resources.getColor(R.color.text_video_option_grey))

    }

    private fun setDefaultTextColors(
        active: Button,
        Inactive: Button,
        Inactive1: Button,
        Inactive2: Button,
        Inactive3: Button
    ) {
        active.setTextColor(resources.getColor(R.color.text_video_option_grey))
        Inactive.setTextColor(resources.getColor(R.color.text_video_option_grey))
        Inactive1.setTextColor(resources.getColor(R.color.text_video_option_grey))
        Inactive2.setTextColor(resources.getColor(R.color.text_video_option_grey))
        Inactive3.setTextColor(resources.getColor(R.color.text_video_option_grey))
    }

    private fun setImageDrawables(
        drawable1: Int,
        drawable2: Int,
        drawable3: Int,
        drawable4: Int,
        drawable5: Int
    ) {
        constraint_favourite.setCompoundDrawablesWithIntrinsicBounds(
            null,
            resources.getDrawable(drawable1),
            null,
            null
        )
        constraint_share.setCompoundDrawablesWithIntrinsicBounds(
            null,
            resources.getDrawable(drawable2),
            null,
            null
        )
        //checkAlreadyDownload(false,false)
        /*  constraint_download.setCompoundDrawablesWithIntrinsicBounds(
              null,
              resources.getDrawable(drawable3),
              null,
              null
          )*/
        constraint_vimeo.setCompoundDrawablesWithIntrinsicBounds(
            null,
            resources.getDrawable(drawable4),
            null,
            null
        )
        constraint_youtube.setCompoundDrawablesWithIntrinsicBounds(
            null,
            resources.getDrawable(drawable5),
            null,
            null
        )

    }

    private fun setDefaultImageDrawables(
        active: Int,
        Inactive: Int,
        Inactive1: Int,
        Inactive2: Int
    ) {
        /* img_fav.setImageDrawable(resources.getDrawable(active))
         img_share.setImageDrawable(resources.getDrawable(Inactive))
         img_down.setImageDrawable(resources.getDrawable(Inactive1))*/
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setplayer(uriString: String) {
        if (error_layout.visibility == View.VISIBLE) {
            error_layout.visibility = View.GONE
        }
        if (constraint_bottom_options.visibility == View.VISIBLE) {
            constraint_bottom_options.visibility = View.GONE
        }
        progress.visibility = View.VISIBLE
        //seekBar.thumb.mutate().alpha = 0
        //val uriString = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"

        videoview.setVideoURI(Uri.parse(uriString))
        videoview.setOnErrorListener(mOnErrorListener);
        videoview.requestFocus()

        //videoview.start()
        img_btn_play_pause.setImageDrawable(this.resources.getDrawable(R.drawable.ic_baseline_pause_white))

        videoview.setOnPreparedListener(MediaPlayer.OnPreparedListener {
            //image_btn_menu.visibility = View.VISIBLE
            mediaPlayer = it
            setVideoProgress()
            progress.visibility = View.GONE
            if (constraint_dimview.visibility == View.VISIBLE) {
                constraint_dimview.visibility = View.GONE
            }
            if (constraint_bottom_options.visibility == View.GONE) {
                constraint_bottom_options.visibility = View.VISIBLE
            }

        })

        videoview.setOnCompletionListener {
            if (autoPlayStatus) {
                image_btn_menu.visibility = View.GONE
                setNextPreVideo(1)
            } else {
                isReload = true
                setErrorAndReloadViews()
                reload_layout.visibility = View.VISIBLE
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    val paramsreload =
                        reload_layout.layoutParams
                    paramsreload.height = ViewGroup.LayoutParams.MATCH_PARENT
                    reload_layout.layoutParams = paramsreload
                } else {
                    val paramsreload =
                        reload_layout.layoutParams
                    paramsreload.height = 250.px
                    reload_layout.layoutParams = paramsreload
                }
            }
        }

        videoview.setOnTouchListener(View.OnTouchListener { _, motionEvent ->
            if (videoview.isPlaying ) {
                image_btn_menu.visibility = View.VISIBLE
                if (!isReload) {
                if (playToggle) {
                    constraint_dimview.visibility = View.VISIBLE
                    constraint_centerscreen.visibility = View.VISIBLE
                    image_btn_menu.visibility = View.VISIBLE
                    img_btn_play_pause_center.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_white_large))
                    img_btn_play_pause.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_white))
                    constraint_centerscreen.postDelayed(Runnable {
                        constraint_centerscreen.visibility = View.GONE
                        image_btn_menu.visibility = View.GONE
                        constraint_dimview.visibility = View.GONE
                        playToggle = true

                    }, 2000)
                    playToggle = false
                } else {
                    constraint_dimview.visibility = View.GONE
                    constraint_centerscreen.visibility = View.GONE
                    image_btn_menu.visibility = View.GONE
                    playToggle = true

                }
                false
 }
                false

            } else {
                if (!isReload) {
                    if (pauseToggle) {
                        constraint_dimview.visibility = View.VISIBLE
                        constraint_centerscreen.visibility = View.VISIBLE
                        image_btn_menu.visibility = View.VISIBLE
                        img_btn_play_pause_center.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_play_white_large))
                        img_btn_play_pause.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_play_white))
                        constraint_centerscreen.postDelayed(Runnable {
                            constraint_centerscreen.visibility = View.GONE
                            image_btn_menu.visibility = View.GONE
                            constraint_dimview.visibility = View.GONE
                            pauseToggle = true

                        }, 3000)
                        pauseToggle = false
                    } else {
                        constraint_dimview.visibility = View.GONE
                        constraint_centerscreen.visibility = View.GONE
                        image_btn_menu.visibility = View.GONE
                        pauseToggle = true
                    }
                }
                false
            }
        })
    }



    @SuppressLint("ClickableViewAccessibility")
    private fun setVideoProgress() {
        currentPos = videoview.currentPosition.toDouble()
        totalDuration = videoview.duration.toDouble()
        tv_played_time.text = timeConversion(currentPos.toLong())
        tv_comp_time.text = timeConversion(totalDuration.toLong())

        seekBar.max =   Math.ceil(totalDuration).toInt()
        //seekBar.max =   videoview.duration

        val handler = Handler()
        val runner: Runnable = object : Runnable {
            override fun run() {
                if (videoview.currentPosition == videoview.duration){
                    handler.removeCallbacksAndMessages(null)

                }


                if (isSeekBarClick) {
                    try {
                        videoview.seekTo(seekbaarProgress)
                        seekBar.progress = seekbaarProgress
                        SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)
                            ?.setPos((seekbaarProgress.toDouble()).toLong().toString())
                        tv_played_time.text = timeConversion(seekbaarProgress.toLong())
                        isSeekBarClick = false
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        currentPos = videoview.currentPosition.toDouble()
                        SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)
                            ?.setPos(currentPos.toLong().toString())
                        seekBar.progress = Math.ceil(currentPos).toInt()
                        // seekBar.progress = videoview.currentPosition
                        tv_played_time.text = timeConversion(currentPos.toLong())
                        // Log.d("asdfghhj", "" + seekBar!!.progress + "|" + seekbaarProgress + "|" + videoview.currentPosition)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }

                handler.postDelayed(this, 1000)

            }
        }
        handler.postDelayed(runner, 1000)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                currentPos = seekBar!!.progress.toDouble()
                videoview.seekTo(seekBar.progress)
            }
        })

        seekBar.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                isSeekBarClick = true
                seekbaarProgress = seekBar.progress
                Log.e("progressed", "" + seekbaarProgress)

                try {
                    //tv_played_time.text = timeConversion(seekBar.progress.toLong())
                    // videoview.seekTo(seekbaarProgress)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@OnTouchListener false
            }
            true
        })
    }

    override fun onPause() {
        if (downloadDialog != null) {
            downloadDialog?.cancel()
        }
        if (videoview != null) {
            videoview?.pause()
        }
        super.onPause()
    }

    private fun setPlayPauseVideo() {
        if (videoview.isPlaying) {
            img_btn_play_pause.setImageDrawable(this.resources.getDrawable(R.drawable.ic_baseline_play_white))
            img_btn_play_pause_center.setImageDrawable(this.resources.getDrawable(R.drawable.ic_baseline_play_white_large))
            videoview.pause()
        } else {
            img_btn_play_pause.setImageDrawable(this.resources.getDrawable(R.drawable.ic_baseline_pause_white))
            img_btn_play_pause_center.setImageDrawable(this.resources.getDrawable(R.drawable.ic_baseline_pause_white_large))
            videoview.start()
        }
    }

    //time conversion
    fun timeConversion(value: Long): String? {
        val videoTime: String
        val dur = value.toInt()
        val hrs = dur / 3600000
        val mns = dur / 60000 % 60000
        val scs = dur % 60000 / 1000
        videoTime = if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mns, scs)
        } else {
            String.format("%02d:%02d", mns, scs)
        }
        return videoTime
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            nestedScrollView.visibility = View.GONE
            constraint_toolbaar.visibility = View.GONE
        } else {
            nestedScrollView.visibility = View.VISIBLE
            constraint_toolbaar.visibility = View.VISIBLE
        }
    }

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()


    override fun onResume() {
        super.onResume()

        if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p720p?.url != null) {
            val videoUrl: String = URLDecoder.decode((selectedModel as LearningData?)!!.p720p!!.url)
            setplayer(videoUrl)
            videoview.start()
        }
        videoview.visibility = View.VISIBLE
        constraint_centerscreen.visibility = View.GONE
        reload_layout.visibility = View.GONE
        if (videoview != null) {
            val roundedBalance =
                SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)?.getPos()
            val temp = roundedBalance?.substringBefore(".")
            if (temp != null && temp != "") {
                videoview.seekTo(
                    temp.toInt()
                )
                videoview.start()
            }
        }
    }

    private val mOnErrorListener: MediaPlayer.OnErrorListener =
        MediaPlayer.OnErrorListener { mp, what, extra ->

            error_layout.visibility = View.VISIBLE
            setErrorAndReloadViews()

            videoview.pause()
            when (what) {
                MediaPlayer.MEDIA_ERROR_UNKNOWN ->
                    handleExtras(extra)
                MediaPlayer.MEDIA_ERROR_SERVER_DIED ->
                    handleExtras(extra)
            }
            true
        }

    private fun handleExtras(extra: Int) {
        when (extra) {
            MediaPlayer.MEDIA_ERROR_IO -> {
            }
            MediaPlayer.MEDIA_ERROR_MALFORMED -> {
            }
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> {
            }
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> {
            }
        }
    }

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            nestedScrollView.visibility = View.VISIBLE
            constraint_toolbaar.visibility = View.VISIBLE
            img_btn_full_screen.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_fullscreen_white))

            val paramsdim =
                constraint_dimview.layoutParams
            paramsdim.height = 250.px
            paramsdim.width = ViewGroup.LayoutParams.MATCH_PARENT
            constraint_dimview.layoutParams = paramsdim
            val paramsvideo =
                videoview.layoutParams
            paramsvideo.height = 250.px
            paramsvideo.width = ViewGroup.LayoutParams.MATCH_PARENT
            videoview.layoutParams = paramsvideo
            if (reload_layout.visibility == View.VISIBLE) {
                val paramsreload =
                    reload_layout.layoutParams
                paramsreload.height = 250.px
                reload_layout.layoutParams = paramsreload
            }
        } else if (downloadDialog != null) {
            downloadDialog!!.cancel()
            super.onBackPressed()
        } else if (videoview != null) {
            videoview.pause()
            super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {


        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun newDownload(url: String, filename: String, poster: String) {

        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var downloadVideoBroadcastReceiver = DownloadVideoBroadcastReceiver()
        var intent = Intent(this, downloadVideoBroadcastReceiver::class.java)
        intent.putExtra(Constants.KEY_URL, url)
        intent.putExtra(Constants.DOWNLOAD_TYPE, Constants.SINGLE_VIDEO_DOWNLOAD)
        intent.putExtra(Constants.FILE_NAME, filename)
        intent.putExtra(Constants.THUMBNAIL, poster)

        sendBroadcast(intent)
        /* var pendingIntent =
             PendingIntent.getBroadcast(applicationContext, PENDING_INTENT_REQ_CODE, intent, 0)
         alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)*/
        if (dialogDownloadBottom != null) {
            dialogDownloadBottom!!.dismiss()
        }


    }

    fun checkFolder() {
        val path = Environment.getExternalStorageDirectory()
            .absolutePath + Constants.FOLDER_NAME
        val dir = File(path)
        var isDirectoryCreated = dir.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir()
        }
        if (isDirectoryCreated) {
            // do something\
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), Constants.MY_PERMISSIONS_REQUEST_WRITE_STORAGE
            )
        } else {
            checkFolder()
            if (selectedId == -1) {
                ReuseFunctions.showToast(this, "Select one category")
            } else {
                if ((selectedModel as LearningData?) != null)
                    try {
                        newDownload(
                            URLDecoder.decode(selected_url),
                            (selectedModel as LearningData?)!!.filename,
                            (selectedModel as LearningData?)!!.poster
                        )
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }

            }
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
                    checkFolder()
                    if (selectedId == -1) {
                        ReuseFunctions.showToast(this, "Select one category")
                    } else {
                        try {
                            newDownload(
                                URLDecoder.decode(selected_url),
                                (selectedModel as LearningData?)!!.filename,
                                (selectedModel as LearningData?)!!.poster
                            )
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

        dialog_quality.dismiss()
        selectedQualityId = qualityId.toInt()
        when (qualityId) {
            "0" -> {
                if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p240p?.url != null) {
                    try {
                        setVideoQuality(URLDecoder.decode((selectedModel as LearningData?)!!.p240p.url))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                }
            }
            "1" -> {
                if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p360p?.url != null) {
                    try {
                        setVideoQuality(URLDecoder.decode((selectedModel as LearningData?)!!.p360p.url))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                }
            }
            "2" -> {
                if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p480p?.url != null) {
                    try {
                        setVideoQuality(URLDecoder.decode((selectedModel as LearningData?)!!.p480p.url))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                }
            }
            "3" -> {
                if ((selectedModel as LearningData?) != null && (selectedModel as LearningData?)?.p720p?.url != null) {
                    try {
                        setVideoQuality(URLDecoder.decode((selectedModel as LearningData?)!!.p720p!!.url))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onSpeedChangeSelected(speedId: String) {
        dialog_quality.dismiss()
        selectedSpeedId = speedId.toInt()
        when (speedId) {
            "0" -> {
                if (mediaPlayer != null) {
                    setSpeed(0.25f,"0.25x")

                }
            }
            "1" -> {
                if (mediaPlayer != null) {
                    setSpeed(0.5f,"0.5x")

                }
            }
            "2" -> {
                if (mediaPlayer != null) {
                    setSpeed(0.75f,"0.75x")

                }
            }
            "3" -> {
                if (mediaPlayer != null) {
                    setSpeed(1.0f,"1.0x")
                }
            }
            "4" -> {
                if (mediaPlayer != null) {
                    setSpeed(1.25f,"1.25x")
                }
            }
            "5" -> {
                if (mediaPlayer != null) {
                    setSpeed(1.5f,"1.5x")
                }
            }
            "6" -> {
                if (mediaPlayer != null) {
                    setSpeed(1.75f,"1.75x")
                }
            }
            "7" -> {
                if (mediaPlayer != null) {
                    setSpeed(2.0f,"2.0x")
                }
            }

        }
    }

    private fun setSpeed(speed: Float,speedText: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //works only from api 23
                val myPlayBackParams = PlaybackParams()
                myPlayBackParams.speed = speed
                mediaPlayer!!.playbackParams = myPlayBackParams
                val roundedBalance =
                    SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)?.getPos()
                val temp = roundedBalance?.substringBefore(".")
                if (temp != null) {
                    videoview.seekTo(temp.toInt())
                    videoview.start()
                    selectedSpeedString = speedText
                }
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }
    private fun setVideoQuality(url: String) {
        val roundedBalance =
            SharedPreferenceClass.getInstance(this@VideoPreviewLearningTutorialActivity)?.getPos()
        val temp = roundedBalance?.substringBefore(".")
        if (temp != null) {
            setplayer(url)
            videoview.seekTo(temp.toInt())
            videoview.start()
        }
    }


    private fun addInFavouriteCall(
        dicId: String,
        tutId: String,
        lessonId: String,
        storyId: String,
        learning_tut_Id: String
    ) {
        if (isNetworkAvailable) {
            val mainListCall: Call<AddToFvouriteModel> =
                apiService.addToFvourite(
                    SharedPreferenceClass.getInstance(this)!!.getSession(),
                    SharedPreferenceClass.getInstance(context)?.getUserType().toString(),
                    dicId,
                    tutId,
                    lessonId,
                    storyId,
                    learning_tut_Id
                )

            mainListCall.enqueue(object : Callback<AddToFvouriteModel?> {
                override fun onResponse(
                    call: Call<AddToFvouriteModel?>,
                    response: Response<AddToFvouriteModel?>
                ) {

                    val addToFvouriteModel: AddToFvouriteModel = response.body()!!
                    if (addToFvouriteModel != null && addToFvouriteModel.code == 200) {
                        if (addToFvouriteModel.data != null) {
                            if (!this@VideoPreviewLearningTutorialActivity.isDestroyed) {
                                ReuseFunctions.snackMessage(
                                    mainlayout,
                                    addToFvouriteModel.response_msg
                                )
                                constraint_favourite.setCompoundDrawablesWithIntrinsicBounds(
                                    null,
                                    applicationContext.resources.getDrawable(R.drawable.ic_favorite_green),
                                    null,
                                    null
                                )
                                constraint_favourite.setTextColor(
                                    applicationContext.resources.getColor(
                                        R.color.colorPrimaryDark
                                    )
                                )

                                favClick = true
                            }
                        }
                    }
                    addToFvouriteModel.code
                }

                override fun onFailure(call: Call<AddToFvouriteModel?>, t: Throwable) {
                    Toast.makeText(applicationContext, "" + t?.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                    call.cancel()
                }
            })
        } else {
            ReuseFunctions.snackMessage(mainlayout, this.getString(R.string.no_internet_text))
        }
    }

    private fun removeFromFavouriteCall(
        dicId: String,
        tutId: String,
        lessonId: String,
        storyId: String,
        learning_tut_Id: String

    ) {
        if (isNetworkAvailable) {
            val mainListCall: Call<AddToFvouriteModel> =
                apiService.RemoveFromFvourite(
                    SharedPreferenceClass.getInstance(this)!!.getSession(),
                    SharedPreferenceClass.getInstance(context)?.getUserType().toString(),
                    dicId,
                    tutId,
                    lessonId,
                    storyId,
                    learning_tut_Id
                )

            mainListCall.enqueue(object : Callback<AddToFvouriteModel?> {
                override fun onResponse(
                    call: Call<AddToFvouriteModel?>,
                    response: Response<AddToFvouriteModel?>
                ) {

                    val addToFvouriteModel: AddToFvouriteModel = response.body()!!
                    if (addToFvouriteModel != null && addToFvouriteModel.code == 200) {
                        if (addToFvouriteModel.data != null) {
                            if (!this@VideoPreviewLearningTutorialActivity.isDestroyed) {
                                ReuseFunctions.snackMessage(mainlayout, "Remove From Favourites")
                                constraint_favourite.setCompoundDrawablesWithIntrinsicBounds(
                                    null,
                                    applicationContext.resources.getDrawable(R.drawable.ic_favorite_grey),
                                    null,
                                    null
                                )
                                constraint_favourite.setTextColor(
                                    applicationContext.resources.getColor(
                                        R.color.text_video_option_grey
                                    )
                                )

                                favClick = false
                            }
                        }
                    }
                    addToFvouriteModel.code
                }

                override fun onFailure(call: Call<AddToFvouriteModel?>, t: Throwable) {
                    Toast.makeText(applicationContext, "" + t?.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                    call.cancel()
                }
            })
        } else {
            ReuseFunctions.snackMessage(mainlayout, this.getString(R.string.no_internet_text))

        }
    }

    override fun onVideoSelect(selectedModel: Data) {

    }
    override fun onVideoSelect(selectedModel: DownloadListModel) {

    }
    override fun onVideoSelect(selectedModelVideo: LearningData) {
        selectedModel = selectedModelVideo
        if (selectedModel != null && (selectedModel as LearningData?)?.p720p?.url != null) {
            try {
                var LearningDataTemp: LearningData? =  null
                LearningDataTemp= selectedModel as LearningData
                var newIndex = list!!.indexOf(LearningDataTemp)
                val videoUrl: String =
                    URLDecoder.decode((selectedModel as LearningData?)!!.p720p!!.url)
                setNormalVideoViews()
                setplayer(videoUrl)
                videoview.start()
                setTitleText((selectedModel as LearningData))
                if (list != null) {
                    val finalList = ListSorting.sortListLearningTutorial(
                        newIndex,
                        list!!
                    )
                    setNextVideosList(finalList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onVideoSelect(selectedModel: StoryData) {

    }

    override fun onVideoSelect(selectedModelVideo: DictionaryData) {

    }

    override fun onVideoSelect(selectedModel: DictionaryDataAPI) {

    }

    override fun onVideoSelect(selectedModel: TutorialData) {

    }

    private fun setNextPreVideo(value: Int) {
        if (selectedModel != null && (selectedModel as LearningData?)?.indexPosition != null) {
            try {
                var nextVideo = (selectedModel as LearningData?)?.indexPosition

                nextVideo = nextVideo!!.plus(value)
                if (nextVideo == list?.size){
                    nextVideo = 0;
                }
                if (list?.size != null) {
                    var LearningDataTemp: LearningData? =  null

                    try {
                        val data = list!!.filter { it.indexPosition == nextVideo }
                        selectedModel = data[0]
                        LearningDataTemp= selectedModel as LearningData
                        var newIndex = list!!.indexOf(LearningDataTemp)
                        val videoUrl: String =
                            URLDecoder.decode((selectedModel as LearningData?)!!.p720p!!.url)
                        setplayer(videoUrl)
                        videoview.start()
                        setTitleText((selectedModel as LearningData?)!!)
                        if (list != null) {
                            val finalList =
                                ListSorting.sortListLearningTutorial(
                                    newIndex,
                                    list!!
                                )
                            setNextVideosList(finalList)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
    }
    private fun setErrorAndReloadViews() {
        constraint_centerscreen.visibility = View.GONE
        image_btn_menu.visibility = View.GONE
        constraint_dimview.visibility = View.GONE
        progress.visibility = View.GONE
        constraint_bottom_options.visibility = View.GONE
    }

    private fun setNormalVideoViews() {
        videoview.visibility = View.VISIBLE
        image_btn_menu.visibility = View.GONE
        constraint_dimview.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE
        constraint_centerscreen.visibility = View.GONE
        error_layout.visibility = View.GONE
        reload_layout.visibility = View.GONE
    }

    private fun getVideoQualityOfId(qualityId: Int): String {
        var quality: String = ""
        when (qualityId) {
            Constants.AUTO -> {
                quality = "AUTO"
                isAutoQuality = true
            }
            Constants.p240p -> {
                quality = "240p"
                isAutoQuality = false
            }
            Constants.p360p -> {
                quality = "360p"
                isAutoQuality = false
            }
            Constants.p480p -> {
                quality = "480p"
                isAutoQuality = false
            }
            Constants.p720p -> {
                quality = "720p"
                isAutoQuality = false
            }
        }
        return quality
    }

    override fun onDestroy() {
        if (downloadDialog != null) {
            downloadDialog?.cancel()
        }
        unregisterReceiver(onDownloadComplete);
        super.onDestroy()
    }




    /**
     * Fetches how many bytes have been downloaded so far and updates ProgressBar
     */
    public class DownloadProgressCounter(
        private val manager: DownloadManager,
        private val downloadId: Long

    ) : Thread() {
        private val query: DownloadManager.Query
        private var cursor: Cursor? = null
        private var totalBytes: Long = 0
        var context: Context = VideoPreviewLearningTutorialActivity()
        override fun run() {
            while (downloadId > 0) {
                try {

                    sleep(300)

                    cursor = manager.query(query)
                    if (cursor!!.moveToFirst()) {
                        //get statuses
                        val status =
                            cursor!!.getInt(cursor!!.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        val stringV =
                            cursor!!.getString(cursor!!.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION))
                        Log.d("asdfghjl", "" + stringV)
                        //get total bytes of the file
                        if (totalBytes <= 0) {
                            totalBytes =
                                cursor!!.getInt(cursor!!.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                    .toLong()
                        }
                        val bytesDownloadedSoFar: Long =
                            cursor!!.getInt(cursor!!.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                .toLong()
                        if (bytesDownloadedSoFar == totalBytes && totalBytes > 0) {
                            ProgressHelper.getInstance(MainClass.appContext)
                                ?.setCompletion(true)
                            interrupt()

                        } else {

                            Handler(Looper.getMainLooper()).post {
                                ProgressHelper.getInstance(MainClass.appContext)
                                    ?.sendValues((bytesDownloadedSoFar * 100 / totalBytes).toInt())

                            }
                        }
                    }
                    cursor!!.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()


                    return
                }
            }
        }

        init {
            query = DownloadManager.Query()
            query.setFilterById(downloadId)

        }
    }

    fun isVideoAlreadyExist(filename: String): Boolean {

        var file: File
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file = File(
                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                        Constants.FOLDER_NAME, filename + ""
            )
        } else {
            file = File(
                Environment.getExternalStorageDirectory().absolutePath +
                        Constants.FOLDER_NAME, filename + ""
            )
        }
        file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.FOLDER_NAME + filename + ""
        )
        return file.exists()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {

    }
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (Constants.idLong == id) {
                if (!this@VideoPreviewLearningTutorialActivity.isDestroyed) {
                    try{
                        constraint_download.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            applicationContext. resources.getDrawable(R.drawable.ic_video_downloaded),
                            null,
                            null
                        )

                        constraint_download.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                        constraint_download.text = resources.getString(R.string.downloaded)
                        if ((selectedModel as LearningData?)!!.favorite != null && (selectedModel as LearningData?)!!.favorite == 0) {
                            constraint_favourite.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                applicationContext.resources.getDrawable(R.drawable.ic_favorite_grey),
                                null,
                                null)
                            constraint_favourite.setTextColor(applicationContext.resources.getColor(R.color.text_video_option_grey))
                        }else {
                            constraint_favourite.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                applicationContext.resources.getDrawable(R.drawable.ic_favorite_green),
                                null,
                                null
                            )
                            constraint_favourite.setTextColor(applicationContext.resources.getColor(R.color.colorPrimaryDark))
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}