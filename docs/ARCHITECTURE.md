# Architecture and technical notes

This document contains implementation and platform details kept out of the [README](../README.md).

## Application design

- The application is a small native Android `Activity` with a fullscreen `WebView`; its settings, keyboard/remote handling, and gear button are native Android UI.
- The app first probes **Material Skin's existing** `/material/np-only` endpoint with a background GET and verifies its recognizable Now Playing redirect. The probe retries transient startup timeouts, missing-route responses, and unexpected pages before reporting an error. Only after success does it load the display in its WebView. Material Skin performs the rendering; no local HTML recreation or extra server plugin is involved.
- The WebView is deliberately **view-only**: touch and media-control interactions with the page are blocked. The settings overlay remains interactive. This is a UI behavior, **not** an authorization or network-security barrier.
- The selected server, player **ID**, keep-awake choice, and appearance settings are saved locally via Android `SharedPreferences`. A player rename therefore does not normally require reconfiguration.
- The app can also be opened with an optional ADB `url` intent extra for development.

### Server discovery and player selection

- Server discovery uses an `eIPAD` broadcast on UDP **3483**. It requires access to the local network, and may not work across VLANs, VPNs, client isolation, or certain Wi-Fi routers.
- Manual server URL entry is always available; local installations commonly use HTTP on port **9000**, although HTTPS is supported when configured on the server.
- Player discovery uses Lyrion's read-only JSON-RPC endpoint, `POST /jsonrpc.js`, with the command `["", ["players", "0", "100"]]`.
- The JSON-RPC client currently handles **unauthenticated** servers and up to **100 players**. Authenticated endpoints and larger player lists are not yet implemented.
- To accommodate a slow first launch, the probe uses a 5-second connect timeout and 6.5-second read timeout, automatically retrying temporary failures (up to three attempts for an absent route/unexpected HTML or HTTP 5xx; up to two for network/timeouts). Repeated HTTP 404/410 produces a native **Material Skin unavailable** dialog with Retry, Settings, and Exit. An unexpected 200 page is reported without claiming the plugin is missing, and timeouts have their own message. The WebView remains covered by a native loading view until the Material Skin UI is verified; cold WebView initialization is retried before reporting failure, and main-frame HTTP failures are intercepted. The separate Material Android app is never required.
- Normal Lyrion clients can operate alongside the display.

### Material Skin themes and native appearance

The settings surface offers Material Skin's native options:

- Themes: **Automatic, Light, Dark, Black, Lyrion**, plus custom themes returned by the server.
- Color usage: **For all players**, **From current cover (all players)**, and **Per player**.
- The original built-in accent-color choices, server-supplied custom colors, and **Color toolbars**.

Custom themes and colors are requested via the same read-only JSON-RPC command used by Material Skin's settings, `["", ["material-skin", "themes", "platform:android"]]`. When first configured, the app can also read Material Skin's optional server-side UI defaults (`plugin.material-skin:defaults`).

The selected theme and color are passed to the Material Skin page with its existing `theme` URL parameter. The selected color-usage mode is written to the WebView origin's `lms-material::colorUsage` local-storage setting; the page reloads once if that preference changes.

Material Skin continues to serve **its own original CSS**. The surrounding native settings use the corresponding built-in theme palette and, when available, sampled CSS colors from the running WebView. Sampling helps match dynamic cover-derived colors and custom themes, but is not a guarantee that every native Android control looks pixel-for-pixel identical to the web page.

**Automatic** follows the device's light/dark preference where Android reports one. Older Fire OS devices may not expose such a preference. The setting is local to this app; it does not overwrite Material Skin preferences in other browsers.

### Input and display behavior

| Input | Behavior from Now Playing |
| --- | --- |
| Fire TV / Android TV remote: Menu | Open native settings immediately |
| D-pad | Reveal / focus the settings gear |
| Remote OK / Enter | Reveal the gear; select it to open settings |
| Touch | Reveal the gear, then tap it |
| Mouse/trackpad | Reveal the gear on pointer movement; click to open |
| Back | Exit the app |

The native gear overlay fades in over **230 ms**, is normally shown for **5 seconds**, and fades out over **700 ms**. The overlay is not part of Material Skin's web content.

When `Keep screen awake` is enabled and supported, the app applies Android's `FLAG_KEEP_SCREEN_ON` **only while the Now Playing display is foregrounded**. It clears the flag in settings and when backgrounded. TV firmware and the physical television may have independent screen or power-saving behavior.

## Android compatibility

- `minSdk 22`: Android **5.1** or later.
- `compileSdk 35` and `targetSdk 35`.
- The original target device is a **first-generation Amazon Fire TV Stick 4K (2018)**, model `AFTMM`, running **Fire OS 6 / Android 7.1 (API 25)**.
- Intended to run on other Android-based Fire TV devices, Android TV, Google TV, tablets and phones, but those combinations still require device-level testing.
- This APK cannot run on **Amazon Vega OS, Samsung Tizen, or LG webOS**.

Android TV launchers may choose the separately packaged TV banner or the regular launcher icon. On the Fire TV device used during development, the launcher displays the **square icon**, even though an Android TV banner is included in the APK.

## Building and local installation

See [Build toolchain](BUILD_TOOLCHAIN.md) for the exact development tool versions.

```sh
gradle :app:lintDebug
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

Example installation on an Android-based Fire TV from Windows PowerShell, with [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools) installed:

```powershell
adb connect FIRE_TV_IP:5555
adb shell getprop ro.build.version.sdk
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

For the 2018 Fire TV Stick 4K, `getprop ro.build.version.sdk` should report **25**.

Optional ADB shortcut to configure a player by URL:

```sh
adb shell am start -n com.chrober.lmsmaterial.nowplaying/.MainActivity \
  --es url 'http://192.168.1.20:9000/material/np-only?player=Living%20Room'
```

Do not expose an unauthenticated Lyrion server to the public internet merely for this app. It is designed for a **trusted local network**.

## CI and releases

- `.github/workflows/ci.yml` runs lint, unit tests and a debug APK build on PRs and changes to `main`. CI publishes a debug APK artifact for testing.
- `.github/workflows/release.yml` builds, signs, and verifies the release APK, generates a SHA-256 checksum, and can publish a GitHub pre-release when triggered by a version tag or `publish/v*` branch.
- A signed release APK uses the configured signing key, **not** the debug certificate. The package ID is `com.chrober.lmsmaterial.nowplaying`.
- Lyrion branding was [approved by Michael Herger](https://github.com/chrober/lms-material-now-playing-app/issues/15#issuecomment-6979289683) while the app remains free and open source; release distribution is permitted within that scope.

See [Release instructions](RELEASING.md) for signing-key management and release mechanics.
