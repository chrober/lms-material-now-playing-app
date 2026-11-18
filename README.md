# Lyrion Now Playing

A lightweight Android app that turns a Fire TV, Android TV, tablet, or other compatible Android-based device into a dedicated, fullscreen **Now Playing display** for [Lyrion Music Server](https://lyrion.org/).

The app uses [Material Skin](https://github.com/CDrummond/lms-material)'s existing `/material/np-only` page. It does **not** reimplement the Now Playing interface, control playback, or require changes to Material Skin.

## Why this app?

Material Skin already offers an excellent Now Playing display, but using it on a television typically means opening a browser, entering a URL, and managing a bookmark with a remote control. That is inconvenient for a screen you just want to switch on and use.

This small Android wrapper provides a more convenient experience: discover a Lyrion server, select a player once, and launch straight into its fullscreen Now Playing display on subsequent starts. Fire TV was the original motivation, but the idea is useful on other Android-based displays too.

## Features

- **Easy setup:** Discover Lyrion servers on the local network or enter a server address manually, then choose an available player.
- **Instant Now Playing:** Remembers your server and player and opens the fullscreen, read-only Material Skin view on launch.
- **Material Skin appearance:** Select the original **Automatic, Light, Dark, Black, or Lyrion** themes (and server-provided custom themes), plus the original color modes and accent colors. The embedded display and native settings follow the selection.
- **TV remote and touchscreen support:** Native, D-pad-friendly settings and a discreet gear overlay.
- **Keep screen awake:** Prevents the usual Android display timeout while the Now Playing screen is foregrounded, where supported.
- **Lightweight:** No extra Lyrion server plugin, Material Skin fork, account, or playback hardware required.

## Devices and requirements

The application targets **Android 5.1 (API 22) or newer**, including:

- **Fire TV** models running Android-based Fire OS — the original development and test platform is the first-generation Fire TV Stick 4K (2018, Fire OS 6 / Android 7.1).
- **Android TV and Google TV** devices, including compatible Android-based smart TVs.
- **Android tablets and phones**.

Other devices are not comprehensively hardware-tested. Actual behavior depends on the installed Android WebView, TV firmware, remote-control support, and display power-management settings.

**Not supported:** Amazon **Vega OS**, Samsung **Tizen**, and LG **webOS** (these do not run this Android APK).

You also need a reachable Lyrion Music Server with [Material Skin](https://github.com/CDrummond/lms-material) installed and enabled **on the server**. Craig Drummond's separate Material Android app does not need to be installed on the device. If Material Skin is unavailable, the app displays a native explanation and offers **Retry**, **Settings**, and **Exit**, rather than showing a server error page.

## Getting started

> **Download:** Signed APKs are available from [GitHub Releases](https://github.com/chrober/lms-material-now-playing-app/releases). Michael Herger [approved use of Lyrion branding](https://github.com/chrober/lms-material-now-playing-app/issues/15#issuecomment-6979289683) for this free and open-source project.

Install a signed APK from [GitHub Releases](https://github.com/chrober/lms-material-now-playing-app/releases), or [build the debug APK from source](#building-from-source). Once installed:

1. Launch the app and discover a Lyrion server, or enter its HTTP(S) address manually.
2. Select one of the players returned by that server.
3. Optionally adjust the **Theme**, **Color**, and **Keep screen awake** settings.
4. Choose **Save and show Now Playing**. The next launch goes straight to the saved player's display.

The display is intentionally **view-only**. Playback remains controlled through your usual Lyrion clients.

### Opening the settings again

| Device/input | Action |
| --- | --- |
| Fire TV / Android TV remote | Press **Menu** to open settings, or use the **D-pad** to reveal the gear and **OK** to select it. |
| Touchscreen | Tap the screen to reveal the gear, then tap it. |
| Mouse or trackpad | Move the pointer to reveal the gear, then click it. |

## How it works

The app is a native Android wrapper around Material Skin's existing `/material/np-only?player=...` endpoint. It discovers servers and players, displays the selected player in an Android WebView, and keeps its own settings locally. Material Skin itself provides the Now Playing rendering and themes.

This is an **unofficial companion project**, not an official Lyrion or Material Skin application. **Michael Herger approved use of the Lyrion name and branding for this free and open-source project** ([permission comment](https://github.com/chrober/lms-material-now-playing-app/issues/15#issuecomment-6979289683)). The application's source-code license does not imply permission to use third-party trademarks or logos. See [Lyrion's terms](https://lyrion.org/terms/) and [artwork attribution / branding status](docs/BRANDING.md).

## Building from source

With JDK 17, Gradle 9.9.1, and Android SDK platform 35/build tools 35.0.0:

```sh
gradle :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

The local debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected Android device using:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

A debug APK uses a different signing certificate from a signed release build; you cannot update one in place with the other.

### Further documentation

- [Architecture, controls, compatibility and limitations](docs/ARCHITECTURE.md)
- [Build toolchain](docs/BUILD_TOOLCHAIN.md)
- [Release process and current distribution hold](docs/RELEASING.md)
- [Artwork sources, attribution and branding status](docs/BRANDING.md)

## License and acknowledgements

This application's source code is licensed under the [MIT License](LICENSE).

The Now Playing UI and themes come from [Craig Drummond's Material Skin](https://github.com/CDrummond/lms-material). Server discovery and part of the artwork reference [Craig Drummond's Material Android app](https://github.com/CDrummond/lms-material-app), which is also MIT-licensed; see the source files and [branding documentation](docs/BRANDING.md) for attribution.

The app is independently developed and is **not affiliated with, sponsored by, or endorsed by** the Lyrion or Material Skin projects.
