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
import com.net.pslapllication.model.favouriteList.Data
import kotlinx.android.synthetic.main.row_video_preview.view.*
import java.net.URLDecoder

class VideoPreviewFavouriteAdapter(var context: Context, var type: String, var name: String, var onVideoSelectedListener: OnVideoSelectedListener) :
    RecyclerView.Adapter<VideoPreviewFavouriteAdapter.ViewHolder>() {
    private var favListFilter = emptyList<Data>()

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
        return favListFilter.size
    }

    override fun onBindViewHolder(holder: VideoPreviewFavouriteAdapter.ViewHolder, position: Int) {

        //set data
        holder.img_arrow.setImageResource(R.drawable.ic_keyboard_arrow_right)


        //set font
        holder.tv_main?.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
        holder.tv_duration?.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
        holder.tv_translate?.typeface =
            ResourcesCompat.getFont(context, R.font.jameelnoorinastaleeqregular)

        //set text
        if (favListFilter[position].dict_video_id != 0 && favListFilter[position].learning_tut_video_id == 0 &&
            favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
        ) {
            holder.tv_main?.text = favListFilter[position].dictionary.english_word
            holder.tv_translate?.text = favListFilter[position].dictionary.urdu_word
            if (!favListFilter[position].dictionary.poster.equals("")) {
                val poster: String = URLDecoder.decode(favListFilter[position].dictionary.poster)
                Glide.with(context).load(poster)
                    .into(holder.imageView_round)
            }
        } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id != 0 &&
            favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
        ) {
            holder.tv_main?.text = favListFilter[position].learningTutorial.title
            // tv_translate?.text = favListFilter[position].dictionary.urdu_word
            if (!favListFilter[position].learningTutorial.poster.equals("")) {
                val poster: String = URLDecoder.decode(favListFilter[position].learningTutorial.poster)
                Glide.with(context).load(poster)
                    .into(holder.imageView_round)
            }
        } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
            favListFilter[position].tut_video_id != 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id == 0
        ) {
            holder.tv_main?.text = favListFilter[position].tutorial.title
            // tv_translate?.text = favListFilter[position].dictionary.urdu_word
            if (!favListFilter[position].tutorial.poster.equals("")) {
                val poster: String = URLDecoder.decode(favListFilter[position].tutorial.poster)
                Glide.with(context).load(poster)
                    .into(holder.imageView_round)
            }
        } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
            favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id != 0 && favListFilter[position].story_video_id == 0
        ) {
            holder.tv_main?.text = favListFilter[position].lesson.title
            // tv_translate?.text = favListFilter[position].dictionary.urdu_word
            if (!favListFilter[position].lesson.poster.equals("")) {
                val poster: String = URLDecoder.decode(favListFilter[position].lesson.poster)
                Glide.with(context).load(poster)
                    .into(holder.imageView_round)
            }
        } else if (favListFilter[position].dict_video_id == 0 && favListFilter[position].learning_tut_video_id == 0 &&
            favListFilter[position].tut_video_id == 0 && favListFilter[position].lesson_video_id == 0 && favListFilter[position].story_video_id != 0
        ) {
            holder.tv_main?.text = favListFilter[position].story.title
            // tv_translate?.text = favListFilter[position].dictionary.urdu_word
            if (!favListFilter[position].story.poster.equals("")) {
                val poster: String = URLDecoder.decode(favListFilter[position].story.poster)
                Glide.with(context).load(poster)
                    .into(holder.imageView_round)
            }
        }


        holder.bind(favListFilter[position], type, name,onVideoSelectedListener)

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val view = view
        val tv_main: TextView? = view.tv_main
        val tv_duration: TextView? = view.tv_duration
        val tv_translate: TextView = view.tv_translate
        val imageView_round: RoundedCornerImageView = view.imageView_round
        val img_arrow: ImageView = view.img_arrow
        fun bind(
            Data: Data,
            type: String,
            name: String,
            onVideoSelectedListener: OnVideoSelectedListener
        ) {
            view.setOnClickListener(View.OnClickListener {
                onVideoSelectedListener.onVideoSelect(Data)
             })
        }
    }

    internal fun setWords(users: List<Data>) {
        this.favListFilter = users
        notifyDataSetChanged()
    }
}
