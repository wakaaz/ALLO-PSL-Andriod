package com.net.pslapllication.activities

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.net.pslapllication.R
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.adpters.RecentAdapter
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.model.favouriteList.FavouriteMain
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_download.view.*
import kotlinx.android.synthetic.main.toolbaar_layout_profile.*
import retrofit2.Call
import java.io.File
import java.lang.Exception
import java.util.*

class ProfileActivity : BaseActivity(), View.OnClickListener, RetrofitResponseListener {
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setProfileData()
        getDownloadedVideos()
        const_header.background = resources.getDrawable(R.drawable.greenbackground)
         setListeners()
        setRecyclerRecent(recycler_recent, Constants.TYPE_RECENT)
        Glide.with(this).load(resources.getDrawable(R.drawable.ic_avatar)).into(img_profile);
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            apiCall()
        }
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun setProfileData() {
        tv_name.text =
            SharedPreferenceClass.getInstance(this)?.getFirstName()?.toUpperCase(Locale.ROOT)
        tv_mail.text = SharedPreferenceClass.getInstance(this)?.getemail()
    }

    private fun setListeners() {
        const_fav.setOnClickListener(this)
        const_down.setOnClickListener(this)
        const_view_all_recent.setOnClickListener(this)
        opsBackBtn.setOnClickListener(this)
    }

   private fun getDownloadedVideos() {
        var catVideos : Int =0
        var VideosCount : Int = 0
         /**
         * count of single downloaded videos
         */
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.FOLDER_NAME
        )
        if (file.exists()) {
            try {
                val file = file.listFiles()
                var filename: String = ""
                if (file?.size != 0) {
                    VideosCount = file!!.size

                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * count of downloaded videos of categories
         */

        val file1 = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.CAT_FOLDER_NAME
        )
        if (file1.exists()) {
            try {
                val folders = file1.listFiles()
                if (folders.isNotEmpty()) {
                    for (i in folders!!.indices) {
                        val fileList = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            Constants.CAT_FOLDER_NAME + folders[i].name
                        )
                        if (fileList.exists()) {
                            catVideos += fileList.listFiles().size
                        }
                    }
                }
                var totalVideos = catVideos + VideosCount
                tv_download_video_text.text = totalVideos.toString()+ " Videos"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    private fun setRecyclerRecent(recyclerView: RecyclerView, type: String) {
        val recentlist = listOf(
            DictionaryListModel(1, "Nusrsery", "", ""),
            DictionaryListModel(1, "Nusrsery", "", ""),
            DictionaryListModel(1, "Nusrsery", "", ""),
            DictionaryListModel(1, "Nusrsery", "", ""),
            DictionaryListModel(1, "Nusrsery", "", "")
        )
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = RecentAdapter(this, type)
        recyclerView.adapter = adapter
        adapter.setWords(recentlist)
    }

    private fun apiCall() {
        if (!this.isDestroyed) {
            apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
            val call = apiService.favouriteList(
                SharedPreferenceClass.getInstance(this)!!.getSession(),
                SharedPreferenceClass.getInstance(this)!!.getUserType().toString()
            )
            ApiCallClass.retrofitCall(this, call as Call<Any>)


        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.const_fav -> {
                ReuseFunctions.startNewActivityTaskWithParameter(
                    this,
                    FavouriteActivity::class.java,
                    Constants.TYPE_FAVOURITE
                )
            }
            R.id.const_down -> {
                ReuseFunctions.startNewActivity(this, DownloadActivity::class.java)
            }

            R.id.const_view_all_recent -> {
                /*ReuseFunctions.startNewActivityTaskWithParameter(
                    this,
                    FavouriteActivity::class.java,
                    Constants.TYPE_RECENT
                )*/
            }
            R.id.opsBackBtn -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSuccess(model: Any?) {
        if (model as FavouriteMain? != null) {
            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    var size = (model?.data?.size).toString()
                    tv_Fav_video_text.text = size + " Videos"
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

    override fun onFailure(error: String) {
    }
}
