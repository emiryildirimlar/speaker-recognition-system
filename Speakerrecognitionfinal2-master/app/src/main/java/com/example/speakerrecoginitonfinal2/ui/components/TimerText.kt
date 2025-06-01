package com.example.speakerrecoginitonfinal2.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Text component for displaying remaining recording time
 */
@Composable
fun TimerText(
    remainingTime: Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Remaining Time: $remainingTime",
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(8.dp)
    )
} 