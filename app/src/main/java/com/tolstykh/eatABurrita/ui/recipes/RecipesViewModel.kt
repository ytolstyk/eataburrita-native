package com.tolstykh.eatABurrita.ui.recipes

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.tolstykh.eatABurrita.location.hasLocationPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import android.location.Location

data class BurritoRecipe(
    val id: Int,
    val name: String,
    val culture: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val sourceUrl: String,
    val countryCodes: List<String>,
)

val allRecipes: List<BurritoRecipe> = listOf(
    BurritoRecipe(
        id = 1,
        name = "Classic Carne Asada Burrito",
        culture = "Mexican",
        ingredients = listOf(
            "1 lb skirt or flank steak",
            "2 limes, juiced",
            "3 cloves garlic, minced",
            "1 tsp cumin",
            "1 tsp chili powder",
            "4 large flour tortillas",
            "1 cup cooked white rice",
            "1 cup black beans, drained",
            "1/2 cup pico de gallo",
            "1/2 cup guacamole",
            "1/4 cup sour cream",
            "1/2 cup shredded cheese",
        ),
        steps = listOf(
            "Marinate steak in lime juice, garlic, cumin, and chili powder for at least 1 hour.",
            "Grill steak over high heat 4–5 minutes per side until charred. Rest 5 minutes, then slice thin against the grain.",
            "Warm tortillas on a dry skillet for 30 seconds per side.",
            "Layer rice, beans, carne asada, pico de gallo, guacamole, sour cream, and cheese in the center of each tortilla.",
            "Fold in the sides, then roll tightly from the bottom up. Optionally, sear seam-side down in the pan for 1 minute to seal.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=carne+asada+burrito",
        countryCodes = listOf("MX", "US"),
    ),
    BurritoRecipe(
        id = 2,
        name = "California Mission-Style Burrito",
        culture = "Californian (USA)",
        ingredients = listOf(
            "1 lb grilled chicken or steak, sliced",
            "4 extra-large flour tortillas (12-inch)",
            "1 1/2 cups Spanish rice",
            "1 cup pinto beans",
            "1/2 cup salsa",
            "1/2 cup sour cream",
            "1/2 cup shredded Monterey Jack cheese",
            "1/4 cup guacamole",
            "1 cup shredded lettuce",
            "1/4 cup pickled jalapeños",
        ),
        steps = listOf(
            "Steam the extra-large tortillas until very pliable, about 20–30 seconds each.",
            "Pile rice, beans, and meat in a thick log shape in the lower third of the tortilla — the key is using a lot of filling.",
            "Add salsa, sour cream, guacamole, cheese, lettuce, and jalapeños on top.",
            "Fold the bottom up tightly over the filling, tuck in both sides firmly, then roll forward to close.",
            "Wrap tightly in foil to hold shape. Let rest 2 minutes before serving.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=california+mission+burrito",
        countryCodes = listOf("US"),
    ),
    BurritoRecipe(
        id = 3,
        name = "New Mexico Green Chile Burrito",
        culture = "New Mexican (USA)",
        ingredients = listOf(
            "1 lb pork shoulder or chicken, cooked and shredded",
            "1 cup roasted Hatch green chiles, peeled and chopped",
            "1 medium onion, diced",
            "3 cloves garlic, minced",
            "1 cup pork or chicken broth",
            "1 tsp oregano",
            "4 flour tortillas",
            "1 cup cooked pinto beans",
            "1/2 cup shredded cheddar cheese",
        ),
        steps = listOf(
            "Sauté onion and garlic until soft. Add shredded meat, green chiles, broth, and oregano.",
            "Simmer uncovered 15–20 minutes until sauce thickens and coats the meat.",
            "Warm tortillas on a dry griddle.",
            "Place beans and a generous scoop of the green chile meat in the center of each tortilla.",
            "Roll burrito-style and top with extra green chile sauce and shredded cheese. Serve open-faced smothered or rolled.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=new+mexico+green+chile+burrito",
        countryCodes = listOf("US"),
    ),
    BurritoRecipe(
        id = 4,
        name = "Baja Fish Burrito",
        culture = "Baja California (Mexico)",
        ingredients = listOf(
            "1 lb white fish (cod or mahi-mahi), cut into strips",
            "1 cup flour",
            "1 cup light beer",
            "1 tsp baking powder",
            "Salt and pepper",
            "Oil for frying",
            "4 flour tortillas",
            "2 cups shredded cabbage",
            "1/2 cup crema or sour cream",
            "1/4 cup chipotle mayo",
            "2 limes, cut into wedges",
            "Fresh cilantro",
            "Pickled red onion",
        ),
        steps = listOf(
            "Whisk flour, beer, baking powder, salt, and pepper into a smooth batter.",
            "Heat 2 inches of oil to 375°F. Dip fish strips in batter and fry 3–4 minutes until golden and crisp. Drain on paper towels.",
            "Warm tortillas. Spread chipotle mayo down the center.",
            "Add cabbage, 2–3 pieces of fried fish, crema, pickled onion, and cilantro.",
            "Squeeze fresh lime over the filling, fold, and serve immediately.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=baja+fish+burrito",
        countryCodes = listOf("MX"),
    ),
    BurritoRecipe(
        id = 5,
        name = "Korean BBQ Burrito",
        culture = "Korean-American",
        ingredients = listOf(
            "1 lb ribeye or short rib beef, thinly sliced (bulgogi-style)",
            "3 tbsp soy sauce",
            "1 tbsp sesame oil",
            "1 tbsp brown sugar",
            "2 cloves garlic, grated",
            "1 tsp ginger, grated",
            "1 cup cooked short-grain rice",
            "1/2 cup kimchi, chopped",
            "2 tbsp gochujang mayo (mix 1 tbsp each)",
            "4 flour tortillas",
            "1/4 cup sliced cucumber",
            "2 green onions, sliced",
            "1 tsp toasted sesame seeds",
        ),
        steps = listOf(
            "Marinate beef in soy sauce, sesame oil, brown sugar, garlic, and ginger for 30 minutes.",
            "Cook beef in a very hot skillet or grill pan 1–2 minutes per side until caramelized. Don't crowd the pan.",
            "Warm tortillas. Spread gochujang mayo down the center.",
            "Add a scoop of rice, bulgogi beef, kimchi, cucumber, and green onions.",
            "Sprinkle sesame seeds, fold into a burrito, and serve immediately.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=korean+bbq+burrito",
        countryCodes = listOf("KR"),
    ),
    BurritoRecipe(
        id = 6,
        name = "Lomo Saltado Burrito",
        culture = "Peruvian",
        ingredients = listOf(
            "1 lb sirloin steak, cut into strips",
            "2 tomatoes, cut into wedges",
            "1 red onion, cut into thick strips",
            "2 ají amarillo peppers (or yellow chiles), sliced",
            "3 tbsp soy sauce",
            "2 tbsp red wine vinegar",
            "1 cup french fries (store-bought or homemade)",
            "1 cup cooked white rice",
            "4 flour tortillas",
            "Fresh cilantro",
            "Salt and pepper",
        ),
        steps = listOf(
            "Heat a wok or large skillet over very high heat until smoking. Season beef with salt and pepper.",
            "Sear beef strips in batches until browned, about 1–2 minutes. Remove and set aside.",
            "In the same pan, stir-fry onion and chile 1 minute. Add tomatoes, soy sauce, and vinegar — toss everything for 1 more minute.",
            "Return beef to pan. Add french fries and toss to combine. The fries should soak up the sauce.",
            "Warm tortillas, add rice and lomo saltado filling, top with fresh cilantro, and roll into a burrito.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=lomo+saltado+burrito",
        countryCodes = listOf("PE", "BO", "CL"),
    ),
    BurritoRecipe(
        id = 7,
        name = "Caribbean Jerk Chicken Burrito",
        culture = "Caribbean",
        ingredients = listOf(
            "1 lb chicken thighs",
            "3 tbsp jerk seasoning paste (scotch bonnet, allspice, thyme, ginger, garlic)",
            "2 tbsp lime juice",
            "1 cup cooked coconut rice (rice cooked in coconut milk)",
            "1/2 cup black beans",
            "1/2 cup mango salsa (diced mango, red onion, cilantro, lime)",
            "4 flour tortillas",
            "1/4 cup sour cream",
            "Shredded lettuce",
        ),
        steps = listOf(
            "Coat chicken in jerk paste and lime juice. Marinate at least 1 hour or overnight.",
            "Grill or pan-sear chicken over medium-high heat until cooked through and charred at the edges, about 6–7 minutes per side. Slice.",
            "Make mango salsa by combining diced mango, red onion, cilantro, and lime juice.",
            "Warm tortillas. Layer coconut rice, black beans, jerk chicken, mango salsa, and sour cream.",
            "Add shredded lettuce, roll into a burrito, and serve with extra lime.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=jerk+chicken+burrito",
        countryCodes = listOf("CU", "JM", "PR", "DO"),
    ),
    BurritoRecipe(
        id = 8,
        name = "Indian Spiced Lamb Burrito",
        culture = "Indian-American",
        ingredients = listOf(
            "1 lb ground lamb",
            "1 medium onion, finely diced",
            "3 cloves garlic, minced",
            "1 tsp garam masala",
            "1 tsp cumin",
            "1 tsp coriander",
            "1/2 tsp turmeric",
            "1/2 tsp cayenne",
            "1 cup cooked basmati rice",
            "1/2 cup chickpeas, drained",
            "4 flour tortillas",
            "1/4 cup raita (yogurt, cucumber, mint)",
            "Fresh cilantro and lime",
        ),
        steps = listOf(
            "Sauté onion until golden. Add garlic and all spices, cook 1 minute until fragrant.",
            "Add ground lamb, breaking it up and cooking until browned, about 7–8 minutes.",
            "Stir in chickpeas and cook 2 more minutes. Season with salt.",
            "Warm tortillas. Add basmati rice, spiced lamb and chickpea mixture.",
            "Drizzle with raita, top with fresh cilantro and a squeeze of lime, then roll into a burrito.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=indian+spiced+lamb+burrito",
        countryCodes = listOf("IN", "PK", "BD"),
    ),
    BurritoRecipe(
        id = 9,
        name = "Japanese Teriyaki Chicken Burrito",
        culture = "Japanese-American",
        ingredients = listOf(
            "1 lb chicken thighs, boneless",
            "3 tbsp soy sauce",
            "2 tbsp mirin",
            "1 tbsp sake or dry sherry",
            "1 tbsp honey",
            "1 tsp sesame oil",
            "1 cup cooked sushi rice (seasoned with rice vinegar and sugar)",
            "1/2 cup edamame, shelled",
            "1/2 avocado, sliced",
            "4 flour tortillas",
            "1/4 cup cucumber, thinly sliced",
            "2 tbsp Japanese mayo (Kewpie)",
            "1 tsp toasted sesame seeds",
        ),
        steps = listOf(
            "Combine soy sauce, mirin, sake, and honey to make teriyaki sauce. Reserve half for glazing.",
            "Marinate chicken in half the sauce for 20 minutes. Pan-fry in sesame oil over medium-high heat 5–6 minutes per side.",
            "Brush with reserved teriyaki sauce in the last 2 minutes until lacquered and sticky. Slice.",
            "Warm tortillas. Spread a thin line of Japanese mayo down the center.",
            "Add sushi rice, teriyaki chicken, edamame, avocado, and cucumber. Sprinkle sesame seeds, then roll into a burrito.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=teriyaki+chicken+burrito",
        countryCodes = listOf("JP"),
    ),
    BurritoRecipe(
        id = 10,
        name = "Smoky BBQ Pulled Pork Burrito",
        culture = "American Southern",
        ingredients = listOf(
            "1 lb pulled pork (slow-cooked with smoked paprika, brown sugar, garlic, and salt)",
            "1/4 cup BBQ sauce",
            "4 flour tortillas",
            "1 cup cooked white rice",
            "1/2 cup pinto beans",
            "1 cup coleslaw (cabbage, carrot, mayo, vinegar, sugar)",
            "1/2 cup shredded cheddar cheese",
            "1/4 cup pickled jalapeños",
            "Extra BBQ sauce for serving",
        ),
        steps = listOf(
            "Rub pork with smoked paprika, brown sugar, garlic powder, salt, and pepper. Cook low and slow (275°F for 4–5 hours) until pull-apart tender. Shred and toss with BBQ sauce.",
            "Make coleslaw by tossing shredded cabbage and carrot with mayo, apple cider vinegar, sugar, and salt. Chill.",
            "Warm tortillas. Layer rice and beans in the center.",
            "Pile on pulled pork, coleslaw, shredded cheddar, and pickled jalapeños.",
            "Drizzle with extra BBQ sauce, roll tightly into a burrito, and slice in half to serve.",
        ),
        sourceUrl = "https://www.seriouseats.com/search?q=bbq+pulled+pork+burrito",
        countryCodes = listOf("CA", "AU", "GB"),
    ),
)

@HiltViewModel
class RecipesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _localFavoriteId = MutableStateFlow<Int?>(null)
    val localFavoriteId: StateFlow<Int?> = _localFavoriteId.asStateFlow()

    init {
        resolveLocalFavorite()
    }

    private fun resolveLocalFavorite() {
        if (!context.hasLocationPermission()) return

        viewModelScope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val location = suspendCancellableCoroutine<Location?> { cont ->
                    client.lastLocation.addOnSuccessListener { loc ->
                        cont.resume(loc)
                    }.addOnFailureListener {
                        cont.resume(null)
                    }
                } ?: return@launch

                val countryCode = suspendCancellableCoroutine<String?> { cont ->
                    val geocoder = Geocoder(context)
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        cont.resume(addresses.firstOrNull()?.countryCode)
                    }
                } ?: return@launch

                val match = allRecipes.firstOrNull { recipe ->
                    recipe.countryCodes.any { it.equals(countryCode, ignoreCase = true) }
                }
                _localFavoriteId.value = match?.id
            } catch (_: SecurityException) {
                // Permission revoked between check and use
            }
        }
    }
}
