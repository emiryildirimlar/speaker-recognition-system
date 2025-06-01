package com.example.speakerrecoginitonfinal2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.speakerrecoginitonfinal2.data.model.SpeakerInfo
import com.example.speakerrecoginitonfinal2.ui.viewmodel.SpeakerViewModel
import com.example.speakerrecoginitonfinal2.data.util.Resource // Resource import edildi
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakersListScreen(
    viewModel: SpeakerViewModel,
    onSpeakerSelected: (SpeakerInfo) -> Unit,
    onClose: () -> Unit
) {
    val speakersState by viewModel.speakersListState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Üst bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kayıtlı Konuşmacılar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (val currentState = speakersState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // currentState.isInitialLoading durumuna göre farklı UI gösterilebilir,
                    // şimdilik her zaman indicator gösteriyoruz.
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hata: ${currentState.message ?: "Bilinmeyen bir hata oluştu."}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadSpeakers() }) {
                        Text("Tekrar Dene")
                    }
                }
            }
            is Resource.Success -> {
                if (currentState.data.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz kayıtlı konuşmacı yok.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentState.data) { speaker -> // data burada null olamaz
                            SpeakerItem(
                                speaker = speaker,
                                onClick = {
                                    onSpeakerSelected(speaker)
                                    onClose()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
} // SpeakersListScreen fonksiyonunun kapanış parantezi eklendi.

@Composable
fun SpeakerItem(
    speaker: SpeakerInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = speaker.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${speaker.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            val formattedDate = try {
                // Basit tarih formatı
                "Kayıt: ${speaker.createdAt.take(10)}" // YYYY-MM-DD formatında alır
            } catch (e: Exception) {
                "Kayıt: ${speaker.createdAt}"
            }
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}