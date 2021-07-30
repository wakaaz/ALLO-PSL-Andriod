package com.net.pslapllication.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.GridView
import androidx.annotation.NonNull
import com.net.pslapllication.R
import com.net.pslapllication.adpters.CustomGridAdapter
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.room.DownloadRepository
import com.net.pslapllication.room.WordsDatabase
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.activity_cat_downloads.tv_sort
import kotlinx.android.synthetic.main.toolbaar_layout.*
import java.io.File
import java.lang.Exception

class CatDownloadsActivity : AppCompatActivity(),View.OnClickListener {
    var catName:String? = null
    private var adapter: CustomGridAdapter? = null
    var downloadList: List<DownloadListModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_downloads)
        getIntentData()
        setTitle()
        setClickListener()
        setDownloadRecycler()
        //searchBarSetting()
        img_btn_download.visibility = View.GONE
    }

    private fun getIntentData() {
         if(intent!=null && intent.getStringExtra("TYPE")!=null){
             catName = intent.getStringExtra("TYPE")
         }

    }

    private fun setTitle() {
        txt_title.text =catName
        opsBackBtn.visibility = View.VISIBLE
    }

    private fun setClickListener() {
         tv_sort.setOnClickListener(this)
        opsBackBtn.setOnClickListener(this)
     }

    private fun setDownloadRecycler() {
        val mainMenu = findViewById<GridView>(R.id.grid_view)
        adapter = CustomGridAdapter(applicationContext, Constants.TYPE_DOWNLOAD)
        mainMenu.adapter = adapter


        checkPermission()
    }

/*    private fun searchBarSetting() {
        val searchView: SearchView =
            findViewById(R.id.searchView_download)
        val searchText =
            searchView.findViewById<View>(R.id.search_src_text) as TextView
        searchText.typeface = ReuseFunctions.regularFont(activity)
        searchText.textSize = 16f
        searchText.setPadding(0, 0, 0, 0);
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                return false
            }
        })
    }*/

    private fun getDownloadedVideos(): List<DownloadListModel> {
        val dicWordList = ArrayList<DownloadListModel>()
        //val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.CAT_FOLDER_NAME+catName+"/"
        )

        /*  var file = File(
              context?.getExternalFilesDir(null)!!.getAbsolutePath() +
                      Constants.FOLDER_NAME
          )*/
        if (file.exists()) {
            try {
                val downloadDao =  WordsDatabase.getInstance(applicationContext).downloadDao()
                var downloadRepository : DownloadRepository = DownloadRepository(downloadDao!!)

                var filename :String = ""
                val fileCat = file.listFiles()
                if (fileCat?.size != 0) {
                    for (i in fileCat!!.indices) {
                        /*val index:Int = file[i].name.indexOf("-")
                        if (index!=-1){
                            filename = file[i].name.substring(0,index)
                        }*/

                        var picture = fileCat[i].absolutePath
                        Log.e("name",fileCat[i].nameWithoutExtension+"")
                        var model = downloadRepository.getSingleDownload(fileCat[i].nameWithoutExtension)
                        if(model != null && !model.link.isNullOrEmpty()){
                            picture = model.link
                        }

                        dicWordList.add(
                            DownloadListModel(
                                i,
                                fileCat[i].nameWithoutExtension,
                                picture,
                                "00:10",
                                false,
                                i
                            )
                        )
                    }
                } else {
                    ReuseFunctions.showToast(applicationContext, "No Video Found")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ReuseFunctions.showToast(applicationContext, "No Video Found")
        }
        return dicWordList
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            /*R.id.tv_sort -> {

                ReuseFunctions.preventTwoClick(v)
                openSortBottomSheet()
            }*/
            R.id.opsBackBtn -> {
               super.onBackPressed()
                this.finish()
            }
        }
     }

    override fun onBackPressed() {
        super.onBackPressed()
    }
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), Constants.MY_PERMISSIONS_REQUEST_WRITE_STORAGE
            )
        } else {

            downloadList = getDownloadedVideos()

            if (downloadList != null && downloadList!!.size != 0) {
                adapter!!.setDownloadss(downloadList!!)
                adapter!!.notifyDataSetChanged()
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

                    downloadList = getDownloadedVideos()

                    if (downloadList != null && downloadList!!.size != 0) {
                        adapter!!.setDownloadss(downloadList!!)
                        adapter!!.notifyDataSetChanged()
                    }

                }
            }
            else -> {
            }
        }
    }

   /* private fun openSortBottomSheet() {
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
        *//*dialogView.recycler_video_quality_option.setOnClickListener {
            openBottomSheetQualityList()

        }*//*
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

    private fun sortList(sortType: String) {
        if (downloadList != null && downloadList?.size != 0) {
            if (activity != null) {
                when (sortType) {

                    "1" -> {
                        val sortedAppsList1: List<DictionaryListModel> =
                            downloadList!!.sortedBy { it.wordName }
                        adapter!!.setWords(sortedAppsList1)
                        adapter!!.notifyDataSetChanged()
                        isSorted = true
                        ReuseFunctions.snackMessage(
                            viewlayout!!.constraint_download,
                            resources.getString(R.string.sortedByAscending)
                        )
                    }
                    "2" -> {
                        val reverseSortedAppsList: List<DictionaryListModel> =
                            downloadList!!.sortedByDescending { it.wordName }
                        adapter!!.setWords(reverseSortedAppsList)
                        adapter!!.notifyDataSetChanged()
                        ReuseFunctions.snackMessage(
                            viewlayout!!.constraint_download,
                            resources.getString(R.string.sortedByDescending)
                        )
                        isSorted = false
                    }
                    else -> {
                        adapter!!.setWords(downloadList!!)
                        adapter!!.notifyDataSetChanged()
                        ReuseFunctions.snackMessage(
                            viewlayout!!.constraint_download,
                            resources.getString(R.string.sortedByDescending)
                        )
                        isSorted = false
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
    }*/
}