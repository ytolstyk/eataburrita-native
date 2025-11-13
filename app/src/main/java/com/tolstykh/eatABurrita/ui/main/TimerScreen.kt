package com.tolstykh.eatABurrita.ui.main

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tolstykh.eatABurrita.dateFromMilliseconds
import com.tolstykh.eatABurrita.formatDuration
import com.tolstykh.eatABurrita.helpers.getRandomStaticMessage
import kotlinx.coroutines.delay
import java.time.Instant

@Composable
fun TimerScreen(viewModel: TimeScreenViewModel = hiltViewModel(), onOpenMap: () -> Unit) {
    val uiState by viewModel.timeScreenState.collectAsStateWithLifecycle()

    if (uiState is TimeScreenViewModel.TimeScreenUIState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState is TimeScreenViewModel.TimeScreenUIState.Error) {
        // You can add an error indicator here if needed
        return
    }

    val data = (uiState as TimeScreenViewModel.TimeScreenUIState.Success).data

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorScheme.background)
            .padding(top = 144.dp, bottom = 96.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTitle()
            TimeSinceLastBurrito(
                modifier = Modifier.padding(top = 32.dp),
                lastTimestamp = data.lastTimestamp,
            )
            TotalBurritos(modifier = Modifier.padding(8.dp), burritoCount = data.burritoCount)
            LastBurritoDate(modifier = Modifier.padding(8.dp), lastTimestamp = data.lastTimestamp)
            Spacer(modifier = Modifier.weight(1F))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                MapButton(onClick = onOpenMap)
                EatButton(
                    onClick = viewModel::addBurrito
                )
                Share(context = LocalContext.current)
            }
        }
    }
}

@Composable
fun TimeSinceLastBurrito(
    modifier: Modifier = Modifier,
    lastTimestamp: Long = 0L,
) {
    var now by remember { mutableLongStateOf(Instant.now().toEpochMilli()) }

    if (lastTimestamp == 0L) {
        Text(
            text = "0 days, 00:00:00",
            fontSize = 30.sp,
            lineHeight = 34.sp,
            modifier = modifier
        )
        return
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = Instant.now().toEpochMilli()
        }
    }

    Text(
        text = formatDuration(now - lastTimestamp),
        fontSize = 30.sp,
        lineHeight = 34.sp,
        modifier = modifier
    )
}

@Composable
fun AppTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Time Since Last Burrito",
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        modifier = modifier
    )
}

@Composable
fun EatButton(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        shape = CircleShape,
        modifier = Modifier
            .size(144.dp)
            .padding(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary
        )
    ) {
        Text(text = "Eat!", fontSize = 24.sp)
    }
}

@Composable
fun Share(context: Context) {
    Button(
        onClick = {
            val text = getRandomStaticMessage()
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)

            context.startActivity(shareIntent, null)
        },
        shape = CircleShape,
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.secondary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
        )
    }
}

@Composable
fun TotalBurritos(modifier: Modifier = Modifier, burritoCount: Int = 0) {
    Text(
        text = "Total burritos: $burritoCount",
        fontSize = 20.sp,
        lineHeight = 24.sp,
        modifier = modifier,
    )
}

@Composable
fun LastBurritoDate(modifier: Modifier = Modifier, lastTimestamp: Long = 0L) {
    if (lastTimestamp == 0L) {
        Text(
            text = "It's time to eat a burrito!",
            fontSize = 18.sp,
            lineHeight = 22.sp,
            modifier = modifier,
        )
        return
    }

    Text(
        text = "Last burrito: ${dateFromMilliseconds(lastTimestamp)}",
        fontSize = 18.sp,
        lineHeight = 22.sp,
        modifier = modifier,
    )
}

@Composable
fun MapButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.tertiary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = null,
        )
    }
}
