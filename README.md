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
A big, satisfying **"Eat!"** button. Press it when you eat a burrito. Optionally tag the location, pick your burrito size, and track the calories — because knowledge is power and power is burritos.

### 📊 Stats & Charts
A full stats screen with:
- 30-day daily bar chart (tap any bar to see that day's locations)
- Day-of-week and hour-of-day heatmaps — find out when you really eat
- 12-month trend chart and top locations leaderboard
- Current and best streaks, weekly average, and total count

### 🏆 Achievements
Unlock achievements as you eat. Count milestones, streak goals, location exploration, time-of-day patterns, and calorie tracking — all tracked automatically and displayed with progress bars in the Stats screen.

### 🎉 Celebrations
The app celebrates when you log a burrito. Confetti. Particles. You deserve it.

### 📍 Favorite Place
The app tracks where you eat most often and surfaces your favorite burrito spot right on the home screen — tappable, for when you need to go back immediately.

### 🗺️ The Map
Can't find a burrito? We've got you. The map screen finds nearby Mexican restaurants, marks them with burrito icons, and shows their ratings so you can make an informed decision immediately.

### 📤 The Share Button
Generates a statistically accurate update for your friends — or a shareable visual card with your burrito stats. Your friends may not care. Share anyway.

### 🔔 Reminders
A full notification system that actually pays attention:
- **Burrito reminders** — 3-day and 7-day nudges when you haven't logged
- **Nearby spot alerts** — geofence-based notifications when you wander within 300m of a favorite burrito location
- **Streak milestones** — push notification when you hit 7, 14, 30, or 50 consecutive days
- **Weekly recap** — Monday morning digest of last week's burrito count
- Each notification type can be toggled independently in Settings

### 📸 Burrito-or-Not
Not sure if what you're holding counts? Tap the camera, take a photo, and the app will tell you whether it's a burrito. Powered by ML Kit on-device image classification. No judgment either way.

### 🌶️ Burrito Facts
Random burrito facts surface on the home screen while you wait for your next burrito. Educational and mildly distracting.

### 🍽️ Recipes
Browse 10 burrito recipes with ingredient checklists — checkboxes persist between sessions. The app detects your country via GPS and surfaces a "Local Favorite" recipe at the top of the list.

### ⚙️ Settings
Dark mode, per-type notification toggles, location modal preferences, full entry history with edit/delete/add, and a reset button for when you want to pretend none of this happened.

## Tech Stack

- **Kotlin** — because Java is a lot of words
- **Jetpack Compose** — UI that actually enjoys being written
- **Room** — every burrito click persisted forever, no burrito forgotten
- **DataStore** — lightweight preferences for settings and flags
- **WorkManager** — periodic reminders running quietly in the background
- **Hilt** — dependency injection, the responsible way
- **Google Maps + Places API** — the burrito locator
- **ML Kit Image Labeling** — the burrito classifier
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
