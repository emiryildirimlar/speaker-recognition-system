package com.example.speakerrecoginitonfinal2.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Button for testing audio recognition
 */
@Composable
fun TestButton(
    text: String,
    isEnabled: Boolean,
    isTestRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.width(200.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTestRecording) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text)
    }
} 