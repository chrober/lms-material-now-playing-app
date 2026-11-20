# Artwork, attribution and branding status

> **Branding permission granted:** Michael Herger [approved the use of the Lyrion name and artwork as shown](https://github.com/chrober/lyrion-display/issues/15#issuecomment-6979289683) **as long as this project remains free and open source**. This is an independent, unofficial companion app and does not imply endorsement by Lyrion. The [Lyrion terms](https://lyrion.org/terms/) continue to apply beyond the scope of that permission.

The application's icon and banner combine a **Now Playing display** motif (screen, play symbol, and progress indicator) with a ten-bar waveform adapted from [Craig Drummond's Material Android app](https://github.com/CDrummond/lms-material-app/blob/master/lms-material/src/main/res/drawable/ic_launcher_foreground.xml). The waveform arrangement was retained and recolored with the app's cyan-to-yellow gradient; the display composition is app-specific.

The upstream vector is retained in [`artwork/upstream-material-icon-foreground.xml`](../artwork/upstream-material-icon-foreground.xml) for traceability. Its source is published under the [MIT License](https://github.com/CDrummond/lms-material-app/blob/master/LICENSE). **That software/artwork licensing attribution is not a trademark or logo permission from the Lyrion project.** No endorsement by Craig Drummond, Material Skin, or the Lyrion project is implied.

## Source artwork

- `artwork/lyrion-now-playing-icon-source.webp` — 512 × 512, with a transparent region outside the monitor frame and stand.
- `artwork/lyrion-now-playing-banner-source.webp` — 640 × 360, landscape TV banner.

The source images are committed so builds and resource regeneration do not depend on an image-generation service.

## Android resources

| Resource | Dimensions | Use |
| --- | ---: | --- |
| `mipmap-mdpi/ic_launcher.png` | 48 × 48 | Legacy launcher icon |
| `mipmap-hdpi/ic_launcher.png` | 72 × 72 | Legacy launcher icon |
| `mipmap-xhdpi/ic_launcher.png` | 96 × 96 | Legacy launcher icon |
| `mipmap-xxhdpi/ic_launcher.png` | 144 × 144 | Legacy launcher icon |
| `mipmap-xxxhdpi/ic_launcher.png` | 192 × 192 | Legacy launcher icon |
| `drawable-nodpi/ic_launcher_foreground.webp` | 432 × 432 | Adaptive launcher foreground |
| `drawable-nodpi/tv_banner.webp` | 640 × 360 | Optional Android TV / Fire TV banner |

The adaptive launcher icon (Android API 26+) uses a transparent background and foreground. **Fire OS 6 / Android API 25** uses the legacy mipmap icon. The five classic launcher resources now use **PNG** rather than WebP for an experimental compatibility comparison with Kodi's legacy Android launcher images. This is **not** a proven fix for Amazon's Fire TV launcher caching issues. The source artwork remains WebP; CI validates the dimensions and contents of both source resources and built APK. Although the APK defines a separate `android:banner`, the Fire TV used during development has so far displayed the **square app icon** rather than the banner when installed by sideloading. A packaged banner does not guarantee that a particular TV launcher will show it.

To regenerate optimized resources after editing the source images:

```sh
python -m pip install Pillow
python tools/render_launcher_assets.py
```

Inspect the resulting launcher icons at **48 px**, as well as on the intended TV device, before publishing an updated APK. Public distributions using this branding must remain within the [free-and-open-source permission granted by Michael Herger](https://github.com/chrober/lyrion-display/issues/15#issuecomment-6979289683).
