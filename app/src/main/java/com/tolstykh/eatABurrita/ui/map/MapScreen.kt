package com.tolstykh.eatABurrita.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable

@Composable
fun MapScreen(onBackPressed: () -> Unit = {}) {
    Surface {
        Column {
            Text("Map Screen")
            Button(onClick = onBackPressed) {
                Text("Back")
            }
        }
    }
}