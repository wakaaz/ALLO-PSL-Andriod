package com.net.pslapllication.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.net.pslapllication.R
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.interfaces.onQualityChangSelectedListener

class VideoQualityOptionAdapter(
    private val context: Context,
    var type: String,
    var currentQuality: Int,
    var onQualityChangSelectedListener: onQualityChangSelectedListener

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var diclist = emptyList<DictionaryListModel>()
    var row_index = currentQuality


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder1(
            LayoutInflater.from(context)
                .inflate(R.layout.bottom_layout_video_qualityoption, parent, false)
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as ViewHolder1).bind(position)

        if (row_index == position) {
            diclist[position].isSelected = true
            holder.Image_quality_video.visibility = View.VISIBLE
            holder.tv_video_quality_option.setTextColor(context.resources.getColor(R.color.colorPrimaryDark))

        } else {
            diclist[position].isSelected = false
            holder.Image_quality_video.visibility = View.INVISIBLE
            holder.tv_video_quality_option.setTextColor(context.resources.getColor(R.color.black))

        }
    }

    override fun getItemCount(): Int {
        return diclist.size
    }

    private inner class ViewHolder1 internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tv_video_quality_option: TextView =
            itemView.findViewById(R.id.tv_video_quality_option)
        var Image_quality_video: ImageView =
            itemView.findViewById(R.id.Image_quality_video)
        var constraint_quality: ConstraintLayout =
            itemView.findViewById(R.id.constraint_quality)

        internal fun bind(position: Int) {
            tv_video_quality_option.typeface = ResourcesCompat.getFont(context, R.font.lato_medium)
            tv_video_quality_option.text = diclist[position].wordName
            if (currentQuality == diclist[position].id) {
                diclist[position].isSelected = true
                Image_quality_video.visibility = View.VISIBLE
                tv_video_quality_option.setTextColor(context.resources.getColor(R.color.colorPrimaryDark))
            }
            itemView.setOnClickListener {
                row_index = position
                notifyDataSetChanged()
                onQualityChangSelectedListener.onQualityChangeSelected(diclist[position].id.toString())
            }
        }
    }

    internal fun setWords(users: List<DictionaryListModel>) {
        this.diclist = users
        notifyDataSetChanged()
    }
}
