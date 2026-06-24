package com.iqbal0107.okok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iqbal0107.okok.screen.MainScreen
import com.iqbal0107.okok.ui.theme.OkokTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OkokTheme {
                MainScreen()
            }
        }
    }
}