package com.net.pslapllication.reetrofit


import com.net.pslapllication.interfaces.RetrofitResponseListener
import com.net.pslapllication.model.preferences.PreferenceMainModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiCallClass{
    companion object {
        var apiService: ApiService =
            RetrofitClientInstance.getClient()!!.create(ApiService::class.java)
         fun retrofitCall(
            retrofitResponseListener: RetrofitResponseListener,
            call: Call<Any>
        ) {

            call.enqueue(object : Callback<Any?> {
                override fun onResponse(
                    call: Call<Any?>,
                    response: Response<Any?>
                ) {

                    val model: Any? = response.body()
                    if (model != null) {
                        retrofitResponseListener.onSuccess(model)
                    }

                }

                override fun onFailure(call: Call<Any?>, t: Throwable) {
                    retrofitResponseListener.onFailure(t.localizedMessage)
                    call.cancel()
                }
            })
        }
       /* fun retrofitCall(retrofitResponseListener: RetrofitResponseListener,apiParamModel: ApiParamModel) {
            val call: Call<Any> = apiService.RemoveFromFvourite(apiParamModel)
            call.enqueue(object : Callback<Any?> {
                override fun onResponse(
                    call: Call<Any?>,
                    response: Response<Any?>
                ) {
                    val model: Any? = response.body()
                    retrofitResponseListener.onSuccess(model)
                    if (model != null) {

                        *//* when (model.code) {
                     Constants.SUCCESS_CODE -> {

                     }
                     Constants.SESSION_ERROR_CODE -> {

                     }
                     Constants.FIELD_VALIDATION_ERROR_CODE -> {
                         //no field here

                     }
                 }*//*

                    }

                }

                override fun onFailure(call: Call<Any?>, t: Throwable) {
                    retrofitResponseListener.onFailure(t.localizedMessage)
                    call.cancel()
                }
            })
        }*/
    }

}