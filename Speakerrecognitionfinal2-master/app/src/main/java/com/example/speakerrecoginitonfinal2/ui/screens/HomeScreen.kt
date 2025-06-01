package com.example.speakerrecoginitonfinal2.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
// import androidx.compose.material.icons.filled.Error // Eklendi - Geçici olarak yorum satırı
import androidx.compose.material.icons.filled.CheckCircle // Eklendi
import androidx.compose.material3.*
// Material3 PullRefresh yerine Accompanist SwipeRefresh kullanılacak
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.vector.ImageVector // Eklendi
import androidx.compose.ui.zIndex
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.util.Log
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.example.speakerrecoginitonfinal2.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.speakerrecoginitonfinal2.ui.components.RecordButton
import com.example.speakerrecoginitonfinal2.ui.components.TestButton
import com.example.speakerrecoginitonfinal2.ui.components.TimerText
// TestResultDialog importu kaldırılacak, yerine TestResultBottomSheet gelecek
import com.example.speakerrecoginitonfinal2.ui.components.AddSpeakerDialog
import com.example.speakerrecoginitonfinal2.ui.components.SpeakerList
import com.example.speakerrecoginitonfinal2.ui.components.InfoDialog // Eklendi
import com.example.speakerrecoginitonfinal2.ui.components.TestResultBottomSheet // Eklendi
import androidx.compose.material3.rememberModalBottomSheetState // Eklendi
import com.example.speakerrecoginitonfinal2.data.recorder.WavAudioRecorder
import com.example.speakerrecoginitonfinal2.ui.viewmodel.SpeakerViewModel
import com.example.speakerrecoginitonfinal2.ui.viewmodel.ViewModelFactory
import com.example.speakerrecoginitonfinal2.data.util.Resource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import android.Manifest
import com.example.speakerrecoginitonfinal2.data.model.SpeakerInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    speakerViewModel: SpeakerViewModel
) {
    val isTestMode = false
    val isServerAvailable = true

    // RECORD_AUDIO izni için state
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // Collect SpeakerViewModel state
    val newSpeakerNameInput by speakerViewModel.speakerNameInput.collectAsState()
    val registrationState by speakerViewModel.registrationState.collectAsState()
    val recognitionState by speakerViewModel.recognitionState.collectAsState()
    val pingState by speakerViewModel.pingState.collectAsState()
    val speakersListState by speakerViewModel.speakersListState.collectAsState()
    val selectedSpeaker by speakerViewModel.selectedSpeaker.collectAsState()
    val isLoading by speakerViewModel.isLoading.collectAsState() // isLoading state'i eklendi
    val showTestResultSheet by speakerViewModel.showTestResultSheet.collectAsState() // Eklendi
    val currentTestResultForSheet by speakerViewModel.currentTestResultForSheet.collectAsState() // Eklendi
 
    // Dialog states
    var showAddSpeakerDialog by remember { mutableStateOf(false) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    // var showTestResultDialog by remember { mutableStateOf(false) } // Bu state kaldırılacak, ViewModel'deki showTestResultSheet kullanılacak
    var showInfoDialog by remember { mutableStateOf(false) } // InfoDialog için state
    var dialogTitle by remember { mutableStateOf("") } // InfoDialog başlığı
    var dialogMessage by remember { mutableStateOf("") } // InfoDialog mesajı
    var dialogIcon by remember { mutableStateOf<ImageVector?>(null) } // InfoDialog ikonu
    var speakerToDelete by remember { mutableStateOf<SpeakerInfo?>(null) } // Silme işlemi için
    var showDeleteDialog by remember { mutableStateOf(false) } // Silme onayı dialogu için

    // Drawer states - KALDIRILDI
    // var isDrawerOpen by remember { mutableStateOf(false) }
    // val density = LocalDensity.current
    // val drawerWidth = 300.dp
    // var offsetX by remember { mutableStateOf(0f) }

    // Audio recording setup
    val context = LocalContext.current
    var audioFileForRegistration: File? by remember { mutableStateOf(null) }

    val registrationAudioRecorder = remember {
        val outputDir = context.filesDir
        val outputFile = File(outputDir, "registration_audio.wav")
        audioFileForRegistration = outputFile
        WavAudioRecorder(context, outputFile)
    }
    var isRecordingForRegistration by remember { mutableStateOf(false) }

    // Test için ses kaydı state'leri
    var audioFileForTest: File? by remember { mutableStateOf(null) }
    val testAudioRecorder = remember(context) {
        val outputDir = context.filesDir
        val outputFile = File(outputDir, "test_recognition_audio.wav")
        audioFileForTest = outputFile // Dosya yolu burada set ediliyor
        WavAudioRecorder(context, outputFile)
    }
    var isRecordingForTest by remember { mutableStateOf(false) }
    // maxRecordTimeSeconds zaten yukarıda tanımlı (satır 87)

    // Zamanlayıcı için state'ler
    val maxRecordTimeSeconds = 15 // Bu satır zaten var, currentRemainingTimeForTest için kullanılacak
    var currentRemainingTime by remember { mutableStateOf(maxRecordTimeSeconds) } // Bu kayıt için
    var currentRemainingTimeForTest by remember { mutableStateOf(maxRecordTimeSeconds) } // Bu test için
    var recordingJob by remember { mutableStateOf<Job?>(null) } // Bu kayıt için
    var testRecordingJob by remember { mutableStateOf<Job?>(null) } // Bu test için
    val coroutineScope = rememberCoroutineScope()
    val modalSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            // Eğer BottomSheet sürüklenerek kapatılmaya çalışılıyorsa (hedef durum Hidden ise),
            // bu değişikliği engelle. Kapatma sadece "Kapat" butonu ile yapılacak.
            if (sheetValue == SheetValue.Hidden) {
                false
            } else {
                true
            }
        }
    ) // Eklendi ve güncellendi
    // val snackbarHostState = remember { SnackbarHostState() } // Snackbar kaldırıldı
    LaunchedEffect(Unit) {
        speakerViewModel.loadSpeakers()
    }

    // Yan kaydırma gesture'ı için - KALDIRILDI
    // val maxDragOffset = with(density) { drawerWidth.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // .pointerInput(Unit) { // KALDIRILDI
            //     detectHorizontalDragGestures(
            //         onDragEnd = {
            //             if (offsetX > maxDragOffset / 2) {
            //                 offsetX = maxDragOffset
            //                 isDrawerOpen = true
            //             } else {
            //                 offsetX = 0f
            //                 isDrawerOpen = false
            //             }
            //         }
            //     ) { _, dragAmount ->
            //         val newOffset = (offsetX + dragAmount).coerceIn(0f, maxDragOffset)
            //         offsetX = newOffset
            //         isDrawerOpen = newOffset > maxDragOffset / 2
            //     }
            // } // KALDIRILDI
    ) {
        // Ana içerik
        Scaffold(
            // snackbarHost = { SnackbarHost(snackbarHostState) }, // Snackbar kaldırıldı
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    // navigationIcon = { // KALDIRILDI
                    //     IconButton(onClick = {
                    //         if (isDrawerOpen) {
                    //             offsetX = 0f
                    //             isDrawerOpen = false
                    //         } else {
                    //             offsetX = maxDragOffset
                    //             isDrawerOpen = true
                    //         }
                    //     }) {
                    //         Icon(
                    //             imageVector = Icons.Default.Menu,
                    //             contentDescription = stringResource(R.string.drawer_menu_content_description)
                    //         )
                    //     }
                    // }, // KALDIRILDI
                    actions = {
                        IconButton(onClick = { showAddSpeakerDialog = true }) { // Dialog'u göster
                            Text("+", fontSize = 24.sp)
                        }
                    }
                )
            }
        ) { paddingValues -> // Scaffold content lambda'sı burada başlıyor

            val speakersListStateValue = speakersListState // speakersListState zaten Resource tipinde
            val isRefreshing = speakersListStateValue is Resource.Loading
            val swipeRefreshState = rememberSwipeRefreshState(
                isRefreshing = isRefreshing
            )

            SwipeRefresh( // SwipeRefresh ile değiştirildi
                state = swipeRefreshState,
                onRefresh = { speakerViewModel.loadSpeakers() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // Scaffold padding'i SwipeRefresh'e uygulandı
                indicator = { state, trigger ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = trigger,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Column( // Orijinal ana Column - Scroll özelliği eklendi
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Banner gösterimi
                    if (isTestMode) {
                // Test modu banner'ı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Uyarı",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.test_mode_banner),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (!isServerAvailable) {
                // Sunucu bağlantısı yok banner'ı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Uyarı",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.server_connection_lost_banner),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Konuşmacı Listesi Alanı
                Text(
                    stringResource(R.string.speakers_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                when (val listState = speakersListState) {
                    is Resource.Loading<*> -> { // Tip argümanı eklendi
                        if (listState.isInitialLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (listState.data.isNullOrEmpty()){
                             Text(stringResource(R.string.no_speakers_registered), modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        // Eğer yükleme devam ederken eski veri varsa onu göstermeye devam edebiliriz.
                        // Şimdilik sadece ilk yüklemede indicator gösteriyoruz.
                        // Eğer liste zaten doluysa ve refresh yapılıyorsa, indicator yerine listeyi göstermeye devam edebiliriz.
                        // Bu örnekte, eğer data varsa ve initial loading değilse listeyi gösteriyoruz.
                        if (listState.data?.isNotEmpty() == true && !listState.isInitialLoading) { // Güvenli çağrı ve isNotEmpty
                            SpeakerList(
                                speakers = listState.data ?: emptyList(),
                                selectedSpeaker = selectedSpeaker,
                                onSpeakerClick = { speakerViewModel.toggleSpeakerSelection(it) },
                                onDeleteSpeaker = { speaker ->
                                    speakerToDelete = speaker
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.heightIn(max = 350.dp)
                            )
                        }
                    }
                    is Resource.Success<*> -> { // Tip argümanı eklendi
                        if (listState.data.isNullOrEmpty()) {
                            Text(stringResource(R.string.no_speakers_registered), modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            SpeakerList(
                                speakers = listState.data!!, // data burada null olamaz (Success durumunda)
                                selectedSpeaker = selectedSpeaker,
                                onSpeakerClick = { speakerViewModel.toggleSpeakerSelection(it) },
                                onDeleteSpeaker = { speaker ->
                                    speakerToDelete = speaker
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.heightIn(max = 224.dp)
                            )
                        }
                    }
                    is Resource.Error<*> -> { // Tip argümanı eklendi
                        Text(
                            stringResource(R.string.error_loading_speakers) + " ${listState.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                selectedSpeaker?.let {
                    Text(
                        stringResource(R.string.selected_speaker_label) + " ${it.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally)
                    )
                }


                Spacer(modifier = Modifier.height(20.dp))

                // Yeni Konuşmacı Kayıt Alanı
                Text(stringResource(R.string.new_speaker_registration_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newSpeakerNameInput,
                    onValueChange = { speakerViewModel.onSpeakerNameChange(it) },
                    label = { Text(stringResource(R.string.speaker_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                RecordButton( // Bu butonu yeni konuşmacı kaydı için ses almak üzere kullanacağız
                    text = if (isRecordingForRegistration) stringResource(R.string.stop_record_and_save) else stringResource(R.string.start_record_new_speaker),
                    isEnabled = newSpeakerNameInput.isNotBlank(), // İsim girildiyse aktif
                    isRecording = isRecordingForRegistration,
                    // onClick ve onStop RecordButton'a taşınacak, burada sadece başlatma/durdurma mantığı kalacak
                    // Bu RecordButton çağrısı güncellenecek. Şimdilik mevcut onClick'i koruyalım ama onStop'ı da ekleyelim.
                    onClick = {
                        if (!isRecordingForRegistration) { // Sadece kayıt başlatma
                            if (recordAudioPermissionState.status.isGranted) {
                                audioFileForRegistration = registrationAudioRecorder.getAudioFile()
                                registrationAudioRecorder.startRecording()
                                Toast.makeText(context, context.getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
                                isRecordingForRegistration = true
                                currentRemainingTime = maxRecordTimeSeconds
                                recordingJob?.cancel()
                                recordingJob = coroutineScope.launch {
                                    for (i in maxRecordTimeSeconds downTo 1) {
                                        currentRemainingTime = i
                                        delay(1000)
                                    }
                                    if (isRecordingForRegistration) {
                                        // Log.d("HomeScreen", "Maksimum kayıt süresi doldu, kayıt durduruluyor.") // DEBUG LOG TEMİZLENDİ
                                        registrationAudioRecorder.stopRecording() // Önce kaydı durdur
                                        isRecordingForRegistration = false // Sonra state'i güncelle
                                        Toast.makeText(context, context.getString(R.string.recording_stopped_and_saved), Toast.LENGTH_SHORT).show()
                                        // recordingJob?.cancel() // Job zaten bitti, tekrar cancel'a gerek yok.
                                        audioFileForRegistration?.let { file -> // Değişken adı 'file' olarak değiştirildi
                                            if (file.exists() && file.length() > 0) { // Güvenli çağrı zaten yapılıyor
                                                speakerViewModel.registerNewSpeaker(file)
                                            } else {
                                                Log.e("HomeScreen", "Otomatik durdurma sonrası kayıt dosyası bulunamadı veya boş.")
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (recordAudioPermissionState.status.shouldShowRationale) {
                                    showPermissionRationaleDialog = true
                                } else {
                                    recordAudioPermissionState.launchPermissionRequest()
                                    Toast.makeText(context, "Ses kayıt izni isteniyor...", Toast.LENGTH_SHORT).show()
                                }
                                // Log.d("HomeScreen", "RECORD_AUDIO izni istendi/gerekçe gösterilecek. Durum: ${recordAudioPermissionState.status}, Rationale: ${recordAudioPermissionState.status.shouldShowRationale}") // DEBUG LOG TEMİZLENDİ
                            }
                        } else { // Kayıt durdurma (manuel)
                            recordingJob?.cancel()
                            registrationAudioRecorder.stopRecording()
                            isRecordingForRegistration = false
                            Toast.makeText(context, context.getString(R.string.recording_stopped_and_saved), Toast.LENGTH_SHORT).show()
                            audioFileForRegistration?.let { file -> // Değişken adı 'file' olarak değiştirildi
                                if (file.exists() && file.length() > 0) { // Güvenli çağrı zaten yapılıyor
                                    speakerViewModel.registerNewSpeaker(file)
                                } else {
                                    Log.e("HomeScreen", "Manuel durdurma sonrası kayıt dosyası bulunamadı veya boş.")
                                }
                            }
                        }
                    }
                    // onStop parametresi RecordButton'dan kaldırılacak, çünkü onClick artık hem başlatma hem durdurma yapıyor.
                )

                Spacer(modifier = Modifier.height(28.dp)) // Test Button
                // TestButton yerine RecordButton benzeri bir yapı kullanacağız veya TestButton'ı güncelleyeceğiz.
                // Şimdilik TestButton'ı güncelleyelim.
                TestButton(
                    text = if (isRecordingForTest) {
                        stringResource(R.string.stop_record_and_recognize)
                    } else {
                        selectedSpeaker?.let { speaker ->
                            stringResource(R.string.start_record_specific_test, speaker.name)
                        } ?: stringResource(R.string.start_record_for_test)
                    },
                    isEnabled = !isRecordingForRegistration && !isRecordingForTest, // Yeni kayıt veya başka bir test kaydı yoksa
                    isTestRecording = isRecordingForTest, // isRecordingForTest state'ine bağlandı
                    onClick = {
                        if (!isRecordingForTest) { // Test için kayıt başlatma
                            if (recordAudioPermissionState.status.isGranted) {
                                audioFileForTest = testAudioRecorder.getAudioFile() // Dosyayı al
                                testAudioRecorder.startRecording()
                                Toast.makeText(context, context.getString(R.string.test_recording_started), Toast.LENGTH_SHORT).show()
                                isRecordingForTest = true
                                currentRemainingTimeForTest = maxRecordTimeSeconds
                                testRecordingJob?.cancel() // Önceki job'ı iptal et
                                testRecordingJob = coroutineScope.launch {
                                    for (i in maxRecordTimeSeconds downTo 1) {
                                        currentRemainingTimeForTest = i
                                        delay(1000)
                                    }
                                    if (isRecordingForTest) { // Süre dolduysa ve hala kayıt yapılıyorsa
                                        testAudioRecorder.stopRecording()
                                        isRecordingForTest = false
                                        audioFileForTest = testAudioRecorder.getAudioFile() // Bu satır zaten var
                                        audioFileForTest?.let { file ->
                                            if (file.exists() && file.length() > 0) { // Güvenli çağrı zaten yapılıyor
                                                speakerViewModel.performTestRecognition(file, selectedSpeaker?.id)
                                            } else {
                                                Log.e("HomeScreen", "Test kaydı (süre dolumu) sonrası dosya bulunamadı veya boş.")
                                                Toast.makeText(context, context.getString(R.string.test_recording_failed_auto_stop), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            } else { // İzin yoksa
                                if (recordAudioPermissionState.status.shouldShowRationale) {
                                    showPermissionRationaleDialog = true
                                } else {
                                    recordAudioPermissionState.launchPermissionRequest()
                                }
                            }
                        } else { // Test için kaydı durdurma (manuel)
                            testRecordingJob?.cancel()
                            testAudioRecorder.stopRecording()
                            isRecordingForTest = false
                            audioFileForTest = testAudioRecorder.getAudioFile() // Bu satır zaten var
                            audioFileForTest?.let { file ->
                                if (file.exists() && file.length() > 0) { // Güvenli çağrı zaten yapılıyor
                                    speakerViewModel.performTestRecognition(file, selectedSpeaker?.id)
                                } else {
                                    Log.e("HomeScreen", "Test kaydı (manuel durdurma) sonrası dosya bulunamadı veya boş.")
                                    Toast.makeText(context, context.getString(R.string.test_recording_failed_manual_stop), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                // Debug amaçlı manuel test sonuçlarını göster butonu (Constants importu kaldırıldığı için bu da kaldırılmalı)
                // if (Constants.TEST_MODE) {
                //     Button(
                //         onClick = {
                //             recognitionViewModel.onEvent(RecognitionUiEvent.ForceShowTestDialog(isTestMode))
                //         },
                //         colors = ButtonDefaults.buttonColors(
                //             containerColor = MaterialTheme.colorScheme.errorContainer,
                //             contentColor = MaterialTheme.colorScheme.onErrorContainer
                //         )
                //     ) {
                //         Text("Test Sonuçlarını Göster (Debug)")
                //     }
                // }
                Spacer(modifier = Modifier.height(10.dp)) // Aralık azaltıldı
                Button(onClick = {
                    // Log.d("HomeScreen", "Ping Sunucu butonuna tıklandı.") // DEBUG LOG TEMİZLENDİ
                    speakerViewModel.pingServer()
                }) {
                    Text("Ping Sunucu")
                }
                Spacer(modifier = Modifier.height(10.dp)) // Aralık azaltıldı
                // StatusText Composable'ı Snackbar ile değiştirileceği için yorum satırı yapılıyor veya kaldırılabilir.
                // val statusText = remember(registrationState, recognitionState, pingState) { ... }
                // StatusText(text = if (statusText.isNotEmpty()) statusText else "Durum bekleniyor...")
             
                // Ping State için AlertDialog
                LaunchedEffect(pingState) {
                    when (val pState = pingState) {
                        is Resource.Loading<*> -> {
                            // Yükleniyor durumu için dialog gösterilmiyor
                        }
                        is Resource.Success<*> -> {
                            dialogTitle = "Ping Başarılı"
                            dialogMessage = pState.data?.message ?: "Sunucuya başarıyla ulaşıldı."
                            dialogIcon = Icons.Filled.CheckCircle
                            showInfoDialog = true
                            // speakerViewModel.clearPingState() // Dialog kapatıldığında temizlenecek
                        }
                        is Resource.Error<*> -> {
                            dialogTitle = "Ping Hatası"
                            dialogMessage = pState.message ?: "Sunucuya ulaşılamadı."
                            dialogIcon = Icons.Filled.Warning // Geçici olarak Warning
                            showInfoDialog = true
                            // speakerViewModel.clearPingState() // Dialog kapatıldığında temizlenecek
                        }
                    }
                }
             
                // Registration State için AlertDialog
                LaunchedEffect(registrationState) {
                    when (val regState = registrationState) {
                        is Resource.Loading<*> -> {
                            // Yükleniyor durumu için dialog gösterilmiyor
                        }
                        is Resource.Success<*> -> {
                            dialogTitle = "Kayıt Başarılı"
                            dialogMessage = "Konuşmacı '${regState.data?.speakerName}' başarıyla kaydedildi. ${regState.data?.message ?: ""}"
                            dialogIcon = Icons.Filled.CheckCircle
                            showInfoDialog = true
                            // speakerViewModel.clearRegistrationState() // Dialog kapatıldığında temizlenecek
                        }
                        is Resource.Error<*> -> {
                            dialogTitle = "Kayıt Hatası"
                            dialogMessage = regState.message ?: "Konuşmacı kaydedilemedi."
                            dialogIcon = Icons.Filled.Warning // Geçici olarak Warning
                            showInfoDialog = true
                            // speakerViewModel.clearRegistrationState() // Dialog kapatıldığında temizlenecek
                        }
                    }
                }
             
                // Recognition State için AlertDialog (Hata durumu) veya TestResultDialog (Başarı durumu)
                LaunchedEffect(recognitionState) {
                    when (val recState = recognitionState) {
                        is Resource.Loading<*> -> {
                            // Yükleniyor durumu için dialog gösterilmiyor
                        }
                        is Resource.Success<*> -> {
                            // Başarılı tanıma durumu ViewModel tarafından yönetiliyor (_showTestResultSheet)
                            // Bu blokta artık bir şey yapmaya gerek yok, BottomSheet ViewModel state'ine göre gösterilecek.
                            // Hata mesajı için InfoDialog kullanımı devam edebilir.
                            // Eğer recState.data null ise ve bu bir hata olarak kabul ediliyorsa, InfoDialog gösterilebilir.
                            // Ancak ViewModel'deki mantık zaten result.data null değilse _showTestResultSheet'i true yapıyor.
                            // Bu yüzden burada ek bir kontrol gerekmeyebilir.
                            // speakerViewModel.clearRecognitionState() çağrısı BottomSheet kapatıldığında yapılacak.
                        }
                        is Resource.Error<*> -> {
                            dialogTitle = "Tanıma Hatası"
                            dialogMessage = recState.message ?: "Konuşmacı tanınamadı."
                            dialogIcon = Icons.Filled.Warning // Geçici olarak Warning
                            showInfoDialog = true
                            // speakerViewModel.clearRecognitionState() çağrısı InfoDialog kapatıldığında yapılacak.
                        }
                    }
                }
             
                            Spacer(modifier = Modifier.height(27.dp))

                // Timer
                // TimerText(remainingTime = remainingTime) // TODO: TimerText özelliği eklenecek (bir sonraki adımda)
                if (isRecordingForRegistration) {
                    TimerText(remainingTime = currentRemainingTime.toLong())
                } else if (isRecordingForTest) { // Test kaydı için de timer göster
                    TimerText(remainingTime = currentRemainingTimeForTest.toLong())
                }
            } // Column kapanışı (ana içerik)

            // Add Speaker Dialog - Yeni ViewModel ile güncellendi
            if (showAddSpeakerDialog) {
                AddSpeakerDialog(
                    speakerName = newSpeakerNameInput, // ViewModel'den gelen ismi kullan
                    onSpeakerNameChange = { speakerViewModel.onSpeakerNameChange(it) }, // ViewModel'i güncelle
                    onAddSpeaker = {
                        showAddSpeakerDialog = false
                    },
                    onDismiss = {
                        showAddSpeakerDialog = false
                    }
                )
            }

            // InfoDialog gösterimi
            if (showInfoDialog) {
                InfoDialog(
                    title = dialogTitle,
                    message = dialogMessage,
                    icon = dialogIcon,
                    onDismissRequest = {
                        showInfoDialog = false
                        // İlgili state'i temizle
                        if (dialogTitle.startsWith("Ping")) speakerViewModel.clearPingState()
                        if (dialogTitle.startsWith("Kayıt")) speakerViewModel.clearRegistrationState()
                        if (dialogTitle.startsWith("Tanıma")) speakerViewModel.clearRecognitionState() // Sadece hata durumunda InfoDialog gösterildiği için
                    }
                )
            }
 
            // Test Sonuçları BottomSheet
            if (showTestResultSheet) {
                currentTestResultForSheet?.let { responseData ->
                    TestResultBottomSheet(
                        recognitionResponse = responseData,
                        onDismiss = {
                            speakerViewModel.dismissTestResultSheet() // ViewModel'deki fonksiyonu çağır
                        },
                        sheetState = modalSheetState
                    )
                }
            }

           // Silme Onay Dialogu
           if (showDeleteDialog) {
               AlertDialog(
                   onDismissRequest = { showDeleteDialog = false },
                   title = { Text("Konuşmacıyı Sil") },
                   text = { Text("${speakerToDelete?.name} adlı konuşmacıyı silmek istediğinizden emin misiniz?") },
                   confirmButton = {
                       Button(
                           onClick = {
                               speakerToDelete?.let { speakerViewModel.deleteSpeaker(it) }
                               showDeleteDialog = false
                           }
                       ) {
                           Text("Evet")
                       }
                   },
                   dismissButton = {
                       Button(onClick = { showDeleteDialog = false }) {
                           Text("Hayır")
                       }
                   }
               )
           }

            if (showPermissionRationaleDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionRationaleDialog = false },
                    title = { Text("İzin Gerekli") },
                    text = { Text("Yeni konuşmacı kaydı yapabilmek için ses kayıt iznine ihtiyacımız var. Bu izin, sadece kayıt sırasında mikrofonunuza erişmek için kullanılacaktır.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPermissionRationaleDialog = false
                                recordAudioPermissionState.launchPermissionRequest()
                                Toast.makeText(context, "Ses kayıt izni tekrar isteniyor...", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Anladım ve İzin Ver")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showPermissionRationaleDialog = false }) {
                            Text("İptal")
                        }
                    }
                )
            }
    } // 177. satırda başlayan Column'un kapanışı
        } // Scaffold content lambda'sının kapanışı

        // Yan drawer (konuşmacı listesi) - KALDIRILDI
        // if (isDrawerOpen) {
        //     Box(
        //         modifier = Modifier
        //             .fillMaxSize()
        //             .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
        //             .zIndex(1f) // Scrim'in içeriğin üzerinde olmasını sağlar
        //             .clickable { // Scrim'e tıklayınca drawer'ı kapat
        //                 offsetX = 0f
        //                 isDrawerOpen = false
        //             }
        //     )
        // }

        // Drawer içeriği - KALDIRILDI
        // Box(
        //     modifier = Modifier
        //         .fillMaxHeight()
        //         .width(drawerWidth)
        //         .offset { IntOffset(x = (offsetX - drawerWidth.toPx()).roundToInt(), y = 0) } // offsetX piksel cinsinden
        //         .background(MaterialTheme.colorScheme.surface)
        //         .zIndex(2f) // Drawer'ın scrim üzerinde olmasını sağlar
        // ) {
        //     if (isDrawerOpen || offsetX > 0f) { // offsetX > 0f kontrolü, drawer kapanırken içeriğin görünmesini sağlar
        //         SpeakersListScreen(
        //             viewModel = speakerViewModel, // speakerViewModel HomeScreen'den geliyor
        //             onSpeakerSelected = { speaker ->
        //                 speakerViewModel.onSpeakerSelected(speaker)
        //                 offsetX = 0f // Konuşmacı seçildiğinde drawer'ı kapat
        //                 isDrawerOpen = false
        //             },
        //             onClose = {
        //                 offsetX = 0f
        //                 isDrawerOpen = false
        //             }
        //         )
        //     }
        // }
 
        // Yükleme göstergesi
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)) // Yarı saydam arka plan
                    .zIndex(3f), // Diğer her şeyin üzerinde olması için yüksek zIndex
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

    } // SwipeRefresh'in kapanışı
    } // HomeScreen'in ana Box'ının kapanışı (Bu Box 150. satırda başlıyordu, pullRefresh Box'ı onun içinde)
      // Düzeltme: HomeScreen'in ana Box'ı (150. satır) artık pullRefresh Box'ı oldu.
      // Bu yüzden bir üstteki kapanış yorumu pullRefresh Box'ına ait.
      // Scaffold'un content lambda'sı içindeki Box'ın kapanışı.
} // HomeScreen Composable fonksiyonunun kapanışı

// HomeScreen.kt içindeki lokal TestResultDialog ve SpeakerList tanımları SİLİNDİ.
// Bu tanımlar artık ui/components paketindeki dosyalardan kullanılacak.
// SpeakerLazyColumn ve AddSpeakerDialog tanımları buradan kaldırıldı.