package com.net.pslapllication.room.datamodel

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ConverterTypeClass {
    companion object {
        @TypeConverter
        fun tourl(timestamp: String): String? {
            return if (timestamp == null) null else String.toString()
        }


        @TypeConverter
        fun fromurl(date: String?): String {
            return (if (date == null) null.toString() else "null")
        }
    }
}
