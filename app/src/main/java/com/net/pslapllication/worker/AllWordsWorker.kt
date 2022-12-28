package com.net.pslapllication.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.room.datamodel.DictionaryMainModelAPI
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.SharedPreferenceClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class AllWordsWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams){
    lateinit var apiService: ApiService

    override fun doWork(): Result {
        return try {
            apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
            Log.e("user",SharedPreferenceClass.getInstance(context)!!.getSession()+"|"+ SharedPreferenceClass.getInstance(context)?.getUserType().toString())
            val call1: Call<DictionaryMainModelAPI> = apiService.getAllDictionaryDataDownload(
                SharedPreferenceClass.getInstance(context)!!.getSession(),
                SharedPreferenceClass.getInstance(context)?.getUserType().toString(),""
            )
            call1.enqueue(object : Callback<DictionaryMainModelAPI?> {
                override fun onResponse(
                    call: Call<DictionaryMainModelAPI?>,
                    response: Response<DictionaryMainModelAPI?>
                ) {
                    try {
                        val formattedDate: String
                        val dictionaryMainModel: DictionaryMainModelAPI = response.body()!!
                        if (dictionaryMainModel != null && dictionaryMainModel.code == 200) {
                            try {

                                val currentDate = Calendar.getInstance().time
                                val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                formattedDate = df.format(currentDate)

                                if (SharedPreferenceClass.getInstance(applicationContext)
                                        ?.getLastDate()
                                        .equals(formattedDate)
                                ) {
                                    Log.d("date22", "date1")

                                    /**
                                     * if data of db and response are different in length then delete the entire table and add new data
                                     */
                                    if (dictionaryMainModel.data.isNotEmpty()) {
                                        Log.d("date22", "date2")
                                        if (context!=null && ProgressHelper.getInstance(context)
                                                .getViewModel() != null
                                        ) {
                                            if (ProgressHelper.getInstance(context)!!
                                                    .getViewModel().allWordsCollection.isNotEmpty()
                                            ) {
                                                Log.d("date22", "date3")

                                                if (dictionaryMainModel.data.size !=
                                                    ProgressHelper.getInstance(context)!!
                                                        .getViewModel().allWordsCollection.size
                                                ) {
                                                    ProgressHelper.getInstance(context)!!
                                                        .getViewModel().deleteWords()
                                                    for (i in dictionaryMainModel.data.indices) {
                                                        dictionaryMainModel.data[i].indexPosition =
                                                            i
                                                    }

                                                    ProgressHelper.getInstance(context)!!
                                                        .getViewModel()
                                                        .insertWords(dictionaryMainModel.data)

                                                    SharedPreferenceClass.getInstance(
                                                        applicationContext
                                                    )
                                                        ?.setLastDate(formattedDate)
                                                }
                                            } else {
                                                if (context!=null && ProgressHelper.getInstance(context)
                                                        .getViewModel() != null
                                                ) {

                                                    if (dictionaryMainModel.data.isNotEmpty()) {

                                                        for (i in dictionaryMainModel.data.indices) {
                                                            dictionaryMainModel.data[i].indexPosition =
                                                                i
                                                        }
                                                        ProgressHelper.getInstance(context)!!
                                                            .getViewModel()
                                                            .insertWords(dictionaryMainModel.data)
                                                        SharedPreferenceClass.getInstance(
                                                            applicationContext
                                                        )
                                                            ?.setLastDate(formattedDate)
                                                    }

                                                }
                                            }
                                        } else {
                                            Log.d("date22", "date4")

                                            /**
                                             *
                                             * first time insertion
                                             */
                                            for (i in dictionaryMainModel.data.indices) {
                                                dictionaryMainModel.data[i].indexPosition = i
                                            }
                                            if (context!=null && ProgressHelper.getInstance(context)
                                                    .getViewModel() != null
                                            ) {

                                                ProgressHelper.getInstance(context)!!.getViewModel()
                                                    .insertWords(dictionaryMainModel.data)
                                                SharedPreferenceClass.getInstance(applicationContext)
                                                    ?.setLastDate(formattedDate)
                                            }
                                        }

                                    }
                                } else {
                                    /**
                                     * if date has been changed th
                                     */
                                    Log.d("date22", "date5")
                                    if (context!=null && ProgressHelper.getInstance(context)
                                            .getViewModel() != null
                                    ) {

                                        if (dictionaryMainModel.data.isNotEmpty()) {
                                            ProgressHelper.getInstance(context)!!
                                                .getViewModel().deleteWords()
                                            for (i in dictionaryMainModel.data.indices) {
                                                dictionaryMainModel.data[i].indexPosition = i
                                            }
                                            if (context!= null) {
                                                ProgressHelper.getInstance(context).getViewModel()
                                                    .insertWords(dictionaryMainModel.data)
                                                SharedPreferenceClass.getInstance(applicationContext)
                                                    ?.setLastDate(formattedDate)
                                            }
                                        }

                                    }
                                }
                            } catch (e: Exception) {
                                Log.d("exceptionError", "fail to add data")
                            }


                        } else if (dictionaryMainModel.code == Constants.SESSION_ERROR_CODE) {
                            Log.d("workertest12344", "apifail session")

                            //authentication error
                            /*ReuseFunctions.startNewActivityTaskTop(
                            context,
                            LoginScreen::class.java
                        )*/


                        }
                    }catch (e:Exception){
                         e.printStackTrace()
                        Log.d("workertest1233", "apifail session"+e.localizedMessage)

                    }
                }

                override fun onFailure(call: Call<DictionaryMainModelAPI?>, t: Throwable) {
                    Log.d("workertest123", "apifail" + t.localizedMessage)
                    call.cancel()
                }
            })
            Result.success()
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.d("workertest22", "workerFail" + e.localizedMessage)
            Result.failure()
        }
    }


}
