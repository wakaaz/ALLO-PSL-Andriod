package com.net.pslapllication.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.net.pslapllication.R
import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.model.login.LoginMainModel
import com.net.pslapllication.reetrofit.ApiCallClass
import com.net.pslapllication.reetrofit.ApiService
import com.net.pslapllication.reetrofit.RetrofitClientInstance
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import com.net.pslapllication.util.SharedPreferenceClass
import com.net.pslapllication.worker.AllWordsWorker
import kotlinx.android.synthetic.main.activity_intro_slider.*
import kotlinx.android.synthetic.main.activity_login_screen.*
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*


class IntroSliderActivity : BaseActivity() , RetrofitResponseListener {
    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var dotsLayout: LinearLayout? = null
    private var layouts: IntArray? = null
    private var isInternetConnected: Boolean = true
    lateinit var apiService: ApiService
    lateinit var constraint_bg:RelativeLayout
         override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContentView(R.layout.activity_intro_slider)

            dotsLayout = findViewById<LinearLayout>(R.id.layoutDots)
             constraint_bg = findViewById(R.id.constraint_bg)
            // layouts of all intro sliders
            layouts = intArrayOf(R.layout.slide1, R.layout.slide2, R.layout.slide3)

            // adding bottom dots
            addBottomDots(0)

            // making notification bar transparent
            //changeStatusBarColor()

            myViewPagerAdapter = MyViewPagerAdapter()
            view_pager!!.adapter = myViewPagerAdapter
            view_pager!!.addOnPageChangeListener(viewPagerPageChangeListener)

            btn_skip!!.setOnClickListener { launchHomeScreen() }

            btn_next!!.setOnClickListener(View.OnClickListener {
                // checking for last page & if last page home screen will be launched
                val current = getItem(+1)
                if (current < layouts!!.size) {
                    // move to next screen
                    view_pager!!.currentItem = current
                } else {
                    launchHomeScreen()
                }
            })

            val userType = Constants.USERTYPE_GUEST
             SharedPreferenceClass.getInstance(this)
                 ?.setUserType(userType)
             setGuestLogin()
        }
/*
         private fun addBottomDots(currentPage: Int) {
            var dots: Array<TextView?> = arrayOfNulls(layouts!!.size)

            dotsLayout!!.removeAllViews()
            for (i in dots.indices) {
                dots[i] = TextView(this)
                dots[i]!!.text = Html.fromHtml("&#8226;")
                dots[i]!!.textSize = 35f
                dots[i]!!.setTextColor(resources.getIntArray(R.array.array_dot_inactive)[currentPage])
                dotsLayout!!.addView(dots[i])
            }

            if (dots.isNotEmpty())
                dots[currentPage]!!.setTextColor(resources.getColor(R.color.red))
        }
*/

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


  fun addBottomDots(currentPage: Int): Unit {
    val dots = arrayOfNulls<TextView>(layouts!!.size)
    val colorsActive = resources.getIntArray(R.array.array_dot_active)
    val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
    dotsLayout!!.removeAllViews()
    for (i in dots.indices) {
        dots[i] = TextView(this)
        dots[i]!!.text = Html.fromHtml("&#8226;")
        dots[i]!!.textSize = 30f
        dots[i]!!.setTextColor(colorsInactive[currentPage])
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(10, 10, 10, 10)
        dots[i]!!.layoutParams = params
        dotsLayout!!.addView(dots[i])
    }
    if (dots.size > 0) dots[currentPage]!!.setTextColor(colorsActive[currentPage])
    //dots[currentPage].setTextSize(40);
}

         private fun getItem(i: Int): Int {
            return view_pager!!.currentItem + i
        }

        private fun launchHomeScreen() {
             startActivity(Intent(this, HomeActivity::class.java))
            finish()
            SharedPreferenceClass.getInstance(this)?.setIntroLaunch(true)
        }

        //	viewpager change listener
        private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                myViewPagerAdapter?.getCount()?.minus(1);
                addBottomDots(position)
                // changing the next button text 'NEXT' / 'GOT IT'
                if (position == layouts!!.size - 1) {
                    // last page. make button text to GOT IT
                    tv_next!!.text ="Start"
                    btn_skip!!.visibility = View.GONE
                } else {
                    // still pages are left
                    tv_next!!.text =  "Next"
                    //btn_skip!!.visibility = View.VISIBLE
                }
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            }

            override fun onPageScrollStateChanged(arg0: Int) {
            }
        }

        /**
         * Making notification bar transparent
         */
        private fun changeStatusBarColor() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
        }

        /**
         * View pager adapter
         */
        inner class MyViewPagerAdapter : PagerAdapter() {
            private var layoutInflater: LayoutInflater? = null

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view = layoutInflater!!.inflate(layouts!![position], container, false)
                container.addView(view)
                return view
            }

            override fun getCount(): Int {
                return layouts!!.size
            }

            override fun isViewFromObject(view: View, obj: Any): Boolean {
                return view === obj
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                val view = `object` as View
                container.removeView(view)
            }
        }


    private fun setGuestLogin() {
        if (isInternetConnected) {

            val call = ApiCallClass.apiService.getLogInGuest("")
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

    override fun onSuccess(model: Any?) {
        if (model as LoginMainModel? != null) {

            when (model?.code) {
                Constants.SUCCESS_CODE -> {

                    SharedPreferenceClass.getInstance(this)?.setSession(model?.object1!!.session)
                    if (SharedPreferenceClass.getInstance(this)?.getUserType()!! == Constants.USERTYPE_GUEST) {
                        SharedPreferenceClass.getInstance(this)
                            ?.setFirstName(Constants.USERTYPE_GUEST)
                    }

                    SharedPreferenceClass.getInstance(this)?.setid(model?.object1!!.id)

                    val currentDate = Calendar.getInstance().time
                    val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val formattedDate: String = df.format(currentDate)
                    SharedPreferenceClass.getInstance(this)?.setLastDate(formattedDate)
                    requestWorker()
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