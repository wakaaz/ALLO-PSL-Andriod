package com.net.pslapllication.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.net.pslapllication.R
import com.net.pslapllication.data.DictionaryListModel
import com.net.pslapllication.data.DownloadListModel
import com.net.pslapllication.model.carrierModels.DictionaryListCarrierDataModel
import com.net.pslapllication.model.dictionary.DictionaryData
import com.net.pslapllication.model.favouriteList.Data
import com.net.pslapllication.model.learningtutorial.LearningData
import com.net.pslapllication.model.preferences.Story_types
import com.net.pslapllication.model.stories.StoryData
import com.net.pslapllication.model.tutorial.TutorialData
import com.net.pslapllication.room.WordsViewModel
import com.net.pslapllication.room.datamodel.DictionaryDataAPI
import java.lang.reflect.Method


class ReuseFunctions {
    companion object {

        fun startNewActivity(
            context: Context,
            nextClass: Class<out Any>
        ) {
            context.startActivity(Intent(context, nextClass))

        }


        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun startNewActivityTask(
            context: Context,
            nextClass: Class<out Any>
        ) {
            val i = Intent(context, nextClass)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }

        fun startNewActivityTaskTop(
            context: Context,
            nextClass: Class<out Any>
        ) {
            val i = Intent(context, nextClass)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            context.startActivity(i)

        }

        fun startNewActivityDataModelParam(
            context: Context,
            nextClass: Class<out Any>,
            dictionaryData: DictionaryData,
            type: String
        ) {
            val bundle = Bundle()
            //  bundle.putSerializable(Constants.DICTIONARY_LIST_MODEL, carrierDataModel)
            bundle.putSerializable(Constants.SELECTED_DICTIONARY_LIST_MODEL, dictionaryData)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra(Constants.CETAGORY_TYPE, type)
            context.startActivity(intent)
        }

        fun startNewActivityDataModelParam(
            context: Context,
            nextClass: Class<out Any>,
            dictionaryData: DictionaryDataAPI,
            type: String
        ) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.SELECTED_DICTIONARY_LIST_MODEL, dictionaryData)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra(Constants.CETAGORY_TYPE, type)
            context.startActivity(intent)
        }
        fun startNewActivityDataModelParam(
            context: Context,
            nextClass: Class<out Any>,
            dictionaryData: DownloadListModel,
            type: String
        ) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.SELECTED_DICTIONARY_LIST_MODEL, dictionaryData)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra(Constants.CETAGORY_TYPE, type)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        fun startNewActivityDataModelParam(
            context: Context,
            nextClass: Class<out Any>,
            tutorialData: TutorialData,
            tutorialType: String,
            type: String
        ) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.SELECTED_DICTIONARY_LIST_MODEL, tutorialData)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra(Constants.CETAGORY_TYPE, type)
            intent.putExtra(Constants.TUTORIAL_TYPE, tutorialType)
            context.startActivity(intent)
        }

        fun startNewActivityDataModelParam(
            context: Context,
            nextClass: Class<out Any>,
            tutorialData: StoryData,
            type: String
        ) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.SELECTED_DICTIONARY_LIST_MODEL, tutorialData)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra(Constants.CETAGORY_TYPE, type)
            context.startActivity(intent)
        }
 fun startNewActivityDataModelParam(
            context: Context,
            nextClass: Class<out Any>,
            data: Data,
            type: String
        ) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.SELECTED_DICTIONARY_LIST_MODEL, data)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra(Constants.CETAGORY_TYPE, type)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        fun startNewActivityDataModelParam(
            context: Context,
            nextClass: Class<out Any>,
            dictionaryData: Story_types,
            type: String
        ) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.SELECTED_DICTIONARY_LIST_MODEL, dictionaryData)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra(Constants.CETAGORY_TYPE, type)
            context.startActivity(intent)
        }
 fun startNewActivityDataModelParam(
     context: Context,
     nextClass: Class<out Any>,
     dictionaryData: LearningData,
     type: String
        ) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.SELECTED_DICTIONARY_LIST_MODEL, dictionaryData)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra(Constants.CETAGORY_TYPE, type)
            context.startActivity(intent)
        }

        fun startNewActivityDataModelParam(
            context: Context,
            nextClass: Class<out Any>,
            id: String,
            carrierDataModel: DictionaryListCarrierDataModel,
            name: String,
            type: String
        ) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.DICTIONARY_LIST_MODEL, carrierDataModel)
            val intent = Intent(context, nextClass)
            intent.putExtras(bundle)
            intent.putExtra("TYPE", type)
            intent.putExtra("ID", id)
            intent.putExtra("NAME", name)
            context.startActivity(intent)
        }

        fun startNewActivityTaskWithParameter(
            context: Context,
            nextClass: Class<out Any>,
            type: String
        ) {
            val i = Intent(context, nextClass)
            i.putExtra("TYPE", type)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }

        fun startNewActivityTaskWithParameterSingleWord(
            context: Context,
            nextClass: Class<out Any>,
            id: String,
            name: String,
            type: String
        ) {
            val i = Intent(context, nextClass)
            i.putExtra("ID", id)
            i.putExtra("NAME", name)
            i.putExtra("TYPE", type)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }

        fun startNewActivityTaskWithParameterSingleWordCount(
            context: Context,
            nextClass: Class<out Any>,
            id: String,
            name: String,
            type: String,
            itemscount: Int
        ) {
            val i = Intent(context, nextClass)
            i.putExtra("ID", id)
            i.putExtra("NAME", name)
            i.putExtra("TYPE", type)
            i.putExtra("ITEMSCOUNT", itemscount)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }

        fun startNewActivityTaskWithParameterSingleWordTutorial(
            context: Context,
            nextClass: Class<out Any>,
            id: String,
            name: String,
            main_name: String,
            type: String
        ) {
            val i = Intent(context, nextClass)
            i.putExtra("ID", id)
            i.putExtra("NAME", name)
            i.putExtra("MAIN_NAME", main_name)
            i.putExtra("TYPE", type)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }

        fun firstLetterCap(str: String?): String? {
            var st: String? = ""
            if (str != null) {
                // Create a char array of given String
                val ch = str.toCharArray()
                for (i in 0 until str.length) {

                    // If first character of a word is found
                    if (i == 0 && ch[i] != ' ' ||
                        ch[i] != ' ' && ch[i - 1] == ' '
                    ) {

                        // If it is in lower-case
                        if (ch[i] >= 'a' && ch[i] <= 'z') {

                            // Convert into Upper-case
                            ch[i] = (ch[i] - 'a' + 'A'.toInt()).toChar()
                        }
                    } else if (ch[i] >= 'A' && ch[i] <= 'Z') // Convert into Lower-Case
                        ch[i] = (ch[i] + 'a'.toInt() - 'A'.toInt())
                }

                // Convert the char array to equivalent String
                st = String(ch)
            }
            return st
        }

        fun regularFont(context: Context?): Typeface? {
            return ResourcesCompat.getFont(context!!, R.font.lato_regular)
        }

        fun heavyFont(context: Context?): Typeface? {
            return ResourcesCompat.getFont(context!!, R.font.lato_heavy)
        }

        fun networkQualityWifi(context: Context): String {
            var qualityStatus: String = "UNKNOWN"
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val numberOfLevels = 5
            val wifiInfo = wifiManager.connectionInfo
            val level = WifiManager.calculateSignalLevel(wifiInfo.rssi, numberOfLevels)
            when (level) {
                2 -> qualityStatus = "POOR"
                3 -> qualityStatus = "MODERATE"
                4 -> qualityStatus = "GOOD"
                5 -> qualityStatus = "EXCELLENT"
            }
            return qualityStatus

        }

        @SuppressLint("MissingPermission")
        fun networkQualityMobData(context: Context): String {
            val telephonyManager: TelephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val cellSignalStrengthGsm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                telephonyManager.getAllCellInfo().get(0).cellConnectionStatus
            } else {
                // TODO("VERSION.SDK_INT < P")
            }

            var qualityStatus: String = "UNKNOWN"
            val networkClass: Int = getNetworkClass(getNetworkType(context))
            qualityStatus = when (networkClass) {
                1 -> "POOR"
                2 -> "GOOD"
                3 -> "EXCELLENT"
                else -> "UNKNOWN"
            }
            return qualityStatus

        }

        fun getNetworkType(context: Context): Int {
            return (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkType
        }

        fun getNetworkClass(networkType: Int): Int {
            try {
                return getNetworkClassReflect(networkType)
            } catch (ignored: Exception) {
            }
            return when (networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS, 16,
                TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> 1
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, 17 -> 2
                TelephonyManager.NETWORK_TYPE_LTE, 18 -> 3
                else -> 0
            }
        }

        private fun getNetworkClassReflect(networkType: Int): Int {
            val getNetworkClass: Method =
                TelephonyManager::class.java.getDeclaredMethod(
                    "getNetworkClass",
                    Int::class.javaPrimitiveType
                )
            if (!getNetworkClass.isAccessible) {
                getNetworkClass.isAccessible = true
            }
            return getNetworkClass.invoke(null, networkType) as Int
        }

        fun preventTwoClick(view: View) {
            view.isEnabled = false
            view.postDelayed({ view.isEnabled = true }, 500)
        }

        fun snackMessage(parentLayout: View?, message: String) {
            if (parentLayout != null) {
                val snackbar = Snackbar.make(parentLayout, message + "", Snackbar.LENGTH_LONG)
                snackbar.view.setBackgroundColor(Color.parseColor("#009E4F"))
                val textView = snackbar.view.findViewById(R.id.snackbar_text) as TextView
                textView.setTextColor(Color.parseColor("#FFFFFF"))
                snackbar.show()
            }
        }

        fun snackMessage(message: String, parentLayout: View?) {
            if (parentLayout != null) {
                val snackbar = Snackbar.make(parentLayout, message + "", Snackbar.LENGTH_LONG)
                snackbar.view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                val textView = snackbar.view.findViewById(R.id.snackbar_text) as TextView
                textView.setTextColor(Color.parseColor("#009E4F"))
                snackbar.show()
            }
        }

    }

    private fun checkPermission(context: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                context,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), Constants.MY_PERMISSIONS_REQUEST_WRITE_STORAGE
            )
            return false
        } else {
            return true
        }
    }

}

