package com.example.tzpoc.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class RetrofitInstance {

    companion object {
        // Function to create a Retrofit instance with a secure connection
        fun create(baseUrl: String): Retrofit {
            // Set up the logging interceptor for debugging
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            // Create a TrustManager that trusts all certificates (for insecure connections)
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                        // No checks for client certificates
                    }

                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                        // No checks for server certificates
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            // Create and initialize SSLContext with all trusted certificates
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create OkHttpClient with the custom SSL context
            val client = OkHttpClient.Builder()
                .addInterceptor(logging) // Logging interceptor to log HTTP request and response
                .connectTimeout(100, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(100, TimeUnit.SECONDS) // Read timeout
                .writeTimeout(100, TimeUnit.SECONDS) // Write timeout
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager) // Use custom SSL socket factory
                .hostnameVerifier { _, _ -> true } // Allow any hostname, insecure (use carefully)
                .build()

            // Return a Retrofit instance with the custom OkHttpClient and GsonConverterFactory
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()) // Gson converter to parse JSON
                .client(client) // Use the OkHttpClient we created above
                .build()
        }

        // Function to obtain the API interface
        fun api(baseUrl: String): TZCrossAPi {
            val retrofit = create(baseUrl)
            return retrofit.create(TZCrossAPi::class.java) // Return the specific API interface instance
        }
    }
}