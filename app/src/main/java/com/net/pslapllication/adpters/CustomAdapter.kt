package com.net.pslapllication.adpters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.net.pslapllication.R
import com.net.pslapllication.activities.dictionary.ClassSubjectListActivity
import com.net.pslapllication.activities.dictionary.DictionarySingleWordActivity
import com.net.pslapllication.activities.dictionary.SubjectTopicListActivity
import com.net.pslapllication.activities.dictionary.VideoPreviewActivity
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.util.Constants
import com.net.pslapllication.util.ReuseFunctions

class CustomAdapter(
    private val context: Context,
    var type: String/*internal var list: ArrayList<String>*/)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var diclist = emptyList<DictionaryListModel>() // Cached copy of words


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return (if (type == Constants.TYPE_DICTIONARY) {
            ViewHolder1(LayoutInflater.from(context).inflate(R.layout.row_main_detail_dic_new, parent, false))
        } else if (type == Constants.TYPE_DOWNLOAD || type == Constants.TYPE_SUB_TOPIC || type == Constants.TYPE_CLASS_SUBJECT || type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_FAVOURITE || type == Constants.TYPE_LEARNING_TUTORIAL ) {
            ViewHolder2(LayoutInflater.from(context).inflate(R.layout.row_single_word_detail, parent, false))
        }else if ( type == Constants.TYPE_VIDEO_QUALITY ) {
            ViewHolder2(LayoutInflater.from(context).inflate(R.layout.bottom_layout_video_qualityoption, parent, false))
        } else{
            Log.d("abc","abc")
        }) as RecyclerView.ViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (type == Constants.TYPE_DICTIONARY) {
            (holder as ViewHolder1).bind(position)

        }else if (type == Constants.TYPE_VIDEO_QUALITY) {
            (holder as ViewHolder3).bind(position)

        } else if (type == Constants.TYPE_DOWNLOAD || type == Constants.TYPE_SUB_TOPIC || type == Constants.TYPE_CLASS_SUBJECT || type == Constants.TYPE_TEACHER_TUTORIAL || type == Constants.TYPE_FAVOURITE || type == Constants.TYPE_LEARNING_TUTORIAL){
            (holder as ViewHolder2).bind(position,type)
        }
    }

    override fun getItemCount(): Int {
        return diclist.size
    }

    private inner class ViewHolder1 internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tv_title: TextView = itemView.findViewById(R.id.tv_title)
        internal var tv_count: TextView = itemView.findViewById(R.id.tv_count)
        init { // Initialize your All views prensent in list items
        }
        internal fun bind(position: Int) {
            tv_title.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
             tv_count.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)

            tv_title.text = diclist[position].wordName
            tv_count.text = diclist[position].wordDetail

            itemView.setOnClickListener {
                ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                    itemView.context, DictionarySingleWordActivity::class.java,
                    diclist[position].id.toString(), diclist[position].wordName, type)
            }
        }
    }

    private inner class ViewHolder2 internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var tv_translate: TextView = itemView.findViewById(R.id.tv_translate)
        internal var tv_word: TextView = itemView.findViewById(R.id.tv_word)
        internal var tv_word2: TextView = itemView.findViewById(R.id.tv_word2)

        init {
            // Initialize your All views prensent in list items
            tv_translate.visibility = View.GONE
        }

        internal fun bind(position: Int, type: String) {
            tv_word.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
            tv_word2.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)

            tv_word.text = diclist[position].wordName
            tv_word2.text = diclist[position].wordDetail

            if (type == Constants.TYPE_CLASS_SUBJECT){
                itemView.setOnClickListener(View.OnClickListener {
                    ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                        itemView.context, SubjectTopicListActivity::class.java,
                        diclist[position].id.toString(), diclist[position].wordName,
                        this@CustomAdapter.type
                    )
                })
            }else if (type == Constants.TYPE_SUB_TOPIC || type == Constants.TYPE_FAVOURITE || type == Constants.TYPE_DOWNLOAD){
                itemView.setOnClickListener{
                    ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                        itemView.context, VideoPreviewActivity::class.java,
                        diclist[position].id.toString(), diclist[position].wordName,
                        this@CustomAdapter.type
                    )
                }
            }
            else{
                itemView.setOnClickListener(View.OnClickListener {
                    ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                        itemView.context, ClassSubjectListActivity::class.java,
                        diclist[position].id.toString(), diclist[position].wordName,
                        this@CustomAdapter.type
                    )
                })
            }
        }
    }

    private inner class ViewHolder3 internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tv_title: TextView = itemView.findViewById(R.id.tv_title)
        internal var tv_count: TextView = itemView.findViewById(R.id.tv_count)
        init { // Initialize your All views prensent in list items
        }
        internal fun bind(position: Int) {
            tv_title.typeface = ResourcesCompat.getFont(context, R.font.lato_semibold)
            tv_count.typeface = ResourcesCompat.getFont(context, R.font.lato_regular)

            tv_title.text = diclist[position].wordName
            tv_count.text = diclist[position].wordDetail

            itemView.setOnClickListener {
                ReuseFunctions.startNewActivityTaskWithParameterSingleWord(
                    itemView.context, DictionarySingleWordActivity::class.java,
                    diclist[position].id.toString(), diclist[position].wordName, type)
            }
        }
    }

    internal fun setWords(users: List<DictionaryListModel>) {
        this.diclist = users
        notifyDataSetChanged()
    }


}
