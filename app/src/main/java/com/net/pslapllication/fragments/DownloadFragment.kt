package com.net.pslapllication.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.net.pslapllication.R
import com.net.pslapllication.activities.DownloadActivity
import com.net.pslapllication.adpters.CustomGridAdapter
import com.net.pslapllication.adpters.DownloadedCatAdapter
import com.net.pslapllication.adpters.VideoQualityOptionAdapter
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.interfaces.onQualityChangSelectedListener
import com.net.pslapllication.interfaces.onVideoDeleteInterface
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.WrappingGridView
import kotlinx.android.synthetic.main.activity_main_listing_new.*
import kotlinx.android.synthetic.main.bottom_layout_video_quality_list.view.*
 import kotlinx.android.synthetic.main.fragment_download.view.*
 import java.io.File
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class DownloadFragment : Fragment(), View.OnClickListener, onQualityChangSelectedListener,
    onVideoDeleteInterface {
    var downloadList: List<DownloadListModel>? = null
    var catList: List<DictionaryListModel>? = null
    private var isSorted: Boolean = true
    private var isStoragePermissionAllowed: Boolean = false
    private var adapter: CustomGridAdapter? = null
    private var adapterCat: DownloadedCatAdapter? = null
    private var viewlayout: View? = null
    private var dialog_sort: BottomSheetDialog? = null
    private var selectedSortyId: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the bottom_layout_delete_video for this fragment
        viewlayout = inflater.inflate(R.layout.fragment_download, container, false)
        setTitle(viewlayout!!)
        setClickListener(viewlayout!!)
        setDownloadRecycler(viewlayout!!)
        searchBarSetting(viewlayout!!)
      //  viewlayout!!.img_btn_download.visibility = View.VISIBLE
        checkPermission(viewlayout!!)
        return viewlayout
    }

    private fun setTitle(view: View) {
        view.txt_title.text = "Downloads"
        view.opsBackBtn.visibility = View.GONE
    }

    private fun setClickListener(view: View) {
        view.tv_sort.setOnClickListener(this)
        view.img_btn_download.setOnClickListener(this)
    }
    private fun setDownloadRecycler(view: View) {
        val mainMenu = view.findViewById<WrappingGridView>(R.id.grid_view)
        adapter = CustomGridAdapter(activity!!.applicationContext, Constants.TYPE_DOWNLOAD)
        mainMenu.adapter = adapter

        view.recycler_category.layoutManager = LinearLayoutManager(activity)
        adapterCat = DownloadedCatAdapter(activity!!.applicationContext, this)
        view.recycler_category.adapter = adapterCat

        checkPermission(view)
    }

    private fun searchBarSetting(view: View) {
        val searchView: SearchView =
            view.findViewById(R.id.searchView_download)
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
    }

    private fun getDownloadedVideos(): List<DownloadListModel> {
        val dicWordList = ArrayList<DownloadListModel>()

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.FOLDER_NAME
        )
        /*  var file = File(
              context?.getExternalFilesDir(null)!!.getAbsolutePath() +
                      Constants.FOLDER_NAME
          )*/

        if (file.exists()) {
            try {
                val file = file.listFiles()
                var filename: String = ""
                if (file?.size != 0) {
                    for (i in file!!.indices) {
                        /*val index:Int = file[i].name.indexOf("-")
                        if (index!=-1){
                            filename = file[i].name.substring(0,index)
                        }*/
                        dicWordList.add(
                            DownloadListModel(
                                i,
                                file[i].nameWithoutExtension,
                                file[i].absolutePath,
                                "00:10",
                                false,
                                i
                            )
                        )
                    }
                } else {
                    viewlayout!!.constraint_noDownload.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            viewlayout!!.constraint_noDownload.visibility = View.VISIBLE
        }
        return dicWordList
    }

    private fun getDownloadedCat(): List<DictionaryListModel> {
        val catList = ArrayList<DictionaryListModel>()
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Constants.CAT_FOLDER_NAME
        )
        /*  var file = File(
              context?.getExternalFilesDir(null)!!.getAbsolutePath() +
                      Constants.FOLDER_NAME)*/

        if (file.exists()) {
            try {

                viewlayout!!.view_download_cat.visibility = View.VISIBLE
                viewlayout!!.tv_title_cat.visibility = View.VISIBLE
                viewlayout!!.view_download_cat.visibility = View.VISIBLE
                val file = file.listFiles()
                if (file?.size != 0) {
                    for (i in file!!.indices) {
                        val fileList = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            Constants.CAT_FOLDER_NAME + file[i].name
                        )
                        if (fileList.exists()) {
                            val length = fileList.listFiles().size
                            Log.d("datasetnew1234566",""+file[i].name)
                            catList.add(
                                DictionaryListModel(
                                    i,
                                    file[i].name,

                                    "",
                                    length.toString()
                                )
                            )
                        }

                    }
                } else {

                    viewlayout!!.view_download_cat.visibility = View.GONE
                    viewlayout!!.tv_title_cat.visibility = View.GONE
                    viewlayout!!.recycler_category.visibility = View.GONE

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            viewlayout!!.view_download_cat.visibility = View.GONE
            viewlayout!!.tv_title_cat.visibility = View.GONE
            viewlayout!!.recycler_category.visibility = View.GONE

        }
        return catList
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_sort -> {

                ReuseFunctions.preventTwoClick(v)
                openSortBottomSheet()
            }
            R.id.img_btn_download -> {
                if (activity != null)
                    ReuseFunctions.startNewActivity(activity!!, DownloadActivity::class.java)
            }
        }
    }


    private fun checkPermission(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), Constants.MY_PERMISSIONS_REQUEST_WRITE_STORAGE
            )
        } else {
            isStoragePermissionAllowed = true
            downloadList = getDownloadedVideos()
            catList = getDownloadedCat()
            if (downloadList != null && downloadList!!.size != 0) {
                viewlayout!!.grid_view.visibility = View.VISIBLE
                viewlayout!!.tv_title_video.visibility = View.VISIBLE
                viewlayout!!.constraint_noDownload.visibility = View.GONE
                adapter!!.setDownloadss(downloadList!!)
                adapter!!.notifyDataSetChanged()
            }
            if (catList != null && catList!!.size != 0) {
                viewlayout!!.recycler_category.visibility = View.VISIBLE
                viewlayout!!.recycler_category.visibility = View.VISIBLE
                viewlayout!!.constraint_noDownload.visibility = View.GONE
                adapterCat!!.setWords(catList!!)
                adapterCat!!.notifyDataSetChanged()
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
                    isStoragePermissionAllowed = true
                    downloadList = getDownloadedVideos()
                    catList = getDownloadedCat()
                    if (downloadList != null && downloadList!!.size != 0) {
                        viewlayout!!.grid_view.visibility = View.VISIBLE
                        viewlayout!!.tv_title_video.visibility = View.VISIBLE
                        viewlayout!!.constraint_noDownload.visibility = View.GONE
                        adapter!!.setDownloadss(downloadList!!)
                        adapter!!.notifyDataSetChanged()
                    }
                    if (catList != null && catList!!.size != 0) {
                        viewlayout!!.recycler_category.visibility = View.VISIBLE
                        viewlayout!!.recycler_category.visibility = View.VISIBLE
                        viewlayout!!.constraint_noDownload.visibility = View.GONE
                        adapterCat!!.setWords(catList!!)
                        adapterCat!!.notifyDataSetChanged()
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
        if (downloadList != null && downloadList?.size != 0) {
            if (activity != null) {
                when (sortType) {

                    "1" -> {
                        val sortedAppsList1: List<DownloadListModel> =
                            downloadList!!.sortedBy { it.wordName }
                        adapter!!.setDownloadss(sortedAppsList1)
                        adapter!!.notifyDataSetChanged()
                        isSorted = true
                        ReuseFunctions.snackMessage(
                            viewlayout!!.constraint_download,
                            resources.getString(R.string.sortedByAscending)
                        )
                    }
                    "2" -> {
                        val reverseSortedAppsList: List<DownloadListModel> =
                            downloadList!!.sortedByDescending { it.wordName }
                        adapter!!.setDownloadss(reverseSortedAppsList)
                        adapter!!.notifyDataSetChanged()
                        ReuseFunctions.snackMessage(
                            viewlayout!!.constraint_download,
                            resources.getString(R.string.sortedByDescending)
                        )
                        isSorted = false
                    }
                    else -> {
                        adapter!!.setDownloadss(downloadList!!)
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
    }

    override fun onResume() {
        super.onResume()
        if (isStoragePermissionAllowed) {
            downloadList = getDownloadedVideos()

            if (downloadList != null && downloadList!!.size != 0) {
                viewlayout!!.constraint_noDownload.visibility = View.GONE
                viewlayout!!.grid_view.visibility = View.VISIBLE
                viewlayout!!.tv_title_video.visibility = View.VISIBLE
                adapter!!.setDownloadss(downloadList!!)
                adapter!!.notifyDataSetChanged()
            }

            catList = getDownloadedCat()

            if (catList != null && catList!!.size != 0) {
                viewlayout!!.recycler_category.visibility = View.VISIBLE
                viewlayout!!.recycler_category.visibility = View.VISIBLE
                viewlayout!!.constraint_noDownload.visibility = View.GONE
                adapterCat!!.setWords(catList!!)
                adapterCat!!.notifyDataSetChanged()
            }
        }
    }

    override fun onAllCatDeleted(isDeleted: Boolean) {
        if(isDeleted){
            viewlayout!!.tv_title_cat.visibility = View.GONE
            viewlayout!!.view_download.visibility = View.GONE
            if (downloadList!=null && downloadList!!.isEmpty()) {
                if (viewlayout != null)
                    viewlayout!!.constraint_noDownload.visibility = View.VISIBLE
            }
        }

    }
}




