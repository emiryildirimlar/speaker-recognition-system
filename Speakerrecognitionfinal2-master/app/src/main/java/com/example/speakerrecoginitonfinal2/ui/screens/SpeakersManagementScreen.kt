package com.example.speakerrecoginitonfinal2.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box // Box importu eklendi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
// Material3 PullRefresh yerine Accompanist SwipeRefresh kullanılacak
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // Alignment importu eklendi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.speakerrecoginitonfinal2.data.model.SpeakerInfo
import com.example.speakerrecoginitonfinal2.data.util.Resource // Resource importu eklendi (speakersListState tipini kontrol etmek için)
import com.example.speakerrecoginitonfinal2.ui.components.SpeakerList
import com.example.speakerrecoginitonfinal2.ui.viewmodel.SpeakerViewModel
import kotlinx.coroutines.flow.collectLatest // collectLatest importu eklendi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakersManagementScreen(
    navController: NavController,
    speakerViewModel: SpeakerViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var speakerToDelete by remember { mutableStateOf<SpeakerInfo?>(null) }

    val speakersListState by speakerViewModel.speakersListState.collectAsState()
    val selectedSpeakerForHighlight by speakerViewModel.selectedSpeaker.collectAsState() // Bu sadece vurgulama için
    val snackbarHostState = remember { SnackbarHostState() }

    // ViewModel'den gelen UI event'lerini işlemek için state'ler
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogTitle by remember { mutableStateOf("") }
    var infoDialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        speakerViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SpeakerViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is SpeakerViewModel.UiEvent.ShowAlertDialog -> {
                    infoDialogTitle = event.title
                    infoDialogMessage = event.message
                    showInfoDialog = true
                }
            }
        }
    }

    // Konuşmacı listesi yüklendiğinde loglama (isteğe bağlı)
    LaunchedEffect(speakersListState) {
        if (speakersListState is com.example.speakerrecoginitonfinal2.data.util.Resource.Success) {
            Log.d("SpeakersManagementScreen", "Konuşmacılar yüklendi: ${speakersListState.data}")
        } else if (speakersListState is com.example.speakerrecoginitonfinal2.data.util.Resource.Error) {
            Log.e("SpeakersManagementScreen", "Konuşmacı listesi yüklenirken hata: ${speakersListState.message}")
        }
    }
     LaunchedEffect(Unit) {
        speakerViewModel.loadSpeakers() // Ekran açıldığında konuşmacıları yükle
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // SnackbarHost eklendi
        topBar = {
            TopAppBar(
                title = { Text("Konuşmacıları Yönet") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        val isRefreshing = speakersListState is Resource.Loading // Yükleme durumunu kontrol et
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
            SpeakerList(
                speakers = speakersListState.data ?: emptyList(),
                selectedSpeaker = selectedSpeakerForHighlight, // Vurgulama için seçili konuşmacı
                onSpeakerClick = { clickedSpeaker ->
                    speakerViewModel.toggleSpeakerSelection(clickedSpeaker)
                },
                onDeleteSpeaker = { speaker -> // SpeakerList'ten gelen yeni callback
                    speakerToDelete = speaker
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxSize() // SpeakerList'i tam boyutta göster
            )
        }

        // AlertDialog'lar Box'ın dışında, Scaffold'un content lambda'sında kalmaya devam edecek.
        // Box fillMaxSize olduğu için AlertDialog'lar Box'ın üzerine çizilecektir, bu sorun değil.
        if (showDeleteDialog && speakerToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    speakerToDelete = null
                },
                title = { Text("Konuşmacıyı Sil") },
                text = { Text("${speakerToDelete?.name} adlı konuşmacıyı silmek istediğinizden emin misiniz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            speakerToDelete?.let { speakerViewModel.deleteSpeaker(it) }
                            showDeleteDialog = false
                            speakerToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Evet")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            speakerToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    ) {
                        Text("Hayır")
                    }
                }
            )
        }

        // ViewModel'den gelen genel AlertDialog'u göstermek için
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text(infoDialogTitle) },
                text = { Text(infoDialogMessage) },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("Tamam")
                    }
                }
            )
        }
    }
}