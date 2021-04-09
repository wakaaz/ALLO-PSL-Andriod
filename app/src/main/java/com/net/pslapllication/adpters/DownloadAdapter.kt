package com.net.pslapllication.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.VideoPreviewActivity
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.row_downloading_videos.view.*
import java.net.URLDecoder


class DownloadAdapter(var context: Context, var type: String) :
    RecyclerView.Adapter<DownloadAdapter.ViewHolder>() {
    var list: MutableList<DictionaryData>? = null
    var listStory: MutableList<StoryData>? = null

    var hashMap: HashMap<Long, DictionaryData> = HashMap<Long, DictionaryData>()
    var hashMapStoryData: HashMap<Long, StoryData> = HashMap<Long, StoryData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_downloading_videos,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        var size : Int = 0
        if (type == Constants.TYPE_STORIES){
            size = hashMap.size
        }
        else if (type == Constants.TYPE_STORIES){
            size = hashMapStoryData.size
        }
        return size
    }

    override fun onBindViewHolder(holder: DownloadAdapter.ViewHolder, position: Int) {

        holder.tvVideoName?.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)

        if (type == Constants.TYPE_STORIES){
            holder.tvVideoName?.text = listStory!![position].filename
            holder.tvDownload?.text = "Downloading Video..." + listStory!![position].downloadprogress + "%"
            holder.progressBar1!!.progress = listStory!![position].downloadprogress
            holder.mainConstraints.setOnClickListener{
                setpopUp(context, holder.mainConstraints, position)
            }
            try {
                Glide.with(context).load(URLDecoder.decode(listStory!![position].poster))
                    .into(holder.imageViewDownload);
            } catch (e: Exception) {
                e.printStackTrace()
            }
            holder.bind( type)
        }
        else if (type == Constants.TYPE_STORIES){
            holder.tvVideoName?.text = list!![position].filename
            holder.tvDownload?.text = "Downloading Video..." + list!![position].downloadprogress + "%"
            holder.progressBar1!!.progress = list!![position].downloadprogress
            holder.mainConstraints.setOnClickListener{
                setpopUp(context, holder.mainConstraints, position)
            }
            try {
                Glide.with(context).load(URLDecoder.decode(list!![position].poster))
                    .into(holder.imageViewDownload);
            } catch (e: Exception) {
                e.printStackTrace()
            }
            holder.bind( type)
            //holder.bind(list!![position], type)
        }



    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val tvVideoName: TextView? = view.tvVideoName
        val tvDownload: TextView? = view.tvDownload
        val progressBar1: ProgressBar? = view.progressBar1
        val imageViewDownload: ImageView = view.imageViewDownload
          val mainConstraints: ConstraintLayout = view.mainConstraints
        private val imgbtn_menu: ImageButton = view.imgbtn_menu

        fun bind(

            type: String
        ) {
            imgbtn_menu.setOnClickListener(this)
            mainConstraints.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.imgbtn_menu -> {
                    setpopUp(v.context, imgbtn_menu, adapterPosition)
                }
                R.id.mainConstraints -> {
                    setpopUp(v.context, imgbtn_menu, adapterPosition)
                }
            }
        }

        private fun setpopUp(
            context: Context,
            imgbtnMenu: ImageButton,
            position: Int
        ) {

            val popupMenu =
                PopupMenu(context, imgbtnMenu, R.style.PopupMenu)

            popupMenu.menuInflater.inflate(R.menu.popupmenu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.cancel -> {
                         //ReuseFunctions.showToast(context,"" +  list!![position].downloadReference)
                        ReuseFunctions.showToast(context, "" + position + "")
                    }

                }
                true
            }
            popupMenu.show()
        }
    }


    fun setWords(hashMap: HashMap<Long, DictionaryData>) {
        this.hashMap = hashMap
        list = hashMap.values.toMutableList()
        notifyDataSetChanged()
    }
    fun setStoryData(hashMapStoryData:HashMap<Long, StoryData>) {
        this.hashMapStoryData = hashMapStoryData
        listStory = hashMapStoryData.values.toMutableList()
        notifyDataSetChanged()
    }
    private fun setpopUp(
        context: Context,
        imgbtnMenu: ConstraintLayout,
        position: Int
    ) {

        val popupMenu =
            PopupMenu(context, imgbtnMenu, R.style.PopupMenu)

        popupMenu.menuInflater.inflate(R.menu.popupmenu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.cancel -> {
                    //ReuseFunctions.showToast(context,"" +  list!![position].downloadReference)
                    ReuseFunctions.showToast(context, "" + position + "")
                }

            }
            true
        }
        popupMenu.show()
    }

}
