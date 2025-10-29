package com.tolstykh.eatABurrita.ui.main

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tolstykh.eatABurrita.StoreManager
import com.tolstykh.eatABurrita.dateFromMilliseconds
import com.tolstykh.eatABurrita.formatDuration
import com.tolstykh.eatABurrita.getRandomStaticMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun TimerScreen(onOpenMap: () -> Unit) {
    val composableScope = rememberCoroutineScope()
    val appStore = StoreManager(context = LocalContext.current)

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppTitle()
        TimeSinceLastBurrito(
            modifier = Modifier.padding(top = 32.dp),
            appStore = appStore
        )
        TotalBurritos(modifier = Modifier.padding(8.dp), appStore = appStore)
        LastBurritoDate(modifier = Modifier.padding(8.dp), appStore = appStore)
        Spacer(modifier = Modifier.weight(1F))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            MapButton(onClick = onOpenMap)
            EatButton(
                onClick = {
                    composableScope.launch {
                        appStore.addBurritoCount()
                        appStore.updateTimestamp()
                    }
                },
            )
            Share(context = LocalContext.current)
        }
    }
}

@Composable
fun TimeSinceLastBurrito(
    modifier: Modifier = Modifier,
    appStore: StoreManager
) {
    var lastTimestamp: Long by rememberSaveable {
        mutableLongStateOf(0)
    }

    LaunchedEffect(Unit) {
        appStore.timestamp.collect { storedTimestamp ->
            lastTimestamp = storedTimestamp
        }
    }

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
            .size(120.dp)
            .padding(4.dp),
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
fun TotalBurritos(modifier: Modifier = Modifier, appStore: StoreManager) {
    var count by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        appStore.burritoCount.collect { storedCount ->
            count = storedCount
        }
    }

    Text(
        text = "Total burritos: $count",
        fontSize = 20.sp,
        lineHeight = 24.sp,
        modifier = modifier,
    )
}

@Composable
fun LastBurritoDate(modifier: Modifier = Modifier, appStore: StoreManager) {
    var lastTimestamp: Long by rememberSaveable {
        mutableLongStateOf(0)
    }

    LaunchedEffect(Unit) {
        appStore.timestamp.collect { storedTimestamp ->
            lastTimestamp = storedTimestamp
        }
    }

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