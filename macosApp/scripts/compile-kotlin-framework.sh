#!/bin/sh
set -eu

if [ "${OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED:-}" = "YES" ]; then
  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
  exit 0
fi

cd "$SRCROOT/.."
./gradlew :bridge:embedAndSignAppleFrameworkForXcode

FRAMEWORK_DIR="${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}"
mkdir -p "$FRAMEWORK_DIR"
rm -rf "$FRAMEWORK_DIR/Shared.framework"
cp -RL "${BUILT_PRODUCTS_DIR}/Shared.framework" "$FRAMEWORK_DIR/"

ARCH="${NATIVE_ARCH_64_BIT:-${CURRENT_ARCH:-}}"
if [ "$ARCH" = "x86_64" ]; then
  KMP_TARGET="macosX64"
else
  KMP_TARGET="macosArm64"
fi

RESOURCE_DST="${TARGET_BUILD_DIR}/${UNLOCALIZED_RESOURCES_FOLDER_PATH}/compose-resources"
RESOURCE_SRC_1="$SRCROOT/../bridge/build/kotlin-multiplatform-resources/resources-from-dependencies/${KMP_TARGET}/composeResources"
RESOURCE_SRC_2="$SRCROOT/../bridge/build/kotlin-multiplatform-resources/aggregated-resources/${KMP_TARGET}/composeResources"
RESOURCE_SRC_3="$SRCROOT/../bridge/build/bin/${KMP_TARGET}/debugTest/compose-resources/composeResources"

rm -rf "$RESOURCE_DST"
mkdir -p "$RESOURCE_DST"

for RESOURCE_SRC in "$RESOURCE_SRC_1" "$RESOURCE_SRC_2" "$RESOURCE_SRC_3"; do
  if [ -d "$RESOURCE_SRC" ] && [ -n "$(find "$RESOURCE_SRC" -mindepth 1 -print -quit)" ]; then
    cp -R "$RESOURCE_SRC" "$RESOURCE_DST/"
    break
  fi
done

if [ ! -f "$RESOURCE_DST/composeResources/app.tich.buildandrun.resources/values/strings.commonMain.cvr" ]; then
  echo "Compose resources are missing for ${KMP_TARGET}"
  exit 1
fi
