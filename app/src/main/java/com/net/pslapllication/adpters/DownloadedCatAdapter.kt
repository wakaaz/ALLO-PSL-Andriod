package com.net.pslapllication.adpters

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.net.pslapllication.R
import com.net.pslapllication.activities.CatDownloadsActivity
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.interfaces.onVideoDeleteInterface
import com.net.pslapllication.model.preferences.Dictionary_categories
import com.net.pslapllication.model.preferences.Life_skills
import com.net.pslapllication.model.preferences.Story_types
import com.net.pslapllication.model.preferences.Tut_grades
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.row_download_categories.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class DownloadedCatAdapter(var context: Context, var onVideoDelete : onVideoDeleteInterface) :
    RecyclerView.Adapter<DownloadedCatAdapter.ViewHolder>() , Filterable {
    var list: MutableList<DictionaryListModel>? = null
    var filteredList: MutableList<DictionaryListModel>? = null


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
        if (filteredList != null) {
            size = filteredList!!.size
        }
        return size
    }

    override fun onBindViewHolder(holder: DownloadedCatAdapter.ViewHolder, position: Int) {

        holder.tv_main?.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)

        var name = filteredList!![position].wordName
        name = name.replace("_", " ")
        holder.tv_main?.text = name
        holder.tv_duration?.text = filteredList!![position].wordDetail + " Video Downloaded"


        holder.bind(context, filteredList!![position])
        holder.constraint_main_cat!!.setOnClickListener {
            ReuseFunctions.startNewActivityTaskWithParameter(
                context,
                CatDownloadsActivity::class.java,
                    filteredList!![position].wordName
            )
        }
        holder.tv_translate!!.setOnClickListener {
            setpopUp(filteredList!![position].wordName+"/",holder.tv_translate,position)

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
        this.filteredList =  list
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
                    filteredList!!.removeAt(position)
                    notifyDataSetChanged()
                    if (filteredList!!.size == 0){
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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                    val charSearch = constraint.toString()
                    if(!list.isNullOrEmpty()) {
                        if (charSearch.isEmpty()) {
                            filteredList = list
                        } else {
                            val resultList = mutableListOf<DictionaryListModel>()
                            for (row: DictionaryListModel in list!!) {
                                if (row.wordName.toLowerCase(Locale.ROOT)
                                                .startsWith(charSearch.toLowerCase(Locale.ROOT))
                                ) {
                                    resultList.add(row)
                                }
                            }
                            filteredList = resultList
                        }

                    }
                    filterResults.values = filteredList


                return filterResults


            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results != null && results?.values != null){
                    filteredList = results?.values as MutableList<DictionaryListModel>


                    notifyDataSetChanged()
                }


            }

        }
    }

}