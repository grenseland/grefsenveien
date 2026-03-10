# Car Test Android App & Wear OS Tile

An Android application with Android Auto support and a Wear OS Tile that makes URL requests to control home automation (e.g., Garage and Gate) via Nabu Casa webhooks.

## Features

**Android Auto & Phone App (`app` module)**
- Simple button UI on phone
- Android Auto compatible
- Makes HTTP GET requests to a URL when button is pressed
- Works in car displays via Android Auto

**Wear OS (`wear` module)**
- Wear OS Tile (Widget) for quick access
- Interactive buttons to control the Garage and Gate directly from the watch face
- Triggers Nabu Casa webhooks via HTTP GET requests

## Project Structure

### `app` module
- `MainActivity.java` - Main phone UI with button
- `CarAppService.java` - Android Auto service
- `MainCarScreen.java` - Android Auto screen with button
- `ConfirmationScreen.java` - Confirmation screen after request

### `wear` module
- `ActionTileService.java` - Wear OS Tile Service providing Garage and Gate control
- `MainActivity.java` - Basic activity for the Wear OS app

## Building

1. Open the project in Android Studio
2. Sync Gradle files
3. Add launcher icon images to the mipmap folders (`ic_launcher.png`)
4. Build and run the `app` module on a phone/car emulator, or the `wear` module on a Wear OS device/emulator

## Testing Android Auto

To test Android Auto functionality:

1. Install Android Auto on your phone
2. Enable Developer Mode in Android Auto settings
3. Connect your phone to Android Studio
4. Run the app
5. Open Android Auto - you should see "Car Test" app

## Configuration

### Android Auto URLs
To change the URL that gets requested in the Android app, modify the URL in:
- `app/src/main/java/com/pixelspore/grefsenveien/MainActivity.java`
- `app/src/main/java/com/pixelspore/grefsenveien/MainCarScreen.java`

### Wear OS Webhooks
To change the Nabu Casa webhooks for the Wear OS Tile, modify the URLs in:
- `wear/src/main/java/com/pixelspore/grefsenveien/wear/ActionTileService.java`
Currently uses Nabu Casa webhook URLs for `garasjeport` and `port`.
