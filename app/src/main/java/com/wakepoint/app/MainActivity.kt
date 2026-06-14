package com.wakepoint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.navigation.WakepointApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WakepointTheme {
                WakepointApp()
            }
        }
    }
}
