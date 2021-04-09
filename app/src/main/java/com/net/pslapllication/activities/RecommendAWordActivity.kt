package com.net.pslapllication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.net.pslapllication.R
import kotlinx.android.synthetic.main.toolbaar_layout.*
import kotlinx.android.synthetic.main.toolbaar_layout_menu.*
import kotlinx.android.synthetic.main.toolbaar_layout_menu.opsBackBtn
import java.util.*

class RecommendAWordActivity : AppCompatActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend_a_word)
        settoolBar()
        setBackPressListener()

    }

    private fun settoolBar() {
        txt_title_menu.text = "Recommend a Word"
        opsBackBtn.visibility=View.VISIBLE
     }
    private fun setBackPressListener() {
        opsBackBtn.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.opsBackBtn->{
                super.onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
