package com.example.equall

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.equall.di.HomeModule
import com.example.equall.presentation.HomeScreen

/** Application composition root. Presentation components receive state and events only. */
@Composable
fun EquallApp() {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF4D54E4), background = Color(0xFFEEEFF3),
        surface = Color.White, onSurface = Color(0xFF17191D)
    ), typography = Typography(bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp))) {
        val model = viewModel { HomeModule.createViewModel() }
        val state by model.state.collectAsStateWithLifecycle()
        HomeScreen(state, model::onEvent)
    }
}
