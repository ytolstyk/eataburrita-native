package com.tolstykh.eatABurrita

const val APP_NAME = "Eat-a-Burrita"

const val genericMessage = "I need to eat a burrito, so I can track it with $APP_NAME!"
val staticMessages = arrayOf(
    genericMessage,
    "I just ate a burrito, and it was delicious!",
    "Tracking my burritos has never been easier - thanks $APP_NAME!",
    "Are you up for some burritos?",
    "Give me a B! Give me a urrito! No, really, give me a burrito. Please.",
)

fun getRandomStaticMessage(): String {
    return staticMessages.random()
}