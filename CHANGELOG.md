# Changelog

## v1.1.2 — "App not installed" fix (2026-09-06)

Root cause: release APKs were built **unsigned** (no keystore in CI) and Android refuses to install unsigned APKs.
- Generated a proper upload key (`RSA-2048, 30y`) and stored it as CI secrets (`UPLOAD_KEYSTORE_B64` / `STORE_PASSWORD` / `KEY_PASSWORD`) — every release from v1.1.2 on is signed with the same key, so updates install cleanly
- Release builds now fall back to debug signing instead of shipping unsigned — a release APK can never be uninstallable again
- ⚠️ One-time step: **uninstall any old UltiNote first**, then install v1.1.2 (signatures changed, Android blocks "updates" across different keys)

## v1.1.1 — CI green + release pipeline (2026-09-06)

- Fix `gradlew: Permission denied` on CI: restore exec bit in git + `chmod +x gradlew` step in both jobs
- Actions refresh: `checkout@v5`, `setup-java@v5`; silence `sdkmanager --licenses` spam
- No app-code changes vs v1.1.0 — same school-ready build, now actually shipping APKs

## v1.1.0 — School-ready (2026-09-06)

Class-tested build. Local-first, no AI / no cloud yet (both Coming Soon).

Added:
- Stylus pressure feel setting (Soft / Medium / Firm) + better fountain/brush/ballpoint curves
- Precision eraser (14px) alongside stroke eraser (36px); both erase ink + shapes + text + stickers
- Lasso rectangular select with Duplicate / Delete / Clear pill
- Photo insert (whiteboard / textbook shots) stored in UltiNoteImages/, drawn + exported into PDFs
- Page Duplicate + ◀ ▶ reorder in thumbnail sheet
- Pin to top, tag editor, title+tag search, pinned-first sorting
- Full-template PDF export: ruled, grid, dotted, Cornell, planner, dark, engineering, stave, pastel, blank + real STAR shape
- Sticker catalog fixed (keys now map to real emoji, 20 stickers via bottom sheet)
- DataStore persistence: theme, font, stylus-only, smoothing, auto-snap, pressure
- Permissions reworked for scoped storage (no MANAGE_EXTERNAL_STORAGE; legacy only pre-Q)
- Tablet portrait vs landscape layouts (rail only on wide landscape; dock otherwise)
- Apple Liquid Glass polish: specular borders, pill glass helper, selection halos
- Branding: com.ultinote.app, UltiNoteExports/UltiNoteImages/UltiNote dirs, ultinote_notes.db v2
- Deps: Compose BOM 2026.04.01, Room 2.8.1, AGP 9.1.1, Kotlin 2.2.10
- CI: debug APK on push, signed release APK + auto changelog on v* tags
- Gradle wrapper + local.properties handling so fresh clones build

Fixed:
- Stickers drawing literal "star" text
- STAR shapes exporting as lines
- Export missing 7 paper templates
- Eraser only deleting one stroke type
- Settings toggles resetting on rotate/restart
- Release build hard-failing without keystore

## v1.0.0 — Initial Liquid Glass prototype

Library, handwriting canvas, layers, PDF import/render, planner, themes. AI companion stubbed.
