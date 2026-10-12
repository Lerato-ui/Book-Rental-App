package com.example.libraryrental

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.libraryrental.ui.navigation.AppNavigation
import com.example.libraryrental.ui.theme.LibraryRentalTheme
import com.example.libraryrental.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as LibraryApplication).repository
        val factory = ViewModelFactory(repository)

        setContent {
            LibraryRentalTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(factory = factory)
                }
            }
        }
    }
}
