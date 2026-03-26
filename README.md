# 🌯 Eat-a-Burrita

> *Because someone has to keep track of this.*

## What Is This?

**Eat-a-Burrita** is an Android app for people who take burritos seriously. It answers the one question that haunts you throughout your day:

**"How long has it been since I ate a burrito?"**

The answer is displayed prominently, in large text, ticking away second by second — a constant reminder of your burrito-free suffering.

## Features

### ⏱️ The Timer
A live countdown (count-up?) showing exactly how long it has been since your last burrito. Days. Hours. Minutes. Seconds. No escape.

### 🌯 The Button
A big, satisfying **"Eat!"** button. Press it when you eat a burrito. Optionally tag the location so you remember where the good ones happened.

### 📊 The Chart
A 30-day bar chart of your burrito consumption history. Are you trending up? Down? Erratic? The chart knows. The chart judges silently. Tap any bar to see which locations you visited that day.

### 📍 Favorite Place
The app tracks where you eat most often and surfaces your favorite burrito spot right on the home screen — tappable, for when you need to go back immediately.

### 🗺️ The Map
Can't find a burrito? We've got you. The map screen finds nearby Mexican restaurants and marks them with little burrito icons so you can solve your problem immediately.

### 📤 The Share Button
Tap it to generate a deeply unnecessary but statistically accurate update for your friends, such as:

> *"🌯 42 burritos in the last 30 days! That's commitment!"*

Your friends may not care. Share anyway.

### 🔔 Reminders
Gone three days without a burrito? The app will remind you. Seven days? It gets more concerned. Notifications are gentle but persistent, like a good friend who also loves burritos.

### ⚙️ Settings
Dark mode, notification toggles, location modal preferences, full entry history with edit/delete, and a reset button for when you want to pretend none of this happened.

## Tech Stack

- **Kotlin** — because Java is a lot of words
- **Jetpack Compose** — UI that actually enjoys being written
- **Room** — every burrito click persisted forever, no burrito forgotten
- **DataStore** — lightweight preferences for settings and flags
- **WorkManager** — periodic reminders running quietly in the background
- **Hilt** — dependency injection, the responsible way
- **Google Maps + Places API** — the burrito locator
- **Material 3** — orange and purple, naturally

## Setup

You'll need a Google Maps API key. Create a `secrets.properties` file in the project root:

```
MAPS_API_KEY=your_key_here
```

Then build and run. Eat a burrito. Press the button. Repeat until satisfied (you won't be).

## Philosophy

Life is short. Track your burritos.

---

*Made with mild obsession and a healthy appreciation for Mexican food.*
