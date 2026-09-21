#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CODE_ROOT="$(cd "$REPO_ROOT/.." && pwd)"
TRACK="internal"
RELEASE_STATUS="completed"
SKIP_UPLOAD=false
SERVICE_ACCOUNT_JSON="${SERVICE_ACCOUNT_JSON:-}"
PACKAGE_NAME="${ANDROID_PACKAGE_NAME:-com.markduenas.android.apigen}"

SHARED_ENV_FILE="$CODE_ROOT/.deploy-config/deploy.env"
if [[ -f "$SHARED_ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$SHARED_ENV_FILE"
fi

usage() {
  cat <<'EOF'
Usage:
  ./scripts/deploy-android.sh [options]

Options:
  --track <internal|alpha|beta|production>   Play track (default: internal)
  --release-status <draft|completed>          Release status (default: completed)
  --skip-upload                               Build only, do not upload
  --service-account <path>                    Google Play JSON key path
  --package-name <id>                         Android application ID
  -h, --help                                  Show help

Build types (AdMob):
  internal | alpha | beta  →  :composeApp:bundleInternal  (Google test ad units)
  production               →  :composeApp:bundleRelease   (production ad units)

Environment:
  PLAY_SERVICE_ACCOUNT
  GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH
  ANDROID_KEYSTORE_BASE64
  KEYSTORE_PATH
  ANDROID_KEYSTORE_PASSWORD
  ANDROID_KEY_ALIAS
  ANDROID_KEY_PASSWORD
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --track)
      TRACK="$2"
      shift 2
      ;;
    --release-status)
      RELEASE_STATUS="$2"
      shift 2
      ;;
    --skip-upload)
      SKIP_UPLOAD=true
      shift
      ;;
    --service-account)
      SERVICE_ACCOUNT_JSON="$2"
      shift 2
      ;;
    --package-name)
      PACKAGE_NAME="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

case "$TRACK" in
  internal|alpha|beta|production) ;;
  *)
    echo "Invalid --track: $TRACK (expected internal|alpha|beta|production)" >&2
    exit 1
    ;;
esac

if [[ "$TRACK" == "production" ]]; then
  GRADLE_BUNDLE_TASK=":composeApp:bundleRelease"
  BUNDLE_OUTPUT_DIR="composeApp/build/outputs/bundle/release"
  ADS_MODE="production (real ad units)"
else
  GRADLE_BUNDLE_TASK=":composeApp:bundleInternal"
  BUNDLE_OUTPUT_DIR="composeApp/build/outputs/bundle/internal"
  ADS_MODE="test (Google sample ad units)"
fi

if [[ -z "$SERVICE_ACCOUNT_JSON" ]]; then
  SERVICE_ACCOUNT_JSON="${PLAY_SERVICE_ACCOUNT:-${GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH:-$CODE_ROOT/play-store-key.json}}"
fi

if [[ -n "${ANDROID_KEYSTORE_BASE64:-}" ]]; then
  TMP_KEYSTORE="${TMPDIR:-/tmp}/pigen-release-key.jks"
  echo "$ANDROID_KEYSTORE_BASE64" | base64 --decode > "$TMP_KEYSTORE"
  KEYSTORE_PATH="$TMP_KEYSTORE"
fi

# PiGenerator2 Play signing cert lives in code/keystore/keystore (not upload_keystore.jks).
# Passwords/alias from rockskipper keystore.properties (same alias/password family).
if [[ -z "${KEYSTORE_PATH:-}" || -z "${ANDROID_KEYSTORE_PASSWORD:-}" ]]; then
  KS_PROPS=""
  for candidate in "$REPO_ROOT/keystore.properties" "$CODE_ROOT/rockskipper/keystore.properties"; do
    [[ -f "$candidate" ]] && KS_PROPS="$candidate" && break
  done
  if [[ -n "$KS_PROPS" ]]; then
    while IFS='=' read -r key value; do
      [[ -z "$key" || "$key" =~ ^[[:space:]]*# ]] && continue
      key="$(echo "$key" | tr -d '[:space:]')"
      value="$(echo "$value" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
      case "$key" in
        storePassword) ANDROID_KEYSTORE_PASSWORD="${ANDROID_KEYSTORE_PASSWORD:-$value}" ;;
        keyAlias) ANDROID_KEY_ALIAS="${ANDROID_KEY_ALIAS:-$value}" ;;
        keyPassword) ANDROID_KEY_PASSWORD="${ANDROID_KEY_PASSWORD:-$value}" ;;
      esac
    done < "$KS_PROPS"
  fi
  KEYSTORE_PATH="${KEYSTORE_PATH:-$CODE_ROOT/keystore/keystore}"
fi

pushd "$REPO_ROOT" >/dev/null

if [[ ! -x "./gradlew" ]]; then
  chmod +x ./gradlew
fi

GRADLE_ARGS=(./gradlew "$GRADLE_BUNDLE_TASK" --no-daemon)

if [[ -n "${KEYSTORE_PATH:-}" && -n "${ANDROID_KEYSTORE_PASSWORD:-}" && -n "${ANDROID_KEY_ALIAS:-}" && -n "${ANDROID_KEY_PASSWORD:-}" ]]; then
  GRADLE_ARGS+=(
    "-Pandroid.injected.signing.store.file=${KEYSTORE_PATH}"
    "-Pandroid.injected.signing.store.password=${ANDROID_KEYSTORE_PASSWORD}"
    "-Pandroid.injected.signing.key.alias=${ANDROID_KEY_ALIAS}"
    "-Pandroid.injected.signing.key.password=${ANDROID_KEY_PASSWORD}"
  )
else
  echo "WARNING: No signing config resolved. Play upload may reject unsigned AAB." >&2
fi

echo "Play track:     $TRACK"
echo "Gradle task:    $GRADLE_BUNDLE_TASK"
echo "AdMob mode:     $ADS_MODE"
echo "Building Android AAB..."
"${GRADLE_ARGS[@]}"

AAB_PATH="$(find "$BUNDLE_OUTPUT_DIR" -name '*.aab' 2>/dev/null | sort | tail -1)"
if [[ -z "$AAB_PATH" ]]; then
  echo "Could not find AAB under $BUNDLE_OUTPUT_DIR" >&2
  exit 1
fi

echo "Built AAB: $AAB_PATH"

if [[ "$SKIP_UPLOAD" == "true" ]]; then
  echo "Upload skipped (--skip-upload)."
  popd >/dev/null
  exit 0
fi

if [[ ! -f "$SERVICE_ACCOUNT_JSON" ]]; then
  echo "Service account JSON not found: $SERVICE_ACCOUNT_JSON" >&2
  popd >/dev/null
  exit 1
fi

if ! command -v fastlane >/dev/null 2>&1; then
  echo "fastlane is required. Install with: gem install fastlane" >&2
  popd >/dev/null
  exit 1
fi

echo "Uploading to Google Play track: $TRACK"
fastlane supply \
  --aab "$AAB_PATH" \
  --json_key "$SERVICE_ACCOUNT_JSON" \
  --package_name "$PACKAGE_NAME" \
  --track "$TRACK" \
  --release_status "$RELEASE_STATUS" \
  --skip_upload_apk true \
  --skip_upload_images true \
  --skip_upload_screenshots true \
  --skip_upload_metadata true \
  --skip_upload_changelogs true

popd >/dev/null

if [[ -n "${TMP_KEYSTORE:-}" && -f "${TMP_KEYSTORE:-}" ]]; then
  rm -f "$TMP_KEYSTORE"
fi

echo "Android deployment complete (track=$TRACK, ads=$ADS_MODE)."
# shellcheck source=/dev/null
source "$CODE_ROOT/.project-tracker/lib-deploy.sh"
log_deploy "$CODE_ROOT" "$(basename "$REPO_ROOT")" "android" "$TRACK" "$(detect_version_android "$REPO_ROOT")"
