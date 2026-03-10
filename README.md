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

## Configuration & Secrets (Local Setup)

To keep sensitive URLs and signing keys out of version control, this project uses local property files that are ignored by Git. 

**You must create these files locally before building the project:**

### 1. Webhooks & Image URLs (`local.properties`)
Create or open the `local.properties` file in the root directory (where `sdk.dir` is usually defined) and add your secret URLs:

```ini
GARAGE_WEBHOOK_URL=https://your-home-assistant.url/api/webhook/secret_code_garage
GATE_WEBHOOK_URL=https://your-home-assistant.url/api/webhook/secret_code_gate
S3_IMAGE_URL=https://your-s3-bucket-url.com/latest.jpg
```
*Gradle will read these during compilation and automatically inject them into both the Mobile App and the Wear OS Tile via `BuildConfig`.*

### 2. Signing Keys for Release (`keystore.properties`)
To build a signed `.aab` for the Google Play Store (`bundleRelease`), you need the original keystore and its passwords.
Place the `pixelspore.keystore` file in the `app/` folder, and create a `keystore.properties` file in the project root:

```ini
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```
