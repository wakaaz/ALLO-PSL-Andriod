package com.net.pslapllication.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.VideoPreviewActivity
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
 import kotlinx.android.synthetic.main.row_recent.view.*

class RecentAdapter(var context: Context,var type: String) :
    RecyclerView.Adapter<RecentAdapter.ViewHolder>() {
    private var diclist = emptyList<DictionaryListModel>() // Cached copy of words

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_recent,
                parent,
                false))
    }

    override fun getItemCount(): Int {
        return diclist.size
    }

    override fun onBindViewHolder(holder: RecentAdapter.ViewHolder, position: Int) {
        holder.tv_word?.setTypeface(ResourcesCompat.getFont(context, R.font.lato_regular))
        holder.tv_word?.text = diclist[position].wordName
        //Glide.with(context).load(diclist[position].wordTyhumb).into(holder.imageViewRecent);

        holder.bind(diclist[position],type);

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val view = view
        val tv_word: TextView? = view.tv_word
        val imageViewRecent: ImageView = view.imageViewRecent
        val img_btn_more: ImageButton = view.img_btn_more
        fun bind(
            dictionaryListModel: DictionaryListModel,
           type: String

        ) {

            view.setOnClickListener(View.OnClickListener {

                if (type == Constants.TYPE_RECENT){
                    ReuseFunctions.startNewActivityTask(view.context, VideoPreviewActivity::class.java)
                }

            })

        }
    }


    internal fun setWords(users: List<DictionaryListModel>) {
        this.diclist = users
        notifyDataSetChanged()
    }
}
