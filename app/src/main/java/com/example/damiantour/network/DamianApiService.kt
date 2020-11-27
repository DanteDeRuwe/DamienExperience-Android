package com.example.damiantour.network

import com.example.damiantour.login.model.LoginFields
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.*
import java.util.logging.XMLFormatter
import com.squareup.moshi.Rfc3339DateJsonAdapter
import org.json.JSONArray


/**
 * @author: Ruben Naudts and Simon Bettens and Jordy Van Kerkvoorde
 */
@JvmSuppressWildcards
interface DamianApiService {

    @POST("login")
    suspend fun login(
        @Body login : LoginData,
    ) : String

    @GET("profile")
    suspend fun getProfile(
            @Header("Authorization") token : String
    ): ProfileData

    @GET("deelnemerscode")
    suspend fun getDeelnemerscode(
            @Header("Authorization") token : String
    ): String

    @GET("route/GetRouteByName/{routeName}")
    suspend fun getRoute(
        @Header("Authorization") token: String,
        @Path("routeName") routeName : String
    ) : RouteData

    @GET("routeregistration/checkcurrentregistered")
    suspend fun isRegistered(
        @Header("Authorization") token: String
    ) : Boolean

    @POST("walk/start")
    suspend fun startWalk(
        @Header("Authorization") token: String
    ) : String

    @PUT("walk/stop")
    suspend fun stopWalk(
            @Header("Authorization") token: String
    ) : String
    @PUT("walk/update")
    suspend fun updateWalk(
            @Header("Authorization") token: String,
            @Body coords : List<List<Double>>
    ):String




    object NULL_TO_EMPTY_STRING_ADAPTER {
        @FromJson
        fun fromJson(reader: JsonReader): String {
            if (reader.peek() != JsonReader.Token.NULL) {
                return reader.nextString()
            }
            reader.nextNull<Unit>()
            return ""
        }
    }


    companion object{

        private const val BASE_URL = "https://damiantourapi.azurewebsites.net/api/"
        private val moshi = Moshi.Builder()
            .add(NULL_TO_EMPTY_STRING_ADAPTER)
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter()
                .nullSafe()).build()
        fun create(): DamianApiService{
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
                .create(DamianApiService::class.java)
        }
    }
}

