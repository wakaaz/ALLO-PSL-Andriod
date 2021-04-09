package com.net.pslapllication.model.preferences
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class PreferenceData (
    @SerializedName("tut_grades") val tut_grades : List<Tut_grades>,
    @SerializedName("learning_tut_grades") val learning_tut_grades : List<Tut_grades>,
    @SerializedName("dictionary_categories") val dictionary_categories : List<Dictionary_categories>,
    @SerializedName("story_types") val story_types : List<Story_types>,
    @SerializedName("life_skills") val life_skills : List<Life_skills>
):Serializable