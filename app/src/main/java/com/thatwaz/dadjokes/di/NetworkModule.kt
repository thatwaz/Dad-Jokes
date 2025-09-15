package com.thatwaz.dadjokes.di

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.thatwaz.dadjokes.data.api.JokeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val BASE_URL = "https://v2.jokeapi.dev/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val versionName = try {
            val pm = context.packageManager
            val pkg = context.packageName
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0).versionName ?: "1.0"
        } catch (_: Exception) { "1.0" }

        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val userAgent = "Dad Jokes Vault/$versionName (support@yourapp.com)"

        val headerInterceptor = Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", userAgent)
                    .build()
            )
        }

        // ✅ Add this: simple host/URL logger
        val hostLog = Interceptor { chain ->
            val req = chain.request()
            Log.d("MAMBY", "→ ${req.method} ${req.url}")       // should show v2.jokeapi.dev
            val resp = chain.proceed(req)
            Log.d("MAMBY", "← ${resp.code} ${resp.request.url.host}")
            resp
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(hostLog)      // ✅ place BEFORE the verbose body logger
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val rt = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // ✅ Log once at startup to confirm base URL
        Log.d("Net", "Retrofit baseUrl=${rt.baseUrl()}")     // → https://v2.jokeapi.dev/
        return rt
    }

    @Provides @Singleton
    fun provideJokeApiService(retrofit: Retrofit): JokeApiService =
        retrofit.create(JokeApiService::class.java)
}








//package com.thatwaz.dadjokes.di
//
//import com.thatwaz.dadjokes.data.api.JokeApiService
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import javax.inject.Singleton
//
//
//
//
//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkModule {
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl("https://icanhazdadjoke.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideJokeApiService(retrofit: Retrofit): JokeApiService {
//        return retrofit.create(JokeApiService::class.java)
//    }
//}
