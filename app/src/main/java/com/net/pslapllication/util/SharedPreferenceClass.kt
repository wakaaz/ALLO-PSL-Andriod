package com.net.pslapllication.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceClass(context: Context) {
    private val SLIDERPREF = "SLIDERPREF"
    private val LOGINPREF = "LOGINPREF"
    private val SESSIONPREF = "SESSIONPREF"
    private val POSPREF = "POSPREF"
     private val QUALITYPREFMOB = "QUALITYPREFMOB"
    private val QUALITYPREFWIFI = "QUALITYPREFWIFI"
    private val DOWNLOADSTART = "DOWNLOADSTART"
    private val AUTOPLAY = "AUTOPLAY"
    private val FIRSTNAME = "FIRSTNAME"
    private val LASTNAME = "LASTNAME"
    private val EMAIL = "EMAIL"
    private val SESSION = "SESSION"
    private val UPDATED_AT = "UPDATED_AT"
    private val CREATED_AT = "CREATED_AT"
    private val ID = "ID"
    private val USERTYPE = "USERTYPE"
    private val DOWNLOADTYPE_PROGRESS = "DOWNLOADTYPE_PROGRESS"
    private val LASTDATE = "LASTDATE"
    private val IS_FIRST_TIME = "IS_FIRST_TIME"
    private var sharedPreferences: SharedPreferences

    companion object {
        var sharedPreferenceClass: SharedPreferenceClass? = null
        var preferenceName = "com.net.pslapllication.util"

        @Synchronized
        fun getInstance(context: Context): SharedPreferenceClass? {
            if (sharedPreferenceClass == null) {
                sharedPreferenceClass = SharedPreferenceClass(context)
            }
            return sharedPreferenceClass
        }
    }

    init {
        sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }

    fun setIntroLaunch(isSet: Boolean) {
        sharedPreferences.edit().putBoolean(SLIDERPREF, isSet).apply()
    }

    fun getIntroLaunch(): Boolean {
        return sharedPreferences.getBoolean(SLIDERPREF, false)
    }

    fun setisLogin(isSet: Boolean) {
        sharedPreferences.edit().putBoolean(LOGINPREF, isSet).apply()
    }

    fun getisLogin(): Boolean {
        return sharedPreferences.getBoolean(LOGINPREF, false)
    }

    fun setPos(pos: String) {
        sharedPreferences.edit().putString(POSPREF, pos).apply()
    }

    fun getPos(): String {
        return sharedPreferences.getString(POSPREF, "").toString()
    }


    fun setSession(pos: String) {
        sharedPreferences.edit().putString(SESSIONPREF, pos).apply()
    }

    fun getSession(): String {
        return sharedPreferences.getString(SESSIONPREF, "").toString()
    }

    fun setQualityMob(pos: String) {
        sharedPreferences.edit().putString(QUALITYPREFMOB, pos).apply()
    }

    fun getQualityMob(): String {
        return sharedPreferences.getString(QUALITYPREFMOB, "").toString()
    }

    fun setQualityWifi(pos: String) {
        sharedPreferences.edit().putString(QUALITYPREFWIFI, pos).apply()
    }

    fun getQualityWifi(): String {
        return sharedPreferences.getString(QUALITYPREFWIFI, "").toString()
    }



    fun setAutoPLayToggle(pos: Boolean) {
        sharedPreferences.edit().putBoolean(AUTOPLAY, pos).apply()
    }

    fun getAutoPLayToggle(): Boolean {
        return sharedPreferences.getBoolean(AUTOPLAY, false)
    }

    /**
     * Profile Preferences
     */
    fun setFirstName(pos: String) {
        sharedPreferences.edit().putString(FIRSTNAME, pos).apply()
    }

    fun getFirstName(): String {
        return sharedPreferences.getString(FIRSTNAME, "").toString()
    }
    fun setlast_name(last_name: String?) {
        sharedPreferences.edit().putString(LASTNAME, last_name).apply()
    }

    fun getlast_name(): String? {
        return sharedPreferences.getString(LASTNAME, "").toString()
    }

    fun setemail(email: String) {
        sharedPreferences.edit().putString(EMAIL, email).apply()
    }

    fun getemail(): String {
        return sharedPreferences.getString(EMAIL, "").toString()
    }


    fun setupdated_at(updated_at: String) {
        sharedPreferences.edit().putString(UPDATED_AT, updated_at).apply()
    }

    fun getupdated_at(): String {
        return sharedPreferences.getString(UPDATED_AT, "").toString()
    }

    fun setcreated_at(createdat: String) {
        sharedPreferences.edit().putString(CREATED_AT, createdat).apply()

    }

    fun getcreated_at(): String {
        return sharedPreferences.getString(CREATED_AT, "").toString()
    }

    fun setid(idvalue: Int) {
        sharedPreferences.edit().putInt(ID, idvalue).apply()

    }

    fun getid(): Int {
        return sharedPreferences.getInt(ID, 0)
    }
    fun setUserType(userType: String) {
        sharedPreferences.edit().putString(USERTYPE, userType).apply()

    }

    fun getUserType(): String {
        return sharedPreferences.getString(USERTYPE,"")!!
    }
    fun setDownloadType(downloadType: String) {
        sharedPreferences.edit().putString(DOWNLOADTYPE_PROGRESS, downloadType).apply()

    }

    fun getDownloadType(): String {
        return sharedPreferences.getString(DOWNLOADTYPE_PROGRESS,"")!!
    }

 fun setLastDate(date: String) {
        sharedPreferences.edit().putString(LASTDATE, date).apply()

    }

    fun getLastDate(): String {
        return sharedPreferences.getString(LASTDATE,"")!!
    }

    fun setFirstTime(isSet: Boolean) {
        sharedPreferences.edit().putBoolean(IS_FIRST_TIME, isSet).apply()
    }

    fun getFirstTime(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_TIME, true)
    }
}