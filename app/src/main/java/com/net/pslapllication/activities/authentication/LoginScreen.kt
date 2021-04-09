package com.net.pslapllication.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.net.pslapllication.R
import com.net.pslapllication.activities.BaseActivity
import com.net.pslapllication.activities.IntroSliderActivity
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.model.login.LoginMainModel
import com.net.pslapllication.model.login.LoginParamModel
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import com.net.pslapllication.util.ValidationChecks
import com.net.pslapllication.worker.AllWordsWorker
import kotlinx.android.synthetic.main.activity_login_screen.*
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*


class LoginScreen : BaseActivity(), View.OnClickListener, RetrofitResponseListener {

    private var callbackManager: CallbackManager? = null
    private val RC_SIGN_IN: Int = 200
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val EMAIL = "email"
    private var mAuth: FirebaseAuth? = null
    private var isInternetConnected: Boolean = true
    lateinit var apiService: ApiService
    private var userType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(this.applicationContext)
        setContentView(R.layout.activity_login_screen)
        mAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        setClickListeners()
        setRetrofitListener()
        //addFbSignIn()
        //addGoogleSignIn()
    }
    //splash sy setsession remove krna h

    private fun setRetrofitListener() {
        apiService = RetrofitClientInstance.getClient()!!.create(ApiService::class.java)

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            isInternetConnected = isConnected
            //constraintInternet.visibility = View.GONE
        } else {
            if (!this.isDestroyed) {
                //  constraintInternet.visibility = View.VISIBLE
                isInternetConnected = isConnected
                ReuseFunctions.snackMessage(
                    this.getString(R.string.no_internet_text),
                    constraint_bg
                )
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }


    private fun addGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    private fun addFbSignIn() {
        val permissionNeeds = Arrays.asList(
            "user_photos", "email",
            "user_birthday", "public_profile", "AccessToken"
        )
        login_button.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    println("onSuccess")
                    val accessToken = loginResult.accessToken
                        .token
                    handleFacebookAccessToken(loginResult.accessToken)

                    Log.i("accessToken", accessToken)

                }

                override fun onCancel() {
                    println("onCancel")
                }

                override fun onError(exception: FacebookException) {
                    println("onError")
                    Log.v("LoginActivity", exception.cause.toString())
                }
            })
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            Log.w(
                "FragmentActivity.TAG1",
                "update" + account.displayName + account.email + account.photoUrl
            )
        }
    }

    private fun setClickListeners() {
        tv_login_btn.setOnClickListener(this)
        tv_signup.setOnClickListener(this)
        layout_google.setOnClickListener(this)
        login_fb_dummy.setOnClickListener(this)
        constraint_skip.setOnClickListener(this)
        tv_forgrt_password.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_login_btn -> {
                userType = Constants.USERTYPE_LOGIN
                SharedPreferenceClass.getInstance(this)
                    ?.setUserType(userType)
                setLogin()
            }
            R.id.tv_forgrt_password -> {
                ReuseFunctions.startNewActivity(this, ForgotPasswordStep1Activity::class.java)

            }
            R.id.tv_signup -> {
                ReuseFunctions.startNewActivity(this, SignUpActivity::class.java)
                finish()
            }
            R.id.layout_google -> {
                // signIn()
            }
            R.id.constraint_skip -> {
                userType = Constants.USERTYPE_GUEST
                SharedPreferenceClass.getInstance(this)
                    ?.setUserType(userType)
                setGuestLogin()
                //ReuseFunctions.startNewActivity(this, IntroSliderActivity::class.java)
                // finish()
            }
            R.id.login_fb_dummy -> {
                // login_button.performClick()
            }

        }
    }

    private fun setGuestLogin() {
        if (isInternetConnected) {

            val call = ApiCallClass.apiService.getLogInGuest()
            ApiCallClass.retrofitCall(this, call as Call<Any>)
        } else {
            if (!this.isDestroyed) {
                ReuseFunctions.snackMessage(
                    this.getString(R.string.no_internet_text),
                    constraint_bg
                )
            }
        }
    }

    private fun setLogin() {
        if (isInternetConnected) {
            if (ValidationChecks.emptyCheck(tv_user_id_text, "email required") &&
                ValidationChecks.emptyCheck(tv_password_text, "password required")
            ) {
                if (ValidationChecks.isValidEmail(tv_user_id_text.text.toString())) {
                    if (ValidationChecks.isPasswordValidLength(
                            tv_password_text,
                            tv_password_text.text.toString(),
                            "Required at least 6 digits"
                        )
                    ) {
                        val loginParamModel = LoginParamModel()
                        loginParamModel.email = tv_user_id_text.text.toString()
                        loginParamModel.password = tv_password_text.text.toString()
                        val call = ApiCallClass.apiService.getLogIn(loginParamModel)
                        ApiCallClass.retrofitCall(this, call as Call<Any>)
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
        } else {
            if (!this.isDestroyed) {
                ReuseFunctions.snackMessage(
                    this.getString(R.string.no_internet_text),
                    constraint_bg
                )
            }
        }
    }

    private fun signIn() {
        val signInIntent: Intent? = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            callbackManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account =
                completedTask.getResult(ApiException::class.java)

            updateUI(account);
            // Signed in successfully, show authenticated UI.
            /* Toast.makeText(
                 this,
                 "Success" + account?.displayName + account?.email + account?.photoUrl,
                 Toast.LENGTH_SHORT
             ).show()*/
            Log.w(
                "FragmentActivity.TAG",
                "Success" + account?.displayName + account?.email + account?.photoUrl
            )

            //updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("FragmentActivity.TAG", "signInResult:failed code=" + e.statusCode)
            //Toast.makeText(this, "fail"+ e.printStackTrace(), Toast.LENGTH_SHORT).show()

            updateUI(null)
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        /* val currentUser = mAuth?.currentUser
         updateUIFB(currentUser)*/
    }

    private fun updateUIFB(currentUser: FirebaseUser?) {
        /*Toast.makeText(
            this, "success" + currentUser?.displayName +
                    currentUser?.email + currentUser?.phoneNumber + currentUser?.photoUrl
            , Toast.LENGTH_SHORT
        ).show()*/
        Log.d(
            "currentUser", "success" + currentUser?.displayName +
                    currentUser?.email + currentUser?.phoneNumber + currentUser?.photoUrl
        )
    }

    private fun handleFacebookAccessToken(token: AccessToken?) {
        Log.d("TAGFB", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token?.token.toString())
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAGFB", "signInWithCredential:success")
                    val user = mAuth?.currentUser
                    updateUIFB(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAGFB", "signInWithCredential:failure", task.exception)
                    /*Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    updateUI(null)
                }

                // ...
            }
    }

    override fun onSuccess(model: Any?) {
        if (model as LoginMainModel? != null) {

            when (model?.code) {
                Constants.SUCCESS_CODE -> {

                    SharedPreferenceClass.getInstance(this)?.setSession(model?.object1!!.session)
                    if (SharedPreferenceClass.getInstance(this)?.getUserType()!! == Constants.USERTYPE_LOGIN) {
                        SharedPreferenceClass.getInstance(this)?.setisLogin(true)

                        SharedPreferenceClass.getInstance(this)
                            ?.setFirstName(model?.object1!!.first_name)

                        if (model?.object1!!.last_name != null &&
                            model?.object1.last_name != "null"
                        ) {
                            SharedPreferenceClass.getInstance(this)
                                ?.setlast_name(model?.object1!!.last_name)
                        }

                        SharedPreferenceClass.getInstance(this)
                            ?.setemail(model?.object1!!.email)

                        SharedPreferenceClass.getInstance(this)
                            ?.setupdated_at(model?.object1!!.updated_at)
                        SharedPreferenceClass.getInstance(this)
                            ?.setcreated_at(model?.object1!!.created_at)
                    } else if (SharedPreferenceClass.getInstance(this)?.getUserType()!! == Constants.USERTYPE_GUEST) {
                        SharedPreferenceClass.getInstance(this)
                            ?.setFirstName(Constants.USERTYPE_GUEST)
                    }

                    SharedPreferenceClass.getInstance(this)?.setid(model?.object1!!.id)

                    val currentDate = Calendar.getInstance().time
                    val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val formattedDate: String = df.format(currentDate)
                    SharedPreferenceClass.getInstance(this)?.setLastDate(formattedDate)
                    requestWorker()
                    ReuseFunctions.startNewActivity(this, IntroSliderActivity::class.java)
                }
                Constants.EMAIL_NOT_FOUND_ERROR -> {
                    if (!this.isDestroyed) {

                        ReuseFunctions.snackMessage(
                            model?.response_msg.toString(),
                            constraint_bg
                        )
                    }
                }
                Constants.AUTHENTICATION_ERROR -> {
                    if (!this.isDestroyed) {
                        ReuseFunctions.snackMessage(
                            model?.response_msg.toString(),
                            constraint_bg
                        )
                    }
                }
            }
        }
    }

    override fun onFailure(error: String) {
        if (!this.isDestroyed) {
            ReuseFunctions.snackMessage(
                error,
                constraint_bg
            )
        }
    }

    private fun requestWorker() {
        if (isInternetConnected) {
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
