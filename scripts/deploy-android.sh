#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CODE_ROOT="$(cd "$REPO_ROOT/.." && pwd)"
TRACK="internal"
RELEASE_STATUS="completed"
RELEASE_STATUS_SET=false
SKIP_UPLOAD=false
PREBUILT_AAB=""
EXPECTED_SHA256=""
MAPPING_PATH=""
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
  --release-status <draft|completed>          Release status (default: completed).
                                              Required (explicit) for --track production;
                                              production releases go up as draft.
  --skip-upload                               Build (or verify --aab) only, do not upload
  --aab <path>                                Upload this already-built, already-tested AAB.
                                              Skips Gradle. Requires --sha256 or a SHA256SUMS
                                              file next to the AAB; aborts on mismatch.
  --sha256 <hex>                              Expected SHA-256 of the --aab file
  --mapping <path>                            R8 mapping.txt to upload with the bundle.
                                              Default: <aab dir>/SHA256SUMS-listed *mapping.txt
                                              next to --aab, else the Gradle build output.
  --service-account <path>                    Google Play JSON key path
  --package-name <id>                         Android application ID
  -h, --help                                  Show help

Production (tested file, draft; Mark publishes in Play Console):
  ./scripts/deploy-android.sh --track production --release-status draft \
    --aab ~/development/releases/PiGenerator2/<version>/<file>.aab --sha256 <hex>

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
      RELEASE_STATUS_SET=true
      shift 2
      ;;
    --aab)
      PREBUILT_AAB="$2"
      shift 2
      ;;
    --sha256)
      EXPECTED_SHA256="$(echo "$2" | tr '[:upper:]' '[:lower:]')"
      shift 2
      ;;
    --mapping)
      MAPPING_PATH="$2"
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

case "$RELEASE_STATUS" in
  draft|completed) ;;
  *)
    echo "Invalid --release-status: $RELEASE_STATUS (expected draft|completed)" >&2
    exit 1
    ;;
esac

if [[ "$TRACK" == "production" && "$RELEASE_STATUS_SET" != "true" ]]; then
  echo "Refusing --track production without an explicit --release-status (use --release-status draft)." >&2
  exit 1
fi

if [[ "$TRACK" == "production" ]]; then
  GRADLE_BUNDLE_TASK=":composeApp:bundleRelease"
  BUNDLE_OUTPUT_DIR="composeApp/build/outputs/bundle/release"
  MAPPING_OUTPUT="composeApp/build/outputs/mapping/release/mapping.txt"
  ADS_MODE="production (real ad units)"
else
  GRADLE_BUNDLE_TASK=":composeApp:bundleInternal"
  BUNDLE_OUTPUT_DIR="composeApp/build/outputs/bundle/internal"
  MAPPING_OUTPUT="composeApp/build/outputs/mapping/internal/mapping.txt"
  ADS_MODE="test (Google sample ad units)"
fi

if [[ -z "$SERVICE_ACCOUNT_JSON" ]]; then
  SERVICE_ACCOUNT_JSON="${PLAY_SERVICE_ACCOUNT:-${GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH:-$CODE_ROOT/play-store-key.json}}"
fi

sha256_of() {
  shasum -a 256 "$1" | awk '{print $1}'
}

abs_path() {
  if [[ -e "$1" ]]; then
    echo "$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
  else
    echo "$1"
  fi
}

# Resolve caller-relative paths before changing into the repo root.
[[ -n "$PREBUILT_AAB" ]] && PREBUILT_AAB="$(abs_path "$PREBUILT_AAB")"
[[ -n "$MAPPING_PATH" ]] && MAPPING_PATH="$(abs_path "$MAPPING_PATH")"

pushd "$REPO_ROOT" >/dev/null

if [[ -n "$PREBUILT_AAB" ]]; then
  # Upload exactly the file that was tested: no Gradle, SHA-256 must match.
  AAB_PATH="$PREBUILT_AAB"
  if [[ ! -f "$AAB_PATH" ]]; then
    echo "AAB not found: $AAB_PATH" >&2
    exit 1
  fi
  AAB_DIR="$(cd "$(dirname "$AAB_PATH")" && pwd)"
  SUMS_FILE="$AAB_DIR/SHA256SUMS"
  if [[ -z "$EXPECTED_SHA256" && -f "$SUMS_FILE" ]]; then
    EXPECTED_SHA256="$(awk -v f="$(basename "$AAB_PATH")" '$2==f || $2=="*"f {print $1}' "$SUMS_FILE" | head -1)"
  fi
  if [[ -z "$EXPECTED_SHA256" ]]; then
    echo "No expected SHA-256 for $AAB_PATH (pass --sha256 <hex> or add it to $SUMS_FILE)." >&2
    exit 1
  fi
  ACTUAL_SHA256="$(sha256_of "$AAB_PATH")"
  if [[ "$ACTUAL_SHA256" != "$EXPECTED_SHA256" ]]; then
    echo "SHA-256 MISMATCH for $AAB_PATH" >&2
    echo "  expected: $EXPECTED_SHA256" >&2
    echo "  actual:   $ACTUAL_SHA256" >&2
    exit 1
  fi
  if [[ -n "$EXPECTED_SHA256" && -f "$SUMS_FILE" ]]; then
    SUMS_SHA="$(awk -v f="$(basename "$AAB_PATH")" '$2==f || $2=="*"f {print $1}' "$SUMS_FILE" | head -1)"
    if [[ -n "$SUMS_SHA" && "$SUMS_SHA" != "$ACTUAL_SHA256" ]]; then
      echo "SHA-256 in $SUMS_FILE does not match $AAB_PATH" >&2
      exit 1
    fi
  fi
  echo "Play track:     $TRACK"
  echo "AdMob mode:     $ADS_MODE (must match how the AAB was built)"
  echo "Prebuilt AAB:   $AAB_PATH"
  echo "SHA-256 OK:     $ACTUAL_SHA256"
  if [[ -z "$MAPPING_PATH" ]]; then
    MAPPING_PATH="$(find "$AAB_DIR" -maxdepth 1 -name '*mapping.txt' 2>/dev/null | sort | tail -1)"
  fi
  if [[ -n "$MAPPING_PATH" && -f "$SUMS_FILE" ]]; then
    MAP_SUM="$(awk -v f="$(basename "$MAPPING_PATH")" '$2==f || $2=="*"f {print $1}' "$SUMS_FILE" | head -1)"
    if [[ -n "$MAP_SUM" && "$MAP_SUM" != "$(sha256_of "$MAPPING_PATH")" ]]; then
      echo "SHA-256 MISMATCH for mapping $MAPPING_PATH" >&2
      exit 1
    fi
  fi
else
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
  if [[ -z "$MAPPING_PATH" && -f "$MAPPING_OUTPUT" ]]; then
    MAPPING_PATH="$MAPPING_OUTPUT"
  fi
fi

if [[ -n "$MAPPING_PATH" ]]; then
  if [[ ! -f "$MAPPING_PATH" ]]; then
    echo "Mapping file not found: $MAPPING_PATH" >&2
    exit 1
  fi
  echo "R8 mapping:     $MAPPING_PATH"
else
  echo "WARNING: no R8 mapping.txt found; Play Console crash stacks will be obfuscated." >&2
fi

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
echo "Release status: $RELEASE_STATUS"
SUPPLY_ARGS=(
  --aab "$AAB_PATH"
  --json_key "$SERVICE_ACCOUNT_JSON"
  --package_name "$PACKAGE_NAME"
  --track "$TRACK"
  --release_status "$RELEASE_STATUS"
  --skip_upload_apk true
  --skip_upload_images true
  --skip_upload_screenshots true
  --skip_upload_metadata true
  --skip_upload_changelogs true
)
if [[ -n "$MAPPING_PATH" ]]; then
  SUPPLY_ARGS+=(--mapping "$MAPPING_PATH")
fi
# Show the command without the service-account path.
echo "Running: fastlane supply ${SUPPLY_ARGS[*]}" | sed "s#--json_key [^ ]*#--json_key <service-account-json>#"
fastlane supply "${SUPPLY_ARGS[@]}"

popd >/dev/null

if [[ -n "${TMP_KEYSTORE:-}" && -f "${TMP_KEYSTORE:-}" ]]; then
  rm -f "$TMP_KEYSTORE"
fi

echo "Android deployment complete (track=$TRACK, ads=$ADS_MODE)."
# shellcheck source=/dev/null
source "$CODE_ROOT/.project-tracker/lib-deploy.sh"
log_deploy "$CODE_ROOT" "$(basename "$REPO_ROOT")" "android" "$TRACK" "$(detect_version_android "$REPO_ROOT")"
