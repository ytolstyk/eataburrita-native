package com.tolstykh.eatABurrita.ui.recipes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RecipesScreen(
    onBackPressed: () -> Unit,
    viewModel: RecipesViewModel = hiltViewModel(),
) {
    val localFavoriteId by viewModel.localFavoriteId.collectAsStateWithLifecycle()
    val localFavorite = remember(localFavoriteId) {
        localFavoriteId?.let { id -> allRecipes.find { it.id == id } }
    }
    val checkedIngredients by viewModel.checkedIngredients.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Burrito Recipes",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (localFavorite != null) {
                Text(
                    text = "Local Favorite",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.primary,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RecipeItem(
                        recipe = localFavorite,
                        isLocalFavorite = true,
                        initiallyExpanded = true,
                        checkedIngredients = checkedIngredients,
                        onToggleIngredient = { index -> viewModel.toggleIngredient(localFavorite.id, index) },
                        onUncheckAll = { viewModel.uncheckAll(localFavorite.id) },
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "All Recipes",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.primary,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    allRecipes.forEachIndexed { index, recipe ->
                        RecipeItem(
                            recipe = recipe,
                            isLocalFavorite = false,
                            initiallyExpanded = false,
                            checkedIngredients = checkedIngredients,
                            onToggleIngredient = { idx -> viewModel.toggleIngredient(recipe.id, idx) },
                            onUncheckAll = { viewModel.uncheckAll(recipe.id) },
                        )
                        if (index < allRecipes.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RecipeItem(
    recipe: BurritoRecipe,
    isLocalFavorite: Boolean,
    initiallyExpanded: Boolean,
    checkedIngredients: Set<String>,
    onToggleIngredient: (Int) -> Unit,
    onUncheckAll: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val uriHandler = LocalUriHandler.current

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = recipe.culture,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            if (isLocalFavorite) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colorScheme.primary.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = "Local Favorite",
                        fontSize = 10.sp,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(modifier = Modifier.padding(end = 8.dp))
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleSmall,
                        color = colorScheme.primary,
                    )
                    val anyChecked = recipe.ingredients.indices.any { i ->
                        "${recipe.id}_${i}" in checkedIngredients
                    }
                    if (anyChecked) {
                        TextButton(
                            onClick = onUncheckAll,
                            modifier = Modifier.padding(0.dp),
                        ) {
                            Text(
                                text = "Uncheck All",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                recipe.ingredients.forEachIndexed { index, ingredient ->
                    val checked = "${recipe.id}_${index}" in checkedIngredients
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleIngredient(index) }
                            .padding(vertical = 1.dp),
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onToggleIngredient(index) },
                        )
                        Text(
                            text = ingredient,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (checked) colorScheme.onSurface.copy(alpha = 0.4f) else colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Steps",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.primary,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                recipe.steps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. $step",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Find Similar →",
                    color = colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { uriHandler.openUri(recipe.sourceUrl) }
                        .padding(vertical = 4.dp),
                )
            }
        }
    }
}
