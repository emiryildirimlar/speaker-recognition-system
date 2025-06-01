package com.example.speakerrecoginitonfinal2.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // Logging Interceptor importu eklendi
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit // TimeUnit importu eklendi

object RetrofitInstance {
    // TODO: Kendi yerel Python sunucunuzun IP adresi ve portu ile güncelleyin.

    //private const val BASE_URL = "http://10.0.2.2:8000"                       //Android Emulator için yerel sunucu adresi
    //private const val BASE_URL = "https://g7ckdv45-8000.euw.devtunnels.ms/"
    private const val BASE_URL = "https://m7d37xdl-8000.euw.devtunnels.ms/"    // Azure Dev Tunnels üzerinden erişim için

    private val json = Json { ignoreUnknownKeys = true }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log seviyesini BODY olarak ayarla (tüm detaylar)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Logging Interceptor eklendi
        .connectTimeout(30, TimeUnit.SECONDS) // Bağlantı zaman aşımı
        .readTimeout(30, TimeUnit.SECONDS)    // Okuma zaman aşımı
        .writeTimeout(30, TimeUnit.SECONDS)   // Yazma zaman aşımı
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}