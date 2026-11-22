package com.guilherme.santana.genui2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.guilherme.santana.genui2.presentation.GenUiScreen
import com.guilherme.santana.genui2.presentation.GenUiViewModel
import com.guilherme.santana.genui2.ui.theme.GenUi2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val screen = ViewModelProvider(this)[GenUiViewModel::class.java]
        setContent {
            GenUi2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GenUiScreen(screen, Modifier.padding(innerPadding))
                }
            }
        }
    }
}
