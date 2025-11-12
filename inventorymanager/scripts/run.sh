#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="$ROOT/build"
SRC_DIR="$ROOT/src/main/java"

mkdir -p "$BUILD_DIR"

find "$SRC_DIR" -name '*.java' > "$BUILD_DIR/sources.list"

javac -d "$BUILD_DIR" @"$BUILD_DIR/sources.list"

java -cp "$BUILD_DIR" com.codex.inventory.InventoryApp
