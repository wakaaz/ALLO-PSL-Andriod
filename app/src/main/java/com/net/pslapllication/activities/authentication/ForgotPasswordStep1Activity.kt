package com.net.pslapllication.activities.authentication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.net.pslapllication.R
import com.net.pslapllication.activities.BaseActivity
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.model.forgotpassword.RecoveryEmailModel
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.ValidationChecks
import kotlinx.android.synthetic.main.activity_forgot_password_step1.*
import retrofit2.Call

class ForgotPasswordStep1Activity : BaseActivity(), View.OnClickListener, RetrofitResponseListener {
    private var isInternetConnected: Boolean = false
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_step1)
        setListener()
    }

    private fun setListener() {
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
        tv_next.setOnClickListener(this)
        btn_back.setOnClickListener(this)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        isInternetConnected = isConnected
        super.onNetworkConnectionChanged(isConnected)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_next -> {
                if (ValidationChecks.emptyCheck(tv_email, "email required")) {
                    if (ValidationChecks.isValidEmail(tv_email.text.toString())) {
                        if (isInternetConnected) {
                            val call =
                                ApiCallClass.apiService.getForgotPassword(tv_email.text.toString())
                            ApiCallClass.retrofitCall(this, call as Call<Any>)
                        } else {
                            if (!this.isDestroyed) {
                                ReuseFunctions.snackMessage(
                                    this.getString(R.string.no_internet_text), constraint_bg
                                )

                            }
                        }
                    } else {
                        if (!this.isDestroyed) {
                            ReuseFunctions.snackMessage(
                                this.getString(R.string.email_error),
                                constraint_bg
                            )
                        }
                    }

                }
            }
            R.id.btn_back -> {
                super.onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSuccess(model: Any?) {
        if (model as RecoveryEmailModel? != null) {
            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    if (model?.response_msg != null) {
                         if (!this.isDestroyed) {
                            Toast.makeText(this, "" + model?.response_msg, Toast.LENGTH_SHORT)
                                .show()
                            ReuseFunctions.startNewActivity(
                                this,ForgotPasswordStep2Activity::class.java)
                            finish()
                        }
                    }
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
                Constants.RECOVERY_EMAIL_NOT_FOUND_ERROR -> {
                    if (!this.isDestroyed) {
                        ReuseFunctions.snackMessage("" + model?.response_msg, constraint_bg)

                    }

                }
            }

        }
    }

    override fun onFailure(error: String) {
        if (!this.isDestroyed) {
            ReuseFunctions.showToast(this, "" + error)
        }
    }
}