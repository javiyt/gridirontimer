# GridironTimer

## A professional-grade countdown timer application for American Football Referees on Wear OS

GridironTimer is a specialized smartwatch app designed to simplify and streamline the timing requirements of American Football referees. Instead of juggling multiple stopwatches or manual timers, referees can now manage all required countdowns directly from their wrist using a single, intuitive interface.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [How It Works](#how-it-works)
  - [The Timers](#the-timers)
  - [Game Modes](#game-modes)
  - [Controls & Buttons](#controls--buttons)
- [Installation on Wear OS Device](#installation-on-wear-os-device)
- [Troubleshooting](#troubleshooting)

---

## Overview

American Football referees need to track multiple timers during a game, each with specific purposes and durations. GridironTimer consolidates all of these critical timing functions into a single, easy-to-use Wear OS application that lives on your wrist.

Whether you're managing the official game clock, monitoring the offense's play clock window, or tracking a timeout, GridironTimer provides fast, reliable timing with clear visual feedback so you can focus on the game, not the timepiece.

---

## Key Features

- **Dual Game Modes**: Support for Flag Football (20-minute halves with 25-second play clocks) and Tackle Football (12-minute quarters with 40-second play clocks)
- **Multiple Concurrent Timers**: Run the game clock, play clock, and timeout simultaneously
- **Custom Timer Duration**: Adjust flag and tackle game durations to suit your league's rules
- **Quick-Start Controls**: One-tap access to the most common timer operations
- **Clear Visual Feedback**: Large, easy-to-read countdown display with visual alerts as time runs out
- **No Internet Required**: Works completely offline
- **Optimized for Wrist Display**: Designed specifically for the small screen of a smartwatch

---

## How It Works

### The Timers

GridironTimer manages three primary timers that referees need to control:

#### 1. **Game Clock** (Main Countdown)

- **Flag Football**: 20 minutes per half
- **Tackle Football**: 12 minutes per quarter
- **Purpose**: Tracks the official game time that runs continuously during play

#### 2. **Play Clock** (Offense's Time Window)

- **Flag Football**: 25 seconds
- **Tackle Football**: 40 seconds
- **Purpose**: The offense must snap the ball before this clock reaches zero, or they lose a down. This timer resets after each play.

#### 3. **Timeout Clock**

- **Duration**: 60 seconds
- **Purpose**: Tracks official team timeouts, which are brief breaks requested by teams during active play

#### 4. **7-Second Countdown** (Flag Football Only)

- **Duration**: 7 seconds
- **Purpose**: Additional precision timing in certain flag football situations

### Game Modes

#### **Flag Football Mode**

- Game duration: 20 minutes per half
- Play clock: 25 seconds
- Ideal for recreational and youth leagues
- Perfect for games with shorter rounds and faster play

#### **Tackle Football Mode**

- Game duration: 12 minutes per quarter
- Play clock: 40 seconds
- Designed for standard organized football
- Accommodates longer timeouts and more complex play sequences

### Controls & Buttons

The app features an intuitive button layout optimized for on-wrist operation:

#### **Main Menu**

- **Flag**: Launches Flag Football mode with pre-configured timers
- **Tackle**: Launches Tackle Football mode with pre-configured timers
- **Settings**: Access custom timer durations to match your league's specific rules

#### **During Play** (Timer Screen)

- **Start/Pause**: Tap the timer display or use physical watch buttons to start or pause any active timer
- **Reset**: Quickly return a timer to its default duration
- **Play Clock Controls**: One-tap access to start the 25-second (flag) or 40-second (tackle) play clock
- **Timeout Timer**: Launch the 60-second timeout clock with a single tap
- **Back**: Return to the main menu to select a different mode

#### **Physical Watch Buttons**

GridironTimer is fully compatible with your Wear OS smartwatch's physical buttons:

- **Upper Button**: Start/stop the current timer or navigate
- **Lower Button**: Reset or go back to previous screen
- **Swipe Gestures**: Swipe left/right to navigate between different screens

---

## Installation on Wear OS Device

Currently, this app is **not publicly available on Google Play** due to Google's restrictions on certain app categories. Follow this guide to install it directly on your Wear OS smartwatch.

### Prerequisites

- **Wear OS smartwatch** (running Wear OS 3.0 or higher recommended)
- **Android Debug Bridge (adb)** installed on your computer
  - Install via Android SDK Platform Tools
  - Or install via Homebrew on macOS: `brew install android-platform-tools`
- **USB cable** (optional: wireless debugging can also be used)
- **Build tools**: Gradle and Android SDK

### Step 1: Enable Developer Mode on Your Watch

1. Go to **Settings** → **About**
2. Scroll down and tap **Build Number** 7 times
3. Go back to **Settings** → **Developer Options**
4. Enable **USB Debugging** (or **Wireless Debugging** if using wireless connection)

### Step 2: Connect Your Watch to Your Computer

**USB Connection:**

```bash
adb connect <watch-ip-address>  # For wireless debugging
# or use USB cable and:
adb devices  # Will list your connected watch
```

**Wireless Connection:**

1. Note the watch's IP address from Settings → Developer Options → Wireless Debugging
2. In Developer Options, find the **pairing code** and **port number**
3. On your computer:

   ```bash
   adb pair <watch-ip>:<port> <pairing-code>
   adb connect <watch-ip>:<port>
   ```

### Step 3: Build the APK

Clone and build the release APK:

```bash
git clone https://github.com/javiyt/gridirontimer.git
cd gridirontimer
./gradlew assembleRelease
```

The APK will be located at: `app/build/outputs/apk/release/app-release.apk`

Alternatively, if you prefer a debug build:

```bash
./gradlew assembleDebug
```

### Step 4: Install on Your Watch

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

If installation fails, you may need to:

- Clear app data: `adb shell pm clear com.gridirontimer`
- Uninstall first: `adb uninstall com.gridirontimer`
- Ensure your watch has enough storage space

### Step 5: Launch the App

On your watch, open the app menu and tap **GridironTimer**, or use:

```bash
adb shell am start -n com.gridirontimer/.MainActivity
```

### Using Pre-built Release APK

If you prefer to skip the build step, you can download pre-built APKs from:

- GitHub Releases (if available): <https://github.com/javiyt/gridirontimer/releases>

### Troubleshooting

| Issue                                        | Solution                                                |
|----------------------------------------------|---------------------------------------------------------|
| `adb: device not found`                      | Ensure USB debugging is enabled and device is connected |
| Installation fails with insufficient storage | Uninstall conflicting apps or factory reset the watch   |
| App crashes on startup                       | Ensure Wear OS 3.0+ is installed                        |
| Cannot find APK file                         | Run `./gradlew clean assembleRelease` and try again     |
| Wireless connection issues                   | Try USB cable connection, or restart both devices       |
| Timers don't respond to taps                 | Try using the physical watch buttons instead            |
| Timer display is too small                   | Your watch may be in low-resolution mode; restart it    |
