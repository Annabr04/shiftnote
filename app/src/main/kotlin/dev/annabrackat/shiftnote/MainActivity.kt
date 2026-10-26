package dev.annabrackat.shiftnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.annabrackat.shiftnote.ui.ShiftnoteApp
import dev.annabrackat.shiftnote.ui.ShiftnoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiftnoteTheme {
                ShiftnoteApp()
            }
        }
    }
}
