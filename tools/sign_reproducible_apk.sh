#!/usr/bin/env bash
# Sign an *unsigned* release APK with the established developer certificate,
# using Android build-tools 34 to support F-Droid's apksigcopier verification.
# Usage: bash tools/sign_reproducible_apk.sh UNSIGNED_APK SIGNED_APK KEYSTORE
# Required environment: ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS,
# ANDROID_KEY_PASSWORD, ANDROID_HOME.
set -euo pipefail

if [[ "$#" -ne 3 ]]; then
  echo "Usage: $0 UNSIGNED_APK SIGNED_APK KEYSTORE" >&2
  exit 2
fi

unsigned_apk="$1"
signed_apk="$2"
keystore="$3"
: "${ANDROID_HOME:?Android SDK location required}"
: "${ANDROID_KEYSTORE_PASSWORD:?Keystore password required}"
: "${ANDROID_KEY_ALIAS:?Signing key alias required}"
: "${ANDROID_KEY_PASSWORD:?Private key password required}"

signer="$ANDROID_HOME/build-tools/34.0.0/apksigner"
test -x "$signer"
test -s "$unsigned_apk"
test -s "$keystore"
if [[ "$unsigned_apk" == "$signed_apk" ]]; then
  echo "Signed and unsigned output must be different files." >&2
  exit 1
fi

# Keep this pin consistent with the first public GitHub-signed v0.2.0.
expected_cert="af624ba3a730a5bf4a9d0b77e2bed2ec557744705362d4a4445e6dcb125f1dfb"
"$signer" sign \
  --ks "$keystore" \
  --ks-key-alias "$ANDROID_KEY_ALIAS" \
  --ks-pass env:ANDROID_KEYSTORE_PASSWORD \
  --key-pass env:ANDROID_KEY_PASSWORD \
  --out "$signed_apk" "$unsigned_apk"

cert_report="$(mktemp)"
trap 'rm -f "$cert_report"' EXIT
"$signer" verify --verbose --print-certs "$signed_apk" | tee "$cert_report"
grep -Fq "Signer #1 certificate SHA-256 digest: $expected_cert" "$cert_report" || {
  echo "Signing certificate differs from existing published release. Aborting." >&2
  exit 1
}

# A local signature-copy test is a prerequisite for F-Droid reproducible builds.
# This does NOT prove byte identity against an independent F-Droid build.
export PATH="$ANDROID_HOME/build-tools/34.0.0:$PATH"
command -v apksigcopier >/dev/null
apksigcopier compare --unsigned "$signed_apk" "$unsigned_apk"
echo "Signed APK matches its unsigned input under apksigcopier."
