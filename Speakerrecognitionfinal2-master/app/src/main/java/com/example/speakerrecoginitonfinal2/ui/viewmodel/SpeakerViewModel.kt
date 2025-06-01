package com.example.speakerrecoginitonfinal2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Eski model importları güncelleniyor
import com.example.speakerrecoginitonfinal2.data.model.BaseResponse
import com.example.speakerrecoginitonfinal2.data.model.DeleteSpeakerResponse
import com.example.speakerrecoginitonfinal2.data.model.PingResponse
import com.example.speakerrecoginitonfinal2.data.model.RegisterSpeakerResponse
import com.example.speakerrecoginitonfinal2.data.model.TestSpeakerResponse
import com.example.speakerrecoginitonfinal2.data.model.SpeakerInfo // SpeakerInfo importu eklendi
import com.example.speakerrecoginitonfinal2.data.model.ListSpeakersResponse // ListSpeakersResponse importu eklendi
// SpeakerRegistrationRequest artık repository katmanında oluşturulmuyor, doğrudan parametreler gönderiliyor.
import com.example.speakerrecoginitonfinal2.data.repository.SpeakerRepository
import com.example.speakerrecoginitonfinal2.data.util.Resource
import com.example.speakerrecoginitonfinal2.R // R importu eklendi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import kotlinx.coroutines.channels.Channel // Channel için eklendi (SharedFlow alternatifi olarak düşünülebilir ama SharedFlow daha uygun)
// SharedFlow zaten kotlinx.coroutines.flow altında olduğu için ayrı import gerekmez.

class SpeakerViewModel(
    private val repository: SpeakerRepository,
    private val application: Application
) : ViewModel() {

    private val _registrationState = MutableStateFlow<Resource<RegisterSpeakerResponse>>(Resource.Loading(null)) // Tip güncellendi
    val registrationState: StateFlow<Resource<RegisterSpeakerResponse>> = _registrationState.asStateFlow()

    private val _recognitionState = MutableStateFlow<Resource<TestSpeakerResponse>>(Resource.Loading(null)) // Tip güncellendi
    val recognitionState: StateFlow<Resource<TestSpeakerResponse>> = _recognitionState.asStateFlow()

    private val _pingState = MutableStateFlow<Resource<PingResponse>>(Resource.Loading(null))
    val pingState: StateFlow<Resource<PingResponse>> = _pingState.asStateFlow()

    // Konuşmacı Listesi için State
    private val _speakersListState = MutableStateFlow<Resource<List<SpeakerInfo>>>(Resource.Loading(null, true))
    val speakersListState: StateFlow<Resource<List<SpeakerInfo>>> = _speakersListState.asStateFlow()

    // Seçili Konuşmacı için State
    private val _selectedSpeaker = MutableStateFlow<SpeakerInfo?>(null)
    val selectedSpeaker: StateFlow<SpeakerInfo?> = _selectedSpeaker.asStateFlow()

    // Kullanıcının girdiği konuşmacı adını tutmak için bir StateFlow
    private val _speakerNameInput = MutableStateFlow("")
    val speakerNameInput: StateFlow<String> = _speakerNameInput.asStateFlow()

    // Yükleme durumu için StateFlow
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Test Sonucu BottomSheet için State'ler
    private val _showTestResultSheet = MutableStateFlow(false)
    val showTestResultSheet: StateFlow<Boolean> = _showTestResultSheet.asStateFlow()

    private val _currentTestResultForSheet = MutableStateFlow<TestSpeakerResponse?>(null)
    val currentTestResultForSheet: StateFlow<TestSpeakerResponse?> = _currentTestResultForSheet.asStateFlow()

    // UI Events için
    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class ShowAlertDialog(val title: String, val message: String) : UiEvent()
    }
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Delete Speaker State
    private val _deleteSpeakerResult = MutableStateFlow<Resource<DeleteSpeakerResponse>?>(null)
    val deleteSpeakerResult: StateFlow<Resource<DeleteSpeakerResponse>?> = _deleteSpeakerResult.asStateFlow()

    // Train Model State
    private val _trainModelResult = MutableStateFlow<Resource<BaseResponse>?>(null)
    val trainModelResult: StateFlow<Resource<BaseResponse>?> = _trainModelResult.asStateFlow()
 
 
    fun onSpeakerNameChange(newName: String) {
        _speakerNameInput.value = newName
    }

    // Fonksiyon imzası ve çağrısı güncellendi
    fun registerNewSpeaker(audioFile: File) { // Artık ses dosyası da alıyor
        val speakerName = _speakerNameInput.value
        if (speakerName.isBlank()) {
            _registrationState.value = Resource.Error(application.getString(R.string.speaker_name_cannot_be_empty))
            return
        }
        if (!audioFile.exists() || audioFile.length() == 0L) {
            _registrationState.value = Resource.Error(application.getString(R.string.error_invalid_audio_file))
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _registrationState.value = Resource.Loading() // Yükleniyor durumunu ayarla
            try {
                repository.registerNewSpeaker(speakerName, audioFile) // Yeni repository fonksiyonu çağrılıyor
                    .collect { result -> _registrationState.value = result }
            } finally {
                _isLoading.value = false
            }
        }
    }
 
    // Fonksiyon güncellendi, API şu an ses dosyası almıyor
    fun performTestRecognition(audioFile: File, speakerId: String? = null) { // Parametreler eklendi
        viewModelScope.launch {
            _isLoading.value = true
            _recognitionState.value = Resource.Loading(null, true) // isInitialLoading eklendi
            try {
                repository.performTestSpeakerRecognition(audioFile, speakerId) // Güncellenmiş repository fonksiyonu çağrılıyor
                    .collect { result ->
                        _recognitionState.value = result
                        if (result is Resource.Success && result.data != null) {
                            _currentTestResultForSheet.value = result.data
                            _showTestResultSheet.value = true
                        } else if (result is Resource.Error) {
                            // Hata durumunda BottomSheet'i gösterme veya önceki veriyi temizle
                            _currentTestResultForSheet.value = null // Hata mesajı için null olmayan bir response da oluşturulabilir
                            _showTestResultSheet.value = true // Hata mesajını da BottomSheet'te göstermek için true yapılabilir. Şimdilik sadece başarıda gösteriyoruz.
                                                            // Ya da hata mesajı için ayrı bir Toast/Snackbar kullanılabilir.
                                                            // Plandaki gibi hata mesajı mevcut yöntemle (Toast/Snackbar) gösterilecekse burası false kalmalı.
                                                            // Şimdilik sadece başarı durumunda BottomSheet'i açıyoruz.
                        }
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
 

    fun pingServer() {
        viewModelScope.launch {
            _pingState.value = Resource.Loading()
            repository.getPing()
                .collect { result -> _pingState.value = result }
        }
    }

    fun loadSpeakers() {
        viewModelScope.launch {
            _speakersListState.value = Resource.Loading(null, true) // Yükleme başladığında state'i güncelle
            repository.getAllSpeakers().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _speakersListState.value = Resource.Success(result.data?.speakers ?: emptyList())
                    }
                    is Resource.Error -> {
                        _speakersListState.value = Resource.Error(result.message ?: application.getString(R.string.error_loading_speaker_list_generic), emptyList())
                    }
                    is Resource.Loading -> {
                        // Eğer repository'den tekrar Loading gelirse (genellikle gelmez ama defensive programming)
                        if (result.isInitialLoading) { // Sadece ilk yükleme ise dikkate al
                             _speakersListState.value = Resource.Loading(null, true)
                        }
                    }
                }
            }
        }
    }

    fun onSpeakerSelected(speaker: SpeakerInfo?) { // Nullable yapalım ki seçimi kaldırabilsin
        _selectedSpeaker.value = speaker
    }

    fun clearSpeakersListState() { // Bu fonksiyon UI'dan çağrılmayacaksa gerekmeyebilir veya farklı bir mantıkla kullanılabilir
        _speakersListState.value = Resource.Loading(null, true)
        _selectedSpeaker.value = null
    }


    // İsteğe bağlı: Kayıt veya tanıma işlemi bittikten sonra state'i sıfırlamak için
    fun clearRegistrationState() {
        _registrationState.value = Resource.Loading(null, true) // isInitialLoading eklendi
        _speakerNameInput.value = "" // İsim alanını da temizleyebiliriz
    }

    fun clearRecognitionState() {
        _recognitionState.value = Resource.Loading(null, true) // isInitialLoading eklendi
        _currentTestResultForSheet.value = null // BottomSheet verisini de temizle
        _showTestResultSheet.value = false // BottomSheet'i gizle
    }

    // BottomSheet kapatıldığında çağrılacak fonksiyon
    fun dismissTestResultSheet() {
        _showTestResultSheet.value = false
        // İsteğe bağlı olarak _currentTestResultForSheet'i burada null yapabiliriz
        // veya clearRecognitionState çağrıldığında zaten null olmasını bekleyebiliriz.
        // Şimdilik sadece show state'ini false yapalım, veri HomeScreen'de bir sonraki açılışa kadar kalabilir
        // ya da clearRecognitionState() çağrısı ile temizlenir.
        // En temizi, kapatıldığında recognition state'ini de temizlemek.
        clearRecognitionState()
    }

    fun clearPingState() {
        _pingState.value = Resource.Loading(null, true) // isInitialLoading eklendi
    }
fun deleteSpeaker(speaker: SpeakerInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            // Geçici olarak konuşmacı listesini güncelleyerek anlık geri bildirim sağlayabiliriz
            // Ancak API çağrısı başarısız olursa geri almak gerekebilir.
            // Şimdilik API'den sonra listeyi yenileyeceğiz.
            // _speakersListState.update { currentState ->
            //     if (currentState is Resource.Success) {
            //         Resource.Success(currentState.data?.filterNot { it.id == speaker.id } ?: emptyList())
            //     } else {
            //         currentState
            //     }
            // }
            // if (_selectedSpeaker.value?.id == speaker.id) {
            //     _selectedSpeaker.value = null
            // }

            try {
                repository.deleteSpeaker(speaker.id).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Silme başarılı olduğunda konuşmacı listesini yeniden yükle
                            val successMessage = result.data.message ?: application.getString(R.string.speaker_deleted_successfully_no_message, speaker.name)
                            _uiEvent.emit(UiEvent.ShowAlertDialog(
                                title = application.getString(R.string.info),
                                message = successMessage
                            ))
                            loadSpeakers() // Bu, _speakersListState'i güncelleyecektir.
                            // Eğer silinen konuşmacı seçili ise, seçimi kaldır
                            if (_selectedSpeaker.value?.id == speaker.id) {
                                _selectedSpeaker.value = null
                            }
                        }
                        is Resource.Error -> {
                            val currentData = if (_speakersListState.value is Resource.Success) {
                                (_speakersListState.value as Resource.Success<List<SpeakerInfo>>).data
                            } else {
                                null
                            }
                            val errorMessage = result.message ?: application.getString(R.string.error_deleting_speaker_generic)
                            _speakersListState.value = Resource.Error(
                                errorMessage,
                                currentData
                            )
                            _uiEvent.emit(UiEvent.ShowAlertDialog(
                                title = application.getString(R.string.error),
                                message = errorMessage
                            ))
                        }
                        is Resource.Loading -> {
                            // Silme işlemi sırasında bir yükleme durumu gösterilebilir.
                            // _isLoading zaten bu işlevi görüyor.
                        }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSpeakerSelection(speaker: SpeakerInfo) {
        if (_selectedSpeaker.value?.id == speaker.id) {
            _selectedSpeaker.value = null // Zaten seçiliyse seçimi kaldır
        } else {
            _selectedSpeaker.value = speaker // Değilse seç
        }
    }

    // Yeni deleteSpeaker fonksiyonu (speakerId ile)
    fun deleteSpeaker(speakerId: String) {
        viewModelScope.launch {
            _deleteSpeakerResult.value = Resource.Loading()
            repository.deleteSpeaker(speakerId).collect { result ->
                _deleteSpeakerResult.value = result
                if (result is Resource.Success) {
                    loadSpeakers() // Konuşmacı listesini yenile
                }
            }
        }
    }

    fun trainModel() {
        viewModelScope.launch {
            _trainModelResult.value = Resource.Loading()
            // TODO: Repository'de trainModel fonksiyonu implemente edildiğinde aşağıdaki satırları aktive et
            // repository.trainModel().collect { result ->
            //     _trainModelResult.value = result
            // }
            // Şimdilik manuel olarak bir sonuç emit edelim (test amaçlı)
            kotlinx.coroutines.delay(1000) // Simüle edilmiş ağ gecikmesi
            _trainModelResult.value = Resource.Success(BaseResponse(message = "Training started successfully.", success = true))
        }
    }
}