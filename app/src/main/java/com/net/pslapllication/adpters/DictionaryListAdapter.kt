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
import com.net.pslapllication.activities.dictionary.DictionarySingleWordActivity
import com.net.pslapllication.model.preferences.Dictionary_categories
import com.net.pslapllication.model.preferences.Life_skills
import com.net.pslapllication.model.preferences.Story_types
import com.net.pslapllication.model.preferences.Tut_grades
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions
import kotlinx.android.synthetic.main.row_dic_list.view.*
import java.io.File
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList

class DictionaryListAdapter(var context: Context, var type: String) :
    RecyclerView.Adapter<DictionaryListAdapter.ViewHolder>() ,Filterable {
    var list: List<Dictionary_categories>? = null
    var FilterdicList: List<Dictionary_categories>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_dic_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        var size = 0
        if (FilterdicList != null) {
            size = FilterdicList!!.size
        }
        return size
    }

    override fun onBindViewHolder(holder:  ViewHolder, position: Int) {


        holder.tv_main?.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
        holder.tv_main?.text = FilterdicList!![position].title
        holder.tv_duration?.text =
            (FilterdicList!![position].videos.toString()) + context.getString(R.string.words)
        Glide.with(context)
            .load(URLDecoder.decode(FilterdicList!![position].image, "UTF-8"))
            .into(holder.imageview_card!!)

        holder.constraint_main_cat!!.setOnClickListener {
            ReuseFunctions.preventTwoClick(holder.constraint_main_cat)
            ReuseFunctions.startNewActivityTaskWithParameterSingleWordCount(
                context,
                DictionarySingleWordActivity::class.java,
                FilterdicList!![position].id.toString(),
                FilterdicList!![position].title,
                type,
                FilterdicList!![position].videos
            )
        }


    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tv_main: TextView? = view.tv_main
        val tv_duration: TextView? = view.tv_duration
        val constraint_main_cat: ConstraintLayout? = view.constraint_main_cat
        val imageview_card: ImageView? = view.imageview_card
    }


    fun setWords(list: List<Dictionary_categories>) {
        this.list = list as MutableList
        this.FilterdicList = list as MutableList

        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (type == Constants.TYPE_DICTIONARY) {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        FilterdicList = list
                    } else {
                        val resultList = ArrayList<Dictionary_categories>()
                        for (row: Dictionary_categories in list!!) {
                            if (row.title.toLowerCase(Locale.ROOT)
                                    .contains(charSearch.toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                        FilterdicList = resultList
                    }

                    filterResults.values = FilterdicList
                }

                return filterResults


            }


            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                FilterdicList = results?.values as List<Dictionary_categories>

                notifyDataSetChanged()
            }

        }
    }
}