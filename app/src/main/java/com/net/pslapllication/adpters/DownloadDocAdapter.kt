package com.net.pslapllication.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.recyclerview.widget.RecyclerView
import com.net.pslapllication.R
import com.net.pslapllication.model.VideoDocuments

class DownloadDocAdapter (var c: Context, var teachers: List<VideoDocuments>) : RecyclerView.Adapter<DownloadDocAdapter.MyHolder>() {
    var checkedTeachers = ArrayList<VideoDocuments>()

    //VIEWHOLDER IS INITIALIZED
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.nav_row_select_lesson, null)
        return MyHolder(v)
    }

    //DATA IS BOUND TO VIEWS
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val teacher = teachers[position]

        holder.myCheckBox.text = teacher.name
        holder.myCheckBox.isChecked = teacher.isSelected

        holder.setItemClickListener(object : ItemClickListener {
            override fun onItemClick(v: View, pos: Int) {
                val myCheckBox = v as AppCompatCheckedTextView
                val currentTeacher = teachers[pos]

                if (myCheckBox.isChecked) {
                    myCheckBox.isChecked = false
                    currentTeacher.isSelected = false
                    checkedTeachers.remove(currentTeacher)
                } else if (!myCheckBox.isChecked) {
                    myCheckBox.isChecked = true
                    currentTeacher.isSelected = true
                    checkedTeachers.add(currentTeacher)
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return teachers.size
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {


        var myCheckBox: CheckedTextView

        lateinit var myItemClickListener: ItemClickListener

        init {


            myCheckBox = itemView.findViewById(R.id.checked_text_view)

            myCheckBox.setOnClickListener(this)
        }

        fun setItemClickListener(ic: ItemClickListener) {
            this.myItemClickListener = ic
        }

        override fun onClick(v: View) {
            this.myItemClickListener.onItemClick(v, layoutPosition)
        }


    }
    interface ItemClickListener {

        fun onItemClick(v: View, pos: Int)
    }
}