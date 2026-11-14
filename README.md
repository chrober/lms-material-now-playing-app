# Lyrion Now Playing

A minimal, view-only Android application for the **Now Playing** screen of [Lyrion Music Server](https://lyrion.org/). It embeds the existing `/material/np-only` display from [Material Skin](https://github.com/CDrummond/lms-material) in a fullscreen Android WebView, without introducing playback controls or changing Material Skin.

Intended for Fire TV / Android TV, tablets, phones and other **Android-based** devices. **Not** compatible with Amazon's Vega OS.

## What it does

- Native settings to **find a Lyrion server** on the LAN (UDP discovery) or enter an HTTP(S) address manually.
- Fetches available players from that server with read-only Lyrion JSON-RPC; stores the selected **player ID** so renames do not break the display.
- Fullscreen, view-only Material Skin Now Playing. Regular Lyrion clients work in parallel.
- **Keep screen awake** setting, enabled by default. Applies Android's `FLAG_KEEP_SCREEN_ON` only while Now Playing is foregrounded. Disabled in settings if no display is available.
- No extra Lyrion plugin, fork of Material Skin, account or playback device required.
- Native settings and overlay that work with touchscreens, mouse and TV remote controls.

### Original Material Skin themes and colors

The native settings include the same **Theme** choices as Material Skin:
**Automatic, Light, Dark, Black and Lyrion**, plus any installed
user-defined themes returned by the connected server's
`material-skin themes platform:android` read-only JSON-RPC command.
There is deliberately no additional "Material Skin default" choice.

The **Color** selector offers the three Material Skin modes:
**For all players** (pick one of the original Material Skin colors, including
server-defined custom colors), **From current cover (all players)** and
**Per player**. It also offers Material Skin's **Color toolbars** option.

Selections are saved locally for this Android application and passed to
Material Skin's **original CSS** using its documented `theme` URL parameter,
and its browser-local `lms-material::colorUsage` preference. They affect both
the embedded Now Playing page and the surrounding native settings.
Native colors use the original built-in palettes and, when available, colors
read back from the running Material Skin page (including cover-derived colors
and custom CSS). The settings can still open if the server is temporarily
unreachable. No other browsers' or players' settings are changed.

"Automatic" tracks the device's light/dark mode where supported.
As on the original Material Skin, a Fire OS version without a system dark-mode
setting cannot provide automatic light/dark switching.
The default initial choices are Material Skin's **Dark** theme and
**From current cover** color behavior; server-provided defaults are read when
the server is first configured.

Buttons retain a highly visible solid-color fill, contrasting text and strong
outline when focused using a TV remote, plus a pressed state for touch.
The launcher artwork and the read-only character of the Now Playing display
are unchanged.

### Settings access and gear animation

| Input | From fullscreen Now Playing |
| --- | --- |
| Fire TV / Android TV remote | **Menu** opens settings immediately. **D-pad** direction reveals and focuses the gear; **OK** opens settings. |
| Remote OK / Enter | First press reveals the gear; second opens settings. |
| Touchscreen | Tap anywhere to reveal the gear, then tap the gear. |
| Mouse or trackpad | Moving the pointer reveals the gear; click it. |
| Back | Leave the app. |

The gear is a native, top-right overlay and **not part of the web page**. It fades in over **230 ms** with a soft deceleration and slight upward-to-neutral movement/scale. After **5 seconds** without activity, it fades out more slowly over **700 ms**. Further interaction cancels and restarts the animation/timer. WebView touch input and remote playback/keyboard commands are intercepted; the page remains view-only.

## Supported Android versions

`minSdk 22` (Android 5.1), `targetSdk 35`, `compileSdk 35`. Android **API 25** is explicitly supported at build level.

- **Amazon Fire TV Stick 4K, first generation (2018), ASIN B07PW9VBK5**: model `AFTMM`, Fire OS 6 / Android 7.1 (API 25).
- Android TVs and Android Fire TV devices at API 22+.
- Android tablets and phones at API 22+.

Device validation is still required: compilation alone does not prove that a particular Fire OS firmware obeys `FLAG_KEEP_SCREEN_ON`, or that its Android WebView can render the installed Material Skin version. The physical television may have an independent power-saving timer.

## Getting started

1. Install the debug APK from a successful [Android CI workflow](https://github.com/chrober/lms-material-now-playing-app/actions/workflows/ci.yml), or a signed APK from [Releases](https://github.com/chrober/lms-material-now-playing-app/releases) when available.
2. Launch the app. Choose **Find Lyrion servers**, or enter your server address, e.g. `192.168.1.20:9000` (default HTTP port 9000), `http://lyrion.local:9000`, or `https://my-server.example`.
3. Choose a discovered player. If needed, use **Refresh player list**.
4. Select **Keep screen awake**.
5. Choose **Save and show Now Playing**.

For example, a player with ID `aa:bb:cc:dd:ee:ff` is displayed using:

```text
http://SERVER:9000/material/np-only?player=aa%3Abb%3Acc%3Add%3Aee%3Aff
```

Optional ADB configuration:

```sh
adb shell am start -n com.chrober.lmsmaterial.nowplaying/.MainActivity \
  --es url 'http://192.168.1.20:9000/material/np-only?player=Living%20Room'
```

### Install on Fire TV from Windows

1. Enable ADB debugging on your Android-based Fire TV Stick.
2. Install Android SDK Platform Tools on Windows.
3. Extract the artifact ZIP and install the downloaded debug APK (`lms-material-now-playing-debug.apk`), or use the versioned APK from Releases:

```powershell
adb connect FIRE_TV_IP:5555
adb shell getprop ro.build.version.sdk
adb install -r .\lms-material-now-playing-debug.apk
```

The reported API level of the 2018 Fire TV Stick 4K should be **25**.

## Development

Prerequisites: JDK 17, Gradle 9.9.1, Android SDK platform 35/build tools 35.0.0, Gradle 9.9.1. No third-party Android runtime dependencies.

```sh
gradle :app:lintDebug
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

- `.github/workflows/ci.yml`: run lint, unit tests, and debug APK build for pull requests, pushes to `main`, and manual dispatch; upload the `lms-material-now-playing-debug` artifact containing `lms-material-now-playing-debug.apk`, plus a lint report.
- `.github/workflows/release.yml`: on a `v*` tag matching `versionName`, verify lint/tests, assemble a **signed** release APK, verify its signature, generate SHA-256 checksums and publish `lms-material-now-playing-vX.Y.Z.apk` to a GitHub Release.
- [Release instructions](docs/RELEASING.md): how to configure your keystore secrets and publish a tag.

The `applicationId` is `com.chrober.lmsmaterial.nowplaying`. The release workflow **requires signing secrets**; it intentionally fails rather than publishing an unsigned, uninstallable production APK.

### Architecture and limits

- Single native `Activity` with fullscreen `WebView`; settings and gear live in native Android UI.
- Lyrion server discovery: `eIPAD` broadcast request on UDP **3483**; needs a reachable LAN and permission to receive broadcast traffic.
- Lyrion player enumeration: `POST /jsonrpc.js` with `["", ["players","0","100"]]` (read-only).
- Material Skin renders its own Now Playing screen from `/material/np-only`.
- The app currently supports **unauthenticated** Lyrion JSON-RPC servers. Authenticated endpoints and player lists exceeding 100 entries are not yet handled.
- LAN broadcasts may fail through VPNs, client isolation, VLANs or some routers; manual address entry remains available.
- This is a **view-only UX**, not an authorization or network security boundary. It expects a trusted home network.
- HTTP cleartext is permitted because Lyrion installations commonly run on local port 9000. Use HTTPS if your server is configured accordingly.

## App artwork

The **Lyrion Now Playing** launcher icon uses a transparent background outside the monitor frame and stand. The Fire TV / Android TV banner remains unchanged. Both use the
approved waveform-and-display design. Its ten-bar waveform geometry comes from [Craig Drummond's MIT-licensed Material app](https://github.com/CDrummond/lms-material-app/blob/master/lms-material/src/main/res/drawable/ic_launcher_foreground.xml), recolored with our existing gradient and paired with
a visual Now Playing progress indicator; the app itself remains view-only.

- Android 5.1–7.1 and newer legacy launchers: density-specific `mipmap-*/ic_launcher.webp`
- Android 8.0+ adaptive launchers: `mipmap-anydpi-v26/ic_launcher.xml`
- TV banner: `drawable-nodpi/tv_banner.webp` (640 × 360, 16:9)
- Artwork sources and regeneration instructions: [Branding](docs/BRANDING.md)

## License and acknowledgements

MIT License — see [LICENSE](LICENSE).

The discovery protocol follows [Craig Drummond's lms-material-app](https://github.com/CDrummond/lms-material-app) implementation (MIT); attribution is preserved in the source.
