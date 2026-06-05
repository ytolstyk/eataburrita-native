package com.tolstykh.eatABurrita.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private data class BurritoSize(val label: String, val calories: Int, val color: Color)
private data class BurritoExtra(val label: String, val calories: Int)

private val SIZES = listOf(
    BurritoSize("Small", 450, Color(0xFF6DBF67)),
    BurritoSize("Regular", 760, Color(0xFFFFAA2C)),
    BurritoSize("Mega", 1100, Color(0xFFFF5733)),
    BurritoSize("Bowl", 650, Color(0xFF2ABFBF)),
)

private val EXTRAS = listOf(
    BurritoExtra("Guac", 80),
    BurritoExtra("Sour Cream", 50),
    BurritoExtra("Cheese", 60),
)

@Composable
fun SizePickerModal(
    onConfirm: (calories: Int) -> Unit,
    onSkip: () -> Unit,
) {
    var selectedSize by remember { mutableStateOf(SIZES[1]) }
    val checkedExtras = remember { mutableStateListOf<BurritoExtra>() }
    val totalCalories = selectedSize.calories + checkedExtras.sumOf { it.calories }

    Dialog(onDismissRequest = onSkip) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("How big was it?", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                SIZES.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEach { size ->
                            SizeButton(
                                size = size,
                                selected = selectedSize == size,
                                onClick = { selectedSize = size },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text("Extras", style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))

                EXTRAS.forEach { extra ->
                    val checked = extra in checkedExtras
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                if (isChecked) checkedExtras.add(extra) else checkedExtras.remove(extra)
                            },
                        )
                        Text("${extra.label} (+${extra.calories} kcal)", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text(
                    "Total: $totalCalories kcal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                )

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onSkip) { Text("Skip") }
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Button(onClick = { onConfirm(totalCalories) }) { Text("Confirm") }
                }
            }
        }
    }
}

@Composable
private fun SizeButton(
    size: BurritoSize,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (selected) size.color else colorScheme.surfaceVariant
    val contentColor = if (selected) Color.White else colorScheme.onSurfaceVariant
    val border = if (selected) null else BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))

    Surface(
        onClick = onClick,
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = border,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                size.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            Text(
                "${size.calories} kcal",
                fontSize = 12.sp,
                color = contentColor.copy(alpha = 0.8f),
            )
        }
    }
}
