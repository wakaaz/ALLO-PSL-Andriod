package com.net.pslapllication.activities

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.net.pslapllication.R
import com.net.pslapllication.adpters.DownloadAdapter
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.interfaces.OnProgressResultListener
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.MainClass
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_download.*
import kotlinx.android.synthetic.main.toolbaar_layout.*

class DownloadActivity : AppCompatActivity(), OnProgressResultListener, View.OnClickListener {
    var downloadAdapter: DownloadAdapter? = null
    var hashMap: HashMap<Long, DictionaryData> = HashMap<Long, DictionaryData>()
    var hashMapStoryData: HashMap<Long, StoryData> = HashMap<Long, StoryData>()
    var keysList: MutableList<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        ProgressHelper.getInstance(this)?.setListener(this)
        setTitleText()
        setListener()
        setRecycler()
        setDataInRecycler()
    }

    private fun setListener() {
        opsBackBtn.setOnClickListener(this)
    }

    private fun setTitleText() {

        txt_title.text = resources.getString(R.string.downloads)
        /*var tempList:List<DictionaryData>? = null
         tempList = Constants.downloadList!! + Constants.singleVidoedownloadList!!
        list = tempList as MutableList<DictionaryData>?*/
        if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_STORIES)){
            hashMapStoryData = Constants.constantHashMapStoryData
            if (hashMapStoryData.size == 0) {
                ReuseFunctions.showToast(this, "No Downloads in Progress")
            }
        }else if (SharedPreferenceClass.getInstance(this)!!.getDownloadType() == Constants.TYPE_DICTIONARY){
            hashMap = Constants.constantHashMap
            if (hashMap.size == 0) {
                ReuseFunctions.showToast(this, "No Downloads in Progress")
            }
        }

        /*if (Constants.downloadList.isNotEmpty() &&
            Constants.singleVidoedownloadList!!.isNotEmpty()) {
            list =
                (Constants.downloadList!! + Constants.singleVidoedownloadList!!) as MutableList<DictionaryData>?
        } else if (Constants.downloadList?.size != 0) {
            list = Constants.downloadList!! as MutableList<DictionaryData>?
        } else if (Constants.singleVidoedownloadList?.size != 0) {
            list = Constants.singleVidoedownloadList!! as MutableList<DictionaryData>?
        }else{
            ReuseFunctions.showToast(this,"No Downloads in Progress")
        }*/

    }

    private fun setRecycler() {
        recyclerview_downloads.layoutManager = LinearLayoutManager(this)
        downloadAdapter = DownloadAdapter(this, SharedPreferenceClass.getInstance(this)!!.getDownloadType())
        recyclerview_downloads.adapter = downloadAdapter
    }

    private fun setDataInRecycler() {
        for ((key, value) in hashMap) {
            println("$key = $value")
        }
        if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_STORIES)){
            keysList = hashMapStoryData.keys.toMutableList()
        }else if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_DICTIONARY)){
            keysList = hashMap.keys.toMutableList()
        }


        when (Constants.DOWNLOAD_STATUS) {
            Constants.DOWNLOAD_START -> {
                if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_STORIES)) {
                    if (hashMapStoryData.size != 0 && downloadAdapter != null) {
                        for ((key, value) in hashMapStoryData) {
                            DownloadProgressCounter(key).start()
                        }
                        downloadAdapter!!.setStoryData(hashMapStoryData)
                    }
                }else if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_DICTIONARY)) {
                    if (hashMap.size != 0 && downloadAdapter != null) {
                        for ((key, value) in hashMap) {
                            DownloadProgressCounter(key).start()
                        }
                        downloadAdapter!!.setWords(hashMap)
                    }}

            }
            Constants.DOWNLOAD_REQUESTED -> {

            }
            Constants.DOWNLOAD_COMPLETED -> {

            }
            Constants.NO_ACTIVE_DOWNLOAD -> {

            }
        }
    }

    class DownloadProgressCounter(
        private val downloadId: Long

    ) : Thread() {

        private val query: DownloadManager.Query = DownloadManager.Query()
        private var cursor: Cursor? = null
        private var lastBytesDownloadedSoFar = 0
        private var totalBytes: Int = 0
        private val manager: DownloadManager? = Constants.dm

        override fun run() {
            while (downloadId > 0) {
                try {
                    sleep(300)
                    cursor = manager!!.query(query)
                    if (cursor!!.moveToFirst()) {
                        when (cursor!!.getInt(cursor!!.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                            DownloadManager.STATUS_FAILED -> {

                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                ProgressHelper.getInstance(MainClass.appContext)
                                    ?.setCompletion(true)
                            }
                        }

                        //get statuses
                        val status =
                            cursor!!.getInt(cursor!!.getColumnIndex(DownloadManager.COLUMN_STATUS))

                        //get total bytes of the file
                        if (totalBytes <= 0) {
                            totalBytes =
                                cursor!!.getInt(cursor!!.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        }
                        val bytesDownloadedSoFar: Int =
                            cursor!!.getInt(cursor!!.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        if (bytesDownloadedSoFar == totalBytes && totalBytes > 0) {
                            ProgressHelper.getInstance(MainClass.appContext)
                                ?.setCompletion(true, downloadId)
                            interrupt()
                        } else {
                            try {
                                val listt: Constants.Companion
                            } catch (e: Exception) {
                            }
                            Handler(Looper.getMainLooper()).post {
                                ProgressHelper.getInstance(MainClass.appContext)
                                    ?.sendValues(
                                        (bytesDownloadedSoFar * 100 / totalBytes),
                                        downloadId
                                    )
                                /*var dicDictionaryData: DictionaryData? = null
                                val data =
                                    Constants.downloadList!!.filter { it.indexPosition == position }
                                if (data != null && data.isNotEmpty()) {
                                    ProgressHelper.getInstance(MainClass.appContext)
                                        ?.sendValues(
                                            (bytesDownloadedSoFar * 100 / totalBytes),
                                            downloadId
                                        )
                                }*/
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
            query.setFilterById(downloadId)
        }
    }

    override fun onProgressResult(progress: Int) {

    }

    override fun onDownloadComplete(downloadCompleteStatus: Boolean) {

    }

    override fun onProgressResult(progress: Int, referenceId: Long) {
        runOnUiThread {
            try {
                if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_STORIES)) {
                    hashMapStoryData[referenceId]!!.downloadprogress = progress

                    downloadAdapter!!.setStoryData(hashMapStoryData)
                    downloadAdapter!!.notifyDataSetChanged()
                }else if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_DICTIONARY)) {
                    hashMap[referenceId]!!.downloadprogress = progress

                    downloadAdapter!!.setWords(hashMap)
                    downloadAdapter!!.notifyDataSetChanged()
                }
                //////////////////////////////////////////////////////////

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDownloadComplete(downloadCompleteStatus: Boolean, referenceId: Long) {
        runOnUiThread {
            try {
                if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_STORIES)) {
                    hashMapStoryData[referenceId]!!.isDownloaded = true
                    hashMapStoryData[referenceId]!!.downloadprogress = 100
                    keysList!!.indexOf(referenceId)
                    Handler().postDelayed({
                        hashMapStoryData.remove(referenceId)
                        Constants.constantHashMapStoryData = hashMapStoryData
                        downloadAdapter!!.setStoryData(hashMapStoryData)
                    },1000)
                }else if (SharedPreferenceClass.getInstance(this)!!.getDownloadType().equals(Constants.TYPE_DICTIONARY)) {
                    hashMap[referenceId]!!.isDownloaded = true
                    hashMap[referenceId]!!.downloadprogress = 100
                    keysList!!.indexOf(referenceId)
                    Handler().postDelayed({
                        hashMap.remove(referenceId)
                        Constants.constantHashMap = hashMap
                        downloadAdapter!!.setWords(hashMap)
                    },1000)
                }


                runOnUiThread {
                    //downloadAdapter!!.notifyItemRangeChanged(keyIndex, hashMap.size)
                    //downloadAdapter!!.notifyItemChanged(keyIndex)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.opsBackBtn -> {
                this.finish()
            }
        }
    }


}
