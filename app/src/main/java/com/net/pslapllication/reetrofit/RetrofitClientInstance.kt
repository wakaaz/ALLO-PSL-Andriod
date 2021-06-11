package com.net.pslapllication.reetrofit

import android.R
import com.net.pslapllication.util.MainClass
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


class RetrofitClientInstance {
    companion object {
        //
        //const val BASE_URL = "https://admin.pslmonitoringserver.com/api/"
        // const val BASE_URL = "http://psl.am7.studio/api/"
        const val BASE_URL = "https://admin.aajkaadin.com/api/"
        public var httpClientBuilder: OkHttpClient.Builder? = null

        var retrofit: Retrofit? = null

        fun getClient(): Retrofit? {
            if (retrofit == null) {




                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())

                    .client(getUnsafeOkHttpClient())
                    .build()
            }
            return retrofit
        }
        private fun getUnsafeOkHttpClient(): OkHttpClient? {
            return try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                            return arrayOf()
                        }
                    }
                )

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory
                val trustManagerFactory: TrustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers: Array<TrustManager> =
                    trustManagerFactory.trustManagers
                check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                    "Unexpected default trust managers:" + trustManagers.contentToString()
                }

                val trustManager =
                    trustManagers[0] as X509TrustManager


                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustManager)
                builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
                builder.build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

}
