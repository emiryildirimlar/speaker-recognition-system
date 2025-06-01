package com.example.speakerrecoginitonfinal2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.speakerrecoginitonfinal2.R
import com.example.speakerrecoginitonfinal2.data.model.TestSpeakerResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultBottomSheet(
    recognitionResponse: TestSpeakerResponse?,
    onDismiss: () -> Unit,
    sheetState: SheetState, // ModalBottomSheetLayout için SheetState
    modifier: Modifier = Modifier // Genel modifier
) {
    if (recognitionResponse == null) {
        // Eğer response null ise bir şey gösterme veya onDismiss çağır
        // Bu durumun ViewModel veya çağıran Composable tarafından yönetilmesi daha iyi olabilir.
        // Şimdilik, eğer null ise BottomSheet'i göstermemeyi tercih edelim.
        // onDismiss() // Eğer hemen kapatılması isteniyorsa
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { BottomSheetDefaults.DragHandle() } // Standart sürükleme tutamacı
    ) {
        // TestResultDialog'dan alınan içerik
        Card( // Card görünümünü koruyabiliriz veya doğrudan Column kullanabiliriz.
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp), // Üst padding drag handle için ayarlandı
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(), // Ekranı dikey olarak doldurması için eklendi
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.recognition_result_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.message_label, recognitionResponse.message), style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    stringResource(R.string.match_status_label, if (recognitionResponse.matchFound) stringResource(R.string.answer_yes) else stringResource(R.string.answer_no)),
                    fontWeight = FontWeight.Bold
                )
                recognitionResponse.speakerName?.let {
                    Text(stringResource(R.string.identified_speaker_label, it))
                }
                Text(stringResource(R.string.confidence_label, recognitionResponse.confidence * 100))

                recognitionResponse.thresholdMet?.let {
                    Text(stringResource(R.string.threshold_status_label, if (it) stringResource(R.string.answer_yes) else stringResource(R.string.answer_no)))
                }
                recognitionResponse.requiredThreshold?.let {
                    Text(String.format("Gerekli Eşik Değer: %.2f%%", it * 100))
                }

                if (!recognitionResponse.allMatches.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tüm Eşleşmeler:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Kalan alanı doldurması için eklendi
                            .padding(top = 8.dp)
                    ) {
                        items(recognitionResponse.allMatches) { match ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Konuşmacı ID: ${match.speakerId}")
                                    Text("İsim: ${match.speakerName ?: "Bilinmiyor"}")
                                    Text(String.format("Skor: %.2f%%", match.confidence * 100))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_close_button))
                }
            }
        }
        // BottomSheet'in altta biraz boşluk bırakması için (navigasyon çubuğu vs. için)
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }
}