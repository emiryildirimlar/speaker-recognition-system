package com.example.speakerrecoginitonfinal2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // items importu önemli
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource // Eklendi
import com.example.speakerrecoginitonfinal2.R // Eklendi
import com.example.speakerrecoginitonfinal2.data.model.TestSpeakerResponse // Yeni ve doğru import
import com.example.speakerrecoginitonfinal2.data.model.SpeakerMatchAPI // Bu import zaten doğru olmalı

@OptIn(ExperimentalMaterial3Api::class) // Gerekirse ekleyin
@Composable
fun TestResultDialog(
    recognitionResponse: TestSpeakerResponse?, // Parametre tipi TestSpeakerResponse? olmalı
    onDismiss: () -> Unit
) {
    if (recognitionResponse == null) {
        return // Eğer response null ise dialogu gösterme
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large // veya RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally // İçerik ortalansın
            ) {
                Text(
                    text = stringResource(R.string.recognition_result_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Yeni TestSpeakerResponse alanlarını göster
                Text(stringResource(R.string.message_label, recognitionResponse.message), style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    stringResource(R.string.match_status_label, if (recognitionResponse.matchFound) stringResource(R.string.answer_yes) else stringResource(R.string.answer_no)),
                    fontWeight = FontWeight.Bold
                )
                recognitionResponse.speakerName?.let {
                    Text(stringResource(R.string.identified_speaker_label, it))
                }
                // Güven skorunu formatlayarak göster
                Text(stringResource(R.string.confidence_label, recognitionResponse.confidence * 100))


                recognitionResponse.thresholdMet?.let {
                    Text(stringResource(R.string.threshold_status_label, if (it) stringResource(R.string.answer_yes) else stringResource(R.string.answer_no)))
                }
                recognitionResponse.requiredThreshold?.let {
                    // Gerekli eşik değerini formatlayarak göster
                    Text(String.format("Gerekli Eşik Değer: %.2f%%", it * 100))
                }

                // Tüm eşleşmeleri göster (eğer varsa)
                if (!recognitionResponse.allMatches.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tüm Eşleşmeler:", style = MaterialTheme.typography.titleMedium)
                    // Kaydırılabilir bir liste için LazyColumn
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp) // Liste için maksimum yükseklik
                            .padding(top = 8.dp)
                    ) {
                        items(recognitionResponse.allMatches) { match ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {                                Column(modifier = Modifier.padding(8.dp)) {
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
    }
}
