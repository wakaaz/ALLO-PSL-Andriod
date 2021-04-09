package com.net.pslapllication.activities.dictionary

 import android.annotation.SuppressLint
 import android.app.Dialog
import android.content.Context
import android.content.Intent

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.*
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
import com.net.pslapllication.adpters.VideoPreviewAdapter
 import com.net.pslapllication.adpters.VideoPreviewDownloadAdapter
 import com.net.pslapllication.adpters.VideoQualityOptionAdapter
import com.net.pslapllication.adpters.VideoSpeedOptionAdapter
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
import kotlinx.android.synthetic.main.bottom_layout_download_video.view.*
import kotlinx.android.synthetic.main.bottom_layout_download_video.view.tv_down_video
import kotlinx.android.synthetic.main.bottom_layout_video_option.view.*
import kotlinx.android.synthetic.main.bottom_layout_video_option.view.bottom_sheet_quality
import kotlinx.android.synthetic.main.bottom_layout_video_quality_list.view.*
import kotlinx.android.synthetic.main.layout_videoview_error.*
import kotlinx.android.synthetic.main.playerbarlayout.*
import kotlinx.android.synthetic.main.toolbaar_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class VideoPreviewDownloadActivity : BaseActivity(), View.OnClickListener,
    onQualityChangSelectedListener,OnVideoSelectedListener{

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
    private var selectedSpeedId: Int = 3
    private var seekbaarProgress: Int = 0

    //string
    private var networkType: String = ""
    private var selected_url = ""
    private var categoryType = ""
    private var selectedSpeedString = "Normal"

    //data model
    var selectedModel: Any? = null

    //list
    public var list: List<DownloadListModel>? = null

    //others
    private lateinit var dialog_quality: BottomSheetDialog
    private var dialogDownloadBottom: BottomSheetDialog? = null
    private var downloadDialog: Dialog? = null
    private var bnp: ProgressBar? = null
    private var tv_progress_txt: TextView? = null
    private lateinit var apiService: ApiService
    private var adapter: VideoPreviewDownloadAdapter? = null
    var mediaPlayer: MediaPlayer? = null
    var context: Context = this@VideoPreviewDownloadActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
        tabs.visibility = View.GONE
        view1.visibility = View.GONE
        SharedPreferenceClass.getInstance(this@VideoPreviewDownloadActivity)
            ?.setPos("0.0")
        getIntentUrl()
         nestedScrollView.isEnableScrolling = false
        setAdapter()
        getIntentData()
        setListener()

        checkAutoPlaySwitch()


    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        isNetworkAvailable = isConnected
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun setAdapter() {
        val mLayoutManager =
            LinearLayoutManagerClass(this, LinearLayout.VERTICAL, false)
        recycler_next.layoutManager = mLayoutManager
        adapter = VideoPreviewDownloadAdapter(this, Constants.TYPE_VIDEO, "", this)
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
            when (categoryType) {
                Constants.TYPE_FAVOURITE -> {
                }
                Constants.TYPE_DICTIONARY -> {
                }
            }
            /********get selected video list data*********/
            selectedModel =
                intent.getSerializableExtra(Constants.SELECTED_DICTIONARY_LIST_MODEL) as DownloadListModel
            if (selectedModel != null) {
                setTitleText((selectedModel as DownloadListModel))
            }

            /********get complete list data*********/

            Handler().postDelayed({
                list = ProgressHelper.getInstance(context).getDownloadList()
                if (list?.size != 0) {
                    val recyclerList = list!!.drop(1)

                    setNextVideosList(recyclerList!!)
                }
            }, 500)

            /* Handler().postDelayed({
                var DownloadListModel: DownloadListModel? = null
                DownloadListModel?.id = selectedId
                if (list != null && list!!.size != 0 && selectedModel != null && (selectedModel!! as DownloadListModel).id != null) {
                    val finalList = ListSorting.sortList(
                        (selectedModel!! as DownloadListModel).indexPosition,
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




    private fun setListener() {
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
        opsBackBtn.setOnClickListener(this)
        viewShadow.visibility = View.GONE
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



    private fun checkAutoPlaySwitch() {
        switch_next.isChecked = SharedPreferenceClass.getInstance(this)?.getAutoPLayToggle()!!
        autoPlayStatus = SharedPreferenceClass.getInstance(this)?.getAutoPLayToggle()!!
        switch_next.setOnCheckedChangeListener { _, isChecked ->
            autoPlayStatus = isChecked
            SharedPreferenceClass.getInstance(this)?.setAutoPLayToggle(isChecked)
        }
    }


    private fun setTitleText(selectedModel: DownloadListModel) {
        txt_title.text = ReuseFunctions.firstLetterCap(selectedModel.wordName)
        tv_main.text = ReuseFunctions.firstLetterCap(selectedModel.wordName)

    }

    private fun setNextVideosList(list: List<DownloadListModel>) {

        if (adapter != null) {
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
                if ((selectedModel as DownloadListModel?) != null && (selectedModel as DownloadListModel?)!!.wordTyhumb != null) {
                    try {
                        isReload = false
                        var videoUrl: String =
                            URLDecoder.decode((selectedModel as DownloadListModel?)!!.wordTyhumb)
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
                 constraint_dimview.visibility = View.VISIBLE
                progress.visibility = View.VISIBLE
                if (selectedModel != null) {
                    val videoUrl: String =
                        URLDecoder.decode((selectedModel as DownloadListModel?)!!.wordTyhumb)
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

            R.id.opsBackBtn -> {
                onBackPressed()
            }
        }
    }



    private fun shareSheet() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = URLDecoder.decode((selectedModel as DownloadListModel?)!!.wordTyhumb)
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }



    private fun openBottomSheetQuality() {

        val dialog = BottomSheetDialog(this)
        val dialogView: View = layoutInflater.inflate(
            R.layout.bottom_layout_video_option,
            null
        )

 dialogView.bottom_sheet_quality.visibility = View.GONE
        dialogView.tv_selected_video.text = selectedSpeedString
        dialogView.bottom_sheet_speed.setOnClickListener {
            dialog.dismiss()
            openBottomSheetSpeedList()
        }
        dialog.setContentView(dialogView)
        dialog.show()
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
        /* val uriString =
             "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"

       */
        videoview.setVideoURI(Uri.parse(uriString))
        videoview.setOnErrorListener(mOnErrorListener);
        videoview.requestFocus()

        //videoview.start()
        img_btn_play_pause.setImageDrawable(this.resources.getDrawable(R.drawable.ic_baseline_pause_white))

        videoview.setOnPreparedListener(MediaPlayer.OnPreparedListener {
            image_btn_menu.visibility = View.VISIBLE

            mediaPlayer = it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //works only from api 23
                val myPlayBackParams = PlaybackParams()
                // myPlayBackParams.speed = 0.5f //here set speed eg. 0.5 for slow 2 for fast mode
                myPlayBackParams.speed = 1.0f
                it.playbackParams = myPlayBackParams

                videoview.start() //start your video
            }

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
                        image_btn_menu.visibility = View.GONE
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

        seekBar.max = Math.ceil(totalDuration).toInt()
        //seekBar.max =   videoview.duration

        val handler = Handler()
        val runner: Runnable = object : Runnable {
            override fun run() {
                if (videoview.currentPosition == videoview.duration) {
                    handler.removeCallbacksAndMessages(null)

                }


                if (isSeekBarClick) {
                    try {
                        videoview.seekTo(seekbaarProgress)
                        seekBar.progress = seekbaarProgress
                        SharedPreferenceClass.getInstance(this@VideoPreviewDownloadActivity)
                            ?.setPos((seekbaarProgress.toDouble()).toLong().toString())
                        tv_played_time.text = timeConversion(seekbaarProgress.toLong())
                        isSeekBarClick = false
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        currentPos = videoview.currentPosition.toDouble()
                        SharedPreferenceClass.getInstance(this@VideoPreviewDownloadActivity)
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
                //seekBar.progress = seekbaarProgress
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

        if ((selectedModel as DownloadListModel?) != null && (selectedModel as DownloadListModel?)?.wordTyhumb != null) {
            val videoUrl: String = URLDecoder.decode((selectedModel as DownloadListModel?)!!.wordTyhumb)
            setplayer(videoUrl)
            videoview.start()
        }
        videoview.visibility = View.VISIBLE
        constraint_centerscreen.visibility = View.GONE
        reload_layout.visibility = View.GONE
        if (videoview != null) {
            val roundedBalance =
                SharedPreferenceClass.getInstance(this@VideoPreviewDownloadActivity)?.getPos()
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

    override fun onQualityChangeSelected(qualityId: String) {

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
                    setSpeed(1.75f,"1.7x")
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
                    SharedPreferenceClass.getInstance(this@VideoPreviewDownloadActivity)?.getPos()
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



    private fun setNextPreVideo(value: Int) {
        if (selectedModel != null && (selectedModel as DownloadListModel?)?.indexPosition != null) {
            try {
                var nextVideo = (selectedModel as DownloadListModel?)?.indexPosition

                nextVideo = nextVideo!!.plus(value)
                if (list?.size != null) {
                    var DownloadListModelTemp: DownloadListModel? = null

                    try {
                        val data = list!!.filter { it.indexPosition == nextVideo }
                        selectedModel = data[0]
                        DownloadListModelTemp = selectedModel as DownloadListModel
                        var newIndex = list!!.indexOf(DownloadListModelTemp)
                        val videoUrl: String =
                            URLDecoder.decode((selectedModel as DownloadListModel?)!!.wordTyhumb)
                        setplayer(videoUrl)
                        videoview.start()
                        setTitleText((selectedModel as DownloadListModel?)!!)
                        if (list != null) {
                            val finalList =
                                ListSorting.sortListDownload(
                                    list!!,
                                    newIndex
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

    override fun onDestroy() {
        if (downloadDialog != null) {
            downloadDialog?.cancel()
        }

        super.onDestroy()
    }



    override fun onVideoSelect(selectedModel: DictionaryData) {
        TODO("Not yet implemented")
    }

    override fun onVideoSelect(selectedModel: DictionaryDataAPI) {
        TODO("Not yet implemented")
    }

    override fun onVideoSelect(selectedModel: TutorialData) {

    }

    override fun onVideoSelect(selectedModel: StoryData) {

    }

    override fun onVideoSelect(selectedModel: LearningData) {

    }

    override fun onVideoSelect(selectedModel: Data) {

    }
    override fun onVideoSelect(selectedModel: DownloadListModel) {
        this.selectedModel = selectedModel
        if (selectedModel != null && (selectedModel as DownloadListModel?)?.wordTyhumb != null) {
            try {
                var DownloadListModelTemp: DownloadListModel? = null
                DownloadListModelTemp = selectedModel as DownloadListModel
                var newIndex = list!!.indexOf(DownloadListModelTemp)
                val videoUrl: String =
                    URLDecoder.decode((selectedModel as DownloadListModel?)!!.wordTyhumb)
                setNormalVideoViews()
                setplayer(videoUrl)
                videoview.start()
                setTitleText((selectedModel as DownloadListModel))
                if (list != null) {
                    val finalList = ListSorting.sortListDownload(
                        list!!,
                        newIndex
                    )

                    //val recyclerList = finalList!!.drop(1)

                    setNextVideosList(finalList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun setNormalVideoViews() {
        videoview.visibility = View.VISIBLE
        image_btn_menu.visibility = View.VISIBLE
        constraint_dimview.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE
        constraint_centerscreen.visibility = View.GONE
        error_layout.visibility = View.GONE
        reload_layout.visibility = View.GONE
    }
}


