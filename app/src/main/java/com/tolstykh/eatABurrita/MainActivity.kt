package com.tolstykh.eatABurrita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tolstykh.eatABurrita.data.AppPreferencesRepository
import com.tolstykh.eatABurrita.ui.theme.EataBurritaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appPrefs: AppPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openMap = intent.getBooleanExtra(EXTRA_OPEN_MAP, false)

        setContent {
            val isDark by appPrefs.isDarkMode.collectAsStateWithLifecycle(initialValue = false)

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    enableEdgeToEdge(
                        statusBarStyle = if (isDark) {
                            SystemBarStyle.dark(android.graphics.Color.argb(80, 0, 0, 0))
                        } else {
                            SystemBarStyle.light(
                                android.graphics.Color.argb(80, 255, 255, 255),
                                android.graphics.Color.argb(80, 0, 0, 0)
                            )
                        },
                        navigationBarStyle = if (isDark) {
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.TRANSPARENT
                            )
                        }
                    )
                }
            }

            EataBurritaTheme(darkTheme = isDark, dynamicColor = false) {
                Navigation(
                    modifier = Modifier.fillMaxSize(),
                    startDestination = if (openMap) Map else Home
                )
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_MAP = "extra_open_map"
    }
}
