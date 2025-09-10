package com.thatwaz.dadjokes.di

import android.content.Context
import android.content.pm.ApplicationInfo
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

private const val BASE_URL = "https://icanhazdadjoke.com/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        // Version name without BuildConfig
        val versionName = try {
            val pm = context.packageManager
            val pkg = context.packageName
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0).versionName ?: "1.0"
        } catch (_: Exception) { "1.0" }

        // Is debug build without BuildConfig
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

        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

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
