# Music Assistant Android Client

A Jetpack Compose Android app that connects directly to your local Music Assistant server (default `http://192.168.0.190:8095`). The app authenticates with a long-lived token, establishes an OkHttp WebSocket for real-time playback updates, and exposes a lightweight UI for selecting players and viewing the queue.

## Project highlights
- Kotlin + Compose Material 3 UI with MVVM and `StateFlow`
- OkHttp WebSocket client that authenticates and requests `players/all` + `player_queues/all` on connect
- Retrofit client for one-off HTTP fetches (players + queues)
- DataStore + encrypted preferences for persisting server URL and access token
- Media3 `MediaSession` placeholder to expose basic metadata to the system
- Network Security Config allowing cleartext traffic specifically to `192.168.0.190`

## Running
1. Set up the Android SDK/Android Studio and connect a device or emulator.
2. Add your Music Assistant server URL and access token on the setup screen.
3. The app will connect to the WebSocket at `ws://<server>/ws`, fetch players and queues, and render the first available player.

> Note: The Gradle wrapper JAR is not bundled in this environment; you may need to regenerate it (`gradle wrapper`) before building locally if the wrapper download fails.
