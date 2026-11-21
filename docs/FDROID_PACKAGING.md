# F-Droid packaging and signing plan (NOT SUBMITTED)

The canonical app package name is `com.chrober.lmsmaterial.nowplaying`, despite the public name **Lyrion Display**.

## Recipe status: v0.3.0 pinned, still disabled

The disabled review recipe `fdroid/metadata/com.chrober.lmsmaterial.nowplaying.yml`
now points to the **exact published GitHub `v0.3.0` source commit**:
`fb4cb6359e1931519f586d6602c25e9b95e51e03`, with versionName
`0.3.0` and versionCode `9`. It deliberately remains **disabled**;
**no F-Droid submission or external publication** has been made.

The earlier pinned QA snapshot (`35b1749…`, versionCode 8) was used
only for the local F-Droid source-build and reproducibility experiments
documented below. It is **not** the actual v0.2.0 release.

Before official F-Droid inclusion:
1. **Completed locally:** exact v0.3.0 source build, published APK SHA-256,
   certificate and `apksigcopier compare --unsigned` passed (see evidence below).
2. Confirm compatibility with F-Droid's **official buildserver** environment;
   a passing GitHub Ubuntu source build is helpful but not equivalent.
3. Complete the storefront screenshots, attribution and policy review.
4. Only after verification, remove `disable:` and add the proper
   upstream-signed APK metadata; submit through an explicitly approved,
   **manual GitLab merge request**, not automatically when GitHub tags change.

## Published v0.3.0 — local independent reproducibility PASSED (21 Nov 2026)

The **actual published GitHub APK** was reproduced in
[GitHub Actions run 44882528618](https://github.com/chrober/lyrion-display/actions/runs/44882528618),
using an independent `fdroidserver 2.4.5` source scan and build.

- Source tag `v0.3.0` and build recipe both resolve to
  `fb4cb6359e1931519f586d6602c25e9b95e51e03`.
- F-Droid source scanner and `fdroid build --test --force`: **1 build succeeded**.
- F-Droid unsigned APK SHA-256:
  `4de2f0deaa2ad9edc7cff597ba60af599122a5183bc348b3107a4c6c902e5e88`.
- The official GitHub `lyrion-display-v0.3.0.apk` passed its published
  `.sha256` check and has developer certificate SHA-256
  `af624ba3a730a5bf4a9d0b77e2bed2ec557744705362d4a4445e6dcb125f1dfb`.
- **`apksigcopier compare --unsigned published.apk fdroid-built.apk` PASSED**,
  confirming the published upstream signature is compatible with the
  independent source-build output.

This proves **local exact-release reproducibility** on GitHub-hosted Ubuntu.
It does **not** prove the official F-Droid buildserver image will produce the
same binary or that F-Droid will accept the application. The recipe
**remains disabled** pending those checks and storefront review.

## Build expectations

- `gradle/wrapper/gradle-wrapper.properties` pins Gradle **9.9.1** for F-Droid's
  `gradlew-fdroid`. We do not bundle any wrapper JAR or fetched Gradle binary.
- Android Gradle Plugin **9.5.1**, JDK **17**, Android SDK platform 35.
- The release APK must be unsigned when no `ANDROID_KEYSTORE_PATH` is set;
  GitHub Actions signs its published APK with the existing private production key.
- The disabled recipe uses `gradle: [yes]` and `subdir: app`.
- During QA, `fdroid build --test --force com.chrober.lmsmaterial.nowplaying`
  can exercise the disabled recipe; do **not** use `--force` for publication.

## Verified local source-build preflight (20 Nov 2026)

We ran the **actual fdroidserver 2.4.5** metadata reader, linter,
source scanner and `fdroid build --test --force` on GitHub-hosted Ubuntu.
The disabled recipe at source commit `35b1749c9d4036ec765aaee51b7dfa2de43ed8a1`
succeeded: `1 build succeeded`, producing
`tmp/com.chrober.lmsmaterial.nowplaying_8.apk` (unsigned).
[CI evidence](https://github.com/chrober/lyrion-display/actions/runs/44760666259).

The pip-installed fdroidserver package does not contain its own
`gradlew-fdroid`, so for this **local** preflight we explicitly configured
Gradle 9.9.1 from GitHub Actions. The official F-Droid buildserver image
has its own Gradle tooling and still needs independent testing.

An archive entry comparison between the older signed PR #23 QA APK
(source revision `12b5f81`) and this F-Droid-built APK showed that
all common APK entries have matching uncompressed contents **except**
`META-INF/version-control-info.textproto`, which contains the expected
different Git revision. The signed APK also contains three v1-signature
entries missing from the unsigned APK. **This does not prove reproducibility**:
the two APKs have different source revisions, and a proper same-revision,
signature-aware comparison is required.

## Local QA reproducibility verified (20 Nov 2026)

A [controlled GitHub Actions test](https://github.com/chrober/lyrion-display/actions/runs/44764689613)
built the **same exact QA source commit**
`35b1749c9d4036ec765aaee51b7dfa2de43ed8a1` twice:

1. `fdroidserver 2.4.5` ran source scanning and its Gradle build with
   an ordinary git checkout.
2. A separate ordinary git clone ran Gradle 9.9.1 against the identical
   commit, with no signing credentials.

**The unsigned APKs matched byte-for-byte**, SHA-256:

`824b6eae13cc1cbd1c9a82d0feb35f15c8ab46817908bf3a23518a49738898ee`

The second APK was signed using **the existing production keystore** and
Android SDK `apksigner` from build-tools **34.0.0**. The command
`apksigcopier compare --unsigned signed.apk fdroid-unsigned.apk` **passed**.
This shows that local, source-built APKs can preserve the original
developer-signing certificate without changing the app ID.

**Important limitations:**

- This is a QA snapshot with `versionCode 8`, *not* the published
  `v0.2.0` tag. It must never be submitted as that release.
- A GitHub-hosted Ubuntu preflight is not the official F-Droid buildserver
  image. Real F-Droid environment and the exact next tagged release must
  be tested again.
- Signing with Android SDK build-tools **35.0.0** failed
  `apksigcopier` verification due to its APK Signing Block layout, as
  [documented by F-Droid](https://f-droid.org/en/docs/Reproducible_Builds/).
  For a future developer-signed F-Droid release, adapt the GitHub **release
  workflow** to sign with build-tools **34.0.0** and verify with
  `apksigcopier`. Do not modify or republish `v0.2.0`.
- Android Gradle Plugin embeds Git revision information in
  `META-INF/version-control-info.textproto`. Use a **normal git clone**,
  not `git worktree`, for independent builds. Our worktree experiment
  produced `NO_VALID_GIT_FOUND` and was not reproducible.
- None of these experiments uploaded, released or submitted an APK
  to F-Droid. The only packaged candidate in this branch remains disabled.

## Reproducible upstream signing (locally verified; official release pending)

To preserve in-place updates from the GitHub-signed APK, the official F-Droid metadata
can ultimately specify, **only once the new tagged release is verified**:

```yaml
# DO NOT activate until an independent F-Droid build was proven byte-for-byte
# equivalent to the unsigned contents of the exact GitHub release APK.
Binaries: https://github.com/chrober/lyrion-display/releases/download/v%v/lyrion-display-v%v.apk
AllowedAPKSigningKeys:
  - af624ba3a730a5bf4a9d0b77e2bed2ec557744705362d4a4445e6dcb125f1dfb
```

The certificate digest above comes from the established production key and must
be checked against the **exact first published F-Droid release candidate** before enabling it.

F-Droid checks reproducibility by copying the upstream signature onto
its independently built unsigned APK and verifying the result. Use
`apksigcopier compare --unsigned signed.apk unsigned.apk` for a preliminary
comparison. This works only when source, build tools and output bytes agree
(except for signing). Passing an independent GitHub build does **not** prove
that F-Droid's own build environment matches.

Without reproducible upstream signing, standard F-Droid signing uses a different
certificate. **Switching between the GitHub and F-Droid APKs would then require
uninstalling the app and losing its saved configuration.**

## Repeatable QA verification without submission

The permanently **disabled** recipe pins the exact existing GitHub v0.3.0 release (versionCode 9), not the earlier versionCode 8 QA snapshot. The validation tool `tools/check_fdroid_recipe.py` checks the tag/source identity and refuses an enabled recipe.

The workflow [F-Droid metadata and source-build preflight](../.github/workflows/fdroid-preflight.yml) provides these paths:

1. **Automatic on F-Droid-related PRs:** metadata parser/linter and an isolated test inside the F-Droid `registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie` CI container. It reads source SHA, version and versionCode from the disabled recipe and compares the resulting unsigned APK against the existing, checksummed, developer-signed GitHub release using `apksigcopier`.
2. **Manual `source_build` input:** source scanner and `fdroid build --test --force` with pinned `fdroidserver 2.4.5` on GitHub-hosted Ubuntu, reproducing the previously successful test without hardcoded old QA commits.
3. **Manual `buildserver_image` input:** explicitly rerun the isolated F-Droid CI container test without a PR.

GitHub Actions → **F-Droid metadata and source-build preflight (no publishing)** → **Run workflow**. Select `main` and enable only the intended test option(s). The local Ubuntu and CI-container jobs are independent: either can be selected without the other. A test success must be confirmed in its linked workflow run rather than inferred from the existence of the job.

These QA jobs have `contents: read` only, no signing secrets or GitLab token, do not change tags, do not activate the recipe, and **never submit or publish APKs**. Logs and checksum/certificate evidence are the only uploaded artifacts. The pre-existing signed GitHub APK is downloaded read-only solely for comparison.

**Scope limit:** F-Droid's buildserver **CI image** uses F-Droid's provisioning but is not proof that an actual official production build or metadata review has succeeded. That verification and a suitable final listing (genuine rights-cleared screenshots, attribution and policy review) are still required before any user-approved submission.

References:
- https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/
- https://f-droid.org/en/docs/Build_Metadata_Reference/
- https://f-droid.org/en/docs/Reproducible_Builds/

## Source/licensing and branding

MIT licensed. The bundled colored waveform is adapted from the upstream
Material Android app and attributed under MIT, and Lyrion logo/name permission
was granted for free-and-open-source distribution as documented in
[BRANDING.md](BRANDING.md). Recheck external artwork permissions and storefront
screenshots before requesting inclusion.
