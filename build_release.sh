#!/bin/bash
set -e

echo "=== Persiapan Signature Key & Build APK/AAB Lovy Chat ==="

KEYSTORE_DIR="./keystore"
KEYSTORE_FILE="${KEYSTORE_DIR}/release.keystore"
ALIAS="lovychat"
PASS="LovyChat2026SecureKey"

if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "Membuat release keystore otomatis..."
    mkdir -p "$KEYSTORE_DIR"
    keytool -genkeypair -v \
      -keystore "$KEYSTORE_FILE" \
      -alias "$ALIAS" \
      -keyalg RSA \
      -keysize 2048 \
      -validity 10000 \
      -storepass "$PASS" \
      -keypass "$PASS" \
      -dname "CN=Lovy Chat Forever, OU=Production, O=LovyChat, L=Jakarta, ST=DKI, C=ID"
    echo "Keystore berhasil dibuat di: $KEYSTORE_FILE"
else
    echo "Keystore sudah ada di: $KEYSTORE_FILE"
fi

export KEYSTORE_PATH="$(pwd)/${KEYSTORE_FILE}"
export STORE_PASSWORD="$PASS"
export KEY_ALIAS="$ALIAS"
export KEY_PASSWORD="$PASS"

echo ""
echo "Pilihan Build:"
echo "1) Build APK Release"
echo "2) Build AAB Play Store"
echo "3) Build Keduanya (APK & AAB)"

ACTION="${1:-3}"

if [ "$ACTION" == "1" ] || [ "$ACTION" == "3" ]; then
    echo "Membangun Signed Release APK..."
    gradle :app:assembleRelease
    echo "APK selesai: app/build/outputs/apk/release/"
fi

if [ "$ACTION" == "2" ] || [ "$ACTION" == "3" ]; then
    echo "Membangun Signed Release AAB (Google Play Store)..."
    gradle :app:bundleRelease
    echo "AAB selesai: app/build/outputs/bundle/release/"
fi

echo "=== Proses Selesai ==="
