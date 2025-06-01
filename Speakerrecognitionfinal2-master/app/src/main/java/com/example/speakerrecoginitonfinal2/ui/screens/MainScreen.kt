package com.example.speakerrecoginitonfinal2.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.* // Bu kalabilir, HomeScreen kullanıyor olabilir
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.speakerrecoginitonfinal2.ui.viewmodel.SpeakerViewModel
// import com.google.accompanist.pager.* // Pager kaldırılıyor
// import kotlinx.coroutines.launch // Kullanılmıyorsa kaldırılabilir
// import com.example.speakerrecognition.ui.viewmodels.SpeakersViewModel // Bu import da kaldırılacak

// @OptIn(ExperimentalPagerApi::class) // Pager kaldırılıyor
@Composable
fun MainScreen(
    speakerViewModel: SpeakerViewModel,
) {
    // val pagerState = rememberPagerState() // Pager kaldırıldı
    // val coroutineScope = rememberCoroutineScope() // Kullanılmıyorsa kaldırılabilir

    // HorizontalPager kaldırıldı, doğrudan HomeScreen gösteriliyor
    HomeScreen(speakerViewModel = speakerViewModel)

    // Her sayfa geçişinde yeni veri yükle kısmı da kaldırıldı
    // LaunchedEffect(pagerState.currentPage) {
    //     if (pagerState.currentPage == 1) {
    //         // Konuşmacı sayfasına geçildiğinde konuşmacı listesini yenile
    //         speakersViewModel.refreshSpeakers()
    //     }
    // }
}