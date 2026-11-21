# F-Droid readiness – preparation (not submitted)

This branch adds build and QA prerequisites only. It does not publish or submit anything to F-Droid.

## Automated gates

- Android Lint and host-side JUnit protocol tests on every pull request.
- Loopback HTTP integration tests for Lyrion JSON-RPC, Material Skin discovery, HTTP errors and fallback handling.
- Android instrumentation smoke tests on an API 29 emulator, with reports in GitHub Actions artifacts.
- An unsigned release APK build without developer credentials; checks for the correct app ID, version and launcher resources.
- GitHub releases continue to use the established production signing credentials.
- English and German text metadata under fastlane/metadata/android/, including changelog for versionCode 8.

## Not yet established

1. Real F-Droid buildserver/scanner compatibility. The [disabled draft recipe](../fdroid/metadata/com.chrober.lmsmaterial.nowplaying.yml) now pins the **exact published v0.3.0 commit**. See the [packaging plan](FDROID_PACKAGING.md). **It is still disabled:** exact-release local reproducibility passed on 21 Nov 2026, but the official F-Droid buildserver and metadata review remain outstanding. Validate it with fdroidserver and the official buildserver before submission; a Gradle build alone is not proof.
2. **Local reproducibility passed for an unpublished QA snapshot**, including byte-identical unsigned APKs and a successful `apksigcopier` comparison using the developer signing key and build-tools 34. See [packaging verification](FDROID_PACKAGING.md). The **published v0.3.0** release was independently reproduced on GitHub Ubuntu and `apksigcopier` passed; official F-Droid buildserver verification remains open. Do not promise F-Droid cross-channel upgrades until it passes.
3. Device compatibility matrix. Fire OS 6 needs manual remote-focus and launcher-cache regression tests; emulator API 29 does not establish Android TV/Fire TV compatibility.
4. Store screenshots and attribution. The README Settings illustration has been edited, so use genuine screenshots for the final F-Droid listing.
5. Security review of Material Skin and trusted HTTP LAN communication. The WebView restricts main-frame navigations to the configured Lyrion server's /material page, but trusted server subresources may use other URLs.
6. Tag/release consistency and toolchain constraints. F-Droid recipes must pin the exact commit for each source release.

## Future publishing workflow

Only after the checks above pass, add a separate manually triggered GitHub workflow_dispatch **submission** workflow. It should accept a release tag, default to a validation-only dry run, and require protected GitHub environment approval and a narrowly scoped GitLab token before creating a GitLab fdroiddata merge request. Do not trigger this from push/tag or automatically publish to F-Droid.

Official references:
- https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/
- https://f-droid.org/en/docs/Build_Metadata_Reference/
