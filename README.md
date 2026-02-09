# ⏱️ Timer & Stopwatch

A simple Timer and Stopwatch application available in both **Web** and **Android** versions.

---

## 📁 Project Structure

```
timer-stopwatch/
├── index.html              # Web version (Brutalist Design)
├── stopwatch/              # Web subdirectory
│   └── index.html
├── android/                # Android app (Kotlin + Jetpack Compose)
│   ├── app/src/main/java/...
│   └── build.gradle.kts
└── TimerStopwatch-v1.0.0-debug.apk
```

---

## 🌐 Web Version

**URL:** https://irfan281.github.io/timer-stopwatch/

### Features:
- ⏱️ **Stopwatch** with millisecond precision
- ⏲️ **Timer** with sound alarm
- 🎨 **Brutalist Design** — bold colors, thick borders
- 📱 **Responsive** — works on mobile

---

## 📱 Android Version

### Tech Stack:
- **Kotlin** — Modern Android language
- **Jetpack Compose** — Declarative UI
- **Material Design 3** — Clean, modern design
- **DataStore** — Local storage for persistence
- **Navigation Compose** — Bottom navigation

### Features:
| Stopwatch | Timer |
|-----------|-------|
| Millisecond precision (00:00:00.00) | Countdown with ms precision |
| Start/Pause/Reset | Start/Pause/Reset |
| **Lap feature** — save split times | **Quick presets** — 1m, 5m, 10m, 15m, 25m |
| Lap history | **Sound alarm** — beep when done |
| | **State persistence** — survives app restart |

### Build Instructions:

```bash
cd android

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📥 Download APK

Download the latest APK from the repository or build it yourself!

---

## 📝 License

MIT License — feel free to use and modify!

---

Built with ❤️ by Irfan281
