package com.net.pslapllication.broadcastReceiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.room.DownloadRepository
import com.net.pslapllication.room.WordsDatabase
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.SharedPreferenceClass
import java.io.File
import java.lang.Exception
import java.net.URLDecoder

open class DownloadVideoBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getStringExtra(Constants.DOWNLOAD_TYPE) != null) {
            Constants.DOWNLOAD_STATUS = Constants.DOWNLOAD_START
            when (intent.getStringExtra(Constants.DOWNLOAD_TYPE)) {

                Constants.SINGLE_VIDEO_DOWNLOAD -> {
                    if (intent.getStringExtra(Constants.KEY_URL) != null) {
                        DownloadData(
                            context!!,
                            intent.getStringExtra(Constants.KEY_URL),
                            intent.getStringExtra(Constants.FILE_NAME),
                            intent.getStringExtra(Constants.THUMBNAIL)
                        )
                    }
                }
                Constants.MULTI_VIDEO_DOWNLOAD -> {
                    if (intent.getBundleExtra("LIST") != null && intent.getStringExtra(Constants.CAT_NAME) != null &&
                        intent.getIntExtra("QUALITY_ID", 0) != -1
                    ) {
                        val args: Bundle = intent.getBundleExtra("LIST")!!
                        val carrierDataModel =
                            args.getSerializable(Constants.CATEGORY_LIST) as DictionaryListCarrierDataModel
                        val catName = intent.getStringExtra(Constants.CAT_NAME)
                        val type = intent.getStringExtra("TYPE")
                        val qualityType = intent.getIntExtra("QUALITY_ID", 0)
                        if (type == Constants.TYPE_DICTIONARY) {
                            val list: List<DictionaryData>? = carrierDataModel.getModelList()
                            downloadMultipleData(context!!, list, catName, qualityType)
                            SharedPreferenceClass.getInstance(context)!!.setDownloadType(type)

                        } else if (type == Constants.TYPE_STORIES) {
                            val list: List<StoryData>? = carrierDataModel.getStoryList()
                            downloadMultipleData(list, context!!, catName, qualityType)
                            SharedPreferenceClass.getInstance(context)!!.setDownloadType(type)
                        }else if (type == Constants.TYPE_SUB_TOPIC) {
                            val list: List<TutorialData>? = carrierDataModel.getTutorialModelList()
                            downloadTutorialMultipleData(list, context!!, catName, qualityType)
                            SharedPreferenceClass.getInstance(context)!!.setDownloadType(type)
                        }else if (type == Constants.TYPE_LEARNING_TUTORIAL) {
                            val list: List<LearningData>? = carrierDataModel.getLeaningModelList()
                            downloadLearningTutorialMultipleData(list, context!!, catName, qualityType)
                            SharedPreferenceClass.getInstance(context)!!.setDownloadType(type)
                        }
                    }
                }
            }
        }
    }


    private fun DownloadData(
        context: Context,
        urlString: String?,
        fileName: String?,
        thumbnail: String?
    ) {
        val downloadManager =
            context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
        var hashMap: HashMap<Long, DictionaryData> = HashMap<Long, DictionaryData>()
        hashMap = Constants.constantHashMap
        val downloadReference: Long

        var listDownload: List<DictionaryData>? = ArrayList<DictionaryData>()
        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(urlString))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle("PSL Application")
        request.setDescription(fileName)
        var dummyfilename = fileName
        if(!fileName!!.contains(".mp4")){
            dummyfilename = fileName+".mp4"
        }
        var file = File(
            context.getExternalFilesDir(null)!!.getAbsolutePath() +
                    Constants.FOLDER_NAME, dummyfilename + ""
        )
        if (file.exists()) {
            file.delete()
        } else {
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                Constants.FOLDER_NAME + dummyfilename + ""
            )
        }

        //Enqueue download and save into referenceId
        downloadReference = downloadManager!!.enqueue(request)


        val dictionaryData: DictionaryData? = DictionaryData()
        dictionaryData?.filename = dummyfilename!!
        dictionaryData?.poster = thumbnail!!
        dictionaryData?.downloadReference = downloadReference


        /*  listDownload = listOf(dictionaryData)
          if (Constants.singleVidoedownloadList!!.isNotEmpty()) {
              listDownload = listDownload + Constants.singleVidoedownloadList!!
          }
          Constants.singleVidoedownloadList = listDownload*/

            var download_id = downloadReference.toInt()
            var title =  fileName

            if(title.contains(".mp4")){
                title =  title.replace(".mp4","")
            }

            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                download_id,
                title.toLowerCase(),
                thumbnail,
                "",
                true
            )
            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
            downloadRepository.insertWords(model)


        hashMap[downloadReference] = dictionaryData!!
        Constants.constantHashMap = hashMap
        Constants.idLong = downloadReference
        /*VideoPreviewActivity.DownloadProgressCounter(
            downloadManager,
            downloadReference
        ).start()*/
        Constants.dm = downloadManager
    }

    private fun downloadMultipleData(
        context: Context,
        list: List<DictionaryData>?,
        catName: String?,
        qualityType: Int
    ) {
        var hashMap: HashMap<Long, DictionaryData> = HashMap<Long, DictionaryData>()
        var downloadReference: Long
        val downloadManager =
            context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
        hashMap = Constants.constantHashMap
        for (i in list!!.indices) {
            when (qualityType) {
                Constants.p240p -> {
                    if (list[i].p240p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p240p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }

                            var download_id = list[i].downloadReference.toInt()
                            var title =  list[i].english_word
                            if(title.equals("-")){
                                title =  list[i].urdu_word
                            }
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)

                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMap = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p360p -> {
                    if (list[i].p360p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p360p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = list[i].downloadReference.toInt()
                            var title =  list[i].english_word
                            if(title.equals("-")){
                                title =  list[i].urdu_word
                            }
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMap = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p480p -> {
                    if (list[i].p480p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p480p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = list[i].downloadReference.toInt()
                            var title =  list[i].english_word
                            if(title.equals("-")){
                                title =  list[i].urdu_word
                            }
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMap = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p720p -> {
                    if (list[i].p720p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p720p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = list[i].downloadReference.toInt()
                            var title =  list[i].english_word
                            if(title.equals("-")){
                                title =  list[i].urdu_word
                            }
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }

                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMap = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.AUTO -> {
                }
            }

        }
        Constants.dm = downloadManager
    }

    /**
     *  Tutorial Data
     */

    private fun downloadTutorialMultipleData(
        list: List<TutorialData>?,
        context: Context,
        catName: String?,
        qualityType: Int
    ) {
        var hashMap: HashMap<Long, TutorialData> = HashMap<Long, TutorialData>()
        var downloadReference: Long
        val downloadManager =
            context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
        hashMap = Constants.constantHashMapTutorialData
        for (i in list!!.indices) {
            when (qualityType) {
                Constants.p240p -> {
                    if (list[i].p240p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p240p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMapTutorialData = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p360p -> {
                    if (list[i].p360p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p360p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMapTutorialData = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p480p -> {
                    if (list[i].p480p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p480p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMapTutorialData = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p720p -> {
                    if (list[i].p720p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p720p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMapTutorialData = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.AUTO -> {
                }
            }

        }
        Constants.dm = downloadManager
    }

  private fun downloadLearningTutorialMultipleData(
      list: List<LearningData>?,
      context: Context,
      catName: String?,
      qualityType: Int
    ) {
        var  HashMapLearningData: HashMap<Long, LearningData> = HashMap<Long, LearningData>()
        var downloadReference: Long
        val downloadManager =
            context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
       HashMapLearningData = Constants.constantHashMapLearningData
        for (i in list!!.indices) {
            when (qualityType) {
                Constants.p240p -> {
                    if (list[i].p240p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p240p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }


                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            HashMapLearningData[downloadReference] = list[i]
                            Constants.constantHashMapLearningData = HashMapLearningData


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p360p -> {
                    if (list[i].p360p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p360p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            HashMapLearningData[downloadReference] = list[i]
                            Constants.constantHashMapLearningData = HashMapLearningData


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p480p -> {
                    if (list[i].p480p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p480p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            HashMapLearningData[downloadReference] = list[i]
                            Constants.constantHashMapLearningData = HashMapLearningData


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p720p -> {
                    if (list[i].p720p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p720p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            /* val file = File(
                                 context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                         Constants.CAT_FOLDER_NAME + catName + "/", list[i].filename+"-"+list[i].urdu_word+ "")*/
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )

                            /*val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.FOLDER_NAME, list[i].english_word + ".mp4")*/

                            if (file.exists()) {
                                file.delete()
                            } else {
                                /*request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename+"-"+list[i].urdu_word + ""
                                )*/
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            HashMapLearningData[downloadReference] = list[i]
                            Constants.constantHashMapLearningData = HashMapLearningData


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.AUTO -> {
                }
            }

        }
        Constants.dm = downloadManager
    }
    private fun downloadMultipleData(
        list: List<StoryData>?,
        context: Context,
        catName: String?,
        qualityType: Int
    ) {
        var hashMap: HashMap<Long, StoryData> = HashMap<Long, StoryData>()
        var downloadReference: Long
        val downloadManager =
            context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
        hashMap = Constants.constantHashMapStoryData
        for (i in list!!.indices) {
            when (qualityType) {
                Constants.p240p -> {
                    if (list[i].p240p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p240p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )
                            if (file.exists()) {
                                file.delete()
                            } else {
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }


                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMapStoryData = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p360p -> {
                    if (list[i].p360p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p360p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )
                            if (file.exists()) {
                                file.delete()
                            } else {
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMapStoryData = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p480p -> {
                    if (list[i].p480p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p480p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )
                            if (file.exists()) {
                                file.delete()
                            } else {
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )
                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMapStoryData = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.p720p -> {
                    if (list[i].p720p.url != "") {
                        try {
                            val request: DownloadManager.Request =
                                DownloadManager.Request(Uri.parse(URLDecoder.decode(list[i].p720p.url)))
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setTitle("PSL Application")
                            request.setDescription(list[i].filename)
                            val file = File(
                                context.getExternalFilesDir(null)!!.getAbsolutePath() +
                                        Constants.CAT_FOLDER_NAME + catName + "/",
                                list[i].filename + ""
                            )
                            if (file.exists()) {
                                file.delete()
                            } else {
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    Constants.CAT_FOLDER_NAME + catName + "/" + list[i].filename + ""
                                )
                            }
                            var download_id = 0
                            var title =  list[i].title
                            if(title.contains(" ")){
                                title =  title.replace(" ","_")
                            }
                            val model = com.net.pslapllication.room.datamodel.DownloadData(0,
                                download_id,
                                title.toLowerCase(),
                                list[i].poster,
                                "",
                                true
                            )


                            val downloadDao = WordsDatabase.getInstance(context).downloadDao()
                            var downloadRepository : DownloadRepository = DownloadRepository(downloadDao)
                            downloadRepository.insertWords(model)
                            downloadReference = downloadManager!!.enqueue(request)

                            hashMap[downloadReference] = list[i]
                            Constants.constantHashMapStoryData = hashMap


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Constants.AUTO -> {
                }
            }

        }
        Constants.dm = downloadManager
    }
}

