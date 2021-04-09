package com.net.pslapllication.adpters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.comix.rounded.RoundedCornerImageView
import com.net.pslapllication.R
import com.net.pslapllication.interfaces.OnVideoSelectedListener
import com.net.pslapllication.model.dictionary.DictionaryData
import kotlinx.android.synthetic.main.row_video_preview.view.*
import java.net.URLDecoder

class VideoPreviewAdapter(var context: Context, var type: String, var name: String,var onVideoSelectedListener: OnVideoSelectedListener) :
    RecyclerView.Adapter<VideoPreviewAdapter.ViewHolder>() {
    private var diclist = emptyList<DictionaryData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_video_preview,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return diclist.size
    }

    override fun onBindViewHolder(holder: VideoPreviewAdapter.ViewHolder, position: Int) {

        //set data
        holder.img_arrow.setImageResource(R.drawable.ic_keyboard_arrow_right)
        holder.tv_main?.text = diclist[position].english_word
        holder.tv_translate.text = " "+diclist[position].urdu_word

        //set font
        holder.tv_main?.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
        holder.tv_duration?.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
        holder.tv_translate?.typeface =
            ResourcesCompat.getFont(context, R.font.jameelnoorinastaleeqregular)
        if (diclist[position].poster.isNotEmpty()) {
            var poster: String = URLDecoder.decode(diclist[position].poster)
            Glide.with(context).load(poster)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        p0: GlideException?,
                        p1: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        p3: Boolean
                    ): Boolean {
                       var errorString =  p0?.localizedMessage
                        return false
                     }
                    override fun onResourceReady(p0: Drawable?, p1: Any?,target: com.bumptech.glide.request.target.Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean {
                         //do something when picture already loaded
                        return false
                    }
                })
                .into(holder.imageView_round)
        }
        holder.bind(diclist[position], type, name,onVideoSelectedListener)

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val view = view
        val tv_main: TextView? = view.tv_main
        val tv_duration: TextView? = view.tv_duration
        val tv_translate: TextView = view.tv_translate
        val imageView_round: RoundedCornerImageView = view.imageView_round
        val img_arrow: ImageView = view.img_arrow
        fun bind(
            dictionaryData: DictionaryData,
            type: String,
            name: String,
            onVideoSelectedListener: OnVideoSelectedListener
        ) {
            view.setOnClickListener(View.OnClickListener {
                onVideoSelectedListener.onVideoSelect(dictionaryData)
             })
        }
    }

    internal fun setWords(users: List<DictionaryData>) {
        this.diclist = users
        notifyDataSetChanged()
    }
}
