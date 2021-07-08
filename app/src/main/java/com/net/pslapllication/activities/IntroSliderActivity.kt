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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.net.pslapllication.R
import com.net.pslapllication.util.SharedPreferenceClass
import kotlinx.android.synthetic.main.activity_intro_slider.*


class IntroSliderActivity : AppCompatActivity() {
    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var dotsLayout: LinearLayout? = null
    private var layouts: IntArray? = null

         override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContentView(R.layout.activity_intro_slider)

            dotsLayout = findViewById<LinearLayout>(R.id.layoutDots)
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
    }