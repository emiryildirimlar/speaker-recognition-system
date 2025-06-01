package com.example.speakerrecoginitonfinal2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme // Bu import gerekli olacak
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.speakerrecoginitonfinal2.ui.screens.MainScreen // MainScreen importu
import com.example.speakerrecoginitonfinal2.ui.theme.SpeakerRecoginitonFinal2Theme // Tema importu
import com.example.speakerrecoginitonfinal2.ui.viewmodel.SpeakerViewModel
import com.example.speakerrecoginitonfinal2.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = ViewModelFactory(application)
        val speakerViewModel = ViewModelProvider(this, viewModelFactory)[SpeakerViewModel::class.java]

        setContent {
            SpeakerRecoginitonFinal2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(speakerViewModel = speakerViewModel)
                }
            }
        }
    }
}