package com.net.pslapllication.activities.authentication

import android.os.Bundle
import android.util.Log
import android.view.View
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
import kotlinx.android.synthetic.main.activity_forgot_password_step3.*
import retrofit2.Call

class ForgotPasswordStep3Activity : BaseActivity(), View.OnClickListener,
    RetrofitResponseListener {
    var codeString: String = ""
    var isInternetConnected: Boolean = false
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_step3)
        getIntentData()
        setListener()
    }


    private fun getIntentData() {
        if (intent != null && intent.getStringExtra("CODE") != null) {
            codeString = intent.getStringExtra("CODE")!!
        }
    }

    private fun setListener() {
        tv_next_btn.setOnClickListener(this)
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        isInternetConnected = isConnected
        super.onNetworkConnectionChanged(isConnected)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_next_btn -> {
                if (ValidationChecks.emptyCheck(tv_new_pass_text, "Password required") &&
                    ValidationChecks.emptyCheck(tv_con_pass_text, "Password required") &&
                    codeString != "" &&
                    ValidationChecks.isPasswordValidLength(
                        tv_new_pass_text,
                        tv_new_pass_text.text.toString(),
                        "Required at least 6 digits"
                    ) &&
                    ValidationChecks.isPasswordValidLength(
                        tv_con_pass_text,
                        tv_con_pass_text.text.toString(),
                        "Required at least 6 digits"
                    )
                ) {
                    if (tv_new_pass_text.text.toString() == tv_con_pass_text.text.toString()) {
                        if (isInternetConnected) {
                            var call = ApiCallClass.apiService.getPasswordRecovered(
                                codeString, tv_new_pass_text.text.toString(),
                                tv_con_pass_text.text.toString()
                            )
                            ApiCallClass.retrofitCall(this, call as Call<Any>)
                        } else {
                            ReuseFunctions.snackMessage(
                                this.getString(R.string.no_internet_text),
                                constraint_bg
                            )
                        }
                    } else {
                        if (!this.isDestroyed) {
                            ReuseFunctions.snackMessage("Both fields should be same", constraint_bg)
                        }
                    }
                }
            }
        }
    }

    override fun onSuccess(model: Any?) {
        if (model as RecoveryEmailModel? != null) {
Log.d("successsnew","successsnew")
            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    if (model?.response_msg != null) {
                        if (!this.isDestroyed) {
                            ReuseFunctions.snackMessage("" + model?.response_msg, constraint_bg)
                            ReuseFunctions.startNewActivityTaskTop(
                                this,
                                LoginScreen::class.java
                            )
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
                Constants.INVALID_RECOVERY_CODE -> {
                    if (!this.isDestroyed) {
                        ReuseFunctions.snackMessage("Invalid recovery code", constraint_bg)
                    }

                }
            }
        }
    }

    override fun onFailure(error: String) {
        Log.d("codestring4", "" + error)

        if (!this.isDestroyed) {
            ReuseFunctions.showToast(this, error)
        }
    }
}