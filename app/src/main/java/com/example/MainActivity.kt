package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.RoutinaNavGraph
import com.example.ui.theme.RoutinaTheme
import com.example.ui.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {

    private val habitViewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userSettings by habitViewModel.userSettings.collectAsStateWithLifecycle()

            RoutinaTheme(darkTheme = userSettings.isDarkMode) {
                val backgroundBrush = if (!userSettings.isDarkMode) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF7F7F7)),
                        start = Offset(Float.POSITIVE_INFINITY, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                } else {
                    SolidColor(androidx.compose.material3.MaterialTheme.colorScheme.background)
                }
                Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                        RoutinaNavGraph(viewModel = habitViewModel)
                    }
                }
            }
        }
    }
}
