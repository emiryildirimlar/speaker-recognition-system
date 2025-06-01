package com.example.speakerrecoginitonfinal2.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PingResponse(
    @SerialName("message") val message: String
)

@Serializable
data class RegisterSpeakerResponse(
    @SerialName("message") val message: String,
    @SerialName("speakerName") val speakerName: String,
    @SerialName("success") val success: Boolean,
    @SerialName("speakerId") val speakerId: String,
    @SerialName("createdAt") val createdAt: String, // ISO formatında tarih
    @SerialName("audioUrl") val audioUrl: String
)

@Serializable
data class SpeakerInfo(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("createdAt") val createdAt: String, // API yanıtında zorunlu görünüyor
    @SerialName("audioUrl") val audioUrl: String,    // Yeni eklendi
    @SerialName("embeddingId") val embeddingId: String // Yeni eklendi
)

@Serializable
data class ListSpeakersResponse(
    @SerialName("speakers") val speakers: List<SpeakerInfo>,
    @SerialName("count") val count: Int // message ve success yerine count eklendi
)

@Serializable
data class TestSpeakerResponse(
    @SerialName("message") val message: String,
    @SerialName("match_found") val matchFound: Boolean = false,
    @SerialName("confidence") val confidence: Float = 0f,
    @SerialName("speaker_id") val speakerId: String? = null,
    @SerialName("speaker_name") val speakerName: String? = null,
    @SerialName("threshold_met") val thresholdMet: Boolean? = null,
    @SerialName("required_threshold") val requiredThreshold: Float? = null,
    @SerialName("all_matches") val allMatches: List<SpeakerMatchAPI>? = null
)


@Serializable
data class SpeakerMatchAPI(
    @SerialName("speaker_id") val speakerId: String,
    @SerialName("speaker_name") val speakerName: String,
    @SerialName("confidence") val confidence: Float
)

@Serializable
data class RecognitionResponse(
    @SerialName("message") val message: String? = null,
    @SerialName("error") val error: String? = null,
    // Başarılı tekli tanıma durumunda bu alanlar dolu olabilir
    @SerialName("speaker_id") val speakerId: String? = null,
    @SerialName("speaker_name") val speakerName: String? = null,
    @SerialName("confidence") val confidence: Float? = null,
    // Çoklu eşleşme durumunda bu liste dolu olabilir
    @SerialName("matches") val matches: List<SpeakerMatchAPI>? = null
)

/**
 * Sunucudan gelebilecek genel hata yanıtlarını modellemek için.
 */
@Serializable
data class ErrorResponse(
    @SerialName("detail") val detail: String? = null, // FastAPI varsayılan hata detayı
    @SerialName("message") val message: String? = null // Özel mesaj alanı varsa
)

@Serializable
data class DeleteSpeakerResponse(
    @SerialName("message")
    val message: String, // Sunucudan non-null geliyor
    @SerialName("success")
    val success: Boolean, // Sunucudan geliyor
    @SerialName("speakerId") // JSON'daki doğru anahtar adı (camelCase)
    val speakerId: String, // Sunucudan non-null geliyor
    @SerialName("speakerName")
    val speakerName: String, // Sunucudan geliyor
    @SerialName("embeddingDeleted")
    val embeddingDeleted: Boolean // Sunucudan geliyor
)

@Serializable
data class BaseResponse(
    @SerialName("message") val message: String,
    @SerialName("success") val success: Boolean // Boolean? -> Boolean
)
