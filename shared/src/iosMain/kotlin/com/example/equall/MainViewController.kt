package com.example.equall

import androidx.compose.ui.window.ComposeUIViewController

// UIKit interoperability is the only platform-specific Kotlin entry point.
fun MainViewController() = ComposeUIViewController { EquallApp() }
