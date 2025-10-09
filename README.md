# Weather Widget

A minimalist Android home screen widget that displays current weather conditions based on your GPS location.

## Features

- **GPS-based weather** - Automatically fetches weather for your current location
- **Transparent design** - Blends seamlessly with your home screen
- **Custom font** - Uses a custom TTF font rendered as bitmaps
- **Auto-updates** - Refreshes weather every 30 minutes
- **Tap to open** - Opens Google Weather app when tapped

## Weather Source

Weather data is provided by [Open-Meteo](https://open-meteo.com/), a free weather API that doesn't require an API key or registration.

**API Details:**
- Source: Open-Meteo Forecast API
- Update frequency: Every 30 minutes (widget auto-refresh)
- Temperature unit: Fahrenheit
- Data: Current temperature and weather conditions

## Permissions

The app requires the following Android permissions:

### Required Permissions
- **`INTERNET`** - To fetch weather data from Open-Meteo API
- **`ACCESS_NETWORK_STATE`** - To check network connectivity
- **`ACCESS_FINE_LOCATION`** - To get precise GPS coordinates for weather
- **`ACCESS_COARSE_LOCATION`** - Fallback for approximate location (WiFi/cell tower)

Location permissions are used exclusively to determine your coordinates for weather fetching. No location data is stored or transmitted except to the Open-Meteo API.

## Development Setup

### Prerequisites
- Flutter 3.x or higher
- Android Studio with Android SDK
- Android emulator or physical Android device

### Running for Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd weatherwidget
   ```

2. **Install dependencies**
   ```bash
   flutter pub get
   ```

3. **Start Android emulator or connect device**
   ```bash
   # List available emulators
   flutter emulators
   
   # Launch emulator
   flutter emulators --launch <emulator-id>
   
   # Or check connected devices
   flutter devices
   ```

4. **Run the app**
   ```bash
   flutter run
   ```

5. **Add widget to home screen**
   - Long-press on home screen
   - Tap "Widgets"
   - Find "weatherwidget"
   - Drag to home screen

6. **Grant location permissions** (if needed)
   ```bash
   adb shell pm grant com.example.weatherwidget android.permission.ACCESS_FINE_LOCATION
   adb shell pm grant com.example.weatherwidget android.permission.ACCESS_COARSE_LOCATION
   ```

7. **Set mock GPS location** (emulator only)
   ```bash
   # Example: Mountain View, CA
   adb emu geo fix -122.084 37.422
   ```

8. **View logs**
   ```bash
   adb logcat -s WeatherWidget:D WeatherService:D LocationService:D
   ```

## Project Structure

```
weatherwidget/
├── lib/
│   └── main.dart                    # Flutter app entry point
└── android/
    └── app/src/main/
        ├── kotlin/com/example/weatherwidget/
        │   ├── WeatherWidgetReceiver.kt    # Widget logic
        │   ├── WeatherService.kt           # Weather API client
        │   ├── LocationService.kt          # GPS location fetching
        │   └── TextBitmapHelper.kt         # Custom font rendering
        ├── res/
        │   ├── font/
        │   │   └── custom_font.ttf         # Custom TTF font
        │   ├── layout/
        │   │   └── weather_widget.xml      # Widget layout
        │   └── xml/
        │       └── weather_widget_info.xml # Widget metadata
        └── AndroidManifest.xml             # Permissions & widget registration
```

## How It Works

1. **Widget Updates**: Android calls `onUpdate()` every 30 minutes
2. **Location Fetch**: Gets GPS coordinates via `LocationService`
3. **Weather API Call**: Fetches weather from Open-Meteo using coordinates
4. **Font Rendering**: Text is rendered to bitmaps using custom TTF font
5. **Display**: Bitmaps are set as ImageViews in the widget
6. **Click Handler**: Opens Google Weather app when tapped

### Why Bitmaps for Text?

Android widgets use `RemoteViews`, which run in the system process and don't support custom fonts. To use a custom TTF font, we:
1. Load the font in our app's process
2. Render text to a bitmap using `Canvas.drawText()`
3. Send the bitmap image to the widget
4. Display as an `ImageView`

This is called "pre-rendering" or "rasterization."

## Building for Release

```bash
flutter build apk --release
```

The APK will be located at:
```
build/app/outputs/flutter-apk/app-release.apk
```

## License

[Add your license here]
