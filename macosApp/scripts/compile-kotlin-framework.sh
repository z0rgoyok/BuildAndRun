#!/bin/sh
set -eu

if [ "${OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED:-}" = "YES" ]; then
  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
  exit 0
fi

cd "$SRCROOT/.."

ARCH="${NATIVE_ARCH_64_BIT:-${CURRENT_ARCH:-}}"
if [ "$ARCH" = "x86_64" ]; then
  KMP_TARGET="macosX64"
  KMP_COMPILE_TASK="compileKotlinMacosX64"
else
  KMP_TARGET="macosArm64"
  KMP_COMPILE_TASK="compileKotlinMacosArm64"
fi

./gradlew --no-build-cache ":presentation:resources:${KMP_COMPILE_TASK}" :bridge:embedAndSignAppleFrameworkForXcode

FRAMEWORK_DIR="${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}"
mkdir -p "$FRAMEWORK_DIR"
rm -rf "$FRAMEWORK_DIR/Shared.framework"
cp -RL "${BUILT_PRODUCTS_DIR}/Shared.framework" "$FRAMEWORK_DIR/"

RESOURCE_DST="${TARGET_BUILD_DIR}/${UNLOCALIZED_RESOURCES_FOLDER_PATH}/compose-resources"
RESOURCE_SRC_0="$SRCROOT/../presentation/resources/build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
RESOURCE_SRC_1="$SRCROOT/../bridge/build/kotlin-multiplatform-resources/resources-from-dependencies/${KMP_TARGET}/composeResources"
RESOURCE_SRC_2="$SRCROOT/../bridge/build/kotlin-multiplatform-resources/aggregated-resources/${KMP_TARGET}/composeResources"
RESOURCE_SRC_3="$SRCROOT/../bridge/build/bin/${KMP_TARGET}/debugTest/compose-resources/composeResources"

rm -rf "$RESOURCE_DST"
mkdir -p "$RESOURCE_DST"

BEST_RESOURCE_SRC=""
BEST_RESOURCE_LINES=0
BEST_RESOURCE_LAYOUT=""

for RESOURCE_SRC in "$RESOURCE_SRC_0" "$RESOURCE_SRC_1" "$RESOURCE_SRC_2" "$RESOURCE_SRC_3"; do
  RESOURCE_FILE="$RESOURCE_SRC/app.tich.buildandrun.resources/values/strings.commonMain.cvr"
  RESOURCE_LAYOUT="scoped"
  if [ ! -f "$RESOURCE_FILE" ]; then
    RESOURCE_FILE="$RESOURCE_SRC/values/strings.commonMain.cvr"
    RESOURCE_LAYOUT="flat"
  fi
  if [ -f "$RESOURCE_FILE" ]; then
    RESOURCE_LINES="$(wc -l < "$RESOURCE_FILE" | tr -d ' ')"
    if [ "$RESOURCE_LINES" -gt "$BEST_RESOURCE_LINES" ]; then
      BEST_RESOURCE_LINES="$RESOURCE_LINES"
      BEST_RESOURCE_SRC="$RESOURCE_SRC"
      BEST_RESOURCE_LAYOUT="$RESOURCE_LAYOUT"
    fi
  fi
done

if [ -n "$BEST_RESOURCE_SRC" ]; then
  if [ "$BEST_RESOURCE_LAYOUT" = "scoped" ]; then
    cp -R "$BEST_RESOURCE_SRC" "$RESOURCE_DST/"
  else
    mkdir -p "$RESOURCE_DST/composeResources/app.tich.buildandrun.resources"
    for VALUE_DIR in "$BEST_RESOURCE_SRC"/values*; do
      if [ -d "$VALUE_DIR" ]; then
        cp -R "$VALUE_DIR" "$RESOURCE_DST/composeResources/app.tich.buildandrun.resources/"
      fi
    done
  fi
fi

if [ ! -f "$RESOURCE_DST/composeResources/app.tich.buildandrun.resources/values/strings.commonMain.cvr" ]; then
  echo "Compose resources are missing for ${KMP_TARGET}"
  exit 1
fi
