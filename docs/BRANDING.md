# Lyrion Now Playing — visual identity

The approved artwork combines the **exact ten-bar waveform height pattern and
bar positions** from [CDrummond's lms-material-app](https://github.com/CDrummond/lms-material-app/blob/master/lms-material/src/main/res/drawable/ic_launcher_foreground.xml)
with our original **Now Playing display** motif (screen, play symbol and progress
line). The source vector is retained verbatim in
[`artwork/upstream-material-icon-foreground.xml`](../artwork/upstream-material-icon-foreground.xml)
for traceability. The waveform is scaled and recolored to retain our existing
cyan-to-yellow gradient; its ten-bar silhouette replaces the previous unrelated
seven-bar approximation. All surrounding icon and banner elements are unchanged.

Upstream waveform/vector © its contributors, MIT-licensed under
[CDrummond/lms-material-app/LICENSE](https://github.com/CDrummond/lms-material-app/blob/master/LICENSE).
The app remains an **unofficial companion**, not an official Lyrion release.

## Source images

- `artwork/lyrion-now-playing-icon-source.webp`: 512 × 512,
  rounded icon tile with transparency at the outer corners.
- `artwork/lyrion-now-playing-banner-source.webp`: 640 × 360,
  landscape Lyrion Now Playing banner.

The sources were cropped and optimized from the approved generated artwork.
They are checked in so future APK builds are independent of image-generation
services. No runtime image-generation or Pillow installation is required.

## Android resources

The generated WebP assets are committed to the repository.

| Resource | Dimensions | Purpose |
| --- | ---: | --- |
| `mipmap-mdpi/ic_launcher.webp` | 48 × 48 | Legacy launcher |
| `mipmap-hdpi/ic_launcher.webp` | 72 × 72 | Legacy launcher |
| `mipmap-xhdpi/ic_launcher.webp` | 96 × 96 | Legacy launcher |
| `mipmap-xxhdpi/ic_launcher.webp` | 144 × 144 | Legacy launcher |
| `mipmap-xxxhdpi/ic_launcher.webp` | 192 × 192 | Legacy launcher |
| `drawable-nodpi/ic_launcher_foreground.webp` | 432 × 432 | Adaptive foreground; padded for safe zone |
| `drawable-nodpi/tv_banner.webp` | 640 × 360 | Android TV / Fire TV banner |

The adaptive launcher icon (Android API 26+) uses an XML declaration with a
dark blue background and transparent foreground. **Fire OS 6 / API 25** uses
the legacy mipmap assets; its TV launcher displays the separate banner.

To regenerate the optimized resources after editing either source:

```sh
python -m pip install Pillow
python tools/render_launcher_assets.py
```

Check the resulting images at 48 px and on an actual TV launcher.
The app remains unofficial; no affiliation with or endorsement by the
Lyrion project is implied.
