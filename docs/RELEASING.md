# Publishing signed Android releases

A release is published **only** when a tag beginning with `v` is pushed (for example `v0.1.0`). The tag must match the version declared as `versionName` in `app/build.gradle`.

## 1. Create and safeguard your release keystore

An Android signing key is needed for users to install future updates over an existing release APK. **Back it up securely**. Losing the key prevents normal updates with the same application ID.

Example with the JDK's `keytool`:

```sh
keytool -genkeypair -v -keystore now-playing-release.jks \
  -alias now-playing -keyalg RSA -keysize 3072 -validity 10000
```

Never commit your `.jks` file or passwords. The project's `.gitignore` excludes keystore files.

## 2. Configure GitHub Actions secrets

Open repository **Settings → Secrets and variables → Actions** and create these four repository secrets:

| Secret | Meaning |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 encoding of the complete keystore file |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Alias chosen when creating the keystore |
| `ANDROID_KEY_PASSWORD` | Private key password |

Encode the file on Windows using PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("now-playing-release.jks"))
```

Copy the entire output into the `ANDROID_KEYSTORE_BASE64` secret, without extra whitespace. On Linux, use `base64 -w0 now-playing-release.jks`.

Secrets are made available only to the release job; pull request CI does not use them.

## 3. Cut a release

1. Confirm Android CI on `main` is green.
2. Update both `versionCode` (integer, must increase for updates) and `versionName` (e.g. `0.1.0`) in `app/build.gradle`.
3. Merge any necessary updates to `main`.
4. Tag the desired commit: `git tag v0.1.0` and `git push origin v0.1.0`.
5. Watch the **Publish Android release** workflow. It checks the tag/version match, lints/tests, builds and verifies the signed APK, and uploads it as `lms-material-now-playing-vX.Y.Z.apk` together with a matching `.sha256` file to a GitHub Release.

**Do not tag a release until the four signing secrets have been configured.** If a job fails, inspect the workflow log; the publish step runs only after successful verification.

## Debug builds

Every successful pull request or `main` CI run publishes an installable **debug APK** named `lms-material-now-playing-debug.apk` inside the `lms-material-now-playing-debug` workflow artifact. Debug builds use Android's debug signing certificate and cannot be updated in-place with the production release APK (uninstall first).
