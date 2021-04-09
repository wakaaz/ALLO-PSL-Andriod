package com.net.pslapllication.adpters

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.net.pslapllication.R
import com.net.pslapllication.activities.CatDownloadsActivity
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.interfaces.onVideoDeleteInterface
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.row_download_categories.view.*
import java.io.File

class DownloadedCatAdapter(var context: Context, var onVideoDelete : onVideoDeleteInterface) :
    RecyclerView.Adapter<DownloadedCatAdapter.ViewHolder>() {
    var list: MutableList<DictionaryListModel>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_download_categories,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        var size = 0
        if (list != null) {
            size = list!!.size
        }
        return size
    }

    override fun onBindViewHolder(holder: DownloadedCatAdapter.ViewHolder, position: Int) {

        holder.tv_main?.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
        holder.tv_main?.text = list!![position].wordName
        holder.tv_duration?.text = list!![position].wordDetail + " Video Downloaded"


        holder.bind(context, list!![position])
        holder.constraint_main_cat!!.setOnClickListener {
            ReuseFunctions.startNewActivityTaskWithParameter(
                context,
                CatDownloadsActivity::class.java,
               list!![position].wordName
            )
        }
        holder.tv_translate!!.setOnClickListener {
            setpopUp(list!![position].wordName+"/",holder.tv_translate,position)

        }

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val tv_main: TextView? = view.tv_main
        val tv_duration: TextView? = view.tv_duration
        val constraint_main_cat: ConstraintLayout? = view.constraint_main_cat
        val tv_translate: ImageButton? = view.tv_translate


        fun bind(
            context: Context,
            dictionaryListModel: DictionaryListModel
        ) {

            constraint_main_cat!!.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.constraint_main_cat -> {
                    /* ReuseFunctions.startNewActivityTaskWithParameter(
                         v.context,
                         CatDownloadsActivity::class.java,
                        list[adapterPosition].wordName
                     )*/
                }
            }
        }


    }


    fun setWords(list: List<DictionaryListModel>) {
        this.list = list as MutableList

        notifyDataSetChanged()
    }
    private fun setpopUp(
        subfolderString: String,
        imgbtnMenu: ImageButton,
        position: Int

    ) {

        val popupMenu =
            PopupMenu(context, imgbtnMenu, R.style.PopupMenu)

        popupMenu.menuInflater.inflate(R.menu.deletemenu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    deleteRecursive(File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        Constants.CAT_FOLDER_NAME+subfolderString))
                    list!!.removeAt(position)
                    notifyDataSetChanged()
                    if (list!!.size == 0){
                        onVideoDelete.onAllCatDeleted(true)
                    }else{
                        onVideoDelete.onAllCatDeleted(false)
                    }


                }

            }
            true
        }
        popupMenu.show()
    }
    fun deleteRecursive(fileOrDirectory:File) {
        if (fileOrDirectory.isDirectory())
            for (child in fileOrDirectory.listFiles())
                deleteRecursive(child)
        fileOrDirectory.delete()
    }

}