package com.net.pslapllication.adpters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.net.pslapllication.room.datamodel.DictionaryDataAPI

class AutoCompleteAdapter (context: Context, @LayoutRes private val layoutResource: Int, private val allPois: List<DictionaryDataAPI>):
    ArrayAdapter<DictionaryDataAPI>(context, layoutResource, allPois),
    Filterable {
    private var mPois: List<DictionaryDataAPI> = allPois

    override fun getCount(): Int {
        return mPois.size
    }

    override fun getItem(p0: Int): DictionaryDataAPI? {
        return mPois.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        // Or just return p0
        return mPois.get(p0).id.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView

        view.text = "${mPois[position].english_word} ${mPois[position].urdu_word} "
        view.setTextColor(Color.BLACK)
        view.setHintTextColor(Color.BLACK)
        return view
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: Filter.FilterResults
            ) {
                mPois = filterResults.values as List<DictionaryDataAPI>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase()

                val filterResults = Filter.FilterResults()
                filterResults.values = if (queryString == null || queryString.isEmpty())
                    allPois
                else
                    allPois.filter {
                        it.english_word.toLowerCase().startsWith(queryString) ||
                                it.urdu_word.toLowerCase().contains(queryString)
                    }
                return filterResults
            }
        }
    }
}
