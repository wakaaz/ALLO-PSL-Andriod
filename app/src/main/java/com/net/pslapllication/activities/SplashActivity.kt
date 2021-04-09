package com.net.pslapllication.activities

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.net.pslapllication.R
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.helperClass.ProgressHelper
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.room.WordViewModelFactory
import com.net.pslapllication.room.WordsViewModel
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import com.net.pslapllication.worker.AllWordsWorker
import java.text.SimpleDateFormat
import java.util.*

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val dictionaryListCarrierDataModel: DictionaryListCarrierDataModel? =
            DictionaryListCarrierDataModel()
        ProgressHelper.getInstance(this).setModelInstance(dictionaryListCarrierDataModel!!)
        var session =  SharedPreferenceClass.getInstance(this)!!.getSession()
Log.d("sessiondata",""+session)
//getDefaultViewModelProviderFactory
        val context: Context = applicationContext

        val viewModelFactory = WordViewModelFactory(applicationContext)

        var wordsViewModel: WordsViewModel =
            ViewModelProvider(this, viewModelFactory).get(WordsViewModel::class.java)
        ProgressHelper.getInstance(this).setViewModel(wordsViewModel)
         setSplash()
       // SharedPreferenceClass.getInstance(applicationContext)?.setLastDate("17-12-2020")

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected){
            requestWorker()
        }
        super.onNetworkConnectionChanged(isConnected)
    }
    private fun setSplash() {
        Handler().postDelayed(
            {
                if (SharedPreferenceClass.getInstance(this)?.getIntroLaunch()!! &&
                    SharedPreferenceClass.getInstance(this)?.getisLogin()!!
                ) {
                    ReuseFunctions.startNewActivity(this, HomeActivity::class.java)
                } else if (SharedPreferenceClass.getInstance(this)?.getisLogin()!!) {
                    ReuseFunctions.startNewActivity(this, IntroSliderActivity::class.java)
                } else if (SharedPreferenceClass.getInstance(this)?.getIntroLaunch()!!) {
                    ReuseFunctions.startNewActivity(this, HomeActivity::class.java)
                } else {
                    ReuseFunctions.startNewActivity(
                        this,
                        LoginScreen::class.java
                    )
                }
                this.finish()
            }, 500
        )
    }

    private fun requestWorker() {
             val constraint: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeWorkRequest: OneTimeWorkRequest.Builder = OneTimeWorkRequest.Builder(
                AllWordsWorker::class.java
            ).setConstraints(constraint)
            val myWork: OneTimeWorkRequest = oneTimeWorkRequest.build()
            val myWorkId: UUID = myWork.id

            WorkManager.getInstance().enqueue(myWork)


    }
}
