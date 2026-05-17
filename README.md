# MiMo Chat WebView

An Android application that serves as a dedicated container for the Xiaomi MiMo Chat web interface. This app provides a seamless, full-screen experience with integrated features like file uploads and pull-to-refresh.

## Features

- **Full-Screen Experience**: Optimized for immersive use with system bars hidden.
- **Pull-to-Refresh**: Easily reload the interface using standard Android swipe gestures.
- **File Upload Support**: Fully compatible with web-based file pickers for uploading assets.
- **Optimized Authentication**: Custom handling for Google and Xiaomi account logins within the app.
- **Offline Support**: Graceful handling of network connectivity issues with a custom offline page.
- **JavaScript Bridge**: Native bridge for future expansions between Web and Android components.

## Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer recommended.
- **Gradle**: 8.9
- **Android SDK**: API 24 (Android 7.0) to API 34 (Android 14).

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/xiaomi-chat-wrapper.git
```

### 2. Open in Android Studio
- Open Android Studio.
- Select **File > Open**.
- Navigate to the project folder and click **OK**.

### 3. Build and Run
- Connect an Android device or start an emulator.
- Click the **Run** button (green play icon) in the toolbar.

## Project Structure

- `app/src/main/java`: Contains the Kotlin source code (`MainActivity.kt`, `WebAppInterface.kt`).
- `app/src/main/res`: Contains layout, drawables, and app icons.
- `app/build.gradle.kts`: Project-specific dependencies and build configurations.

## Technical Details

- **Language**: Kotlin
- **WebView**: Enhanced with `WebChromeClient` for file handling and `WebViewClient` for navigation control.
- **Theme**: Material Components with Edge-to-Edge support.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
