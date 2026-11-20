# Lyrion Display rename — release checklist

The new public name does **not** mean a new Android app/package. The old app must remain updateable in place.

## Rename changes prepared

- [x] Rename the launcher/native settings title to **Lyrion Display**.
- [x] Set Android version to `0.2.0`, `versionCode 8`.
- [x] Rename debug APK/artifact, signed release APK/checksum and GitHub Release title to `lyrion-display`.
- [x] Rename the Gradle root project, update documentation and temporarily remove the outdated settings screenshot from the README.
- [x] Keep the original Android app ID `com.chrober.lmsmaterial.nowplaying`, Java package, SharedPreferences name and release signing key.
- [x] Replace the five **classic** `mipmap-*/ic_launcher.webp` assets with pixel-preserving PNG copies and change the asset generation script to produce PNG; keep adaptive icon and TV banner unchanged.
- [x] Validate the icon dimensions and verify packaged PNG resources in both CI and signed-release workflows.

## Still required before publication

- [x] Preserve the existing 640 × 360 TV banner design and replace its visible **Lyrion Now Playing** headline with **Lyrion Display** in `artwork/lyrion-display-banner-source.webp`; regenerate `app/src/main/res/drawable-nodpi/tv_banner.webp`. TV launchers may show this even though the tested Fire TV launcher shows the square icon.
- [ ] Test `0.2.0` on the Fire TV: launch, server/player discovery, native settings, remote focus, gear, Material themes, display and keep-awake.
- [ ] Specifically verify the **Fire TV Launcher icon** after first installation, app update, reboot and extended use; compare it with the icon under Fire TV's installed-app settings. PNG is an experiment, not a confirmed workaround for Amazon's launcher cache.
- [ ] Verify a **signed release** updates the existing **signed release** in place with settings intact. A debug APK cannot normally update an installation signed with the release key.
- [x] Add the approved **Lyrion Display** settings image (with anonymized server URL and **Living Room** player) as `screenshots/settings.webp`, and restore the two-column README screenshot preview.
- [ ] After merging this PR, rename the GitHub repository to `chrober/lyrion-display` via GitHub Settings → General → Repository name. Verify documentation, releases and external links. GitHub normally redirects legacy repository URLs.
- [ ] Ensure green CI, matching release tag/version and existing signing secrets before publishing the signed `v0.2.0` release.

## Intentionally unchanged

Do **not** rename `com.chrober.lmsmaterial.nowplaying`, Java source packages, the SharedPreferences file `material_now_playing`, Material Skin URLs, player IDs, or the square launcher icon. These are either persistent app identity or unrelated functionality.
