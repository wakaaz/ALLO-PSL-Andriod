package com.net.pslapllication.reetrofit

import com.net.pslapllication.model.dictionary.DictionaryMainModel
import com.net.pslapllication.model.ResponsePixebay
import com.net.pslapllication.model.signUp.SignUpDataModel
import com.net.pslapllication.model.signUp.SignUpMain
import com.net.pslapllication.model.addToFavourite.AddToFvouriteModel
import com.net.pslapllication.model.favouriteList.FavouriteMain
import com.net.pslapllication.model.forgotpassword.RecoveryEmailModel
import com.net.pslapllication.model.learningtutorial.LearningTutorialMainModel
import com.net.pslapllication.model.login.LoginMainModel
import com.net.pslapllication.model.login.LoginParamModel
import com.net.pslapllication.model.preferences.PreferenceMainModel
import com.net.pslapllication.model.stories.StoriesMainModel
import com.net.pslapllication.model.tutorial.TutorialMainModel
import com.net.pslapllication.room.datamodel.DictionaryMainModelAPI
import retrofit2.Call
import retrofit2.http.*


interface ApiService {
    @GET(".")
    fun getImages(@QueryMap hashMap: HashMap<String, String>): Call<ResponsePixebay>

    /********signup******************/
    @POST("Signup")
    fun getSignUp(@Body signUpDataModel: SignUpDataModel): Call<SignUpMain>

   /********login******************/
    @POST("Login")
    fun getLogIn(@Body loginParamModel: LoginParamModel): Call<LoginMainModel>

    /********Guest login******************/
    @FormUrlEncoded
    @POST("GuestLogin")
    fun  getLogInGuest(@Field("sample") sample: String): Call<LoginMainModel>

    /********dictionary detail******************/
    @FormUrlEncoded
    @POST("Dictionary")
    fun getDictionaryData(
        @Header("session") session: String,
        @Header("userType") userType: String,
        @Field("category_id") categoryId: String
    ): Call<DictionaryMainModel>

 /********dictionary detail without category id******************/
 @FormUrlEncoded
 @POST("Dictionary")
    fun getAllDictionaryData(
        @Header("session") session: String,
        @Header("userType") userType: String,@Field("sample") sample: String
    ): Call<DictionaryMainModel>

/********dictionary detail without category id download******************/
     @FormUrlEncoded
     @POST("Dictionary")
    fun getAllDictionaryDataDownload(
        @Header("session") session: String,
        @Header("userType") userType: String,@Field("sample") sample: String
    ): Call<DictionaryMainModelAPI>

     /********Tutorial Grade-Sub detail******************/
    @FormUrlEncoded
    @POST("Tutorials")
    fun getTutorialData(
        @Header("session") session: String,
        @Header("userType") userType: String,
        @Field("grade_id") grade_id: String,
        @Field("subject_id") subject_id: String

    ): Call<TutorialMainModel>

 /********Learning Tutorial Grade-Sub detail******************/
    @FormUrlEncoded
    @POST("LearningTutorials")
    fun getLearningTutorialData(
        @Header("session") session: String,
        @Header("userType") userType: String,
        @Field("grade_id") grade_id: String,
        @Field("subject_id") subject_id: String

    ): Call<TutorialMainModel>

/********Stories******************/
    @FormUrlEncoded
    @POST("Stories")
    fun getStoriesData(
        @Header("session") session: String,
        @Header("userType") userType: String,
        @Field("type_id") type_id: String):
        Call<StoriesMainModel>

/********Learning Tutorial******************/
    @FormUrlEncoded
    @POST("Lessons")
    fun getLessonData(
        @Header("session") session: String,
        @Header("userType") userType: String,
        @Field("title_id") type_id: String):
        Call<LearningTutorialMainModel>

    /********preferences,main list of all******************/
     @GET("Preferences")
    fun getPreferenceData(@Header("session") session:String,
                          @Header("userType") userType: String
    ): Call<PreferenceMainModel>

    /********add to favourite******************/
    @FormUrlEncoded
     @POST("AddToFavorites")
    fun addToFvourite(@Header("session") session: String,
                      @Header("userType") userType: String,
        @Field("dict_video_id") dict_video_id: String,
        @Field("tut_video_id") tut_video_id: String,
        @Field("lesson_video_id") lesson_video_id: String,
        @Field("story_video_id") story_video_id: String,
        @Field("learning_tut_video_id") learning_tut_video_id: String
    ): Call<AddToFvouriteModel>

    /*******remove from favourite******************/
    @FormUrlEncoded
     @POST("RemoveFromFavorites")
    fun RemoveFromFvourite(@Header("session") session: String,
                           @Header("userType") userType: String,
        @Field("dict_video_id") dict_video_id: String,
        @Field("tut_video_id") tut_video_id: String,
        @Field("lesson_video_id") lesson_video_id: String,
        @Field("story_video_id") story_video_id: String,
        @Field("learning_tut_video_id") learning_tut_video_id: String

    ): Call<AddToFvouriteModel>


    /*******favourite list******************/
     @GET("Favorites")
    fun favouriteList(@Header("session") session: String,
                      @Header("userType") userType: String
    ): Call<FavouriteMain>



    /********forgot password email submission******************/
    @FormUrlEncoded
    @POST("ForgotPassword")
    fun getForgotPassword(@Field("email") email :String): Call<RecoveryEmailModel>

  /********forgot password confirm code submission******************/
    @FormUrlEncoded
    @POST("ConfirmResetPasswordCode")
    fun getConfirmResetPasswordCode(@Field("code") code :String): Call<RecoveryEmailModel>

 /********forgot password submit new password******************/
    @FormUrlEncoded
    @POST("RecoverPassword")
    fun getPasswordRecovered(@Field("code") code :String,
                             @Field("new_password") new_password :String,
                             @Field("confirm_password") confirm_password :String): Call<RecoveryEmailModel>


    /********logout******************/

    @GET("Logout")
    fun getLogout(@Header("session") session: String): Call<RecoveryEmailModel>


    /*
      Recommeded Word Endpoint
     */

    @FormUrlEncoded
    @POST("RecommendAWord")
    fun postRecomendation(@Header("session") session: String,
                          @Header("userType") userType: String,@Field("email") email :String,@Field("name") name :String,@Field("word") word :String): Call<RecoveryEmailModel>

}