package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainNavigationContainer
import com.example.ui.theme.KindredTheme
import com.example.ui.viewmodel.KindredViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: KindredViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activeUser by viewModel.activeUser.collectAsState()
            val themeName = activeUser?.themeName ?: "LAVENDER_DUSK"
            val isDarkMode = activeUser?.isDarkMode ?: false
            val isSystemTheme = activeUser?.isSystemTheme ?: true

            KindredTheme(
                themeName = themeName,
                darkTheme = isDarkMode,
                isSystemTheme = isSystemTheme
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainNavigationContainer(viewModel = viewModel)
                }
            }
        }
    }
}
