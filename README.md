# Grocery Inventory Manager

Swing-based desktop application for keeping grocery stock organized with a focus on quick data entry, filtering, and low-stock awareness.

## Features
- Clean table layout with search, category filtering, and low-stock focus toggle.
- Fast inline actions to add, edit, restock, delete, or export rows.
- Visual cues for expired, expiring, and low-quantity items.
- Persistent storage on disk (`data/inventory-data.csv`) with sensible starter data auto-generated on first launch.

## Requirements
- Java 11 or newer
- macOS/Linux/Windows terminal that can run shell scripts

## Run It
```bash
cd inventorymanager
./scripts/run.sh
```
The helper script compiles sources into `inventorymanager/build` and launches the UI. Closing the window persists any edits automatically.

## Directory Notes
- `src/main/java/com/codex/inventory` – Java sources (UI, data model, persistence).
- `data/` – Flat-file storage created/updated at runtime.
- `scripts/run.sh` – Build/run helper, also useful for verifying the code compiles (`javac`) before running headless environments.
# polter-dev.github.io
# polter-dev.github.io
