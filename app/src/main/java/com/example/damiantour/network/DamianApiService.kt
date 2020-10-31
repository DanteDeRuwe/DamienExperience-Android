package com.example.damiantour.network

import com.example.damiantour.login.model.LoginFields
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.logging.XMLFormatter

/**
 * @author: Ruben Naudts
 */
interface DamianApiService {
    @POST("login")
    suspend fun login(
        @Body login : LoginData,
    ) : String



    companion object{
        private const val BASE_URL = "https://damiantourapi.azurewebsites.net/api/"

        fun create(): DamianApiService{
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DamianApiService::class.java)
        }
    }
}

