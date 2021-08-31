package com.net.pslapllication.activities


import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.net.pslapllication.R
import com.net.pslapllication.activities.authentication.LoginScreen
import com.net.pslapllication.fragments.DownloadFragment
import com.net.pslapllication.fragments.FavouriteFragment
import com.net.pslapllication.fragments.HomeFragmentNew
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.model.forgotpassword.RecoveryEmailModel
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_forgot_password_step2.*
import kotlinx.android.synthetic.main.activity_home.*
import retrofit2.Call
import java.util.*
import android.content.Intent
import android.net.Uri


class HomeActivity : BaseActivity(), RetrofitResponseListener {
    public var isInternetConnected = false
    var mMenuItem: Menu? = null
    var mBottomNavigationView: BottomNavigationView.OnNavigationItemSelectedListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        /* */
        /************network connection*************//*
        val networkConnection = NetworkConnection(applicationContext,this)
        networkConnection.observe(this, Observer { isConnected ->
            isNetworkAvailable = isConnected
        })
        */
        /************network connection*************//*
*/
        setUpNavigationView()
        setBottomNavigation()
        checkIntent()
    }

    private fun checkIntent() {

        if (intent != null && intent.getStringExtra("TYPE") != null
            && intent.getStringExtra("TYPE").equals("NOINTERNET")
        ) {
            bottom_navigation_view.selectedItemId = R.id.downloads
        } else {
            replaceFragment(HomeFragmentNew())
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            isInternetConnected = isConnected
        } else {
            if (!this.isDestroyed) {
                ReuseFunctions.snackMessage(
                    root_layout,
                    this.getString(R.string.no_internet_text)
                )
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }

    private fun setUpNavigationView() {
        val navigationView =
            NavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.nav_profile -> {
                        ReuseFunctions.startNewActivity(this, ProfileActivity::class.java)
                        /*if(drawer_layout.isDrawerOpen(Gravity.RIGHT)){
                            drawer_layout.closeDrawer(Gravity.RIGHT)
                        }*/
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.nav_psl -> {
                        /*if(drawer_layout.isDrawerOpen(Gravity.RIGHT)){
                            drawer_layout.closeDrawer(Gravity.RIGHT)
                        }*/
                        openUrl()
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.nav_recommend_in -> {
                        ReuseFunctions.startNewActivity(this, RecommendAWordActivity::class.java)
                        /*if(drawer_layout.isDrawerOpen(Gravity.RIGHT)){
                            drawer_layout.closeDrawer(Gravity.RIGHT)
                        }*/
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.nav_signout -> {
                        if (isInternetConnected) {
                            var call = ApiCallClass.apiService.getLogout(
                                SharedPreferenceClass.getInstance(this)!!.getSession()
                            )
                            ApiCallClass.retrofitCall(this, call as Call<Any>)
                        } else {
                            ReuseFunctions.snackMessage(
                                this.getString(R.string.no_internet_text),
                                constraint_bg
                            )
                        }
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }
        nav_view.setNavigationItemSelectedListener(navigationView)
        nav_view.itemIconTintList = null

        val headerView = nav_view.getHeaderView(0)
        val name =
            headerView.findViewById<View>(R.id.name) as TextView
        name.text =
            SharedPreferenceClass.getInstance(this)?.getFirstName()?.toUpperCase(Locale.ROOT)


    }

    private fun setBottomNavigation() {
        bottom_navigation_view.itemTextAppearanceActive = R.style.BottomNavigationView

        mBottomNavigationView =
            BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.home -> {
                        replaceFragment(HomeFragmentNew())

                        bottom_navigation_view.itemTextColor =
                            ContextCompat.getColorStateList(this, R.color.color_bnv1)
                        bottom_navigation_view.itemIconTintList = null
                        bottom_navigation_view.itemTextAppearanceActive = R.style.BottomNavigationView
                        return@OnNavigationItemSelectedListener true

                    }
                    R.id.favourite -> {
                        replaceFragment(FavouriteFragment())
                        bottom_navigation_view.itemIconTintList =
                            ContextCompat.getColorStateList(this, R.color.color_bnv1)
                        bottom_navigation_view.itemIconTintList = null
                        bottom_navigation_view.itemTextAppearanceActive = R.style.BottomNavigationView

                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.downloads -> {
                        replaceFragment(DownloadFragment())
                        bottom_navigation_view.itemTextColor =
                            ContextCompat.getColorStateList(this, R.color.color_bnv1)
                        bottom_navigation_view.itemIconTintList = null
                        bottom_navigation_view.itemTextAppearanceActive = R.style.BottomNavigationView

                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.menutab -> {
                        drawer_layout.openDrawer(Gravity.RIGHT)
                        //bottom_navigation_view.itemTextColor =
                         //   ContextCompat.getColorStateList(this, R.color.color_bnv3)
                       // bottom_navigation_view.itemIconTintList = null
                        return@OnNavigationItemSelectedListener false
                    }
                }
                false
            }
        bottom_navigation_view.setOnNavigationItemSelectedListener(mBottomNavigationView)
        bottom_navigation_view.itemIconTintList = null


    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment, fragment.javaClass.simpleName)
            .commit()
    }


    override fun onResume() {
        super.onResume()

    }

    override fun onBackPressed() {
        if (drawer_layout != null && drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
            drawer_layout.closeDrawer(Gravity.RIGHT);
        } else {
            cancelDialog()
        }

    }

    private fun cancelDialog() {
        val exitDialog =
            AlertDialog.Builder(this)
        val eView: View = layoutInflater.inflate(R.layout.cancel_dialog_new, null)
        val btn_yes: Button
        val btn_cancel: Button
        btn_cancel = eView.findViewById(R.id.btn_cancel)
        btn_yes = eView.findViewById(R.id.btn_yes)
        exitDialog.setView(eView)
        val dialog = exitDialog.create()
        btn_cancel.setOnClickListener { dialog.cancel() }
        btn_yes.setOnClickListener {
            dialog.cancel()
            finish()
        }
        dialog.show()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onSuccess(model: Any?) {
        if (model as RecoveryEmailModel? != null) {

            when (model?.code) {
                Constants.SUCCESS_CODE -> {
                    if (model?.response_msg != null) {
                        if (!this.isDestroyed) {
                            ReuseFunctions.snackMessage("" + model?.response_msg, constraint_bg)
                            ReuseFunctions.startNewActivityTaskTop(
                                this,
                                LoginScreen::class.java
                            )
                            SharedPreferenceClass.getInstance(this)?.setisLogin(false)
                            SharedPreferenceClass.getInstance(this)?.setIntroLaunch(false)!!

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

    fun openUrl(){
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.psl.org.pk/about/about-psl"))
        startActivity(browserIntent)
    }
}
