# F-Droid readiness – not yet submitted

This branch adds build and QA prerequisites only. It does not publish or submit anything to F-Droid.

## Automated gates

- Android Lint and host-side JUnit protocol tests on every pull request.
- Loopback HTTP integration tests for Lyrion JSON-RPC, Material Skin discovery, HTTP errors and fallback handling.
- Android instrumentation smoke tests on an API 29 emulator, with reports in GitHub Actions artifacts.
- An unsigned release APK build without developer credentials; checks for the correct app ID, version and launcher resources.
- GitHub releases continue to use the established production signing credentials.
- English and German text metadata under fastlane/metadata/android/, including changelog for versionCode 8.

## Not yet established

1. Real F-Droid buildserver/scanner compatibility. A Gradle source build on GitHub is useful but is NOT proof that fdroidserver can build the app. Prepare a draft fdroiddata recipe and validate with fdroid lint and fdroid build on the official buildserver image before submission.
2. Reproducible upstream-signature verification. By default, F-Droid signs APKs with a different key than GitHub, and users cannot update across those certificates. Investigate verified reproducible upstream-signed APKs before promising continuity.
3. Device compatibility matrix. Fire OS 6 needs manual remote-focus and launcher-cache regression tests; emulator API 29 does not establish Android TV/Fire TV compatibility.
4. Store screenshots and attribution. The README Settings illustration has been edited, so use genuine screenshots for the final F-Droid listing.
5. Security review of Material Skin and trusted HTTP LAN communication. The WebView restricts main-frame navigations to the configured Lyrion server's /material page, but trusted server subresources may use other URLs.
6. Tag/release consistency and toolchain constraints. F-Droid recipes must pin the exact commit for each source release.

## Future publishing workflow

Only after the checks above pass, add a manually triggered GitHub workflow_dispatch workflow. It should accept a release tag, default to a validation-only dry run, and require protected GitHub environment approval and a narrowly scoped GitLab token before creating a GitLab fdroiddata merge request. Do not trigger this from push/tag or automatically publish to F-Droid.

Official references:
- https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/
- https://f-droid.org/en/docs/Build_Metadata_Reference/
