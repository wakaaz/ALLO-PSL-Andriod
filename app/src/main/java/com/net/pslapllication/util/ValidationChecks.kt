package com.net.pslapllication.util

import android.text.TextUtils
import android.util.Patterns
import android.widget.EditText


class ValidationChecks {
    companion object {
        fun emptyCheck(view: EditText, message: String): Boolean =
            if (view.text.isEmpty()) {
                view.error = message
                false
            } else {
                view.error = null
                true
            }

        fun isValidEmail(target: CharSequence?): Boolean {
            return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }

        fun isPasswordValidLength(view: EditText, passwordString: String,errorMessage :String): Boolean =
            if(passwordString.length >= 6){
                view.error = null
                true
            }else{
                view.error = errorMessage
                false
            }
        }
}