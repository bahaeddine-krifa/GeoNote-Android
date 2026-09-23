# 🌍 GeoNote: Smart Travel Journal

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-7F52FF?style=for-the-badge&logo=kotlin)
![Android](https://img.shields.io/badge/Android-Native-3DDC84?style=for-the-badge&logo=android)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-FF6F00?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-Room-4285F4?style=for-the-badge)
![Network](https://img.shields.io/badge/Network-Retrofit-4285F4?style=for-the-badge)
![UI](https://img.shields.io/badge/UI-Material%20Design%203-007396?style=for-the-badge)

**GeoNote** is a modern, native Android application designed as a smart travel journal. It allows users to create rich, interactive travel entries by combining text, photos, geolocation, and real-time weather data. Built with a robust, enterprise-grade architecture, GeoNote features offline-first capabilities, secure authentication, and local reminders.

---

## 📑 Table of Contents
- [✨ Key Features](#-key-features)
- [🛠️ Tech Stack & Architecture](#️-tech-stack--architecture)
- [🔄 Data Flow & Technical Highlights](#-data-flow--technical-highlights)
- [📱 App Screenshots](#-app-screenshots)
- [🚀 Getting Started](#-getting-started)
- [ Future Improvements](#-future-improvements)
- [🎓 Acknowledgments](#-acknowledgments)

---

## ✨ Key Features

- 📝 **Rich Travel Entries:** Create detailed journal entries with titles, descriptions, dates, and photos.
- 📍 **Smart Geocoding & Weather:** Automatically enriches entries by fetching the city name (via Nominatim API) and current weather conditions (via Open-Météo API) based on GPS coordinates.
- 🔐 **Secure Authentication:** Seamless user registration and login powered by Firebase Authentication.
-  **Camera Integration:** Capture photos directly within the app using CameraX/Intents, with secure `FileProvider` URI handling.
- ⏰ **Local Reminders:** Set up trip reminders using `AlarmManager` and a custom `BroadcastReceiver`.
- 📶 **Offline-First Design:** All data is persisted locally using Room Database, ensuring the app works perfectly even without an internet connection.
-  **Modern UI/UX:** Built with Material Design 3, featuring smooth transitions, dynamic loading states, and elegant empty/error handling.

---

## 🛠️ Tech Stack & Architecture

| Category | Technologies & Libraries |
| :--- | :--- |
| **Language** | Kotlin 1.9.10 |
| **Architecture** | MVVM + Repository Pattern |
| **UI Toolkit** | XML, Material Design 3, ViewBinding |
| **Local Persistence** | Room (SQLite) + Kotlin Flow |
| **Networking** | Retrofit, OkHttp, Gson |
| **Asynchronous** | Kotlin Coroutines, StateFlow, SharedFlow |
| **Hardware APIs** | FusedLocationProvider (GPS), Camera, AlarmManager |
| **Authentication** | Firebase Authentication |
| **Build System** | Gradle (Kotlin DSL) |

---

## 🔄 Data Flow & Technical Highlights

### 📥 Lifecycle of a Travel Entry
1. **UI Layer:** The user fills out the form in `AddEntryActivity` and taps "Save".
2. **ViewModel Layer:** `AddEntryViewModel` launches a coroutine in `viewModelScope` to keep the main thread unblocked.
3. **Repository Layer:** Fetches GPS coordinates, then calls both REST APIs in parallel (`async/awaitAll`) to format the results (city name + weather icon).
4. **Database Layer:** Inserts the enriched `TravelEntry` entity into the local Room SQLite database.
5. **Reactivity:** Room automatically emits the updated list via `Flow`. The `MainViewModel` transforms it into a `StateFlow<UiState>`.
6. **UI Update:** `MainActivity` observes the `StateFlow`, submits the list to the `RecyclerView`. `DiffUtil` calculates the differences and animates only the modified elements at 60fps.

### 🛡️ Error Management & Resilience
- **Network:** Encapsulated in a sealed class `NetworkResult<T>`. Automatically falls back to raw coordinates with an "Offline" badge if the connection drops.
- **Permissions:** Contextually requested via `ActivityResultContracts.RequestPermission()`. Handles denials gracefully without crashing.
- **Android 12/13 Compliance:** Native handling of `SCHEDULE_EXACT_ALARM` and `POST_NOTIFICATIONS` with automatic redirection to system settings if refused.

---

## 📱 App Screenshots

*(Add your screenshots here! Replace the paths below with your actual image links)*

| Splash & Login | Main Dashboard | Add Entry | Entry Details |
| :---: | :---: | :---: | :---: |
| ![Splash](screenshots/splash.png) | ![Main](screenshots/main.png) | ![Add](screenshots/add.png) | ![Detail](screenshots/detail.png) |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Latest stable version, e.g., Iguana or Jellyfish)
- JDK 17 or higher
- A physical Android device or Emulator (API 24+)
- A Firebase Project (for Authentication)

### Installation
1. **Clone the repository:**
   ```bash
   git https://github.com/bahaeddine-krifa/GeoNote-Android.git
   cd GeoNote-Android