package com.example.speakerrecoginitonfinal2.ui.components

import androidx.compose.foundation.background // Eksik import eklendi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable // combinedClickable yerine
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Bu import doğru
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete // Silme ikonu için eklendi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.speakerrecoginitonfinal2.data.model.SpeakerInfo // Yeni model importu

// import com.example.speakerrecognition.data.model.Speaker // Eski model

// @OptIn(ExperimentalFoundationApi::class) // combinedClickable kullanılmadığı için kaldırıldı
@Composable
fun SpeakerList(
    speakers: List<SpeakerInfo>,
    selectedSpeaker: SpeakerInfo?,
    onSpeakerClick: (SpeakerInfo) -> Unit,
    onDeleteSpeaker: (SpeakerInfo) -> Unit, // Yeni parametre
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            // .heightIn(max = 200.dp) // Kaldırıldı - Liste için maksimum yükseklik HomeScreen'deki gibi
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        if (speakers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Genişliği doldur
                    .padding(16.dp), // Padding ekle
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Kayıtlı konuşmacı bulunmuyor.", // Metin HomeScreen'deki gibi
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                // modifier = Modifier.padding(8.dp) // Bu padding Card'dan geliyor, tekrar gerek yok
            ) {
                items(
                    items = speakers,
                    key = { speaker -> speaker.id } // Key eklendi
                ) { speaker ->
                    val isSelected = selectedSpeaker?.id == speaker.id
                    // val backgroundColor = if (isSelected) // Arka plan HomeScreen'deki gibi Box içinde
                    //     MaterialTheme.colorScheme.primaryContainer
                    // else
                    //     MaterialTheme.colorScheme.surface

                    Column { // Her bir öğe ve ayırıcı için bir Column
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSpeakerClick(speaker) } // combinedClickable yerine clickable
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp) // Dikey padding azaltıldı
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = speaker.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f) // Text'in genişlemesini sağlar
                                )

                                Row { // Seçim ikonu ve Silme butonu için
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seçildi",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    IconButton(onClick = { onDeleteSpeaker(speaker) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Konuşmacıyı Sil",
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        // createdAt bilgisi SpeakerInfo modelinde yok, bu yüzden kaldırıldı.
                        // İstenirse SpeakerInfo modeline eklenebilir veya başka bir bilgi gösterilebilir.
                        // val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                        // Text(
                        // text = "Eklenme: ${dateFormat.format(speaker.createdAt)}", // Eski model alanı
                        // style = MaterialTheme.typography.bodySmall,
                        // color = MaterialTheme.colorScheme.onSurfaceVariant
                        // )
                        if (speakers.indexOf(speaker) < speakers.size - 1) { // Son eleman değilse ayırıcı ekle
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}