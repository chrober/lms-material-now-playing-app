# F-Droid packaging and signing plan (NOT SUBMITTED)

The canonical app package name is `com.chrober.lmsmaterial.nowplaying`, despite the public name **Lyrion Display**.

## Recipe status

`fdroid/metadata/com.chrober.lmsmaterial.nowplaying.yml` is a **disabled review-only build** pointing at QA merge commit
`e3c5ba66683158b287d9bad18f27cb44f2266ae1`. That commit still declares
`0.2.0` / versionCode 8, but **it is NOT the published `v0.2.0` release source**.
Do **not** submit this entry or enable the build.

We deliberately do not rewrite the published tag or reuse versionCode 8 for a new release.

Before the first submission:
1. Decide and create a new version (e.g. `v0.2.1`, versionCode **9** or higher) **only after QA**.
2. Make a signed GitHub release **from that exact tag**, then update this draft recipe to the tag.
3. Remove `disable:` only after successful `fdroid build` and signature reproducibility checks.
4. Submit the resulting metadata by a **manual**, explicitly approved GitLab merge request.
   Never submit or publish automatically because a GitHub release/tag was created.

## Build expectations

- `gradle/wrapper/gradle-wrapper.properties` pins Gradle **9.9.1** for F-Droid's
  `gradlew-fdroid`. We do not bundle any wrapper JAR or fetched Gradle binary.
- Android Gradle Plugin **9.5.1**, JDK **17**, Android SDK platform 35.
- The release APK must be unsigned when no `ANDROID_KEYSTORE_PATH` is set;
  GitHub Actions signs its published APK with the existing private production key.
- The disabled recipe uses `gradle: [yes]` and `subdir: app`.
- During QA, `fdroid build --test --force com.chrober.lmsmaterial.nowplaying`
  can exercise the disabled recipe; do **not** use `--force` for publication.

## Reproducible upstream signing (preferred, not yet verified)

To preserve in-place updates from the GitHub-signed APK, the official F-Droid metadata
can ultimately specify:

```yaml
# DO NOT activate until an independent F-Droid build was proven byte-for-byte
# equivalent to the unsigned contents of the exact GitHub release APK.
Binaries: https://github.com/chrober/lyrion-display/releases/download/v%v/lyrion-display-v%v.apk
AllowedAPKSigningKeys:
  - af624ba3a730a5bf4a9d0b77e2bed2ec557744705362d4a4445e6dcb125f1dfb
```

The signing-certificate digest above was observed in the production-signed QA
APK and must be independently checked against the **exact first published
F-Droid release candidate** before enabling it.

F-Droid checks reproducibility by copying the upstream signature onto
its independently built unsigned APK and verifying the result. Use
`apksigcopier compare --unsigned signed.apk unsigned.apk` for a preliminary
comparison. This works only when source, build tools and output bytes agree
(except for signing). Passing an independent GitHub build does **not** prove
that F-Droid's own build environment matches.

Without reproducible upstream signing, standard F-Droid signing uses a different
certificate. **Switching between the GitHub and F-Droid APKs would then require
uninstalling the app and losing its saved configuration.**

## Testing

This repository provides a read-only preflight workflow to validate the recipe
and its pinned source and run `fdroid readmeta` and `fdroid lint`.
The build-server test is a separate, resource-intensive manual verification.
Full instructions:
- https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/
- https://f-droid.org/docs/Build_Metadata_Reference/
- https://f-droid.org/en/docs/Reproducible_Builds/

## Source/licensing and branding

MIT licensed. The bundled colored waveform is adapted from the upstream
Material Android app and attributed under MIT, and Lyrion logo/name permission
was granted for noncommercial open-source distribution as documented in
[BRANDING.md](BRANDING.md). Recheck external artwork permissions and storefront
screenshots before requesting inclusion.
