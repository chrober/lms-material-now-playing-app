# F-Droid readiness – preparation only (NOT SUBMITTED)

The source repository contains a **disabled** review recipe for the exact published v0.3.0 release. No GitLab fdroiddata merge request, release publishing, or submission to F-Droid is performed by these checks.

## Automated gates

- Android Lint and host-side JUnit protocol tests on every pull request.
- Loopback HTTP integration tests for Lyrion JSON-RPC, Material Skin discovery, HTTP errors and fallback handling.
- Android instrumentation smoke tests on an API 29 emulator, with reports in GitHub Actions artifacts.
- An unsigned release APK build without developer credentials; checks for the correct app ID, version and launcher resources.
- GitHub releases continue to use the established production signing credentials.
- English and German text metadata under fastlane/metadata/android/, including current versionCode 9 changelogs.
- The F-Droid QA workflow validates the disabled recipe, exact release tag and metadata on relevant PRs.
- The F-Droid **Trixie CI container image** reproduced the exact already-published v0.3.0 APK successfully **with Java 17 explicitly mounted**. The checksum, certificate and `apksigcopier` comparison passed ([run 44897086974](https://github.com/chrober/lyrion-display/actions/runs/44897086974)). This is *not* official F-Droid production acceptance.

## Not yet established

1. Official F-Droid production buildserver/scanner compatibility. The [disabled draft recipe](../fdroid/metadata/com.chrober.lmsmaterial.nowplaying.yml) now pins the **exact published v0.3.0 commit**. See the [packaging plan](FDROID_PACKAGING.md). **It is still disabled:** exact-release local reproducibility passed on 21 Nov 2026, but the official F-Droid buildserver and metadata review remain outstanding. The standard Trixie image has Java 21, which changes `classes.dex` and fails reproducible signature transfer; a Java 17 override was needed for the passing CI-container test. Before submission, demonstrate that the actual official production build recipe/buildserver can supply Java 17 and reproduce the APK.
2. **Local reproducibility passed for an unpublished QA snapshot**, including byte-identical unsigned APKs and a successful `apksigcopier` comparison using the developer signing key and build-tools 34. See [packaging verification](FDROID_PACKAGING.md). The **published v0.3.0** release was independently reproduced on GitHub Ubuntu and `apksigcopier` passed. The buildserver **CI image** test also passed with injected Java 17, including the full signature-compatibility comparison; it is not evidence of acceptance by F-Droid's actual production buildserver. Do not promise F-Droid cross-channel upgrades until it passes.
3. Device compatibility matrix. Fire OS 6 needs manual remote-focus and launcher-cache regression tests; emulator API 29 does not establish Android TV/Fire TV compatibility.
4. Store screenshots and attribution. The README Settings illustration has been edited, so use genuine screenshots for the final F-Droid listing.
5. Security review of Material Skin and trusted HTTP LAN communication. The WebView restricts main-frame navigations to the configured Lyrion server's /material page, but trusted server subresources may use other URLs.
6. Tag/release consistency and toolchain constraints. F-Droid recipes must pin the exact commit for each source release.

## Verification workflow (safe, no publication)

`.github/workflows/fdroid-preflight.yml` runs metadata linting on PRs that affect the F-Droid files. The heavier buildserver-image test is also tied to these PRs, and checks the **published** v0.3.0 Git tag/source against the existing GitHub-signed APK. The passing run used Java 17 supplied from GitHub Actions rather than the Trixie image's default Java 21. It verifies repeatability in this controlled CI container, but **does not itself certify production F-Droid acceptance**.

The workflow also provides two manually triggered, opt-in test jobs under **Actions → F-Droid metadata and source-build preflight (no publishing) → Run workflow**:

- `source_build=true`: pinned-source `fdroidserver 2.4.5` rebuild on a GitHub Ubuntu runner, followed by signing-compatibility comparison.
- `buildserver_image=true`: rebuild in F-Droid's `buildserver-trixie` CI container with a read-only mounted Java 17 runtime, with the same comparison.

Both paths require a **disabled** recipe, use no signing secrets, download but never upload the existing released APK, and upload logs/checksums only. The build inputs are read from the exact pinned recipe; no obsolete QA snapshot is built. No F-Droid publishing credentials or GitLab APIs are involved.

## Future publishing workflow

Only after the checks above pass, add a separate manually triggered GitHub workflow_dispatch **submission** workflow. It should accept a release tag, default to a validation-only dry run, and require protected GitHub environment approval and a narrowly scoped GitLab token before creating a GitLab fdroiddata merge request. Do not trigger this from push/tag or automatically publish to F-Droid.

Official references:
- https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/
- https://f-droid.org/en/docs/Build_Metadata_Reference/
