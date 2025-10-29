package com.tolstykh.eatABurrita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tolstykh.eatABurrita.ui.theme.EataBurritaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        val appStore = StoreManager(context = this)

        setContent {
            EataBurritaTheme (dynamicColor = false) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(top = 144.dp, bottom = 96.dp)
                ) {
//                    TimerScreen(appStore = appStore, activityContext = this)
                    Navigation(
//                        appStore = appStore,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}
