package com.example.speakerrecoginitonfinal2.data.network

import com.example.speakerrecoginitonfinal2.data.model.BaseResponse
import com.example.speakerrecoginitonfinal2.data.model.DeleteSpeakerResponse
import com.example.speakerrecoginitonfinal2.data.model.ListSpeakersResponse
import com.example.speakerrecoginitonfinal2.data.model.PingResponse
import com.example.speakerrecoginitonfinal2.data.model.RegisterSpeakerResponse
import com.example.speakerrecoginitonfinal2.data.model.TestSpeakerResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    /**
     * Sunucunun ayakta olup olmadığını kontrol eder.
     */
    @GET("ping")
    suspend fun ping(): PingResponse

    /**
     * Yeni bir konuşmacıyı ses dosyası ve ismiyle kaydeder.
     * @param speakerName Konuşmacının ismi (text/plain olarak gönderilir).
     * @param audio Konuşmacıya ait .wav formatındaki ses dosyası.
     * @return Kayıt işleminin sonucunu içeren [RegisterSpeakerResponse].
     */
    @Multipart
    @POST("register/") // Sunucu prefix'i /register olduğu için
    suspend fun registerSpeaker(
        @Part("speaker_name") speakerName: RequestBody,
        @Part audio: MultipartBody.Part
    ): RegisterSpeakerResponse

    /**
     * Kayıtlı konuşmacıların listesini (veya mevcut durumda bir mesajı) getirir.
     * @return Konuşmacı listesi bilgilerini (veya mesajı) içeren [ListSpeakersResponse].
     */
    @GET("speakers/") // Sunucu prefix'i /speakers olduğu için
    suspend fun listSpeakers(): ListSpeakersResponse

    /**
     * Konuşmacı tanıma testi için bir endpoint (mevcut durumda sadece bir mesaj döndürür).
     * Gelecekte ses dosyası alarak tanıma yapması beklenir.
     * @return Test sonucunu (veya mesajı) içeren [TestSpeakerResponse].
     */
    @Multipart
    @POST("test/")
    suspend fun testSpeakerRecognition(
    @Part audio: MultipartBody.Part,
    @Part("speaker_id") speakerId: RequestBody? = null // Opsiyonel speaker_id
    ): TestSpeakerResponse

    /**
     * Belirtilen ID'ye sahip konuşmacıyı siler.
     * @param speakerId Silinecek konuşmacının ID'si.
     * @return Silme işleminin sonucunu içeren [DeleteSpeakerResponse].
     */
    @DELETE("speakers/{speaker_id}") // Plandaki endpoint
    suspend fun deleteSpeaker(
        @Path("speaker_id") speakerId: String
    ): Response<DeleteSpeakerResponse>

    /**
     * Kayıtlı konuşmacıların ses verilerini kullanarak modeli eğitir.
     * @return Eğitim işleminin sonucunu içeren [BaseResponse].
     */
    @POST("train_model") // Plandaki endpoint
    suspend fun trainModel(): BaseResponse // Repository'deki kullanım doğrudan BaseResponse bekliyor
}