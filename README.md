# 🌌 Noor Clock
> **Noor Clock** is a premium, beautifully crafted Smart Alarm Clock, Checklist Productivity Tool, Live Weather Monitor, and Location-Based Islamic Prayer Companion (Salah, Tahajjud, Suhoor). Styled in pristine Material Design 3 pastel palettes with fluid responsive interactions, it is designed to keep you spiritually on track and productively grounded.

---

## ✨ Features Spotlight

### 📍 1. Global Touch-to-Locate System (Anywhere on Earth)
*   **Touch-To-Change UI**: Touch the city location label on the home dashboard to prompt the **Global Geocoding Override Dialog**.
*   **Universal Search Engine**: Search for any country, city, district, or remote village using our responsive georeferenced API matching.
*   **Precision Coordinates Override**: Input exact latitude and longitude values manually to override prayer calculations for remote locations (e.g., deserts, fields, or offshore locations) where network databases are absent.
*   **Islamic Capital & Historical Seeds**: Easily set your location to one of the ready-made seeds:
    *   *Makkah (Haram)*
    *   *Madinah (Al-Masjid)*
    *   *Al-Aqsa (Jerusalem)*
    *   *Dhaka, Jakarta, Cairo, Kuala Lumpur, London, New York...*

### 🎵 2. High-Fidelity Custom Alarm Tone Deck
*   **Standard Presets**:
    *   🌅 *Serene Dawn* — A minimalist pip sequence.
    *   🐦 *Birds Melody* — Playful ambient chirping rhythms.
    *   🎹 *Gentle Harp* — Cascading confirmation chords.
    *   🔔 *Melodious Echo* — Guard alert rings.
*   **Custom Local File Imports**: Choose, upload, and import any `.mp3`, `.wav`, or `.ogg` sound file directly from your local storage system into the application's secure sandbox.
*   **Flexible Lifecycle Management**: Manage your Imported list inside the picker itself with instant deletion of unneeded files.
*   **Auto-Synth Fallback**: In the rare event of an invalid or deleted audio URI, the background scheduler initiates a smooth synthesizer pulse (`ToneGenerator`) to ensure you are always awakened gracefully.

### 🎨 3. Elegant Themes & Fluid Aesthetics
*   **Pastel Aesthetic Backgrounds**: Switch amongst themes with dynamic flowing backdrops (e.g., Makkah Mosque vectors, Peaceful Cosmos deep plums, Mountain Sunrise radial gradients, and Ocean Ripples).
*   **Sleek Digital Clock & Prayer Rails**: Live date formatting and modern countdown status cards.
*   **Checklist & Focus Mode**: Organize daily spiritual, secular, or household chores inside a streamlined, persistent checklist tracking suite.

---

## 🛠️ Tech Stack & Architecture

*   **Language**: 100% Kotlin
*   **UI Toolkit**: Jetpack Compose (Material Design 3 with dynamic light/dark schemes, Edge-to-Edge window insets support, adaptive layouts)
*   **Data Persistence**: Room Database for permanent storage of customized alarms, checklist files, and state trackers.
*   **Audio Engines**: Android `MediaPlayer` coupled with context-aware `AudioAttributes` for USAGE_ALARM, paired with custom hardware `ToneGenerator` pip synthesizers.
*   **Asynchronous Flow**: Kotlin Coroutines, StateFlow, and SharedFlow for reactivity.

---

## 🚀 Running the App Locally

### Prerequisites
1.  **Android Studio** Ladybug or newer.
2.  **JDK 17** configured in your gradle settings.
3.  Active Internet connection (optional, for geocoding search and live weather, though calculation algorithms run 100% offline).

### Build & Run
1.  Import this project into Android Studio.
2.  Wait for the Gradle Sync to complete.
3.  Run the application using the green play button or execute via command line:
    ```bash
    gradle :app:assembleDebug
    ```

### Running Unit & Screenshot Tests
Validate correctness of custom calculations and theme layout renderings:
```bash
# Run unit and local integration tests
gradle :app:testDebugUnitTest
```

---

## 🎨 Design System Design Values
*   **Visual Rhythm**: Strictly aligned to the 8dp grid spacing with comfortable, expansive paddings.
*   **Interaction Ripples**: Standardized Material ripples on every clicking surface.
*   **Color Uniformity**: Avoids hardcoded hex variables; coordinates colors dynamically directly with `MaterialTheme.colorScheme` and modern color pairs.
