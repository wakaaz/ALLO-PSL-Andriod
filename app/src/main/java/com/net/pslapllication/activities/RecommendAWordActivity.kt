package com.net.pslapllication.activities

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.net.pslapllication.R
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.model.forgotpassword.RecoveryEmailModel
import com.net.pslapllication.model.preferences.PreferenceMainModel
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_main_listing_new.*
import kotlinx.android.synthetic.main.activity_recommend_a_word.*
import kotlinx.android.synthetic.main.toolbaar_layout_menu.*
import retrofit2.Call

class RecommendAWordActivity : BaseActivity(),View.OnClickListener, RetrofitResponseListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend_a_word)
        settoolBar()
        setBackPressListener()
        setListner();
    }
    private fun setListner(){
        tv_submit.setOnClickListener(this)
    }
    private fun settoolBar() {
        txt_title_menu.text = "Recommend a Word"
        opsBackBtn.visibility=View.VISIBLE
        imgbtn_menu.visibility =  View.GONE
     }
    private fun setBackPressListener() {
        opsBackBtn.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.opsBackBtn -> {
                super.onBackPressed()
            }

            R.id.tv_submit -> {
                submitProcess()
            }
        }
    }
    private fun submitProcess(){

        if(tf_name.getText().toString().isEmpty()){
            tf_name.setError("Enter valid name")
        }else if(!isValidEmail(tf_email.getText().toString())){
            tf_email.setError("Enter valid email")

        }else if(tv_recommend_text.getText().toString().isEmpty()){
            tv_recommend_text.setError("Enter valid word")

        }else{
            if(isConnected){
                var name = tf_name.getText().toString()
                var email = tf_email.getText().toString()
                var word = tv_recommend_text.getText().toString()
                val call = ApiCallClass.apiService.postRecomendation( SharedPreferenceClass.getInstance(this)?.getSession().toString(),
                        SharedPreferenceClass.getInstance(this)?.getUserType().toString(),name,email,word)
                ApiCallClass.retrofitCall(this, call as Call<Any>)
            }else{
                ReuseFunctions.snackMessage(main_layout, "Nothing to Sort")

            }
        }

    }
    override fun onBackPressed() {
        super.onBackPressed()
    }


    fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) false else Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    override fun onSuccess(model: Any?) {
        if (model as RecoveryEmailModel? != null) {
            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    tf_name.setText("")
                    tf_email.setText("")
                    tv_recommend_text.setText("")
                    ReuseFunctions.showToast(this, "Thank you for recommendation")
                }
                Constants.SESSION_ERROR_CODE -> {

                }
                Constants.FIELD_VALIDATION_ERROR_CODE -> {
                    //no field here

                }
            }

        }
    }

    override fun onFailure(error: String) {
        ReuseFunctions.showToast(this, error)
        Log.d("errormessage", "" + error)
    }


}
