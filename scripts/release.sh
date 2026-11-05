#!/usr/bin/env bash
# Builds the signed release APK and publishes it as the newest GitHub release.
#
# The app's update banner and the download button on ruuun.app both read the
# newest full release through GitHub's /releases/latest/download/ links, so every
# release must carry exactly these two files: ruuun.apk and version.json.
#
# Usage: bump versionCode and versionName in app/build.gradle.kts, commit, push,
# write the release notes to a file, then run:  scripts/release.sh NOTES_FILE
set -euo pipefail
cd "$(dirname "$0")/.."

REPO=to-ie/ruuun
# The release key's certificate. Updates only install over the app when this matches.
EXPECTED_CERT=6aeffedb9822bb038f3ec2c7fb01bd2c89e1c4c285bfbf8173f168cc44979bbb

notes=${1:?Usage: scripts/release.sh NOTES_FILE}
[[ -f $notes ]] || { echo "No notes file at $notes" >&2; exit 1; }

code=$(grep -oP 'versionCode = \K[0-9]+' app/build.gradle.kts)
name=$(grep -oP 'versionName = "\K[^"]+' app/build.gradle.kts)
tag="v$name"
sdk=$(grep -oP '^sdk.dir=\K.*' local.properties)

[[ -z $(git status --porcelain) ]] || { echo "Commit or stash your changes first." >&2; exit 1; }
git fetch -q origin
[[ $(git rev-parse HEAD) == $(git rev-parse origin/main) ]] \
  || { echo "Push main first: the release is tagged from origin/main." >&2; exit 1; }
if gh release view "$tag" -R "$REPO" >/dev/null 2>&1; then
  echo "$tag already exists. Bump versionName." >&2; exit 1
fi
published=$(curl -fsSL "https://github.com/$REPO/releases/latest/download/version.json" 2>/dev/null \
  | grep -oP '"versionCode":\s*\K[0-9]+' || echo 0)
(( code > published )) || { echo "versionCode $code must be above the published $published." >&2; exit 1; }

./gradlew -q :app:testDebugUnitTest :app:assembleRelease
apk=app/build/outputs/apk/release/app-release.apk
cert=$("$sdk/build-tools/34.0.0/apksigner" verify --print-certs "$apk" | grep -oP 'SHA-256 digest: \K[0-9a-f]+' | head -1)
[[ $cert == "$EXPECTED_CERT" ]] || { echo "APK is not signed with the ruuun release key ($cert)." >&2; exit 1; }

out=$(mktemp -d)
cp "$apk" "$out/ruuun.apk"
sha=$(sha256sum "$out/ruuun.apk" | cut -d' ' -f1)
cat > "$out/version.json" <<JSON
{"versionCode": $code, "versionName": "$name", "apkUrl": "https://github.com/$REPO/releases/download/$tag/ruuun.apk", "sha256": "$sha"}
JSON

gh release create "$tag" -R "$REPO" --target main --latest \
  --title "ruuun $name" --notes-file "$notes" \
  "$out/ruuun.apk" "$out/version.json"
echo "Released $tag (versionCode $code). Phones will see the update banner on their next launch."
