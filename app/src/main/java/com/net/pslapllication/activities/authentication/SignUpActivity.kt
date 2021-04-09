package com.net.pslapllication.activities.authentication

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.net.pslapllication.R
import com.net.pslapllication.activities.BaseActivity
import com.net.pslapllication.activities.HomeActivity
import com.net.pslapllication.activities.IntroSliderActivity
import com.net.pslapllication.model.signUp.SignUpDataModel
import com.net.pslapllication.model.signUp.SignUpMain
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.util.SharedPreferenceClass
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.ValidationChecks
import com.net.pslapllication.worker.AllWordsWorker
import kotlinx.android.synthetic.main.activity_login_screen.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.constraint_skip
import kotlinx.android.synthetic.main.activity_sign_up.tv_login
import kotlinx.android.synthetic.main.activity_sign_up.tv_password_text
import kotlinx.android.synthetic.main.activity_sign_up.tv_user_id_text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class SignUpActivity : BaseActivity(), View.OnClickListener {
    lateinit var apiService: ApiService
     var isNetworkAvailable: Boolean = false
    private var isInternetConnected: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
        setClickListeners()
    }



    private fun setClickListeners() {
        tv_signup_btn.setOnClickListener(this)
        constraint_skip.setOnClickListener(this)
        tv_login.setOnClickListener(this)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
        isNetworkAvailable = isConnected
        } else {
            if (!this.isDestroyed) {
                //  constraintInternet.visibility = View.VISIBLE
                isInternetConnected = isConnected
                ReuseFunctions.snackMessage(
                    this.getString(R.string.no_internet_text),
                    mainlayout
                )
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_signup_btn -> {
                if (isNetworkAvailable) {
                    if (ValidationChecks.emptyCheck(tv_name_text, "email required") &&
                        ValidationChecks.emptyCheck(tv_user_id_text, "user Id required") &&
                        ValidationChecks.emptyCheck(tv_password_text, "password required")
                    ) {
                        if (ValidationChecks.isValidEmail(tv_user_id_text.text.toString())){
                            if (tv_password_text.text.length>= 6){
                                signUpRetrofit2Api(tv_name_text.text.toString(), tv_user_id_text.text.toString(),
                                    tv_password_text.text.toString())
                            }else{
                                if (!this.isDestroyed)
                                    ReuseFunctions.snackMessage("Password length must be 6 digit or greater",mainlayout)
                            }
                        }else{
                            ReuseFunctions.snackMessage("Enter email Address",mainlayout)
                        }
                    }
                }else{
                    ReuseFunctions.snackMessage("No Internet Available",mainlayout)
                 }
            }
            R.id.constraint_skip -> {
                this.finish()
            }
            R.id.tv_login -> {
                ReuseFunctions.startNewActivity(this, LoginScreen::class.java)
            }
        }
    }

    /************network request*************/
    private fun signUpRetrofit2Api(name: String, email: String, password: String) {

        val signUpPayLoad = SignUpDataModel(
            name,
            "",
            email,
            password,
            0,
            "",
            "",
            "",
            "",
            "",
            0
        )
        val call1: Call<SignUpMain> = apiService.getSignUp(signUpPayLoad)
        call1.enqueue(object : Callback<SignUpMain?> {
            override fun onResponse(
                call: Call<SignUpMain?>?,
                response: Response<SignUpMain?>
            ) {
                 val signUpMain: SignUpMain = response.body()!!
                if (signUpMain != null && signUpMain.code == 200) {

                    if (signUpMain.objectmodel != null){
                        SharedPreferenceClass.getInstance(this@SignUpActivity)
                            ?.setFirstName(signUpMain.objectmodel!!.first_name)
                        SharedPreferenceClass.getInstance(this@SignUpActivity)
                            ?.setemail(signUpMain.objectmodel!!.email)
                        SharedPreferenceClass.getInstance(this@SignUpActivity)
                            ?.setupdated_at(signUpMain.objectmodel!!.updated_at)
                        SharedPreferenceClass.getInstance(this@SignUpActivity)
                            ?.setcreated_at(signUpMain.objectmodel!!.created_at)
                        SharedPreferenceClass.getInstance(this@SignUpActivity)
                            ?.setSession(signUpMain.objectmodel!!.session)
                        SharedPreferenceClass.getInstance(this@SignUpActivity)?.setid(signUpMain.objectmodel!!.id)
                        }

                    SharedPreferenceClass.getInstance(this@SignUpActivity)?.setisLogin(true)
                    val currentDate = Calendar.getInstance().time
                    val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val formattedDate: String = df.format(currentDate)
                    SharedPreferenceClass.getInstance(this@SignUpActivity)?.setLastDate(formattedDate)
                    requestWorker()
                    ReuseFunctions.startNewActivity(this@SignUpActivity, IntroSliderActivity::class.java)
                } else if (signUpMain.code == 102) {

                    ReuseFunctions.showToast(this@SignUpActivity, "Email is already taken")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        tv_user_id_text.focusable

                    }
                    tv_user_id_text.text.clear()
                    tv_name_text.text.clear()
                    tv_password_text.text.clear()
                } else if (signUpMain.code == 101) {
                    ReuseFunctions.showToast(this@SignUpActivity, "Field Validation Errors")
                }
            }
            override fun onFailure(call: Call<SignUpMain?>, t: Throwable?) {
                 Toast.makeText(applicationContext, "" + t?.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
                call.cancel()
            }
        })
    }
    /************network request*************/


    /************lifecycles related*************/
    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {

        super.onPause()
    }
    /************lifecycles related*************/
    private fun requestWorker() {
        if (  isInternetConnected) {
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

}
