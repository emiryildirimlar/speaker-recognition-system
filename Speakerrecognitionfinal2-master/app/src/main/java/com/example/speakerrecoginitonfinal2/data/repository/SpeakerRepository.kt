package com.example.speakerrecoginitonfinal2.data.repository

import com.example.speakerrecoginitonfinal2.data.model.BaseResponse
import com.example.speakerrecoginitonfinal2.data.model.DeleteSpeakerResponse
import com.example.speakerrecoginitonfinal2.data.model.ErrorResponse // ApiErrorResponse yerine ErrorResponse
import com.example.speakerrecoginitonfinal2.data.model.ListSpeakersResponse
import com.example.speakerrecoginitonfinal2.data.model.PingResponse
import com.example.speakerrecoginitonfinal2.data.model.RegisterSpeakerResponse
import com.example.speakerrecoginitonfinal2.data.model.TestSpeakerResponse
import com.example.speakerrecoginitonfinal2.data.network.ApiService
import com.example.speakerrecoginitonfinal2.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class SpeakerRepository(private val apiService: ApiService) {

    // Hata mesajlarını ayrıştırmak için yardımcı fonksiyon
    private fun parseErrorMessage(errorBodyString: String?, httpStatusCode: Int): String {
        return if (errorBodyString != null) {
            try {
                val errorResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ErrorResponse>(errorBodyString) // ApiErrorResponse yerine ErrorResponse
                errorResponse.detail ?: errorResponse.message ?: "Sunucu Hatası ($httpStatusCode)"
            } catch (e: Exception) {
                "Sunucu yanıtı ayrıştırılamadı ($httpStatusCode): $errorBodyString" // Ham yanıtı da ekleyelim
            }
        } else {
            "Bilinmeyen sunucu hatası ($httpStatusCode)"
        }
    }

    suspend fun getPing(): Flow<Resource<PingResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.ping()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(parseErrorMessage(e.response()?.errorBody()?.string(), e.code())))
        } catch (e: IOException) {
            emit(Resource.Error("Ağ bağlantı hatası: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Ping sırasında bilinmeyen bir hata oluştu: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        }
    }

    suspend fun registerNewSpeaker(speakerNameValue: String, audioFile: File): Flow<Resource<RegisterSpeakerResponse>> = flow {
        emit(Resource.Loading())
        try {
            val speakerNameRequestBody = speakerNameValue.toRequestBody("text/plain".toMediaTypeOrNull())
            val audioFileRequestBody = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            val audioFilePart = MultipartBody.Part.createFormData("audio", audioFile.name, audioFileRequestBody)

            val response = apiService.registerSpeaker(
                speakerName = speakerNameRequestBody,
                audio = audioFilePart
            )
            // Sunucudan gelen 'success' alanına göre kontrol edelim
            if (response.success) {
                emit(Resource.Success(response))
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(parseErrorMessage(e.response()?.errorBody()?.string(), e.code())))
        } catch (e: IOException) {
            emit(Resource.Error("Ağ bağlantı hatası: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Konuşmacı kaydedilirken bilinmeyen bir hata oluştu: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        }
    }

    suspend fun getAllSpeakers(): Flow<Resource<ListSpeakersResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.listSpeakers()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(parseErrorMessage(e.response()?.errorBody()?.string(), e.code())))
        } catch (e: IOException) {
            emit(Resource.Error("Ağ bağlantı hatası: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Konuşmacılar listelenirken bilinmeyen bir hata oluştu: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        }
    }

    suspend fun performTestSpeakerRecognition(
        audioFile: File,
        speakerId: String? = null // Bu parametre eklendi
    ): Flow<Resource<TestSpeakerResponse>> = flow {
        emit(Resource.Loading(null, true)) // isInitialLoading eklendi
        try {
            val requestFile = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)

            // speakerId'yi RequestBody'ye dönüştür (eğer varsa)
            val speakerIdPart = speakerId?.let {
                RequestBody.create("text/plain".toMediaTypeOrNull(), it)
            }

            // apiService.testSpeakerRecognition çağrısı güncellendi
            val response = apiService.testSpeakerRecognition(audioPart, speakerIdPart)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            val errorBodyString = e.response()?.errorBody()?.string()
            val errorMessage = parseErrorMessage(errorBodyString, e.code())
            emit(Resource.Error(errorMessage, null))
        } catch (e: IOException) {
            emit(Resource.Error("Ağ bağlantı hatası: ${e.message}", null))
        } catch (e: Exception) {
            // Log.e("SpeakerRepository", "Test tanıma sırasında beklenmedik hata", e) // DEBUG LOG TEMİZLENDİ
            emit(Resource.Error(e.message ?: "Test tanıma sırasında bilinmeyen bir hata oluştu", null))
        }
    }
suspend fun deleteSpeaker(speakerId: String): Flow<Resource<DeleteSpeakerResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteSpeaker(speakerId)
            if (response.isSuccessful) {
                val deleteSpeakerResponse = response.body()
                if (deleteSpeakerResponse != null) {
                    // Sunucudan gelen 'success' alanına göre kontrol edelim
                    // DeleteSpeakerResponse'da success alanı olmayabilir, doğrudan mesajı kontrol edebiliriz
                    // veya HTTP durum koduna göre işlem yapabiliriz.
                    // Şimdilik, body varsa ve success true ise başarılı kabul edelim.
                    // Eğer DeleteSpeakerResponse'da success alanı yoksa, bu kontrolü kaldırıp
                    // doğrudan Resource.Success(deleteSpeakerResponse) emit edebiliriz.
                    // API_ENDPOINTS_GUIDE.md'ye göre /speakers/{speaker_id} (DELETE)
                    // başarılı olduğunda {"message": "Speaker deleted successfully", "speaker_id": "..."} döner.
                    // Bu durumda 'success' alanı yok.
                    emit(Resource.Success(deleteSpeakerResponse))
                } else {
                    // Yanıt başarılı (2xx) ama body boş. Bu durum endpoint tanımına göre beklenmedik olabilir.
                    emit(Resource.Error("Konuşmacı silindi ancak sunucudan boş yanıt geldi."))
                }
            } else {
                // HTTP hata durumu (4xx, 5xx)
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (e: HttpException) {
            // Bu blok genellikle Retrofit'in Response<T> sarmalayıcısı kullanılmadığında veya
            // ağ katmanında daha düşük seviyeli bir hata olduğunda tetiklenir.
            // Response<T> kullandığımız için çoğu HTTP hatası yukarıdaki else bloğunda yakalanır.
            emit(Resource.Error(parseErrorMessage(e.response()?.errorBody()?.string(), e.code())))
        } catch (e: IOException) {
            // Ağ bağlantısı veya I/O hatası
            emit(Resource.Error("Ağ bağlantı hatası: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        } catch (e: Exception) {
            // Diğer beklenmedik hatalar
            emit(Resource.Error("Konuşmacı silinirken bilinmeyen bir hata oluştu: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        }
    }

    suspend fun trainModel(): Flow<Resource<BaseResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.trainModel()
            // Sunucudan gelen 'success' alanına göre kontrol edelim
            // BaseResponse'da success alanı olmayabilir, doğrudan mesajı kontrol edebiliriz
            // veya HTTP durum koduna göre işlem yapabiliriz.
            // API_ENDPOINTS_GUIDE.md'ye göre /train_model
            // başarılı olduğunda {"message": "Training started successfully.", "success": true} döner.
            if (response.success) { // BaseResponse'da success alanı olduğunu varsayıyoruz.
                emit(Resource.Success(response))
            } else {
                emit(Resource.Error(response.message ?: "Model eğitimi başlatılamadı ancak sunucudan ek bilgi gelmedi."))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(parseErrorMessage(e.response()?.errorBody()?.string(), e.code())))
        } catch (e: IOException) {
            emit(Resource.Error("Ağ bağlantı hatası: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Model eğitimi sırasında bilinmeyen bir hata oluştu: ${e.localizedMessage ?: e.message ?: "Bilinmiyor"}"))
        }
    }
 
    // Eski registerSpeaker ve recognizeSpeaker fonksiyonları kaldırıldı.
    // Onların yerine registerNewSpeaker ve performTestSpeakerRecognition geldi.
}