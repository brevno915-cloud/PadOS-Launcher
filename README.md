# PadOS Launcher

PadOS Launcher is a premium, iPadOS-inspired Android tablet launcher prototype. The project is structured as an Android app that can be built with Android Studio or a compatible Android SDK environment.

The project intentionally preserves the existing codebase and tries to keep the launcher incremental and editable rather than restarting from scratch.

## Current status

- Native launcher intent filter is present in the manifest.
- Installed apps are loaded through Android PackageManager APIs.
- Home screen, dock, search, wallpaper preferences, and settings are included.
- The app is designed for Android 15 tablets and supports portrait + landscape layouts.
- Full APK export is not claimed yet, because this environment does not include a verified Android SDK/Gradle build toolchain to compile and install the artifact.

## Build in a real Android environment

1. Install Android Studio or a compatible Android SDK.
2. Open the project in Android Studio.
3. Ensure the following are available:
   - JDK 17
   - Android SDK 35
   - Android build tools
4. Let Gradle sync the project.
5. Build the debug APK with:

   ./gradlew assembleDebug

   or run the equivalent Android Studio build task.

6. Install the generated APK to a real tablet or emulator.

## Set as default home app

On Android, choose the launcher manually:

- Settings
- Apps
- Default apps
- Home app
- PadOS Launcher

## Important limitation

This environment cannot validate a real Android APK build because the Android SDK/Gradle toolchain is not available here. The project is laid out for that build, but the compiled APK itself is not yet verified. This is the honest status until a valid Android build environment is used.
