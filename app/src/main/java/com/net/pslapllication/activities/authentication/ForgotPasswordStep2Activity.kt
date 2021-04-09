package com.net.pslapllication.activities.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_forgot_password_step2.*
import kotlinx.android.synthetic.main.activity_forgot_password_step2.*
import kotlinx.android.synthetic.main.activity_forgot_password_step2.constraint_bg
import kotlinx.android.synthetic.main.activity_forgot_password_step3.*
import retrofit2.Call

class ForgotPasswordStep2Activity : BaseActivity(), View.OnClickListener,
    RetrofitResponseListener {
    var isInternetConnected: Boolean = false
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_step2)
        setListener()

    }

    private fun setListener() {
        tv_next_code.setOnClickListener(this)
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        isInternetConnected = isConnected
        super.onNetworkConnectionChanged(isConnected)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_next_code -> {
                if (ValidationChecks.emptyCheck(tv_recovery_code, "Code Required") &&
                    ValidationChecks.isPasswordValidLength(
                        tv_recovery_code,
                        tv_recovery_code.text.toString(),
                        "Required at least 6 digits"
                    )
                ) {
                    if (isInternetConnected) {
                        var call = ApiCallClass.apiService.getConfirmResetPasswordCode(
                            tv_recovery_code.text.toString()
                        )
                        ApiCallClass.retrofitCall(this, call as Call<Any>)
                    } else {
                        ReuseFunctions.snackMessage(
                            this.getString(R.string.no_internet_text),
                            constraint_bg
                        )
                    }

                }
            }
        }
    }

    override fun onSuccess(model: Any?) {
        if (model as RecoveryEmailModel? != null) {

            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    if (model?.response_msg != null) {
                        if (!this.isDestroyed) {
                            ReuseFunctions.snackMessage("" + model?.response_msg, constraint_bg)
                            val intent = Intent(this, ForgotPasswordStep3Activity::class.java)
                            intent.putExtra("CODE", tv_recovery_code.text.toString())
                            startActivity(intent)
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